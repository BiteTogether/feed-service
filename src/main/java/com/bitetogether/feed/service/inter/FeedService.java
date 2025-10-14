package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.feed.dto.request.FeedRequest;
import com.bitetogether.feed.dto.response.FeedResponse;

public interface FeedService {
  ApiResponse<FeedResponse> createFeed(FeedRequest feed);

  ApiResponse<FeedResponse> getFeedById(String id);

  ApiResponsePagination<FeedResponse> getFeedsByUserId(Long userId, int page, int size);

  ApiResponse<FeedResponse> updateFeed(String id, FeedRequest feedDTO);

  ApiResponse<String> deleteFeed(String id);

  ApiResponsePagination<FeedResponse> getNewFeed(Long userId, int page, int size);
}
