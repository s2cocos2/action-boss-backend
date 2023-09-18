package com.sparta.actionboss.domain.post.repository;

import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.post.entity.Agree;
import com.sparta.actionboss.domain.post.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AgreeRepository extends JpaRepository<Agree, Long> {
    boolean existsAgreeByUserAndPost(User user, Post post);

    Optional<Agree> findByUserAndPost(User user, Post post);

    List<Agree> findByPost(Post post);

    @Query("SELECT a FROM Agree a WHERE a.user.userId = :userId ORDER BY a.createdAt DESC")
    Page<Agree> findAgreeByUserId(@Param("userId") Long userId, Pageable pageable);
}
