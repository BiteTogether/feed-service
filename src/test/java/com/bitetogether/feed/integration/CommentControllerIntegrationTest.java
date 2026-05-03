package com.bitetogether.feed.integration;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.CommentRequest;
import com.bitetogether.feed.model.Comment;
import com.bitetogether.feed.model.Post;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class CommentControllerIntegrationTest extends BaseIntegrationTest {

  private static final String COMMENTS_URL = BASE_URL + "/comments";

  @Test
  void createComment_success() throws Exception {
    Post post = createTestPost(1L, "Test post");
    mockUserClientForUser(2L);

    CommentRequest request = new CommentRequest();
    request.setPostId(post.getId());
    request.setContent("Nice post!");

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(COMMENTS_URL)
                .header(X_USER_ID, "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("Nice post!"));

    Assertions.assertEquals(1, commentRepository.count());
    Post updated = postRepository.findById(post.getId()).orElseThrow();
    Assertions.assertEquals(1, updated.getCommentCount());
  }

  @Test
  void createComment_postNotFound() throws Exception {
    CommentRequest request = new CommentRequest();
    request.setPostId("nonexistent");
    request.setContent("Lost comment");

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(COMMENTS_URL)
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404));
  }

  @Test
  void createComment_withReply_success() throws Exception {
    Post post = createTestPost(1L, "Test post");
    Comment parent = createTestComment(post.getId(), 2L, "Parent comment");
    mockUserClientForUser(3L);
    mockUserClientForUser(2L);

    CommentRequest request = new CommentRequest();
    request.setPostId(post.getId());
    request.setContent("Reply!");
    request.setParentCommentId(parent.getId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(COMMENTS_URL)
                .header(X_USER_ID, "3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("Reply!"));

    Comment updatedParent = commentRepository.findById(parent.getId()).orElseThrow();
    Assertions.assertEquals(1, updatedParent.getRepliesCount());
  }

  @Test
  void createComment_parentNotFound() throws Exception {
    Post post = createTestPost(1L, "Test post");

    CommentRequest request = new CommentRequest();
    request.setPostId(post.getId());
    request.setContent("Reply to nothing");
    request.setParentCommentId("nonexistent");

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(COMMENTS_URL)
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404));
  }

  @Test
  void getCommentById_success() throws Exception {
    Post post = createTestPost(1L, "Test post");
    Comment comment = createTestComment(post.getId(), 2L, "My comment");
    mockUserClientForUser(2L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(COMMENTS_URL + "/" + comment.getId()).header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("My comment"));
  }

  @Test
  void getCommentById_notFound() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(COMMENTS_URL + "/nonexistent").header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404));
  }

  @Test
  void getCommentsByPost_success() throws Exception {
    Post post = createTestPost(1L, "Test post");
    createTestComment(post.getId(), 2L, "Comment 1");
    createTestComment(post.getId(), 3L, "Comment 2");
    mockUserClientForUser(2L);
    mockUserClientForUser(3L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(COMMENTS_URL + "/post/" + post.getId())
                .header(X_USER_ID, "1")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2));
  }

  @Test
  void getCommentsByUser_success() throws Exception {
    Post post = createTestPost(1L, "Test post");
    createTestComment(post.getId(), 2L, "Comment A");
    createTestComment(post.getId(), 2L, "Comment B");
    mockUserClientForUser(2L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(COMMENTS_URL + "/user/2")
                .header(X_USER_ID, "2")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2));
  }

  @Test
  void getRepliesByComment_success() throws Exception {
    Post post = createTestPost(1L, "Test post");
    Comment parent = createTestComment(post.getId(), 2L, "Parent");
    createTestReply(post.getId(), 3L, "Reply 1", parent.getId());
    createTestReply(post.getId(), 4L, "Reply 2", parent.getId());
    mockUserClientForUser(3L);
    mockUserClientForUser(4L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(COMMENTS_URL + "/" + parent.getId() + "/replies")
                .header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2));
  }

  @Test
  void getRepliesByComment_parentNotFound() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(COMMENTS_URL + "/nonexistent/replies")
                .header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404));
  }

  @Test
  void updateComment_success() throws Exception {
    Post post = createTestPost(1L, "Test post");
    Comment comment = createTestComment(post.getId(), 2L, "Original");
    mockUserClientForUser(2L);

    CommentRequest updateReq = new CommentRequest();
    updateReq.setContent("Updated comment");

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(COMMENTS_URL + "/" + comment.getId())
                .header(X_USER_ID, "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("Updated comment"));
  }

  @Test
  void updateComment_forbidden_whenNotOwner() throws Exception {
    Post post = createTestPost(1L, "Test post");
    Comment comment = createTestComment(post.getId(), 2L, "My comment");

    CommentRequest updateReq = new CommentRequest();
    updateReq.setContent("Hacked");

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(COMMENTS_URL + "/" + comment.getId())
                .header(X_USER_ID, "3")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(403));
  }

  @Test
  void updateComment_notFound() throws Exception {
    CommentRequest updateReq = new CommentRequest();
    updateReq.setContent("Updated");

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(COMMENTS_URL + "/nonexistent")
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404));
  }

  @Test
  void deleteComment_success() throws Exception {
    Post post = createTestPost(1L, "Test post");
    post.setCommentCount(1);
    postRepository.save(post);
    Comment comment = createTestComment(post.getId(), 2L, "To delete");

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(COMMENTS_URL + "/" + comment.getId())
                .header(X_USER_ID, "2"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200));

    Assertions.assertEquals(0, commentRepository.count());
    Post updatedPost = postRepository.findById(post.getId()).orElseThrow();
    Assertions.assertEquals(0, updatedPost.getCommentCount());
  }

  @Test
  void deleteComment_forbidden_whenNotOwner() throws Exception {
    Post post = createTestPost(1L, "Test post");
    Comment comment = createTestComment(post.getId(), 2L, "My comment");

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(COMMENTS_URL + "/" + comment.getId())
                .header(X_USER_ID, "3"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(403));

    Assertions.assertEquals(1, commentRepository.count());
  }

  @Test
  void deleteComment_notFound() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(COMMENTS_URL + "/nonexistent").header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404));
  }

  @Test
  void deleteComment_withParent_decrementsParentRepliesCount() throws Exception {
    Post post = createTestPost(1L, "Test post");
    post.setCommentCount(2);
    postRepository.save(post);
    Comment parent = createTestComment(post.getId(), 2L, "Parent");
    parent.setRepliesCount(1);
    commentRepository.save(parent);
    Comment reply = createTestReply(post.getId(), 3L, "Reply", parent.getId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(COMMENTS_URL + "/" + reply.getId())
                .header(X_USER_ID, "3"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200));

    Comment updatedParent = commentRepository.findById(parent.getId()).orElseThrow();
    Assertions.assertEquals(0, updatedParent.getRepliesCount());
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
