package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.feed.dto.request.LikeRequest;
import com.bitetogether.feed.dto.response.LikeResponse;

public interface LikeService {
  ApiResponse<LikeResponse> like(LikeRequest request);

  ApiResponse<String> unlike(LikeRequest request);

  ApiResponsePagination<LikeResponse> getLikesByUser(Long userId, int page, int size);

  ApiResponsePagination<LikeResponse> getLikesByPost(String postId, int page, int size);

  ApiResponsePagination<LikeResponse> getLikesByComment(String commentId, int page, int size);
}
