package com.bitetogether.feed.controller;

import static com.bitetogether.common.util.Constants.DEFAULT_PAGE_NUMBER;
import static com.bitetogether.common.util.Constants.DEFAULT_PAGE_SIZE;
import static com.bitetogether.common.util.Constants.PREFIX_REQUEST_MAPPING_FEED;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.feed.dto.request.PostRequest;
import com.bitetogether.feed.dto.response.PostResponse;
import com.bitetogether.feed.service.inter.FirebaseStorageService;
import com.bitetogether.feed.service.inter.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Post Service", description = "APIs for managing posts")
@RequiredArgsConstructor
@RequestMapping(PREFIX_REQUEST_MAPPING_FEED)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PostController {
  PostService postService;
  FirebaseStorageService firebaseStorageService;

  @Operation(
      summary = "Create Post",
      description = "Create a new post with the provided content and metadata.")
  @PostMapping
  public ResponseEntity<ApiResponseDTO<PostResponse>> createPost(
      @RequestBody PostRequest postRequest) {
    ApiResponseDTO<PostResponse> response = postService.createPost(postRequest);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get Post by ID", description = "Retrieve a post by its unique identifier.")
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponseDTO<PostResponse>> getPostById(@PathVariable String id) {
    ApiResponseDTO<PostResponse> response = postService.getPostById(id);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get Posts by User ID",
      description = "Retrieve all posts created by a specific user.")
  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponsePaginationDTO<PostResponse>> getPostsByUserId(
      @PathVariable Long userId,
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
    ApiResponsePaginationDTO<PostResponse> response =
        postService.getPostsByUserId(userId, page, size);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get New Feeds",
      description = "Retrieve posts from friends ordered by newest.")
  @GetMapping("/new-feeds")
  public ResponseEntity<ApiResponsePaginationDTO<PostResponse>> getNewFeeds(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
    ApiResponsePaginationDTO<PostResponse> response = postService.getNewFeedTimeBased(page, size);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get New Feeds By Location",
      description = "Retrieve posts from friends within the map viewport.")
  @GetMapping("/new-feeds/location")
  public ResponseEntity<ApiResponsePaginationDTO<PostResponse>> getNewFeedsByLocation(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size,
      @RequestParam double latitude,
      @RequestParam double longitude,
      @RequestParam double latitudeDelta,
      @RequestParam double longitudeDelta) {
    ApiResponsePaginationDTO<PostResponse> response =
        postService.getNewFeedLocationBased(
            page, size, latitude, longitude, latitudeDelta, longitudeDelta);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Update Post",
      description = "Update an existing post with new content or metadata.")
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponseDTO<PostResponse>> updatePost(
      @PathVariable String id, @RequestBody PostRequest postRequest) {
    ApiResponseDTO<PostResponse> response = postService.updatePost(id, postRequest);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete Post", description = "Delete a post by its unique identifier.")
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponseDTO<String>> deletePost(@PathVariable String id) {
    ApiResponseDTO<String> response = postService.deletePost(id);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Upload Image",
      description =
          "Upload an image file to Firebase Storage and get the public URL. Maximum file size is 5MB. Supported formats: JPEG, PNG, GIF, WEBP.")
  @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponseDTO<String>> uploadImage(
      @RequestParam("file")
          @io.swagger.v3.oas.annotations.parameters.RequestBody(
              description = "Image file to upload",
              required = true,
              content =
                  @Content(
                      mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                      schema = @Schema(type = "string", format = "binary")))
          MultipartFile file) {
    String imageUrl = firebaseStorageService.uploadPostImage(file);
    ApiResponseDTO<String> response =
        com.bitetogether.common.util.ApiResponseUtil.buildApiResponse(
            com.bitetogether.common.enums.ApiResponseStatus.SUCCESS,
            "File uploaded successfully",
            imageUrl);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/test")
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("Feed service is working");
  }
}
