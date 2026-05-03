package com.bitetogether.feed.integration;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.feed.dto.FriendDTO;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.PostRequest;
import com.bitetogether.feed.model.Post;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class PostControllerIntegrationTest extends BaseIntegrationTest {

  @Test
  void createPost_success() throws Exception {
    PostRequest request = new PostRequest();
    request.setContent("Great food!");
    request.setPlaceId("p1");
    request.setPlaceName("Test Place");
    request.setPlaceAddress("123 St");
    request.setLatitude(10.0);
    request.setLongitude(106.0);
    request.setRating(5);

    mockUserClientForUser(1L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.post(BASE_URL)
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("Great food!"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.rating").value(5));

    Assertions.assertEquals(1, postRepository.count());
  }

  @Test
  void getPostById_success() throws Exception {
    Post post = createTestPost(1L, "Hello World");
    mockUserClientForUser(1L);

    mockMvc
        .perform(MockMvcRequestBuilders.get(BASE_URL + "/" + post.getId()).header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("Hello World"))
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.id").value(post.getId()));
  }

  @Test
  void getPostById_notFound_throwsException() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(BASE_URL + "/nonexistent").header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isInternalServerError());
  }

  @Test
  void getPostsByUserId_success() throws Exception {
    createTestPost(1L, "Post 1");
    createTestPost(1L, "Post 2");
    createTestPost(2L, "Post 3");
    mockUserClientForUser(1L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(BASE_URL + "/user/1")
                .header(X_USER_ID, "1")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2));
  }

  @Test
  void updatePost_success() throws Exception {
    Post post = createTestPost(1L, "Original");
    post.setPhotoUrl("http://example.com/photo.jpg");
    postRepository.save(post);
    mockUserClientForUser(1L);

    PostRequest updateRequest = new PostRequest();
    updateRequest.setContent("Updated content");
    updateRequest.setPhotoUrl("http://example.com/photo.jpg");

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(BASE_URL + "/" + post.getId())
                .header(X_USER_ID, "1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.content").value("Updated content"));
  }

  @Test
  void updatePost_forbidden_whenNotOwner() throws Exception {
    Post post = createTestPost(1L, "Original");
    mockUserClientForUser(2L);

    PostRequest updateRequest = new PostRequest();
    updateRequest.setContent("Hacked");

    mockMvc
        .perform(
            MockMvcRequestBuilders.put(BASE_URL + "/" + post.getId())
                .header(X_USER_ID, "2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(403));
  }

  @Test
  void deletePost_success() throws Exception {
    Post post = createTestPost(1L, "To be deleted");

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(BASE_URL + "/" + post.getId()).header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200));

    Assertions.assertEquals(0, postRepository.count());
  }

  @Test
  void deletePost_forbidden_whenNotOwner() throws Exception {
    Post post = createTestPost(1L, "My post");

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(BASE_URL + "/" + post.getId()).header(X_USER_ID, "2"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(403));

    Assertions.assertEquals(1, postRepository.count());
  }

  @Test
  void deletePost_withPhoto_deletesFromFirebase() throws Exception {
    Post post = createTestPost(1L, "Photo post");
    post.setPhotoUrl("https://firebase.com/photo.jpg");
    postRepository.save(post);

    mockMvc
        .perform(
            MockMvcRequestBuilders.delete(BASE_URL + "/" + post.getId()).header(X_USER_ID, "1"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(200));

    Mockito.verify(firebaseStorageService).deleteFile("https://firebase.com/photo.jpg");
    Assertions.assertEquals(0, postRepository.count());
  }

  @Test
  void getNewFeeds_success() throws Exception {
    createTestPost(1L, "My post");
    createTestPost(2L, "Friend post");

    ApiResponsePaginationDTO<FriendDTO> friendResponse = new ApiResponsePaginationDTO<>();
    FriendDTO friend = FriendDTO.builder().id(2L).username("friend").fullName("Friend").build();
    friendResponse.setData(List.of(friend));
    Mockito.when(userClient.getFriendList(0, 100)).thenReturn(ResponseEntity.ok(friendResponse));
    mockUserClientForUser(1L);
    mockUserClientForUser(2L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(BASE_URL + "/new-feeds")
                .header(X_USER_ID, "1")
                .param("page", "0")
                .param("size", "10"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2));
  }

  @Test
  void getNewFeedsByLocation_success() throws Exception {
    Post post = createTestPost(1L, "Nearby post");
    post.setLatitude(10.77);
    post.setLongitude(106.70);
    postRepository.save(post);

    ApiResponsePaginationDTO<FriendDTO> friendResponse = new ApiResponsePaginationDTO<>();
    friendResponse.setData(List.of());
    Mockito.when(userClient.getFriendList(0, 100)).thenReturn(ResponseEntity.ok(friendResponse));
    mockUserClientForUser(1L);

    mockMvc
        .perform(
            MockMvcRequestBuilders.get(BASE_URL + "/new-feeds/location")
                .header(X_USER_ID, "1")
                .param("page", "0")
                .param("size", "10")
                .param("latitude", "10.77")
                .param("longitude", "106.70")
                .param("latitudeDelta", "1.0")
                .param("longitudeDelta", "1.0"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(1));
  }

  @Test
  void test_endpoint_works() throws Exception {
    mockMvc
        .perform(MockMvcRequestBuilders.get(BASE_URL + "/test"))
        .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string("Feed service is working"));
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
