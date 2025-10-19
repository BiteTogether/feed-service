package com.bitetogether.feed.service.impl;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.ApiResponseUtil;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.CommentRequest;
import com.bitetogether.feed.dto.response.CommentResponse;
import com.bitetogether.feed.mapper.CommentMapper;
import com.bitetogether.feed.model.Comment;
import com.bitetogether.feed.model.Post;
import com.bitetogether.feed.repository.CommentRepository;
import com.bitetogether.feed.repository.PostRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.inter.CommentService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CommentServiceImpl implements CommentService {

  CommentRepository commentRepository;
  PostRepository postRepository;
  CommentMapper commentMapper;
  UserClient userClient;

  @Override
  @Transactional
  public ApiResponse<CommentResponse> createComment(CommentRequest request) {
    log.info(
        "Creating comment for postId={} by userId={}", request.getPostId(), request.getUserId());

    Post post = postRepository.findById(request.getPostId()).orElse(null);
    if (post == null)
      return ApiResponseUtil.buildApiResponse(ApiResponseStatus.NOT_FOUND, "Post not found", null);

    Comment comment = commentMapper.toComment(request);
    Comment saved = commentRepository.save(comment);

    post.setCommentCount(post.getCommentCount() + 1);
    postRepository.save(post);

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        "Comment created successfully",
        mapCommentToResponseWithUser(saved));
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<CommentResponse> getCommentById(String id) {
    Comment comment = commentRepository.findById(id).orElse(null);
    if (comment == null)
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.NOT_FOUND, "Comment not found", null);

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        "Comment retrieved successfully",
        mapCommentToResponseWithUser(comment));
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponsePagination<CommentResponse> getCommentsByPostId(
      String postId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Comment> commentPage = commentRepository.findByPostId(postId, pageable);
    Page<CommentResponse> responsePage = commentPage.map(this::mapCommentToResponseWithUser);

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responsePage.getContent(),
        responsePage.getNumber(),
        responsePage.getTotalPages(),
        responsePage.getTotalElements());
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponsePagination<CommentResponse> getCommentsByUserId(
      Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Comment> commentPage = commentRepository.findByUserId(userId, pageable);
    Page<CommentResponse> responsePage = commentPage.map(this::mapCommentToResponseWithUser);

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responsePage.getContent(),
        responsePage.getNumber(),
        responsePage.getTotalPages(),
        responsePage.getTotalElements());
  }

  @Override
  @Transactional
  public ApiResponse<CommentResponse> updateComment(String id, CommentRequest request) {
    Comment existing = commentRepository.findById(id).orElse(null);
    if (existing == null)
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.NOT_FOUND, "Comment not found", null);

    commentMapper.updateCommentFromCommentRequest(request, existing);
    Comment updated = commentRepository.save(existing);

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        "Comment updated successfully",
        mapCommentToResponseWithUser(updated));
  }

  @Override
  @Transactional
  public ApiResponse<String> deleteComment(String id) {
    Comment existing = commentRepository.findById(id).orElse(null);
    if (existing == null)
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.NOT_FOUND, "Comment not found", null);

    String postId = existing.getPostId();
    Post post = postRepository.findById(postId).orElse(null);
    if (post != null) {
      post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
      postRepository.save(post);
    }
    commentRepository.delete(existing);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS, "Comment deleted successfully", null);
  }

  private CommentResponse mapCommentToResponseWithUser(Comment comment) {
    UserDTO userDTO = null;
    try {
      ResponseEntity<ApiResponse<UserDTO>> userResponse =
          userClient.getUserById(comment.getUserId());
      userDTO = userResponse.getBody() != null ? userResponse.getBody().getData() : null;
    } catch (Exception ex) {
      log.error("Failed to fetch userDTO for userId {}: {}", comment.getUserId(), ex.getMessage());
    }
    CommentResponse response = commentMapper.toCommentResponse(comment);
    response.setUser(userDTO);
    return response;
  }
}
