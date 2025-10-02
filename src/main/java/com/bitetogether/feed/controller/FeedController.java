package com.bitetogether.feed.controller;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.feed.dto.request.FeedRequest;
import com.bitetogether.feed.dto.response.FeedResponse;
import com.bitetogether.feed.service.inter.FeedService;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

@RestController
@RequiredArgsConstructor
@RequestMapping("/feeds")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedController {
  FeedService feedService;

  @PostMapping
  public ResponseEntity<ApiResponse<FeedResponse>> createFeed(
      @RequestBody FeedRequest feedRequest) {
    ApiResponse<FeedResponse> response = feedService.createFeed(feedRequest);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<FeedResponse>> getFeedById(@PathVariable Long id) {
    ApiResponse<FeedResponse> response = feedService.getFeedById(id);
    return ResponseEntity.ok(response);
  }

  // @PreAuthorize(Constants.HAS_ROLE_ADMIN)
  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponsePagination<List<FeedResponse>>> getFeedByUserId(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    Pageable pageable = PageRequest.of(page, size);
    ApiResponsePagination<List<FeedResponse>> response =
        feedService.getFeedByUserId(userId, pageable);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<FeedResponse>> updateFeed(
      @PathVariable Long id, @RequestBody FeedRequest feedRequest) {
    ApiResponse<FeedResponse> response = feedService.updateFeed(id, feedRequest);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<String>> deleteFeed(@PathVariable Long id) {
    ApiResponse<String> response = feedService.deleteFeed(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/test")
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("Feed service is working");
  }
}
