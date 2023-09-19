package com.sparta.actionboss.domain.post.service;

import com.sparta.actionboss.domain.post.dto.response.MapListResponseDto;
import com.sparta.actionboss.domain.post.dto.response.PostListAndTotalPageResponseDto;
import com.sparta.actionboss.domain.post.dto.response.PostListResponseDto;
import com.sparta.actionboss.domain.post.dto.response.PostModalResponseDto;
import com.sparta.actionboss.domain.post.entity.Post;
import com.sparta.actionboss.domain.post.repository.PostRepository;
import com.sparta.actionboss.global.exception.CommonException;
import com.sparta.actionboss.global.exception.errorcode.ClientErrorCode;
import com.sparta.actionboss.global.response.CommonResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

import static com.sparta.actionboss.global.response.SuccessMessage.GET_POST_MESSAGE;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MainPageService {

    @Value("${aws.bucket.url}")
    private String s3Url;

    private final PostRepository postRepository;
    private final EntityManager entityManager;

    public CommonResponse<PostListAndTotalPageResponseDto> getPostList(Integer page, Integer size, String sortBy, boolean done,
                                                                            Double northLatitude, Double eastLongitude, Double southLatitude, Double westLongitude) {
        Sort.Direction direction = Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size);

        TypedQuery<Post> query = entityManager.createQuery(
                "SELECT p FROM Post p WHERE " +
                        "(:done IS NULL OR p.done = :done) " +
                        "AND (:north IS NULL OR p.latitude <= :north) " +
                        "AND (:east IS NULL OR p.longitude <= :east) " +
                        "AND (:south IS NULL OR p.latitude >= :south) " +
                        "AND (:west IS NULL OR p.longitude >= :west)" +
                        (sortBy != null && !sortBy.isEmpty() ? (" ORDER BY p." + sortBy + (direction.isAscending() ? "" : " DESC, createdAt DESC")) : "")
                , Post.class);

        query.setParameter("done", done);
        query.setParameter("north", northLatitude);
        query.setParameter("east", eastLongitude);
        query.setParameter("south", southLatitude);
        query.setParameter("west", westLongitude);
        query.setFirstResult(pageable.getPageNumber() * pageable.getPageSize());
        query.setMaxResults(pageable.getPageSize());

        List<Post> postList = query.getResultList();
        Long totalCount = getPostCount(done, northLatitude, eastLongitude, southLatitude, westLongitude);

        List<PostListResponseDto> postListResponseDtos = postList.stream()
                .map(a -> {
                    String imageUrl = s3Url + "/images/" + a.getImageList().get(0).getFolderName() + "/" + a.getImageList().get(0).getImageName();

                    return new PostListResponseDto(
                            a.getPostId(),
                            a.getTitle(),
                            a.getAgreeCount(),
                            a.getUser().getNickname(),
                            a.getAddress(),
                            imageUrl
                    );
                })
                .collect(Collectors.toList());

        return new CommonResponse<>(GET_POST_MESSAGE, new PostListAndTotalPageResponseDto<>(postListResponseDtos, calculateTotalPages(totalCount, size), page));
    }

    public CommonResponse<PostModalResponseDto> getModalPost(Long postId) {
        Post findPost = postRepository.findById(postId)
                .orElseThrow(() -> new CommonException(ClientErrorCode.NO_POST));

        String imageUrl = s3Url + "/images/" + findPost.getImageList().get(0).getFolderName() + "/" + findPost.getImageList().get(0).getImageName();

        return new CommonResponse<>(GET_POST_MESSAGE, new PostModalResponseDto(findPost, imageUrl));
    }

    public CommonResponse<List<MapListResponseDto>> getMapList(boolean done,
                                                                    Double northLatitude, Double eastLongitude, Double southLatitude, Double westLongitude) {
        String queryCondition = "(:done IS NULL OR p.done = :done) " +
                "AND (:north IS NULL OR p.latitude <= :north) " +
                "AND (:east IS NULL OR p.longitude <= :east) " +
                "AND (:south IS NULL OR p.latitude >= :south) " +
                "AND (:west IS NULL OR p.longitude >= :west)";

        TypedQuery<Post> query = entityManager.createQuery(
                "SELECT p FROM Post p WHERE " + queryCondition, Post.class
        );

        query.setParameter("done", done);
        query.setParameter("north", northLatitude);
        query.setParameter("east", eastLongitude);
        query.setParameter("south", southLatitude);
        query.setParameter("west", westLongitude);

        List<Post> postList = query.getResultList();

        List<MapListResponseDto> mapListResponseDto = postList.stream()
                .map(a -> new MapListResponseDto(
                        a.getPostId(),
                        a.getLatitude(),
                        a.getLongitude()
                ))
                .collect(Collectors.toList());

        return new CommonResponse<>(GET_POST_MESSAGE, mapListResponseDto);
    }

    private Long getPostCount(boolean done, Double north, Double east, Double south, Double west) {
        TypedQuery<Long> countQuery = entityManager.createQuery(
                "SELECT COUNT(p) FROM Post p WHERE " +
                        "(:done IS NULL OR p.done = :done) " +
                        "AND (:north IS NULL OR p.latitude <= :north) " +
                        "AND (:east IS NULL OR p.longitude <= :east) " +
                        "AND (:south IS NULL OR p.latitude >= :south) " +
                        "AND (:west IS NULL OR p.longitude >= :west)", Long.class
        );

        countQuery.setParameter("done", done);
        countQuery.setParameter("north", north);
        countQuery.setParameter("east", east);
        countQuery.setParameter("south", south);
        countQuery.setParameter("west", west);

        return countQuery.getSingleResult();
    }

    private int calculateTotalPages(Long totalCount, Integer pageSize) {
        return (int) Math.ceil((double) totalCount / pageSize);
    }
}
