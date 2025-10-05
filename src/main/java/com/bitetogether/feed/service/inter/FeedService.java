package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.feed.dto.request.FeedRequest;
import com.bitetogether.feed.dto.response.FeedResponse;

import java.util.List;

public interface FeedService {
  ApiResponse<FeedResponse> createFeed(FeedRequest feed);

  ApiResponse<FeedResponse> getFeedById(String id);

  ApiResponse<List<FeedResponse>> getFeedsByUserId(Long userId, int page, int size);

  ApiResponse<FeedResponse> updateFeed(String id, FeedRequest feedDTO);

  ApiResponse<String> deleteFeed(String id);
}
