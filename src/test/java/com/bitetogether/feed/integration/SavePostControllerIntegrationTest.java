package com.bitetogether.feed.integration;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.model.Post;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class SavePostControllerIntegrationTest extends BaseIntegrationTest {

  private static final String SAVED_URL = BASE_URL + "/saved-posts";

  @Test
  void savePost_success() throws Exception {
    Post post = createTestPost(2L, "Test post");
    mockUserClientForUser(1L);
    mockUserClientForUser(2L);

    mockMvc
        .perform(MockMvcRequestBuilders.post(SAVED_URL + "/" + post.getId()).header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200))
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.alreadySaved").value(true));

    Assertions.assertEquals(1, savePostRepository.count());
  }

  @Test
  void savePost_postNotFound() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.post(SAVED_URL + "/nonexistent").header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404));
  }

  @Test
  void savePost_alreadySaved_conflict() throws Exception {
    Post post = createTestPost(2L, "Test post");
    createTestSavePost(1L, post.getId());

    mockMvc
        .perform(MockMvcRequestBuilders.post(SAVED_URL + "/" + post.getId()).header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(409));
  }

  @Test
  void getSavedPosts_success() throws Exception {
    Post post1 = createTestPost(2L, "Post 1");
    Post post2 = createTestPost(3L, "Post 2");
    createTestSavePost(1L, post1.getId());
    createTestSavePost(1L, post2.getId());
    mockUserClientForUser(2L);
    mockUserClientForUser(3L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(SAVED_URL)
                .header(X_USER_ID, "1")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2));
  }

  @Test
  void getSavedPosts_empty() throws Exception {
    mockMvc
        .perform(
            MockMvcRequestBuilders.get(SAVED_URL)
                .header(X_USER_ID, "1")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(0));
  }

  @Test
  void deleteSavedPost_success() throws Exception {
    Post post = createTestPost(2L, "Test post");
    createTestSavePost(1L, post.getId());

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(SAVED_URL + "/" + post.getId()).header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200));

    Assertions.assertEquals(0, savePostRepository.count());
  }

  @Test
  void deleteSavedPost_notFound() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.delete(SAVED_URL + "/nonexistent").header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(404));
  }

  @Test
  void savePost_blankPostId_badRequest() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.post(SAVED_URL + "/ ").header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(400));
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
