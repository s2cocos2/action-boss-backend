package com.sparta.actionboss.domain.notification.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sparta.actionboss.domain.notification.entity.Notification;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponseDto {
    private final Long notificationId;
    private final Long postId;
    private final String title;
    private final String actor;
    private final String recipient;
    private final String type;
    private final Boolean readStatus;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy.MM.dd HH:mm:ss", timezone = "Asia/Seoul")
    private final LocalDateTime createdAt;

    public NotificationResponseDto(Notification notification) {
        this.notificationId = notification.getId();
        this.postId = notification.getPost().getPostId();
        this.title = notification.getTitle();
        this.actor = notification.getActor().getNickname();
        this.recipient = notification.getRecipient().getNickname();
        this.type = notification.getType();
        this.createdAt = notification.getCreatedAt();
        this.readStatus = notification.getReadStatus();
    }
}
