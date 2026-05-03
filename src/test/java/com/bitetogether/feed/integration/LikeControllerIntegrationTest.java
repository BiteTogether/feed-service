package com.bitetogether.feed.integration;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.LikeRequest;
import com.bitetogether.feed.model.Comment;
import com.bitetogether.feed.model.Post;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class LikeControllerIntegrationTest extends BaseIntegrationTest {

  private static final String LIKES_URL = BASE_URL + "/likes";

  @Test
  void likePost_success() throws Exception {
    Post post = createTestPost(2L, "Test post");
    mockUserClientForUser(1L);

    LikeRequest request = new LikeRequest();
    request.setPostId(post.getId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(LIKES_URL)
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200));

    Assertions.assertEquals(1, likeRepository.count());
    Post updated = postRepository.findById(post.getId()).orElseThrow();
    Assertions.assertEquals(1, updated.getLikeCount());
  }

  @Test
  void likePost_alreadyLiked_conflict() throws Exception {
    Post post = createTestPost(2L, "Test post");
    createTestLikeOnPost(1L, post.getId());

    LikeRequest request = new LikeRequest();
    request.setPostId(post.getId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(LIKES_URL)
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(409));
  }

  @Test
  void likePost_missingTargets_badRequest() throws Exception {
    LikeRequest request = new LikeRequest();

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(LIKES_URL)
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400));
  }

  @Test
  void likeComment_success() throws Exception {
    Post post = createTestPost(2L, "Test post");
    Comment comment = createTestComment(post.getId(), 2L, "Comment");
    mockUserClientForUser(1L);

    LikeRequest request = new LikeRequest();
    request.setPostId(post.getId());
    request.setCommentId(comment.getId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(LIKES_URL)
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200));

    Comment updated = commentRepository.findById(comment.getId()).orElseThrow();
    Assertions.assertEquals(1, updated.getLikeCount());
  }

  @Test
  void unlikePost_success() throws Exception {
    Post post = createTestPost(2L, "Test post");
    post.setLikeCount(1);
    postRepository.save(post);
    createTestLikeOnPost(1L, post.getId());

    LikeRequest request = new LikeRequest();
    request.setPostId(post.getId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(LIKES_URL)
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200));

    Assertions.assertEquals(0, likeRepository.count());
    Post updated = postRepository.findById(post.getId()).orElseThrow();
    Assertions.assertEquals(0, updated.getLikeCount());
  }

  @Test
  void unlikePost_notFound() throws Exception {
    LikeRequest request = new LikeRequest();
    request.setPostId("nonexistent");

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(LIKES_URL)
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404));
  }

  @Test
  void unlikePost_missingTargets_badRequest() throws Exception {
    LikeRequest request = new LikeRequest();

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(LIKES_URL)
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400));
  }

  @Test
  void unlikeComment_success() throws Exception {
    Post post = createTestPost(2L, "Test post");
    Comment comment = createTestComment(post.getId(), 2L, "Comment");
    comment.setLikeCount(1);
    commentRepository.save(comment);
    createTestLikeOnComment(1L, comment.getId());

    LikeRequest request = new LikeRequest();
    request.setCommentId(comment.getId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(LIKES_URL)
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200));

    Comment updated = commentRepository.findById(comment.getId()).orElseThrow();
    Assertions.assertEquals(0, updated.getLikeCount());
  }

  @Test
  void getLikesByPost_success() throws Exception {
    Post post = createTestPost(2L, "Test post");
    createTestLikeOnPost(1L, post.getId());
    createTestLikeOnPost(3L, post.getId());
    mockUserClientForUser(1L);
    mockUserClientForUser(3L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(LIKES_URL + "/post/" + post.getId())
                .header(X_USER_ID, "1")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2));
  }

  @Test
  void getLikesByUser_success() throws Exception {
    Post post1 = createTestPost(2L, "Post 1");
    Post post2 = createTestPost(2L, "Post 2");
    createTestLikeOnPost(1L, post1.getId());
    createTestLikeOnPost(1L, post2.getId());
    mockUserClientForUser(1L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(LIKES_URL + "/user/1")
                .header(X_USER_ID, "1")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2));
  }

  @Test
  void getLikesByComment_success() throws Exception {
    Post post = createTestPost(2L, "Test post");
    Comment comment = createTestComment(post.getId(), 2L, "Comment");
    createTestLikeOnComment(1L, comment.getId());
    mockUserClientForUser(1L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(LIKES_URL + "/comment/" + comment.getId())
                .header(X_USER_ID, "1")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(1));
  }

  private void mockUserClientForUser(Long userId) {
    ApiResponseDTO<UserDTO> userBody = new ApiResponseDTO<>();
    UserDTO userDto = new UserDTO();
    userDto.setId(userId);
    userDto.setFullName("User " + userId);
    userDto.setUsername("user" + userId);
    userBody.setData(userDto);
    Mockito.when(userClient.getUserById(userId)).thenReturn(ResponseEntity.ok(userBody));
  }
}
