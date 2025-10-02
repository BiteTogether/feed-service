package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.feed.dto.request.FeedRequest;
import com.bitetogether.feed.dto.response.FeedResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface FeedService {
  ApiResponse<FeedResponse> createFeed(FeedRequest feed);

  ApiResponse<FeedResponse> getFeedById(Long id);

  ApiResponsePagination<List<FeedResponse>> getFeedByUserId(Long userId, Pageable pageable);

  ApiResponse<FeedResponse> updateFeed(Long id, FeedRequest feedDTO);

  ApiResponse<String> deleteFeed(Long id);
}
