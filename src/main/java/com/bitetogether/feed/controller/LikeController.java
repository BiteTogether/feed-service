package com.bitetogether.feed.controller;

import static com.bitetogether.common.util.Constants.PREFIX_REQUEST_MAPPING_FEED;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.common.dto.PaginationRequest;
import com.bitetogether.feed.dto.request.LikeRequest;
import com.bitetogether.feed.dto.response.LikeResponse;
import com.bitetogether.feed.service.inter.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
  public ResponseEntity<ApiResponse<LikeResponse>> like(@RequestBody LikeRequest request) {
    ApiResponse<LikeResponse> response = likeService.like(request);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Unlike Post or Comment",
      description =
          "Remove a like from a post (if only have postId) or comment (if have both postId and commentId).")
  @DeleteMapping
  public ResponseEntity<ApiResponse<String>> unlike(@RequestBody LikeRequest request) {
    ApiResponse<String> response = likeService.unlike(request);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get Likes by User",
      description = "Retrieve all likes created by a specific user.")
  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponsePagination<LikeResponse>> getLikesByUser(
      @PathVariable Long userId, @Valid @RequestBody PaginationRequest paginationRequest) {
    ApiResponsePagination<LikeResponse> response =
        likeService.getLikesByUser(
            userId, paginationRequest.getPage(), paginationRequest.getSize());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get Likes by Post",
      description = "Retrieve all likes associated with a specific post.")
  @GetMapping("/post/{postId}")
  public ResponseEntity<ApiResponsePagination<LikeResponse>> getLikesByPost(
      @PathVariable String postId, @Valid @RequestBody PaginationRequest paginationRequest) {
    ApiResponsePagination<LikeResponse> response =
        likeService.getLikesByPost(
            postId, paginationRequest.getPage(), paginationRequest.getSize());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get Likes by Comment",
      description = "Retrieve all likes associated with a specific comment.")
  @GetMapping("/comment/{commentId}")
  public ResponseEntity<ApiResponsePagination<LikeResponse>> getLikesByComment(
      @PathVariable String commentId, @Valid @RequestBody PaginationRequest paginationRequest) {
    ApiResponsePagination<LikeResponse> response =
        likeService.getLikesByComment(
            commentId, paginationRequest.getPage(), paginationRequest.getSize());
    return ResponseEntity.ok(response);
  }
}
