package com.sparta.actionboss.domain.notification.entity;


import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.post.entity.Post;
import com.sparta.actionboss.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Notification extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String type;
    private Boolean readStatus = false;

    @ManyToOne
    private User recipient;

    @ManyToOne
    private Post post;

    @ManyToOne
    private User actor;

    public Notification(String title, String type, User recipient, Post post, User actor) {
        this.title = title;
        this.type = type;
        this.recipient = recipient;
        this.post = post;
        this.actor = actor;
        readStatus = this.getReadStatus();
    }

    public void setRead() {
        this.readStatus = true;
    }
}
