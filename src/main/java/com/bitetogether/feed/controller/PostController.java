package com.bitetogether.feed.controller;

import static com.bitetogether.common.util.Constants.PREFIX_REQUEST_MAPPING_FEED;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.feed.dto.request.PostRequest;
import com.bitetogether.feed.dto.response.PostResponse;
import com.bitetogether.feed.service.inter.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Post Service", description = "APIs for managing posts")
@RequiredArgsConstructor
@RequestMapping(PREFIX_REQUEST_MAPPING_FEED)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
  PostService postService;

  @Operation(
      summary = "Create Post",
      description = "Create a new post with the provided content and metadata.")
  @PostMapping
  public ResponseEntity<ApiResponse<PostResponse>> createPost(
      @RequestBody PostRequest postRequest) {
    ApiResponse<PostResponse> response = postService.createPost(postRequest);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get Post by ID", description = "Retrieve a post by its unique identifier.")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable String id) {
    ApiResponse<PostResponse> response = postService.getPostById(id);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get Posts by User ID",
      description = "Retrieve all posts created by a specific user.")
  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponsePagination<PostResponse>> getPostsByUserId(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    ApiResponsePagination<PostResponse> response = postService.getPostsByUserId(userId, page, size);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get New Feeds", description = "Retrieve the latest posts for the new feed.")
  @GetMapping("/new-feeds")
  public ResponseEntity<ApiResponsePagination<PostResponse>> getNewFeeds(
      @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
    ApiResponsePagination<PostResponse> response = postService.getNewFeed(page, size);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Update Post",
      description = "Update an existing post with new content or metadata.")
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<PostResponse>> updatePost(
      @PathVariable String id, @RequestBody PostRequest postRequest) {
    ApiResponse<PostResponse> response = postService.updatePost(id, postRequest);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete Post", description = "Delete a post by its unique identifier.")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<String>> deletePost(@PathVariable String id) {
    ApiResponse<String> response = postService.deletePost(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/test")
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("Feed service is working");
  }
}
