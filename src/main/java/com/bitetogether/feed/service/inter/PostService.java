package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.feed.dto.request.PostRequest;
import com.bitetogether.feed.dto.response.PostResponse;

public interface PostService {
  ApiResponseDTO<PostResponse> createPost(PostRequest postRequest);

  ApiResponseDTO<PostResponse> getPostById(String id);

  ApiResponsePaginationDTO<PostResponse> getPostsByUserId(Long userId, int page, int size);

  ApiResponseDTO<PostResponse> updatePost(String id, PostRequest postRequest);

  ApiResponseDTO<String> deletePost(String id);

  ApiResponsePaginationDTO<PostResponse> getNewFeed(
      int page,
      int size,
      double latitude,
      double longitude,
      double latitudeDelta,
      double longitudeDelta);
}
