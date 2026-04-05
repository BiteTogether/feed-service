package com.bitetogether.feed.service.inter;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.feed.dto.request.CommentRequest;
import com.bitetogether.feed.dto.response.CommentResponse;
import java.util.List;

public interface CommentService {
  ApiResponseDTO<CommentResponse> createComment(CommentRequest request);

  ApiResponseDTO<CommentResponse> getCommentById(String commentId);

  ApiResponsePaginationDTO<CommentResponse> getCommentsByPostId(String postId, int page, int size);

  ApiResponsePaginationDTO<CommentResponse> getCommentsByUserId(Long userId, int page, int size);

  ApiResponseDTO<List<CommentResponse>> getRepliesByCommentId(String commentId);

  ApiResponseDTO<CommentResponse> updateComment(String commentId, CommentRequest request);

  ApiResponseDTO<String> deleteComment(String commentId);
}
