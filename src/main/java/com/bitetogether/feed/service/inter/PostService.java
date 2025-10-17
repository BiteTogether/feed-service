package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.feed.dto.request.PostRequest;
import com.bitetogether.feed.dto.response.PostResponse;

public interface PostService {
  ApiResponse<PostResponse> createPost(PostRequest postRequest);

  ApiResponse<PostResponse> getPostById(String id);

  ApiResponsePagination<PostResponse> getPostsByUserId(Long userId, int page, int size);

  ApiResponse<PostResponse> updatePost(String id, PostRequest postRequest);

  ApiResponse<String> deletePost(String id);

  ApiResponsePagination<PostResponse> getNewFeed(int page, int size);
}
