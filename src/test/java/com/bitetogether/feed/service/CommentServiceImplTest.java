package com.bitetogether.feed.service;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.UserContextUtils;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.CommentRequest;
import com.bitetogether.feed.dto.response.CommentResponse;
import com.bitetogether.feed.mapper.CommentMapper;
import com.bitetogether.feed.model.Comment;
import com.bitetogether.feed.model.Like;
import com.bitetogether.feed.model.Post;
import com.bitetogether.feed.repository.CommentRepository;
import com.bitetogether.feed.repository.LikeRepository;
import com.bitetogether.feed.repository.PostRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.impl.CommentServiceImpl;
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
class CommentServiceImplTest {

  @Mock private CommentRepository commentRepository;

  @Mock private PostRepository postRepository;

  @Mock private CommentMapper commentMapper;

  @Mock private UserClient userClient;

  @Mock private LikeRepository likeRepository;

  @InjectMocks private CommentServiceImpl service;

  @Test
  void createComment_whenPostMissing_returnsNotFound() {
    CommentRequest request = new CommentRequest();
    request.setPostId("p1");

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(11L);
      Mockito.when(postRepository.findById("p1")).thenReturn(Optional.empty());

      ApiResponseDTO<CommentResponse> response = service.createComment(request);

      Assertions.assertEquals(ApiResponseStatus.NOT_FOUND.getCode(), response.getStatus());
      Assertions.assertNull(response.getData());
    }
  }

  @Test
  void createComment_whenReplyParentMissing_returnsNotFound() {
    CommentRequest request = new CommentRequest();
    request.setPostId("p1");
    request.setParentCommentId("parent1");

    Post post = new Post();
    post.setId("p1");
    post.setCommentCount(0);

    Comment comment = new Comment();
    comment.setPostId("p1");

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(11L);
      Mockito.when(postRepository.findById("p1")).thenReturn(Optional.of(post));
      Mockito.when(commentMapper.toComment(request)).thenReturn(comment);
      Mockito.when(commentRepository.findById("parent1")).thenReturn(Optional.empty());

      ApiResponseDTO<CommentResponse> response = service.createComment(request);

      Assertions.assertEquals(ApiResponseStatus.NOT_FOUND.getCode(), response.getStatus());
      Assertions.assertNull(response.getData());
      Mockito.verify(commentRepository, Mockito.never()).save(comment);
      Mockito.verify(postRepository, Mockito.never()).save(Mockito.any(Post.class));
    }
  }

  @Test
  void createComment_whenReplyAndUserClientThrows_stillCreates() {
    CommentRequest request = new CommentRequest();
    request.setPostId("p1");
    request.setParentCommentId("parentId");

    Post post = new Post();
    post.setId("p1");
    post.setCommentCount(5);

    Comment parent = new Comment();
    parent.setId("parentId");
    parent.setRepliesCount(2);

    Comment toSave = new Comment();
    toSave.setId("c1");
    toSave.setPostId("p1");
    toSave.setParentCommentId("parentId");

    CommentResponse mapped = new CommentResponse();
    mapped.setId("c1");

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(UserContextUtils::getCurrentUserId).thenReturn(99L);

      Mockito.when(postRepository.findById("p1")).thenReturn(Optional.of(post));
      Mockito.when(commentMapper.toComment(request)).thenReturn(toSave);
      Mockito.when(commentRepository.findById("parentId")).thenReturn(Optional.of(parent));

      Mockito.when(commentRepository.save(Mockito.same(parent))).thenReturn(parent);
      Mockito.when(commentRepository.save(Mockito.same(toSave))).thenReturn(toSave);

      Mockito.when(userClient.getUserById(99L)).thenThrow(new RuntimeException("down"));
      Mockito.when(commentMapper.toCommentResponse(toSave)).thenReturn(mapped);

      ApiResponseDTO<CommentResponse> response = service.createComment(request);

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertNotNull(response.getData());
      Assertions.assertEquals("c1", response.getData().getId());
      Assertions.assertNull(response.getData().getUser());
      Mockito.verify(postRepository).save(Mockito.argThat(p -> p.getCommentCount() == 6));
      Mockito.verify(commentRepository).save(Mockito.argThat(c -> c.getRepliesCount() == 3));
    }
  }

  @Test
  void getCommentById_whenMissing_returnsNotFound() {
    Mockito.when(commentRepository.findById("c1")).thenReturn(Optional.empty());

    ApiResponseDTO<CommentResponse> response = service.getCommentById("c1");

    Assertions.assertEquals(ApiResponseStatus.NOT_FOUND.getCode(), response.getStatus());
    Assertions.assertNull(response.getData());
  }

  @Test
  void getCommentById_whenFoundAndLikeLookupFails_setsAlreadyLikedFalse() {
    Comment comment = new Comment();
    comment.setId("c1");
    comment.setUserId(10L);

    CommentResponse mapped = new CommentResponse();
    mapped.setId("c1");

    ApiResponseDTO<UserDTO> userBody = new ApiResponseDTO<>();
    userBody.setData(new UserDTO());

    Mockito.when(commentRepository.findById("c1")).thenReturn(Optional.of(comment));
    Mockito.when(userClient.getUserById(10L)).thenReturn(ResponseEntity.ok(userBody));
    Mockito.when(commentMapper.toCommentResponse(comment)).thenReturn(mapped);

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(55L);
      Mockito.when(
              likeRepository.findByUserIdAndCommentIdIn(Mockito.eq(55L), Mockito.eq(List.of("c1"))))
          .thenThrow(new RuntimeException("db"));

      ApiResponseDTO<CommentResponse> response = service.getCommentById("c1");

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertNotNull(response.getData());
      Assertions.assertEquals("c1", response.getData().getId());
      Assertions.assertFalse(response.getData().isAlreadyLiked());
    }
  }

  @Test
  void getCommentsByPostId_whenEmptyPage_returnsSuccessEmptyList() {
    Page<Comment> page = new PageImpl<>(List.of());
    Mockito.when(
            commentRepository.findByPostIdAndParentCommentIdIsNull(
                Mockito.eq("p1"), Mockito.any(Pageable.class)))
        .thenReturn(page);

    ApiResponsePaginationDTO<CommentResponse> response = service.getCommentsByPostId("p1", 0, 10);

    Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
    Assertions.assertNotNull(response.getData());
    Assertions.assertTrue(response.getData().isEmpty());
  }

  @Test
  void getCommentsByUserId_whenLiked_setsAlreadyLikedTrue() {
    Comment comment = new Comment();
    comment.setId("c1");
    comment.setUserId(10L);

    CommentResponse mapped = new CommentResponse();
    mapped.setId("c1");

    Like like = new Like();
    like.setCommentId("c1");

    Page<Comment> page = new PageImpl<>(List.of(comment));

    ApiResponseDTO<UserDTO> userBody = new ApiResponseDTO<>();
    userBody.setData(new UserDTO());

    Mockito.when(commentRepository.findByUserId(Mockito.eq(10L), Mockito.any(Pageable.class)))
        .thenReturn(page);
    Mockito.when(userClient.getUserById(10L)).thenReturn(ResponseEntity.ok(userBody));
    Mockito.when(commentMapper.toCommentResponse(comment)).thenReturn(mapped);

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(10L);
      Mockito.when(likeRepository.findByUserIdAndCommentIdIn(10L, List.of("c1")))
          .thenReturn(List.of(like));

      ApiResponsePaginationDTO<CommentResponse> response = service.getCommentsByUserId(10L, 0, 10);

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertEquals(1, response.getData().size());
      Assertions.assertTrue(response.getData().get(0).isAlreadyLiked());
    }
  }

  @Test
  void updateComment_whenMissing_returnsNotFound() {
    CommentRequest request = new CommentRequest();
    Mockito.when(commentRepository.findById("c1")).thenReturn(Optional.empty());

    ApiResponseDTO<CommentResponse> response = service.updateComment("c1", request);

    Assertions.assertEquals(ApiResponseStatus.NOT_FOUND.getCode(), response.getStatus());
  }

  @Test
  void updateComment_whenNotOwner_returnsForbidden() {
    Comment existing = new Comment();
    existing.setId("c1");
    existing.setUserId(1L);

    CommentRequest request = new CommentRequest();

    Mockito.when(commentRepository.findById("c1")).thenReturn(Optional.of(existing));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(2L);

      ApiResponseDTO<CommentResponse> response = service.updateComment("c1", request);

      Assertions.assertEquals(ApiResponseStatus.FORBIDDEN.getCode(), response.getStatus());
      Mockito.verify(commentRepository, Mockito.never()).save(Mockito.any(Comment.class));
    }
  }

  @Test
  void updateComment_whenOwner_updatesAndReturnsMapped() {
    Comment existing = new Comment();
    existing.setId("c1");
    existing.setUserId(2L);

    CommentRequest request = new CommentRequest();

    CommentResponse mapped = new CommentResponse();
    mapped.setId("c1");

    ApiResponseDTO<UserDTO> userBody = new ApiResponseDTO<>();
    userBody.setData(new UserDTO());

    Mockito.when(commentRepository.findById("c1")).thenReturn(Optional.of(existing));
    Mockito.when(commentRepository.save(existing)).thenReturn(existing);
    Mockito.when(commentMapper.toCommentResponse(existing)).thenReturn(mapped);

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(2L);
      Mockito.when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(userBody));
      Mockito.when(likeRepository.findByUserIdAndCommentIdIn(2L, List.of("c1")))
          .thenReturn(List.of());

      ApiResponseDTO<CommentResponse> response = service.updateComment("c1", request);

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertNotNull(response.getData());
      Assertions.assertEquals("c1", response.getData().getId());
      Assertions.assertFalse(response.getData().isAlreadyLiked());
      Mockito.verify(commentMapper).updateCommentFromCommentRequest(request, existing);
    }
  }

  @Test
  void deleteComment_whenMissing_returnsNotFound() {
    Mockito.when(commentRepository.findById("c1")).thenReturn(Optional.empty());

    ApiResponseDTO<String> response = service.deleteComment("c1");

    Assertions.assertEquals(ApiResponseStatus.NOT_FOUND.getCode(), response.getStatus());
  }

  @Test
  void deleteComment_whenNotOwner_returnsForbidden() {
    Comment existing = new Comment();
    existing.setId("c1");
    existing.setUserId(1L);

    Mockito.when(commentRepository.findById("c1")).thenReturn(Optional.of(existing));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(2L);

      ApiResponseDTO<String> response = service.deleteComment("c1");

      Assertions.assertEquals(ApiResponseStatus.FORBIDDEN.getCode(), response.getStatus());
      Mockito.verify(commentRepository, Mockito.never()).delete(Mockito.any(Comment.class));
    }
  }

  @Test
  void deleteComment_whenOwner_decrementsPostAndParentWithFloorAtZero() {
    Comment existing = new Comment();
    existing.setId("c1");
    existing.setUserId(2L);
    existing.setPostId("p1");
    existing.setParentCommentId("parent1");

    Post post = new Post();
    post.setId("p1");
    post.setCommentCount(0);

    Comment parent = new Comment();
    parent.setId("parent1");
    parent.setRepliesCount(0);

    Mockito.when(commentRepository.findById("c1")).thenReturn(Optional.of(existing));
    Mockito.when(postRepository.findById("p1")).thenReturn(Optional.of(post));
    Mockito.when(commentRepository.findById("parent1")).thenReturn(Optional.of(parent));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(2L);

      ApiResponseDTO<String> response = service.deleteComment("c1");

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Mockito.verify(postRepository).save(Mockito.argThat(p -> p.getCommentCount() == 0));
      Mockito.verify(commentRepository).save(Mockito.argThat(c -> c.getRepliesCount() == 0));
      Mockito.verify(commentRepository).delete(existing);
    }
  }

  @Test
  void getRepliesByCommentId_whenParentMissing_returnsNotFound() {
    Mockito.when(commentRepository.findById("c1")).thenReturn(Optional.empty());

    ApiResponseDTO<List<CommentResponse>> response = service.getRepliesByCommentId("c1");

    Assertions.assertEquals(ApiResponseStatus.NOT_FOUND.getCode(), response.getStatus());
    Assertions.assertNull(response.getData());
  }

  @Test
  void getRepliesByCommentId_whenNoReplies_returnsSuccessEmptyList() {
    Comment parent = new Comment();
    parent.setId("c1");

    Mockito.when(commentRepository.findById("c1")).thenReturn(Optional.of(parent));
    Mockito.when(commentRepository.findByParentCommentId("c1")).thenReturn(List.of());

    ApiResponseDTO<List<CommentResponse>> response = service.getRepliesByCommentId("c1");

    Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
    Assertions.assertNotNull(response.getData());
    Assertions.assertTrue(response.getData().isEmpty());
  }
}
