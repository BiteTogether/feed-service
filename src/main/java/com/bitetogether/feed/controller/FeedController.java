package com.bitetogether.feed.controller;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.feed.dto.FeedDTO;
import com.bitetogether.feed.model.Feed;
import com.bitetogether.feed.service.inter.FeedService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FeedController {
    FeedService feedService;

    @PostMapping()
    public ResponseEntity<ApiResponse<Feed>> createFeed(@RequestBody FeedDTO feedDTO) {
        ApiResponse<Feed> response = feedService.createFeed(feedDTO);
        return ResponseEntity.ok(response);
    }
}
