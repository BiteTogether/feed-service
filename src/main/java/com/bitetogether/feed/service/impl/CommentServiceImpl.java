package com.bitetogether.feed.service.impl;

import com.bitetogether.common.dto.ApiResponse;
import com.bitetogether.common.dto.ApiResponsePagination;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.ApiResponseUtil;
import com.bitetogether.common.util.UserContextUtils;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.CommentRequest;
import com.bitetogether.feed.dto.response.CommentResponse;
import com.bitetogether.feed.mapper.CommentMapper;
import com.bitetogether.feed.model.Comment;
import com.bitetogether.feed.model.Like;
import com.bitetogether.feed.model.Post;
import com.bitetogether.feed.repository.CommentRepository;
import com.bitetogether.feed.repository.LikeRepository;
import com.bitetogether.feed.repository.PostRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.inter.CommentService;
import java.util.List;
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
  LikeRepository likeRepository;

  @Override
  @Transactional
  public ApiResponse<CommentResponse> createComment(CommentRequest request) {
    Long currentUserId = UserContextUtils.getCurrentUserId();
    log.info("Creating comment for postId={} by userId={}", request.getPostId(), currentUserId);

    Post post = postRepository.findById(request.getPostId()).orElse(null);
    if (post == null)
      return ApiResponseUtil.buildApiResponse(ApiResponseStatus.NOT_FOUND, "Post not found", null);

    Comment comment = commentMapper.toComment(request);
    comment.setUserId(currentUserId);

    // Validate parent comment exists if this is a reply
    if (request.getParentCommentId() != null) {
      Comment parentComment = commentRepository.findById(request.getParentCommentId()).orElse(null);
      if (parentComment == null) {
        return ApiResponseUtil.buildApiResponse(
            ApiResponseStatus.NOT_FOUND, "Parent comment not found", null);
      }
      log.info("Creating reply comment to parentCommentId={}", request.getParentCommentId());
    }

    Comment saved = commentRepository.save(comment);

    // Increment post comment count for both top-level comments and replies
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
    // Fetch only top-level comments (parentCommentId is null)
    Page<Comment> commentPage = commentRepository.findByPostIdAndParentCommentIdIsNull(postId, pageable);

    List<CommentResponse> responses =
        mapCommentsToResponsesWithBatchLikesAndReplies(commentPage.getContent());

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responses,
        commentPage.getNumber(),
        commentPage.getTotalPages(),
        commentPage.getTotalElements());
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponsePagination<CommentResponse> getCommentsByUserId(
      Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Comment> commentPage = commentRepository.findByUserId(userId, pageable);

    List<CommentResponse> responses =
        mapCommentsToResponsesWithBatchLikes(commentPage.getContent());

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        ApiResponseStatus.SUCCESS.getDefaultMessage(),
        responses,
        commentPage.getNumber(),
        commentPage.getTotalPages(),
        commentPage.getTotalElements());
  }

  @Override
  @Transactional
  public ApiResponse<CommentResponse> updateComment(String id, CommentRequest request) {
    Comment existing = commentRepository.findById(id).orElse(null);
    if (existing == null)
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.NOT_FOUND, "Comment not found", null);

    Long currentUserId = UserContextUtils.getCurrentUserId();
    if (!existing.getUserId().equals(currentUserId)) {
      log.warn("User {} attempted to update comment {} owned by user {}",
               currentUserId, id, existing.getUserId());
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.FORBIDDEN,
          "You are not authorized to update this comment",
          null);
    }

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

    Long currentUserId = UserContextUtils.getCurrentUserId();
    if (!existing.getUserId().equals(currentUserId)) {
      log.warn("User {} attempted to delete comment {} owned by user {}",
               currentUserId, id, existing.getUserId());
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.FORBIDDEN,
          "You are not authorized to delete this comment",
          null);
    }

    // Decrement post comment count for both top-level comments and replies
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

    // Check if current user has already liked this comment
    try {
      Long currentUserId = UserContextUtils.getCurrentUserId();
      boolean alreadyLiked =
          likeRepository.existsByUserIdAndCommentId(currentUserId, comment.getId());
      response.setAlreadyLiked(alreadyLiked);
    } catch (Exception ex) {
      log.debug("Could not determine alreadyLiked status: {}", ex.getMessage());
      response.setAlreadyLiked(false);
    }

    return response;
  }

  private List<CommentResponse> mapCommentsToResponsesWithBatchLikes(List<Comment> comments) {
    if (comments.isEmpty()) {
      return List.of();
    }

    List<String> commentIds = comments.stream().map(Comment::getId).toList();
    List<String> likedCommentIds = List.of();
    try {
      Long currentUserId = UserContextUtils.getCurrentUserId();
      likedCommentIds =
          likeRepository.findByUserIdAndCommentIdIn(currentUserId, commentIds).stream()
              .map(Like::getCommentId)
              .toList();
    } catch (Exception ex) {
      log.debug("Could not fetch batch likes: {}", ex.getMessage());
    }

    List<String> finalLikedCommentIds = likedCommentIds;
    return comments.stream()
        .map(
            comment -> {
              CommentResponse response = mapCommentToResponseWithUser(comment);
              // Override the alreadyLiked with batch result
              response.setAlreadyLiked(finalLikedCommentIds.contains(comment.getId()));
              return response;
            })
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponse<List<CommentResponse>> getRepliesByCommentId(String commentId) {
    Comment parentComment = commentRepository.findById(commentId).orElse(null);
    if (parentComment == null) {
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.NOT_FOUND, "Comment not found", null);
    }

    List<Comment> replies = commentRepository.findByParentCommentId(commentId);
    List<CommentResponse> replyResponses = replies.stream()
        .map(this::mapCommentToResponseWithUser)
        .toList();

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        "Replies retrieved successfully",
        replyResponses);
  }

  private List<CommentResponse> mapCommentsToResponsesWithBatchLikesAndReplies(List<Comment> comments) {
    if (comments.isEmpty()) {
      return List.of();
    }

    // Batch fetch likes for top-level comments
    List<String> commentIds = comments.stream().map(Comment::getId).toList();
    List<String> likedCommentIds = List.of();
    try {
      Long currentUserId = UserContextUtils.getCurrentUserId();
      likedCommentIds =
          likeRepository.findByUserIdAndCommentIdIn(currentUserId, commentIds).stream()
              .map(Like::getCommentId)
              .toList();
    } catch (Exception ex) {
      log.debug("Could not fetch batch likes: {}", ex.getMessage());
    }

    List<String> finalLikedCommentIds = likedCommentIds;
    return comments.stream()
        .map(
            comment -> {
              CommentResponse response = mapCommentToResponseWithUser(comment);
              response.setAlreadyLiked(finalLikedCommentIds.contains(comment.getId()));

              // Fetch first 3 replies as preview
              List<Comment> replies = commentRepository.findByParentCommentId(comment.getId());
              List<CommentResponse> replyResponses = replies.stream()
                  .limit(3) // Show only first 3 replies as preview
                  .map(this::mapCommentToResponseWithUser)
                  .toList();
              response.setReplies(replyResponses);

              return response;
            })
        .toList();
  }
}
