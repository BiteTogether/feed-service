package com.bitetogether.feed.service;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.UserContextUtils;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.LikeRequest;
import com.bitetogether.feed.dto.response.LikeResponse;
import com.bitetogether.feed.mapper.LikeMapper;
import com.bitetogether.feed.model.Comment;
import com.bitetogether.feed.model.Like;
import com.bitetogether.feed.model.Post;
import com.bitetogether.feed.repository.CommentRepository;
import com.bitetogether.feed.repository.LikeRepository;
import com.bitetogether.feed.repository.PostRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.impl.LikeServiceImpl;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@ExtendWith(MockitoExtension.class)
class LikeServiceImplTest {

  @Mock private LikeRepository likeRepository;

  @Mock private PostRepository postRepository;

  @Mock private CommentRepository commentRepository;

  @Mock private LikeMapper likeMapper;

  @Mock private UserClient userClient;

  @InjectMocks private LikeServiceImpl service;

  @Test
  void like_whenMissingTargets_returnsBadRequest() {
    LikeRequest request = new LikeRequest();

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(1L);

      ApiResponseDTO<LikeResponse> response = service.like(request);

      Assertions.assertEquals(ApiResponseStatus.BAD_REQUEST.getCode(), response.getStatus());
      Mockito.verifyNoInteractions(likeRepository);
    }
  }

  @Test
  void like_whenAlreadyLikedPost_returnsConflict() {
    LikeRequest request = new LikeRequest();
    request.setPostId("p1");

    Page<Like> existing = new PageImpl<>(List.of(new Like()));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(1L);
      Mockito.when(
              likeRepository.findByUserIdAndPostId(
                  Mockito.eq(1L), Mockito.eq("p1"), Mockito.any(Pageable.class)))
          .thenReturn(existing);

      ApiResponseDTO<LikeResponse> response = service.like(request);

      Assertions.assertEquals(ApiResponseStatus.CONFLICT.getCode(), response.getStatus());
      Mockito.verify(likeRepository, Mockito.never()).save(Mockito.any(Like.class));
      Mockito.verify(postRepository, Mockito.never()).findById(Mockito.anyString());
      Mockito.verify(commentRepository, Mockito.never()).findById(Mockito.anyString());
    }
  }

  @Test
  void like_whenNewCommentLike_setsPostIdNull_updatesCommentCount_andMapsUserNullOnClientFailure() {
    LikeRequest request = new LikeRequest();
    request.setPostId("pIgnored");
    request.setCommentId("c1");

    Page<Like> none = new PageImpl<>(List.of());

    Like mappedLike = new Like();
    mappedLike.setCommentId("c1");

    Like saved = new Like();
    saved.setId("l1");
    saved.setCommentId("c1");
    saved.setUserId(1L);

    LikeResponse mappedResponse = new LikeResponse();
    mappedResponse.setId("l1");

    Comment comment = new Comment();
    comment.setId("c1");
    comment.setLikeCount(0);

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(1L);

      Mockito.when(
              likeRepository.findByUserIdAndCommentId(
                  Mockito.eq(1L), Mockito.eq("c1"), Mockito.any(Pageable.class)))
          .thenReturn(none);

      Mockito.when(likeMapper.toLike(Mockito.same(request))).thenReturn(mappedLike);
      Mockito.when(likeRepository.save(Mockito.any(Like.class))).thenReturn(saved);

      Mockito.when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
      Mockito.when(commentRepository.save(Mockito.any(Comment.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      Mockito.when(userClient.getUserById(1L)).thenThrow(new RuntimeException("down"));
      Mockito.when(likeMapper.toLikeResponse(saved)).thenReturn(mappedResponse);

      ApiResponseDTO<LikeResponse> response = service.like(request);

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertNotNull(response.getData());
      Assertions.assertEquals("l1", response.getData().getId());
      Assertions.assertNull(response.getData().getUser());
      Assertions.assertNull(request.getPostId());
      Mockito.verify(commentRepository).save(Mockito.argThat(c -> c.getLikeCount() == 1));
      Mockito.verify(postRepository, Mockito.never()).findById(Mockito.anyString());
    }
  }

  @Test
  void unlike_whenMissingTargets_returnsBadRequest() {
    LikeRequest request = new LikeRequest();

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(1L);

      ApiResponseDTO<String> response = service.unlike(request);

      Assertions.assertEquals(ApiResponseStatus.BAD_REQUEST.getCode(), response.getStatus());
      Mockito.verifyNoInteractions(likeRepository);
    }
  }

  @Test
  void unlike_whenNotFound_returnsNotFound() {
    LikeRequest request = new LikeRequest();
    request.setPostId("p1");

    Page<Like> none = new PageImpl<>(List.of());

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(1L);
      Mockito.when(
              likeRepository.findByUserIdAndPostId(
                  Mockito.eq(1L), Mockito.eq("p1"), Mockito.any(Pageable.class)))
          .thenReturn(none);

      ApiResponseDTO<String> response = service.unlike(request);

      Assertions.assertEquals(ApiResponseStatus.NOT_FOUND.getCode(), response.getStatus());
      Mockito.verify(likeRepository, Mockito.never()).delete(Mockito.any(Like.class));
      Mockito.verify(postRepository, Mockito.never()).findById(Mockito.anyString());
    }
  }

  @Test
  void unlike_whenFoundCommentLike_deletesAndFloorsAtZero() {
    LikeRequest request = new LikeRequest();
    request.setCommentId("c1");

    Like existing = new Like();
    existing.setId("l1");
    existing.setCommentId("c1");
    existing.setUserId(1L);

    Page<Like> page = new PageImpl<>(List.of(existing));

    Comment comment = new Comment();
    comment.setId("c1");
    comment.setLikeCount(0);

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(1L);

      Mockito.when(
              likeRepository.findByUserIdAndCommentId(
                  Mockito.eq(1L), Mockito.eq("c1"), Mockito.any(Pageable.class)))
          .thenReturn(page);

      Mockito.when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
      Mockito.when(commentRepository.save(Mockito.any(Comment.class)))
          .thenAnswer(inv -> inv.getArgument(0));

      ApiResponseDTO<String> response = service.unlike(request);

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Mockito.verify(likeRepository).delete(existing);
      Mockito.verify(commentRepository).save(Mockito.argThat(c -> c.getLikeCount() == 0));
      Mockito.verify(postRepository, Mockito.never()).findById(Mockito.anyString());
    }
  }

  @Test
  void getLikesByPost_andGetLikesByUser_andGetLikesByComment_mapUserFromClientBody() {
    Like like = new Like();
    like.setId("l1");
    like.setUserId(7L);

    LikeResponse mapped = new LikeResponse();
    mapped.setId("l1");

    ApiResponseDTO<UserDTO> userBody = new ApiResponseDTO<>();
    userBody.setData(new UserDTO());

    Mockito.when(userClient.getUserById(7L)).thenReturn(ResponseEntity.ok(userBody));
    Mockito.when(likeMapper.toLikeResponse(like)).thenReturn(mapped);

    Page<Like> page = new PageImpl<>(List.of(like));

    Mockito.when(likeRepository.findByPostId(Mockito.eq("p1"), Mockito.any(Pageable.class)))
        .thenReturn(page);
    ApiResponsePaginationDTO<LikeResponse> byPost = service.getLikesByPost("p1", 0, 10);
    Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), byPost.getStatus());
    Assertions.assertEquals(1, byPost.getData().size());
    Assertions.assertNotNull(byPost.getData().get(0).getUser());

    Mockito.when(likeRepository.findByUserId(Mockito.eq(7L), Mockito.any(Pageable.class)))
        .thenReturn(page);
    ApiResponsePaginationDTO<LikeResponse> byUser = service.getLikesByUser(7L, 0, 10);
    Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), byUser.getStatus());
    Assertions.assertEquals(1, byUser.getData().size());
    Assertions.assertNotNull(byUser.getData().get(0).getUser());

    Mockito.when(likeRepository.findByCommentId(Mockito.eq("c1"), Mockito.any(Pageable.class)))
        .thenReturn(page);
    ApiResponsePaginationDTO<LikeResponse> byComment = service.getLikesByComment("c1", 0, 10);
    Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), byComment.getStatus());
    Assertions.assertEquals(1, byComment.getData().size());
    Assertions.assertNotNull(byComment.getData().get(0).getUser());
  }

  @Test
  void like_whenPostLike_updatesPostCountAndMapsUserNullWhenBodyNull() {
    LikeRequest request = new LikeRequest();
    request.setPostId("p1");

    Page<Like> none = new PageImpl<>(List.of());

    Like mappedLike = new Like();
    mappedLike.setPostId("p1");

    Like saved = new Like();
    saved.setId("l1");
    saved.setPostId("p1");
    saved.setUserId(1L);

    LikeResponse mappedResponse = new LikeResponse();
    mappedResponse.setId("l1");

    Post post = new Post();
    post.setId("p1");
    post.setLikeCount(0);

    Mockito.when(userClient.getUserById(1L)).thenReturn(ResponseEntity.ok(null));

    Mockito.when(likeMapper.toLike(Mockito.same(request))).thenReturn(mappedLike);
    Mockito.when(likeRepository.save(Mockito.any(Like.class))).thenReturn(saved);
    Mockito.when(likeMapper.toLikeResponse(saved)).thenReturn(mappedResponse);

    Mockito.when(
            likeRepository.findByUserIdAndPostId(
                Mockito.eq(1L), Mockito.eq("p1"), Mockito.any(Pageable.class)))
        .thenReturn(none);

    Mockito.when(postRepository.findById("p1")).thenReturn(Optional.of(post));
    Mockito.when(postRepository.save(Mockito.any(Post.class)))
        .thenAnswer(inv -> inv.getArgument(0));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(1L);

      ApiResponseDTO<LikeResponse> response = service.like(request);

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertNotNull(response.getData());
      Assertions.assertNull(response.getData().getUser());
      Mockito.verify(postRepository).save(Mockito.argThat(p -> p.getLikeCount() == 1));
      Mockito.verify(commentRepository, Mockito.never()).findById(Mockito.anyString());
    }
  }
}
