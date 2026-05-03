package com.bitetogether.feed.service;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.UserContextUtils;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.response.PostResponse;
import com.bitetogether.feed.mapper.PostMapper;
import com.bitetogether.feed.model.Like;
import com.bitetogether.feed.model.Post;
import com.bitetogether.feed.model.SavePost;
import com.bitetogether.feed.repository.LikeRepository;
import com.bitetogether.feed.repository.PostRepository;
import com.bitetogether.feed.repository.SavePostRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.impl.SavePostServiceImpl;
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
class SavePostServiceImplTest {

  @Mock private SavePostRepository savePostRepository;

  @Mock private PostRepository postRepository;

  @Mock private PostMapper postMapper;

  @Mock private UserClient userClient;

  @Mock private LikeRepository likeRepository;

  @InjectMocks private SavePostServiceImpl service;

  @Test
  void savePost_whenPostMissing_returnsNotFound() {
    Mockito.when(postRepository.findById("p1")).thenReturn(Optional.empty());

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(UserContextUtils::getCurrentUserId).thenReturn(1L);

      ApiResponseDTO<PostResponse> response = service.savePost("p1");

      Assertions.assertEquals(ApiResponseStatus.NOT_FOUND.getCode(), response.getStatus());
      Mockito.verify(savePostRepository, Mockito.never()).save(Mockito.any(SavePost.class));
    }
  }

  @Test
  void savePost_whenAlreadySaved_returnsConflict() {
    Post post = new Post();
    post.setId("p1");
    post.setUserId(2L);

    Mockito.when(postRepository.findById("p1")).thenReturn(Optional.of(post));
    Mockito.when(savePostRepository.existsByUserIdAndPostId(1L, "p1")).thenReturn(true);

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(UserContextUtils::getCurrentUserId).thenReturn(1L);

      ApiResponseDTO<PostResponse> response = service.savePost("p1");

      Assertions.assertEquals(ApiResponseStatus.CONFLICT.getCode(), response.getStatus());
      Mockito.verify(savePostRepository, Mockito.never()).save(Mockito.any(SavePost.class));
    }
  }

  @Test
  void savePost_success_returnsPostResponse() {
    Post post = new Post();
    post.setId("p1");
    post.setUserId(2L);

    PostResponse mappedResponse = new PostResponse();
    mappedResponse.setId("p1");

    ApiResponseDTO<UserDTO> userBody = new ApiResponseDTO<>();
    userBody.setData(new UserDTO());

    Mockito.when(postRepository.findById("p1")).thenReturn(Optional.of(post));
    Mockito.when(savePostRepository.existsByUserIdAndPostId(1L, "p1")).thenReturn(false, true);
    Mockito.when(postMapper.toPostResponse(post)).thenReturn(mappedResponse);
    Mockito.when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(userBody));
    Mockito.when(likeRepository.existsByUserIdAndPostId(1L, "p1")).thenReturn(true);

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(UserContextUtils::getCurrentUserId).thenReturn(1L);

      ApiResponseDTO<PostResponse> response = service.savePost("p1");

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertNotNull(response.getData());
      Assertions.assertEquals("p1", response.getData().getId());
      Assertions.assertTrue(response.getData().isAlreadyLiked());
      Assertions.assertTrue(response.getData().isAlreadySaved());
      Mockito.verify(savePostRepository)
          .save(
              Mockito.argThat(
                  savePost ->
                      savePost.getUserId().equals(1L) && savePost.getPostId().equals("p1")));
    }
  }

  @Test
  void getSavedPosts_returnsMappedPostsAndSkipsMissingPosts() {
    SavePost savePost1 = new SavePost();
    savePost1.setPostId("p1");
    SavePost savePost2 = new SavePost();
    savePost2.setPostId("missing");

    Page<SavePost> page = new PageImpl<>(List.of(savePost1, savePost2));

    Post post = new Post();
    post.setId("p1");
    post.setUserId(2L);

    PostResponse mappedResponse = new PostResponse();
    mappedResponse.setId("p1");

    ApiResponseDTO<UserDTO> userBody = new ApiResponseDTO<>();
    userBody.setData(new UserDTO());

    Like like = new Like();
    like.setPostId("p1");

    Mockito.when(savePostRepository.findByUserId(Mockito.eq(1L), Mockito.any(Pageable.class)))
        .thenReturn(page);
    Mockito.when(postRepository.findAllById(List.of("p1", "missing"))).thenReturn(List.of(post));
    Mockito.when(postMapper.toPostResponse(post)).thenReturn(mappedResponse);
    Mockito.when(userClient.getUserById(2L)).thenReturn(ResponseEntity.ok(userBody));
    Mockito.when(likeRepository.findByUserIdAndPostIdIn(1L, List.of("p1", "missing")))
        .thenReturn(List.of(like));
    Mockito.when(savePostRepository.findByUserIdAndPostIdIn(1L, List.of("p1", "missing")))
        .thenReturn(List.of(savePost1, savePost2));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(UserContextUtils::getCurrentUserId).thenReturn(1L);

      ApiResponsePaginationDTO<PostResponse> response = service.getSavedPosts(0, 10);

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Assertions.assertEquals(1, response.getData().size());
      Assertions.assertEquals("p1", response.getData().getFirst().getId());
      Assertions.assertTrue(response.getData().getFirst().isAlreadyLiked());
      Assertions.assertTrue(response.getData().getFirst().isAlreadySaved());
    }
  }

  @Test
  void savePost_whenBlankPostId_returnsBadRequest() {
    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(UserContextUtils::getCurrentUserId).thenReturn(1L);

      ApiResponseDTO<PostResponse> response = service.savePost("  ");

      Assertions.assertEquals(ApiResponseStatus.BAD_REQUEST.getCode(), response.getStatus());
      Mockito.verify(postRepository, Mockito.never()).findById(Mockito.anyString());
    }
  }

  @Test
  void savePost_whenNullPostId_returnsBadRequest() {
    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(UserContextUtils::getCurrentUserId).thenReturn(1L);

      ApiResponseDTO<PostResponse> response = service.savePost(null);

      Assertions.assertEquals(ApiResponseStatus.BAD_REQUEST.getCode(), response.getStatus());
    }
  }

  @Test
  void deleteSavedPost_whenNotFound_returnsNotFound() {
    Mockito.when(savePostRepository.findByUserIdAndPostId(1L, "p99")).thenReturn(Optional.empty());

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(UserContextUtils::getCurrentUserId).thenReturn(1L);

      ApiResponseDTO<String> response = service.deleteSavedPost("p99");

      Assertions.assertEquals(ApiResponseStatus.NOT_FOUND.getCode(), response.getStatus());
      Mockito.verify(savePostRepository, Mockito.never()).delete(Mockito.any(SavePost.class));
    }
  }

  @Test
  void deleteSavedPost_whenFound_deletesSuccessfully() {
    SavePost existing = new SavePost();
    existing.setId("s1");
    existing.setPostId("p1");
    existing.setUserId(1L);

    Mockito.when(savePostRepository.findByUserIdAndPostId(1L, "p1"))
        .thenReturn(Optional.of(existing));

    try (MockedStatic<UserContextUtils> mocked = Mockito.mockStatic(UserContextUtils.class)) {
      mocked.when(UserContextUtils::getCurrentUserId).thenReturn(1L);

      ApiResponseDTO<String> response = service.deleteSavedPost("p1");

      Assertions.assertEquals(ApiResponseStatus.SUCCESS.getCode(), response.getStatus());
      Mockito.verify(savePostRepository).delete(existing);
    }
  }
}
