package com.sparta.actionboss.domain.post.entity;

import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.notification.entity.Notification;
import com.sparta.actionboss.domain.post.dto.request.PostRequestDto;
import com.sparta.actionboss.global.entity.Timestamped;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Formula;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Post extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column
    private Long postId;

    @Column(nullable = false, length = 55)
    private String title;

    @Column(nullable = false, length = 550)
    private String content;

    @Column(nullable = false)
    private boolean done;

    @Column(nullable = false)
    private String address;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Formula("(SELECT COUNT(*) FROM agree a WHERE a.post_id = post_id)")
    private int agreeCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Done> postDoneList;

    @OneToMany(mappedBy = "post", cascade = CascadeType.REMOVE)
    private List<Agree> postAgreeList;

    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<Image> imageList;

    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<Comment> commentList;

    @OneToMany(mappedBy = "post", orphanRemoval = true)
    private List<Notification> notificationList;

    public Post(PostRequestDto postRequestDto, User user) {
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
        this.address = postRequestDto.getAddress();
        this.latitude = postRequestDto.getLatitude();
        this.longitude = postRequestDto.getLongitude();
        this.user = user;
    }

    public void update(PostRequestDto postRequestDto) {
        this.title = postRequestDto.getTitle();
        this.content = postRequestDto.getContent();
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
