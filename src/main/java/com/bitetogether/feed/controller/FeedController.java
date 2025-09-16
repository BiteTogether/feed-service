package com.bitetogether.feed.controller;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.feed.dto.FeedDTO;
import com.bitetogether.feed.model.Feed;
import com.bitetogether.feed.service.inter.FeedService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedController {
    FeedService feedService;

    @PostMapping("/")
    public ResponseEntity<ApiResponse<Feed>> createFeed(@RequestBody FeedDTO feedDTO) {
        ApiResponse<Feed> response = feedService.createFeed(feedDTO);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<ApiResponse<Feed>> getFeedById(@PathVariable Long id) {
        ApiResponse<Feed> response = feedService.getFeedById(id);
        return  ResponseEntity.ok(response);
    }

    @GetMapping("/get-by-user-id/{userId}")
    public ResponseEntity<ApiResponse<List<Feed>>> getFeedByUserId(@PathVariable Long userId) {
        ApiResponse<List<Feed>> response = feedService.getFeedByUserId(userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Feed>> updateFeed(@PathVariable Long id, @RequestBody FeedDTO feedDTO) {
        ApiResponse<Feed> response = feedService.updateFeed(id, feedDTO);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<String>> deleteFeed(@PathVariable Long id) {
        ApiResponse<String> response = feedService.deleteFeed(id);
        return ResponseEntity.ok(response);
    }
}
