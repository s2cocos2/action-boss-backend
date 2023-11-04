package com.sparta.actionboss.domain.post.service;

import com.sparta.actionboss.domain.notification.service.NotificationService;
import com.sparta.actionboss.domain.post.dto.response.PostListResponseDto;
import com.sparta.actionboss.domain.post.entity.*;
import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.user.type.UserRoleEnum;
import com.sparta.actionboss.domain.post.dto.request.PostRequestDto;
import com.sparta.actionboss.domain.post.dto.response.PostResponseDto;
import com.sparta.actionboss.domain.post.repository.AgreeRepository;
import com.sparta.actionboss.domain.post.repository.DoneRepository;
import com.sparta.actionboss.domain.post.repository.ImageRepository;
import com.sparta.actionboss.domain.post.repository.PostRepository;
import com.sparta.actionboss.global.exception.CommonException;
import com.sparta.actionboss.global.exception.errorcode.ClientErrorCode;
import com.sparta.actionboss.global.response.CommonResponse;
import com.sparta.actionboss.global.security.UserDetailsImpl;
import com.sparta.actionboss.global.util.EmailUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.sparta.actionboss.global.exception.errorcode.ClientErrorCode.*;
import static com.sparta.actionboss.global.response.SuccessMessage.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class PostService {

    @Value("${aws.bucket.url}")
    private String s3Url;

    private final S3Service s3Service;
    private final EmailUtil emailUtil;
    private final PostRepository postRepository;
    private final DoneRepository doneRepository;
    private final AgreeRepository agreeRepository;
    private final ImageRepository imageRepository;
    private final CommentService commentService;
    private final NotificationService notificationService;

    private static final int MAXIMUM_IMAGES = 3;    // 이미지 업로드 최대 개수
    private static final int MAXIMUM_DONE = 5;    // 해결했어요 최대 개수

    // 게시글 작성
    @Transactional
    public CommonResponse createPost(
            PostRequestDto postRequestDto,
            List<MultipartFile> images,
            User user
    ) throws IOException {

        if (limitImage(images)) {
            throw new CommonException(UPLOAD_NO_IMAGE);
        }
        if (images.size() > MAXIMUM_IMAGES) {
            throw new CommonException(UPLOAD_MAXIMUM_IMAGE);
        }

        Post post = new Post(postRequestDto, user);
        postRepository.save(post);

        // 요청별로 폴더생성 -> 저장
        String folderName = "[" + post.getPostId() + "]" + "-" + UUID.randomUUID().toString().substring(19);

        List<String> imageNameList = s3Service.upload(images, folderName);

        for (String imageName : imageNameList) {
            Image image = new Image(imageName, folderName, post);
            imageRepository.save(image);
        }

        return new CommonResponse(CREATE_POST_MESSAGE, new PostResponseDto(post.getPostId()));
    }

    //게시글 전체 조회
    @Transactional(readOnly = true)
    public CommonResponse<List<PostListResponseDto>> getAllPost(int page, int size) {
        Sort.Direction direction = Sort.Direction.DESC;
        Sort sort = Sort.by(direction, "modifiedAt");

        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Post> postPage = postRepository.findAll(pageable);
        List<PostListResponseDto> postListResponseDtos = postPage.getContent().stream()
                .map(post -> {
                    String thumbnail = null;
                    List<Image> images = post.getImageList();
                    if (!images.isEmpty()) {
                        Image firstImage = images.get(0);  // 첫 번째 이미지를 가져옵니다.
                        thumbnail = firstImage.getFolderName() + "/" + firstImage.getImageName();  // URL을 만듭니다.
                    }
                    return new PostListResponseDto(post.getPostId(), post.getTitle(), post.getAgreeCount(), post.getUser().getNickname(), post.getAddress(), thumbnail);
                })
                .collect(Collectors.toList());

        return new CommonResponse<>(GET_POST_MESSAGE, postListResponseDtos);
    }



    // 게시글 상세 조회
    public CommonResponse<PostResponseDto> getPost(Long postId) {
        Optional<UserDetailsImpl> userDetails = Optional.ofNullable(getUserDetails());
        Post post = findPost(postId);
        List<Image> imageList = findImagesByPost(postId);
        List<String> imageURLs = imageUrlPrefix(imageList);
        User loginUser = null;  // null 이면 안되기 때문에 빈 문자열로 초기화

        boolean done = false;
        boolean owner = false;
        boolean agree = false;

        if (userDetails.isPresent()) {
            loginUser = userDetails.get().getUser();
            done = doneRepository.findByPostAndUser(post, loginUser).isPresent();
            agree = agreeRepository.findByUserAndPost(loginUser, post).isPresent();
            owner = hasAuthority(post, loginUser);
        }

        // 댓글 가져오기
        List<Comment> comments = commentService.findComments(postId);

        return new CommonResponse<>(GET_POST_MESSAGE, new PostResponseDto(post, imageURLs, done, owner, agree, comments, loginUser));
    }


    // 게시글 업데이트: Only 제목, 내용
    @Transactional
    public CommonResponse updatePost(
            Long postId,
            PostRequestDto postRequestDto,
            User user
    ) {
        Post post = findPost(postId);
        if (hasAuthority(post, user)) {
            post.update(postRequestDto);
        } else {
            throw new CommonException(NO_PERMISSION_UPDATE);
        }
        return new CommonResponse(UPDATE_POST_MESSAGE);
    }


    // 게시글 삭제
    @Transactional
    public CommonResponse deletePost(
            Long postId,
            User user
    ) {
        Post post = findPost(postId);
        List<Image> images = findImagesByPost(postId);

        if (hasAuthority(post, user)) {
            postRepository.delete(findPost(postId));
            if (!images.isEmpty()) {
                s3Service.deleteFolder(images.get(0).getFolderName());
            }
        } else {
            throw new CommonException(NO_PERMISSION_DELETE);
        }

        return new CommonResponse(DELETE_POST_MESSAGE);
    }

    // 해당 게시글 찾기
    private Post findPost(Long postId) {
        return postRepository.findById(postId).orElseThrow(
                () -> new CommonException(NO_POST));
    }

    public List<Image> findImagesByPost(Long postId) {
        List<Image> images = imageRepository.findImagesByPostId(postId);
        if (images.isEmpty()) {
            throw new CommonException(NO_POST);
        }
        return images;
    }

    // 권한 확인
    private boolean hasAuthority(Post post, User user) {
        return post.getUser()
                .getUserId()
                .equals(user.getUserId())
                ||
                user.getRole().equals(UserRoleEnum.ADMIN);
    }

    private boolean limitImage(List<MultipartFile> images) {
        return images == null
                ||
                images.isEmpty()
                ||
                images.stream().allMatch(image -> image.isEmpty());
    }


    // 이미지 파일명에 URL Prefix 붙이기
    private List<String> imageUrlPrefix(List<Image> imageNames) {
        return imageNames
                .stream()
                .map(imageName -> s3Url + "/images/" + imageName.getFolderName() + "/" + imageName.getImageName())
                .toList();
    }

    // 로그인을 하지 않을 경우, 로그인을 한 경우 -> UserDetailsImpl 이 들어오는 경우, 들어오지 않는 경우
    private UserDetailsImpl getUserDetails() {
        UserDetailsImpl userDetails = null;
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            userDetails = (UserDetailsImpl) authentication.getPrincipal();
        }
        return userDetails;
    }

    //'나도 불편해요' 버튼
    public CommonResponse agreePost(Long postId, User user) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new CommonException(NO_POST));

        if (!agreeRepository.existsAgreeByUserAndPost(user, post)) {
            Agree agree = new Agree(user, post);
            agreeRepository.save(agree);
            if (!agree.getUser().getNickname().equals(post.getUser().getNickname())) {
                notificationService.agreeNotification(agree.getAgreeId());
            }
            return new CommonResponse(CREATE_AGREE);
        } else {
            Agree agree = agreeRepository.findByUserAndPost(user, post).orElseThrow(
                    () -> new CommonException(NO_AGREE));
            agreeRepository.delete(agree);
            return new CommonResponse(CANCEL_AGREE);
        }
    }

    //'해결했어요' 버튼
    @Transactional
    public CommonResponse createDone(Long postId, User user) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new CommonException(NO_POST));

        if (!doneRepository.existsDoneByPostAndUser(post, user)) {
            Done done = new Done(post, user);
            doneRepository.save(done);
            if (!done.getUser().getNickname().equals(post.getUser().getNickname())) {
                notificationService.doneNotification(done.getDoneId());
            }

            // 해결됐어요 5개 되면 done => true
            if (post.getPostDoneList().size() >= MAXIMUM_DONE) {
                post.setDone(true);
                notificationService.postDoneNotification(postId);
                if (user.getEmail() == null) {
                    return new CommonResponse<>(CREATE_DONE_MESSAGE);
                } else {
                    emailUtil.sendDoneEmail(post.getUser(), post);
                }
            }
            return new CommonResponse<>(CREATE_DONE_MESSAGE);
        } else {
            Done done = doneRepository.findByPostAndUser(post, user).orElseThrow(
                    () -> new CommonException(NO_DONE));
            doneRepository.delete(done);
            return new CommonResponse<>(CANCEL_DONE_MESSAGE);
        }
    }
}