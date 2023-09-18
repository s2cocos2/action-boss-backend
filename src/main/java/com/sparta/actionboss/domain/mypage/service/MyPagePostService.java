package com.sparta.actionboss.domain.mypage.service;

import com.sparta.actionboss.domain.user.entity.User;
import com.sparta.actionboss.domain.mypage.dto.response.MyPagePostsResponseDto;
import com.sparta.actionboss.domain.mypage.dto.response.PagingResponseDto;
import com.sparta.actionboss.domain.post.entity.Agree;
import com.sparta.actionboss.domain.post.entity.Comment;
import com.sparta.actionboss.domain.post.entity.Post;
import com.sparta.actionboss.domain.post.repository.AgreeRepository;
import com.sparta.actionboss.domain.post.repository.CommentRepository;
import com.sparta.actionboss.domain.post.repository.PostRepository;
import com.sparta.actionboss.global.exception.PostException;
import com.sparta.actionboss.global.exception.errorcode.ClientErrorCode;
import com.sparta.actionboss.global.response.CommonResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.sparta.actionboss.global.response.SuccessMessage.GET_POST_MESSAGE;

@Service
@RequiredArgsConstructor
public class MyPagePostService {
    private final PostRepository postRepository;
    private final AgreeRepository agreeRepository;
    private final CommentRepository commentRepository;

    public CommonResponse<PagingResponseDto> getMyPosts(User user, Pageable pageable) {
        Page<Post> myPosts = postRepository.findPostByUserId(user.getUserId(), pageable);
        Page<MyPagePostsResponseDto> myPagePostsResponseDtoList = myPosts.map(MyPagePostsResponseDto::new);
        PagingResponseDto pageInfoResponseDto = new PagingResponseDto(myPagePostsResponseDtoList);
        return new CommonResponse<>(GET_POST_MESSAGE, pageInfoResponseDto);
    }

    public CommonResponse<PagingResponseDto> getMyAgrees(User user, Pageable pageable) {
        Page<Agree> agrees = agreeRepository.findAgreeByUserId(user.getUserId(), pageable);
        List<Post> agreesPosts = new ArrayList<>();
        List<MyPagePostsResponseDto> myPagePostsResponseDtoList = new ArrayList<>();
        for (Agree agree : agrees) {
            Post post = postRepository.findById(agree.getPost().getPostId()).orElseThrow(
                    () -> new PostException(ClientErrorCode.NO_POST));
            agreesPosts.add(post);
            myPagePostsResponseDtoList.add(new MyPagePostsResponseDto(post, agree));
        }
        PagingResponseDto pageInfoResponseDto = new PagingResponseDto(
                agrees.getTotalPages(),
                agrees.getNumber(),
                myPagePostsResponseDtoList
        );
        return new CommonResponse<>(GET_POST_MESSAGE, pageInfoResponseDto);
    }

    public CommonResponse<PagingResponseDto> getMyComments(User user, Pageable pageable) {
        Page<Comment> comments = commentRepository.findCommentsByUserId(user.getUserId(), pageable);
        List<Post> commentPosts = new ArrayList<>();
        List<MyPagePostsResponseDto> myPagePostsResponseDtoList = new ArrayList<>();
        for (Comment comment : comments) {
            Post post = postRepository.findById(comment.getPost().getPostId()).orElseThrow(
                    () -> new PostException(ClientErrorCode.NO_POST));
            commentPosts.add(post);
            myPagePostsResponseDtoList.add(new MyPagePostsResponseDto(post, comment));
        }
        PagingResponseDto pagingResponseDto = new PagingResponseDto(
                comments.getTotalPages(),
                comments.getNumber(),
                myPagePostsResponseDtoList
        );
        return new CommonResponse<>(GET_POST_MESSAGE, pagingResponseDto);
    }
}
