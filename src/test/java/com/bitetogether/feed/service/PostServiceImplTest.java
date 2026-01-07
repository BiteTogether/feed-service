package com.bitetogether.feed.service;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.UserContextUtils;
import com.bitetogether.feed.dto.FriendDTO;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.PostRequest;
import com.bitetogether.feed.dto.response.PostResponse;
import com.bitetogether.feed.mapper.PostMapper;
import com.bitetogether.feed.model.Like;
import com.bitetogether.feed.model.Post;
import com.bitetogether.feed.repository.LikeRepository;
import com.bitetogether.feed.repository.PostRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.impl.PostServiceImpl;
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
class PostServiceImplTest {

  @Mock private PostRepository postRepository;

  @Mock private PostMapper postMapper;

  @Mock private UserClient userClient;

  @Mock private LikeRepository likeRepository;

  @InjectMocks private PostServiceImpl service;

  @Test
  void createPost_success_setsUserId_mapsUser_handlesAlreadyLikedFailure() {
    PostRequest request = new PostRequest();

    Post mapped = new Post();

    Post saved = new Post();
    saved.setId("p1");
    saved.setUserId(9L);

    ApiResponseDTO<UserDTO> userBody = new ApiResponseDTO<>();
    userBody.setData(new UserDTO());

    PostResponse mappedResponse = new PostResponse();
    mappedResponse.setId("p1");

    Mockito.when(postMapper.toPost(Mockito.same(request))).thenReturn(mapped);
    Mockito.when(postRepository.save(Mockito.any(Post.class))).thenReturn(saved);
    Mockito.when(userClient.getUserById(Mockito.eq(9L))).thenReturn(ResponseEntity.ok(userBody));
    Mockito.when(postMapper.toPostResponse(saved)).thenReturn(mappedResponse);
    Mockito.when(likeRepository.existsByUserIdAndPostId(9L, "p1")).thenThrow(new RuntimeException("db"));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(9L);

      ApiResponseDTO<PostResponse> response = service.createPost(request);

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertNotNull(response.getData());
      Assertions.assertEquals("p1", response.getData().getId());
      Assertions.assertNotNull(response.getData().getUser());
      Assertions.assertFalse(response.getData().isAlreadyLiked());
      Mockito.verify(postRepository).save(Mockito.argThat(p -> p.getUserId() != null && p.getUserId().equals(9L)));
    }
  }

  @Test
  void getPostById_success_whenUserClientThrows_andAlreadyLikedTrue() {
    Post post = new Post();
    post.setId("p1");
    post.setUserId(11L);

    PostResponse mappedResponse = new PostResponse();
    mappedResponse.setId("p1");

    Mockito.when(postRepository.findById("p1")).thenReturn(Optional.of(post));
    Mockito.when(userClient.getUserById(11L)).thenThrow(new RuntimeException("down"));
    Mockito.when(postMapper.toPostResponse(post)).thenReturn(mappedResponse);
    Mockito.when(likeRepository.existsByUserIdAndPostId(55L, "p1")).thenReturn(true);

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(55L);

      ApiResponseDTO<PostResponse> response = service.getPostById("p1");

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertNotNull(response.getData());
      Assertions.assertNull(response.getData().getUser());
      Assertions.assertTrue(response.getData().isAlreadyLiked());
    }
  }

  @Test
  void getPostById_whenMissing_throwsRuntimeException() {
    Mockito.when(postRepository.findById("missing")).thenReturn(Optional.empty());

    RuntimeException ex =
        Assertions.assertThrows(RuntimeException.class, () -> service.getPostById("missing"));

    Assertions.assertTrue(ex.getMessage().contains("Post not found"));
  }

  @Test
  void getPostsByUserId_emptyPage_returnsSuccessEmptyList() {
    Page<Post> page = new PageImpl<>(List.of());

    Mockito.when(postRepository.findByUserIdOrderByCreatedAtDesc(Mockito.eq(1L), Mockito.any(Pageable.class)))
        .thenReturn(page);

    ApiResponsePaginationDTO<PostResponse> response = service.getPostsByUserId(1L, 0, 10);

    Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
    Assertions.assertNotNull(response.getData());
    Assertions.assertTrue(response.getData().isEmpty());
  }

  @Test
  void getPostsByUserId_returnsBatchAlreadyLikedTrue_whenLikeRepoWorks() {
    Post post = new Post();
    post.setId("p1");
    post.setUserId(2L);

    Page<Post> page = new PageImpl<>(List.of(post));

    Like like = new Like();
    like.setPostId("p1");

    ApiResponseDTO<UserDTO> userBody = new ApiResponseDTO<>();
    userBody.setData(new UserDTO());

    PostResponse mappedResponse = new PostResponse();
    mappedResponse.setId("p1");

    Mockito.when(postRepository.findByUserIdOrderByCreatedAtDesc(Mockito.eq(2L), Mockito.any(Pageable.class)))
        .thenReturn(page);

    Mockito.when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(userBody));
    Mockito.when(postMapper.toPostResponse(post)).thenReturn(mappedResponse);
    Mockito.when(likeRepository.findByUserIdAndPostIdIn(Mockito.eq(7L), Mockito.eq(List.of("p1"))))
        .thenReturn(List.of(like));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(7L);

      ApiResponsePaginationDTO<PostResponse> response = service.getPostsByUserId(2L, 0, 10);

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertEquals(1, response.getData().size());
      Assertions.assertTrue(response.getData().get(0).isAlreadyLiked());
      Assertions.assertNotNull(response.getData().get(0).getUser());
    }
  }

  @Test
  void getPostsByUserId_returnsBatchAlreadyLikedFalse_whenLikeRepoThrows() {
    Post post = new Post();
    post.setId("p1");
    post.setUserId(2L);

    Page<Post> page = new PageImpl<>(List.of(post));

    ApiResponseDTO<UserDTO> userBody = new ApiResponseDTO<>();
    userBody.setData(new UserDTO());

    PostResponse mappedResponse = new PostResponse();
    mappedResponse.setId("p1");

    Mockito.when(postRepository.findByUserIdOrderByCreatedAtDesc(Mockito.eq(2L), Mockito.any(Pageable.class)))
        .thenReturn(page);

    Mockito.when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(userBody));
    Mockito.when(postMapper.toPostResponse(post)).thenReturn(mappedResponse);

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(7L);
      Mockito.when(likeRepository.findByUserIdAndPostIdIn(Mockito.eq(7L), Mockito.eq(List.of("p1"))))
          .thenThrow(new RuntimeException("db"));

      ApiResponsePaginationDTO<PostResponse> response = service.getPostsByUserId(2L, 0, 10);

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertEquals(1, response.getData().size());
      Assertions.assertFalse(response.getData().get(0).isAlreadyLiked());
    }
  }

  @Test
  void updatePost_whenNotOwner_returnsForbidden() {
    Post post = new Post();
    post.setId("p1");
    post.setUserId(1L);

    Mockito.when(postRepository.findById("p1")).thenReturn(Optional.of(post));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(2L);

      ApiResponseDTO<PostResponse> response = service.updatePost("p1", new PostRequest());

      Assertions.assertEquals(ApiResponseStatus.FORBIDDEN.getCode(), response.getStatus());
      Assertions.assertNull(response.getData());
      Mockito.verify(postMapper, Mockito.never()).updatePostFromPostRequest(Mockito.any(PostRequest.class), Mockito.any(Post.class));
      Mockito.verify(postRepository, Mockito.never()).save(Mockito.any(Post.class));
    }
  }

  @Test
  void updatePost_whenOwner_updatesAndReturnsSuccess() {
    Post post = new Post();
    post.setId("p1");
    post.setUserId(2L);

    ApiResponseDTO<UserDTO> userBody = new ApiResponseDTO<>();
    userBody.setData(new UserDTO());

    PostResponse mappedResponse = new PostResponse();
    mappedResponse.setId("p1");

    Mockito.when(postRepository.findById("p1")).thenReturn(Optional.of(post));
    Mockito.when(postRepository.save(post)).thenReturn(post);
    Mockito.when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(userBody));
    Mockito.when(postMapper.toPostResponse(post)).thenReturn(mappedResponse);
    Mockito.when(likeRepository.existsByUserIdAndPostId(2L, "p1")).thenReturn(false);

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(2L);

      ApiResponseDTO<PostResponse> response = service.updatePost("p1", new PostRequest());

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertNotNull(response.getData());
      Assertions.assertEquals("p1", response.getData().getId());
      Mockito.verify(postMapper).updatePostFromPostRequest(Mockito.any(PostRequest.class), Mockito.same(post));
      Mockito.verify(postRepository).save(Mockito.same(post));
    }
  }

  @Test
  void deletePost_whenNotOwner_returnsForbidden() {
    Post post = new Post();
    post.setId("p1");
    post.setUserId(1L);

    Mockito.when(postRepository.findById("p1")).thenReturn(Optional.of(post));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(2L);

      ApiResponseDTO<String> response = service.deletePost("p1");

      Assertions.assertEquals(ApiResponseStatus.FORBIDDEN.getCode(), response.getStatus());
      Mockito.verify(postRepository, Mockito.never()).delete(Mockito.any(Post.class));
    }
  }

  @Test
  void deletePost_whenOwner_deletesAndReturnsSuccess() {
    Post post = new Post();
    post.setId("p1");
    post.setUserId(2L);

    Mockito.when(postRepository.findById("p1")).thenReturn(Optional.of(post));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(2L);

      ApiResponseDTO<String> response = service.deletePost("p1");

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Mockito.verify(postRepository).delete(Mockito.same(post));
    }
  }

  @Test
  void getNewFeed_whenUserClientThrows_returnsSuccessEmptyList() {
    Mockito.when(userClient.getFriendList(0, 100)).thenThrow(new RuntimeException("down"));
    Mockito.when(postRepository.findByUserIdInAndCreatedAtAfter(Mockito.eq(List.of()), Mockito.any(), Mockito.any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    ApiResponsePaginationDTO<PostResponse> response = service.getNewFeed(1, 1);

    Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
    Assertions.assertNotNull(response.getData());
    Assertions.assertTrue(response.getData().isEmpty());
  }

  @Test
  void getNewFeed_whenFriendBodyNull_returnsSuccessEmptyList() {
    Mockito.when(userClient.getFriendList(0, 100)).thenReturn(ResponseEntity.ok(null));
    Mockito.when(postRepository.findByUserIdInAndCreatedAtAfter(Mockito.eq(List.of()), Mockito.any(), Mockito.any(Pageable.class)))
        .thenReturn(new PageImpl<>(List.of()));

    ApiResponsePaginationDTO<PostResponse> response = service.getNewFeed(0, 10);

    Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
    Assertions.assertNotNull(response.getData());
    Assertions.assertTrue(response.getData().isEmpty());
  }

  @Test
  void getNewFeed_whenHasFriends_andBatchLikeWorks_setsAlreadyLikedTrue() {
    FriendDTO friend = Mockito.mock(FriendDTO.class);
    Mockito.when(friend.getId()).thenReturn(2L);

    ApiResponsePaginationDTO<FriendDTO> friendBody = new ApiResponsePaginationDTO<>();
    friendBody.setData(List.of(friend));

    Mockito.when(userClient.getFriendList(0, 100)).thenReturn(ResponseEntity.ok(friendBody));

    Post post = new Post();
    post.setId("p1");
    post.setUserId(2L);

    Page<Post> feedPage = new PageImpl<>(List.of(post));

    ApiResponseDTO<UserDTO> userBody = new ApiResponseDTO<>();
    userBody.setData(new UserDTO());

    PostResponse mappedResponse = new PostResponse();
    mappedResponse.setId("p1");

    Like like = new Like();
    like.setPostId("p1");

    Mockito.when(postRepository.findByUserIdInAndCreatedAtAfter(Mockito.eq(List.of(2L)), Mockito.any(), Mockito.any(Pageable.class)))
        .thenReturn(feedPage);

    Mockito.when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(userBody));
    Mockito.when(postMapper.toPostResponse(post)).thenReturn(mappedResponse);
    Mockito.when(likeRepository.findByUserIdAndPostIdIn(Mockito.eq(5L), Mockito.eq(List.of("p1"))))
        .thenReturn(List.of(like));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(() -> UserContextUtils.getCurrentUserId()).thenReturn(5L);

      ApiResponsePaginationDTO<PostResponse> response = service.getNewFeed(0, 10);

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertEquals(1, response.getData().size());
      Assertions.assertTrue(response.getData().get(0).isAlreadyLiked());
      Assertions.assertNotNull(response.getData().get(0).getUser());
    }
  }
}

