package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.feed.dto.response.PostResponse;

public interface SavePostService {
  ApiResponseDTO<PostResponse> savePost(String postId);

  ApiResponsePaginationDTO<PostResponse> getSavedPosts(int page, int size);

  ApiResponseDTO<String> deleteSavedPost(String postId);
}
