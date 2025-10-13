package com.bitetogether.feed.controller;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.feed.dto.request.FeedRequest;
import com.bitetogether.feed.dto.response.FeedResponse;
import com.bitetogether.feed.service.inter.FeedService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
  public ResponseEntity<ApiResponse<FeedResponse>> getFeedById(@PathVariable String id) {
    ApiResponse<FeedResponse> response = feedService.getFeedById(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<ApiResponse<List<FeedResponse>>> getFeedsByUserId(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    ApiResponse<List<FeedResponse>> response = feedService.getFeedsByUserId(userId, page, size);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/new-feeds/{userId}")
  public ResponseEntity<ApiResponse<List<FeedResponse>>> getNewFeeds(
      @PathVariable Long userId,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "10") int size) {
    ApiResponse<List<FeedResponse>> response = feedService.getNewFeed(userId, page, size);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<FeedResponse>> updateFeed(
      @PathVariable String id, @RequestBody FeedRequest feedRequest) {
    ApiResponse<FeedResponse> response = feedService.updateFeed(id, feedRequest);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<String>> deleteFeed(@PathVariable String id) {
    ApiResponse<String> response = feedService.deleteFeed(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/test")
  public ResponseEntity<String> test() {
    return ResponseEntity.ok("Feed service is working");
  }
}
