package com.sparta.actionboss.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.notification.dto.response.NotificationResponseDto;
import com.sparta.actionboss.domain.notification.entity.Notification;
import com.sparta.actionboss.domain.notification.repository.NotificationRepository;
import com.sparta.actionboss.domain.post.entity.Agree;
import com.sparta.actionboss.domain.post.entity.Comment;
import com.sparta.actionboss.domain.post.entity.Done;
import com.sparta.actionboss.domain.post.entity.Post;
import com.sparta.actionboss.domain.post.repository.AgreeRepository;
import com.sparta.actionboss.domain.post.repository.CommentRepository;
import com.sparta.actionboss.domain.post.repository.DoneRepository;
import com.sparta.actionboss.domain.post.repository.PostRepository;
import com.sparta.actionboss.global.exception.*;
import com.sparta.actionboss.global.exception.errorcode.ClientErrorCode;
import com.sparta.actionboss.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.sparta.actionboss.global.response.SuccessMessage.GET_NOTIFICATION_MESSAGE;
import static com.sparta.actionboss.global.response.SuccessMessage.READ_NOTIFICATION;


@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    @Value("${email.secret.id}")
    private String id;
    @Value("${email.secret.pw}")
    private String password;


    public static Map<Long, SseEmitter> sseEmitters = new ConcurrentHashMap<>();
    private final CommentRepository commentRepository;
    private final AgreeRepository agreeRepository;
    private final DoneRepository postDoneRepository;
    private final PostRepository postRepository;
    private final NotificationRepository notificationRepository;

    // 사용자 연결
    public SseEmitter subscribe(User user) {
        SseEmitter sseEmitter = new SseEmitter(Long.MAX_VALUE);
        try {
            sseEmitter.send(SseEmitter.event()
                    .name("Connect").data("연결되었습니다."));
            log.info("[OPEN] - SSE connection opened for user {}", user.getNickname());
        } catch (IOException e) {
            e.printStackTrace();
        }

        sseEmitters.put(user.getUserId(), sseEmitter);
        sseEmitter.onCompletion(() -> sseEmitters.remove(user.getUserId()));
        sseEmitter.onTimeout(() -> sseEmitters.remove(user.getUserId()));
        sseEmitter.onError((e) -> sseEmitters.remove(user.getUserId()));

        return sseEmitter;
    }

    // 사용자 연결 취소
    public void unsubscribe(User user) {
        SseEmitter sseEmitter = sseEmitters.get(user.getUserId());
        if (sseEmitter != null) {
            sseEmitter.complete();
            sseEmitters.remove(user.getUserId());
            log.info("[CLOSE] - SSE connection closed for user {}", user.getNickname());
        }
    }

    // 알림 조회
    public CommonResponse<List<NotificationResponseDto>> getNotification(User user) {
        List<Notification> notifications = notificationRepository
                .findNotificationByUserId(user.getUserId());
        List<NotificationResponseDto> notificationResponse = notifications.stream()
                .map(NotificationResponseDto::new).toList();
        return new CommonResponse<>(GET_NOTIFICATION_MESSAGE, notificationResponse);
    }


    // 댓글 알림
    public void commentNotification(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new CommentException(ClientErrorCode.NO_COMMENT));
        Long postUserId = comment.getPost().getUser().getUserId();
        String actor = comment.getUser().getNickname();
        String title = comment.getPost().getTitle();
        Long postId = comment.getPost().getPostId();
        LocalDateTime time = comment.getCreatedAt();
        String recipient = comment.getPost().getUser().getNickname();

        Notification notification = new Notification(
                title, "comment",
                comment.getPost().getUser(),
                comment.getPost(),
                comment.getUser()
        );

        notificationRepository.save(notification);
        if (sseEmitters.containsKey(postUserId)) {
            SseEmitter sseEmitter = sseEmitters.get(postUserId);
            try {
                // JSON 형태로 변환
                String dataJsonString = convertToJson(
                        actor, title, postId, time,
                        comment.getContent(), recipient
                );

                assert dataJsonString != null;
                sseEmitter.send(SseEmitter.event()
                        .name("AddComment").data(dataJsonString));
            } catch (Exception e) {
                sseEmitters.remove(postUserId);
            }
        }
    }


    // "나도 불편해요" 알림
    public void agreeNotification(Long agreeId) {
        Agree agree = agreeRepository.findById(agreeId).orElseThrow(
                () -> new AgreeException(ClientErrorCode.NO_AGREE));
        Long postUserId = agree.getPost().getUser().getUserId();
        String actor = agree.getUser().getNickname();
        String title = agree.getPost().getTitle();
        Long postId = agree.getPost().getPostId();
        LocalDateTime time = agree.getCreatedAt();
        String recipient = agree.getPost().getUser().getNickname();

        Notification notification = new Notification(
                agree.getPost().getTitle(),
                "agree",
                agree.getPost().getUser(),
                agree.getPost(),
                agree.getUser()
        );
        notificationRepository.save(notification);

        if (sseEmitters.containsKey(postUserId)) {
            SseEmitter sseEmitter = sseEmitters.get(postUserId);
            try {
                // JSON 형태로 변환
                String dataJsonString = convertToJson(actor, title, postId, time, recipient);

                assert dataJsonString != null;
                sseEmitter.send(SseEmitter.event()
                        .name("AddAgree").data(dataJsonString));
            } catch (Exception e) {
                sseEmitters.remove(postUserId);
            }
        }
    }


    // "해결된 민원이에요" 알림
    public void doneNotification(Long doneId) {
        Done done = postDoneRepository.findById(doneId).orElseThrow(
                () -> new DoneException(ClientErrorCode.NO_DONE));
        Long postUserId = done.getPost().getUser().getUserId();
        String actor = done.getUser().getNickname();
        String title = done.getPost().getTitle();
        Long postId = done.getPost().getPostId();
        LocalDateTime time = done.getCreatedAt();
        String recipient = done.getPost().getUser().getNickname();

        Notification notification = new Notification(
                done.getPost().getTitle(),
                "done",
                done.getPost().getUser(),
                done.getPost(),
                done.getUser()
        );
        System.out.println("done.getUser() = " + done.getUser());
        notificationRepository.save(notification);

        if (sseEmitters.containsKey(postUserId)) {
            SseEmitter sseEmitter = sseEmitters.get(postUserId);
            try {
                // JSON 형태로 변환
                String dataJsonString = convertToJson(actor, title, postId, time, recipient);

                assert dataJsonString != null;
                sseEmitter.send(SseEmitter.event()
                        .name("AddDone").data(dataJsonString));
            } catch (Exception e) {
                sseEmitters.remove(postUserId);
            }
        }
    }


    // 해결된 게시글이에요 알림
    public void postDoneNotification(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostException(ClientErrorCode.NO_POST));
        Long postUserId = post.getUser().getUserId();
        String title = post.getTitle();
        boolean postDone = post.isDone();

        Notification notification = new Notification(title, "postDone", post.getUser(), post, post.getUser());
        notificationRepository.save(notification);
        LocalDateTime time = notification.getCreatedAt();

        if (sseEmitters.containsKey(postUserId)) {
            SseEmitter sseEmitter = sseEmitters.get(postUserId);
            try {
                // JSON 형태로 변환
                String dataJsonString = convertToJson(title, postId, postDone, time);

                assert dataJsonString != null;
                sseEmitter.send(SseEmitter.event()
                        .name("DonePost").data(dataJsonString));
            } catch (Exception e) {
                sseEmitters.remove(postUserId);
            }
        }
    }

    // 댓글 JSON
    private String convertToJson(
            String actor,
            String title,
            Long postId,
            LocalDateTime time,
            String content,
            String recipient
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();

        objectMapper.registerModule(new JavaTimeModule());

        map.put("actor", actor);
        map.put("title", title);
        map.put("postId", postId);
        map.put("time", time);
        map.put("content", content);
        map.put("recipient", recipient);

        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    // "나도 불편해요" OR "해결됐어요" JSON
    private String convertToJson(
            String actor,
            String title,
            Long postId,
            LocalDateTime time,
            String recipient
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();

        objectMapper.registerModule(new JavaTimeModule());

        map.put("actor", actor);
        map.put("title", title);
        map.put("postId", postId);
        map.put("time", time);
        map.put("recipient", recipient);

        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 게시글 DONE JSON
    private String convertToJson(
            String title,
            Long postId,
            boolean postDone,
            LocalDateTime time
    ) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> map = new HashMap<>();

        objectMapper.registerModule(new JavaTimeModule());

        map.put("postId", postId);
        map.put("title", title);
        map.put("postDone", postDone);

        try {
            return objectMapper.writeValueAsString(map);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Transactional
    public CommonResponse changeReadStatus(Long notificationId, User user) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow(
                () -> new NotificationException(ClientErrorCode.NO_NOTIFICATION));
        if (notification.getRecipient().getNickname().equals(user.getNickname())) {
            notification.setRead();
        } else {
            throw new NotificationException(ClientErrorCode.NO_REMISSION_READ);
        }

        return new CommonResponse<>(READ_NOTIFICATION);
    }
}
