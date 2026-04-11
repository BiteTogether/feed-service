package com.bitetogether.feed.controller;

import static com.bitetogether.common.util.Constants.DEFAULT_PAGE_NUMBER;
import static com.bitetogether.common.util.Constants.DEFAULT_PAGE_SIZE;
import static com.bitetogether.common.util.Constants.PREFIX_REQUEST_MAPPING_FEED;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.feed.dto.request.CommentRequest;
import com.bitetogether.feed.dto.response.CommentResponse;
import com.bitetogether.feed.service.inter.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Comment Service", description = "APIs for managing comments on posts")
@RequiredArgsConstructor
@RequestMapping(PREFIX_REQUEST_MAPPING_FEED + "/comments")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentController {

  CommentService commentService;

  @Operation(summary = "Create Comment", description = "Create a new comment for a post.")
  @PostMapping
  public ResponseEntity<ApiResponseDTO<CommentResponse>> createComment(
      @Valid @RequestBody CommentRequest request) {
    ApiResponseDTO<CommentResponse> response = commentService.createComment(request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Get Comment by ID", description = "Retrieve a single comment by its ID.")
  @GetMapping("/{commentId}")
  public ResponseEntity<ApiResponseDTO<CommentResponse>> getCommentById(
      @PathVariable String commentId) {
    ApiResponseDTO<CommentResponse> response = commentService.getCommentById(commentId);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get Comments by Post",
      description = "Retrieve all comments for a specific post.")
  @GetMapping("/post/{postId}")
  public ResponseEntity<ApiResponsePaginationDTO<CommentResponse>> getCommentsByPost(
      @PathVariable String postId,
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
    ApiResponsePaginationDTO<CommentResponse> response =
        commentService.getCommentsByPostId(postId, page, size);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get Comments by User",
      description = "Retrieve all comments created by a user.")
  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponsePaginationDTO<CommentResponse>> getCommentsByUser(
      @PathVariable Long userId,
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
    ApiResponsePaginationDTO<CommentResponse> response =
        commentService.getCommentsByUserId(userId, page, size);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get Replies by Comment",
      description =
          "Retrieve direct replies for a specific comment. Returns flat list of replies with user info and like status.")
  @GetMapping("/{commentId}/replies")
  public ResponseEntity<ApiResponseDTO<List<CommentResponse>>> getRepliesByComment(
      @PathVariable String commentId) {
    ApiResponseDTO<List<CommentResponse>> response =
        commentService.getRepliesByCommentId(commentId);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Update Comment", description = "Update the content of an existing comment.")
  @PutMapping("/{commentId}")
  public ResponseEntity<ApiResponseDTO<CommentResponse>> updateComment(
      @PathVariable String commentId, @Valid @RequestBody CommentRequest request) {
    ApiResponseDTO<CommentResponse> response = commentService.updateComment(commentId, request);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete Comment", description = "Delete a comment by its ID.")
  @DeleteMapping("/{commentId}")
  public ResponseEntity<ApiResponseDTO<String>> deleteComment(@PathVariable String commentId) {
    ApiResponseDTO<String> response = commentService.deleteComment(commentId);
    return ResponseEntity.ok(response);
  }
}
