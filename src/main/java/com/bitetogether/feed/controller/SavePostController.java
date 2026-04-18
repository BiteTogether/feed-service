package com.bitetogether.feed.controller;

import static com.bitetogether.common.util.Constants.DEFAULT_PAGE_NUMBER;
import static com.bitetogether.common.util.Constants.DEFAULT_PAGE_SIZE;
import static com.bitetogether.common.util.Constants.PREFIX_REQUEST_MAPPING_FEED;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.feed.dto.response.PostResponse;
import com.bitetogether.feed.service.inter.SavePostService;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Saved Post Service", description = "APIs for managing saved posts")
@RequiredArgsConstructor
@RequestMapping(PREFIX_REQUEST_MAPPING_FEED + "/saved-posts")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SavePostController {

  SavePostService savePostService;

  @Operation(summary = "Save Post", description = "Save a post by postId for current user.")
  @PostMapping("/{postId}")
  public ResponseEntity<ApiResponseDTO<PostResponse>> savePost(@PathVariable String postId) {
    ApiResponseDTO<PostResponse> response = savePostService.savePost(postId);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get Saved Posts",
      description = "Retrieve all saved posts for current user.")
  @GetMapping
  public ResponseEntity<ApiResponsePaginationDTO<PostResponse>> getSavedPosts(
      @RequestParam(defaultValue = DEFAULT_PAGE_NUMBER) int page,
      @RequestParam(defaultValue = DEFAULT_PAGE_SIZE) int size) {
    ApiResponsePaginationDTO<PostResponse> response = savePostService.getSavedPosts(page, size);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete Saved Post", description = "Remove saved post by postId.")
  @DeleteMapping("/{postId}")
  public ResponseEntity<ApiResponseDTO<String>> deleteSavedPost(@PathVariable String postId) {
    ApiResponseDTO<String> response = savePostService.deleteSavedPost(postId);
    return ResponseEntity.ok(response);
  }
}
