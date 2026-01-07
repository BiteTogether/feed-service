package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.feed.dto.request.LikeRequest;
import com.bitetogether.feed.dto.response.LikeResponse;

public interface LikeService {
  ApiResponseDTO<LikeResponse> like(LikeRequest request);

  ApiResponseDTO<String> unlike(LikeRequest request);

  ApiResponsePaginationDTO<LikeResponse> getLikesByUser(Long userId, int page, int size);

  ApiResponsePaginationDTO<LikeResponse> getLikesByPost(String postId, int page, int size);

  ApiResponsePaginationDTO<LikeResponse> getLikesByComment(String commentId, int page, int size);
}
