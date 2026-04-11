package com.bitetogether.feed.controller;

import static com.bitetogether.common.util.Constants.DEFAULT_PAGE_NUMBER;
import static com.bitetogether.common.util.Constants.DEFAULT_PAGE_SIZE;
import static com.bitetogether.common.util.Constants.PREFIX_REQUEST_MAPPING_FEED;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.feed.dto.request.LikeRequest;
import com.bitetogether.feed.dto.response.LikeResponse;
import com.bitetogether.feed.service.inter.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Like Service", description = "APIs for managing likes on posts and comments")
@RequiredArgsConstructor
@RequestMapping(PREFIX_REQUEST_MAPPING_FEED + "/likes")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LikeController {

  LikeService likeService;

  @Operation(
      summary = "Like Post or Comment",
      description =
          "Like a post (if only have postId) or comment (if have both postId and commentId).")
  @PostMapping
  public ResponseEntity<ApiResponseDTO<LikeResponse>> like(@RequestBody LikeRequest request) {
    ApiResponseDTO<LikeResponse> response = likeService.like(request);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Unlike Post or Comment",
      description =
          "Remove a like from a post (if only have postId) or comment (if have both postId and commentId).")
  @DeleteMapping
  public ResponseEntity<ApiResponseDTO<String>> unlike(@RequestBody LikeRequest request) {
    ApiResponseDTO<String> response = likeService.unlike(request);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get Likes by User",
      description = "Retrieve all likes created by a specific user.")
  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponsePaginationDTO<LikeResponse>> getLikesByUser(
      @PathVariable Long userId,
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
    ApiResponsePaginationDTO<LikeResponse> response =
        likeService.getLikesByUser(userId, page, size);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get Likes by Post",
      description = "Retrieve all likes associated with a specific post.")
  @GetMapping("/post/{postId}")
  public ResponseEntity<ApiResponsePaginationDTO<LikeResponse>> getLikesByPost(
      @PathVariable String postId,
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
    ApiResponsePaginationDTO<LikeResponse> response =
        likeService.getLikesByPost(postId, page, size);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get Likes by Comment",
      description = "Retrieve all likes associated with a specific comment.")
  @GetMapping("/comment/{commentId}")
  public ResponseEntity<ApiResponsePaginationDTO<LikeResponse>> getLikesByComment(
      @PathVariable String commentId,
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
    ApiResponsePaginationDTO<LikeResponse> response =
        likeService.getLikesByComment(commentId, page, size);
    return ResponseEntity.ok(response);
  }
}
