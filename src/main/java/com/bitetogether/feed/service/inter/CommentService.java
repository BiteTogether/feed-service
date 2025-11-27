package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.feed.dto.request.CommentRequest;
import com.bitetogether.feed.dto.response.CommentResponse;

public interface CommentService {
  ApiResponse<CommentResponse> createComment(CommentRequest request);

  ApiResponse<CommentResponse> getCommentById(String commentId);

  ApiResponsePagination<CommentResponse> getCommentsByPostId(String postId, int page, int size);

  ApiResponsePagination<CommentResponse> getCommentsByUserId(Long userId, int page, int size);

  ApiResponse<java.util.List<CommentResponse>> getRepliesByCommentId(String commentId);

  ApiResponse<CommentResponse> updateComment(String commentId, CommentRequest request);

  ApiResponse<String> deleteComment(String commentId);
}
