package com.bitetogether.feed.service.impl;

import com.bitetogether.common.dto.ApiResponseDTO;
import com.bitetogether.common.dto.ApiResponsePaginationDTO;
import com.bitetogether.common.enums.ApiResponseStatus;
import com.bitetogether.common.util.ApiResponseUtil;
import com.bitetogether.common.util.UserContextUtils;
import com.bitetogether.feed.dto.FeedNotificationEvent;
import com.bitetogether.feed.dto.UserDTO;
import com.bitetogether.feed.dto.request.CommentRequest;
import com.bitetogether.feed.dto.response.CommentResponse;
import com.bitetogether.feed.enums.FeedNotificationType;
import com.bitetogether.feed.mapper.CommentMapper;
import com.bitetogether.feed.model.Comment;
import com.bitetogether.feed.model.Like;
import com.bitetogether.feed.model.Post;
import com.bitetogether.feed.repository.CommentRepository;
import com.bitetogether.feed.repository.LikeRepository;
import com.bitetogether.feed.repository.PostRepository;
import com.bitetogether.feed.repository.httpclient.UserClient;
import com.bitetogether.feed.service.NotificationProducer;
import com.bitetogether.feed.service.inter.CommentService;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
  NotificationProducer notificationProducer;

  private static final String COMMENT_NOT_FOUND = "Comment not found";

  @Override
  @Transactional
  public ApiResponseDTO<CommentResponse> createComment(CommentRequest request) {
    Long currentUserId = UserContextUtils.getCurrentUserId();
    log.info("Creating comment for postId={} by userId={}", request.getPostId(), currentUserId);

    Post post = postRepository.findById(request.getPostId()).orElse(null);
    if (post == null)
      return ApiResponseUtil.buildApiResponse(ApiResponseStatus.NOT_FOUND, "Post not found", null);

    Comment comment = commentMapper.toComment(request);
    comment.setUserId(currentUserId);

    if (request.getParentCommentId() != null) {
      Comment parentComment = commentRepository.findById(request.getParentCommentId()).orElse(null);
      if (parentComment == null) {
        return ApiResponseUtil.buildApiResponse(
            ApiResponseStatus.NOT_FOUND, "Parent comment not found", null);
      }
      log.info("Creating reply comment to parentCommentId={}", request.getParentCommentId());

      parentComment.setRepliesCount(parentComment.getRepliesCount() + 1);
      commentRepository.save(parentComment);
    }

    Comment saved = commentRepository.save(comment);

    post.setCommentCount(post.getCommentCount() + 1);
    postRepository.save(post);

    // Send notification: reply → parent comment owner, top-level → post owner
    if (request.getParentCommentId() != null) {
      Comment parent = commentRepository.findById(request.getParentCommentId()).orElse(null);
      if (parent != null) {
        sendReplyNotification(currentUserId, parent, saved.getContent());
      }
    } else {
      sendCommentNotification(currentUserId, post, saved.getContent());
    }

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS,
        "Comment created successfully",
        mapCommentToResponseWithUser(saved));
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponseDTO<CommentResponse> getCommentById(String id) {
    Comment comment = commentRepository.findById(id).orElse(null);
    if (comment == null)
      return ApiResponseUtil.buildApiResponse(ApiResponseStatus.NOT_FOUND, COMMENT_NOT_FOUND, null);

    List<CommentResponse> responses = mapCommentsWithAlreadyLiked(List.of(comment));
    CommentResponse response = responses.isEmpty() ? null : responses.getFirst();

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS, "Comment retrieved successfully", response);
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponsePaginationDTO<CommentResponse> getCommentsByPostId(
      String postId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    // Fetch only top-level comments (parentCommentId is null)
    Page<Comment> commentPage =
        commentRepository.findByPostIdAndParentCommentIdIsNull(postId, pageable);

    List<CommentResponse> responses = mapCommentsWithAlreadyLiked(commentPage.getContent());

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
  public ApiResponsePaginationDTO<CommentResponse> getCommentsByUserId(
      Long userId, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Comment> commentPage = commentRepository.findByUserId(userId, pageable);

    List<CommentResponse> responses = mapCommentsWithAlreadyLiked(commentPage.getContent());

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
  public ApiResponseDTO<CommentResponse> updateComment(String id, CommentRequest request) {
    Comment existing = commentRepository.findById(id).orElse(null);
    if (existing == null)
      return ApiResponseUtil.buildApiResponse(ApiResponseStatus.NOT_FOUND, COMMENT_NOT_FOUND, null);

    Long currentUserId = UserContextUtils.getCurrentUserId();
    if (!existing.getUserId().equals(currentUserId)) {
      log.warn(
          "User {} attempted to update comment {} owned by user {}",
          currentUserId,
          id,
          existing.getUserId());
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.FORBIDDEN, "You are not authorized to update this comment", null);
    }

    commentMapper.updateCommentFromCommentRequest(request, existing);
    Comment updated = commentRepository.save(existing);

    // Map comment with likes and replies
    List<CommentResponse> responses = mapCommentsWithAlreadyLiked(List.of(updated));
    CommentResponse response = responses.isEmpty() ? null : responses.getFirst();

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS, "Comment updated successfully", response);
  }

  @Override
  @Transactional
  public ApiResponseDTO<String> deleteComment(String id) {
    Comment existing = commentRepository.findById(id).orElse(null);
    if (existing == null)
      return ApiResponseUtil.buildApiResponse(ApiResponseStatus.NOT_FOUND, COMMENT_NOT_FOUND, null);

    Long currentUserId = UserContextUtils.getCurrentUserId();
    if (!existing.getUserId().equals(currentUserId)) {
      log.warn(
          "User {} attempted to delete comment {} owned by user {}",
          currentUserId,
          id,
          existing.getUserId());
      return ApiResponseUtil.buildApiResponse(
          ApiResponseStatus.FORBIDDEN, "You are not authorized to delete this comment", null);
    }

    String postId = existing.getPostId();
    Post post = postRepository.findById(postId).orElse(null);
    if (post != null) {
      post.setCommentCount(Math.max(0, post.getCommentCount() - 1));
      postRepository.save(post);
    }

    if (existing.getParentCommentId() != null) {
      Comment parentComment =
          commentRepository.findById(existing.getParentCommentId()).orElse(null);
      if (parentComment != null) {
        parentComment.setRepliesCount(Math.max(0, parentComment.getRepliesCount() - 1));
        commentRepository.save(parentComment);
      }
    }

    commentRepository.delete(existing);
    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS, "Comment deleted successfully", null);
  }

  private CommentResponse mapCommentToResponseWithUser(Comment comment) {
    UserDTO userDTO = null;
    try {
      ResponseEntity<ApiResponseDTO<UserDTO>> userResponse =
          userClient.getUserById(comment.getUserId());
      userDTO = userResponse.getBody() != null ? userResponse.getBody().getData() : null;
    } catch (Exception ex) {
      log.error("Failed to fetch userDTO for userId {}: {}", comment.getUserId(), ex.getMessage());
    }
    CommentResponse response = commentMapper.toCommentResponse(comment);
    response.setUser(userDTO);
    return response;
  }

  private List<String> fetchLikedCommentIds(List<String> commentIds) {
    try {
      Long currentUserId = UserContextUtils.getCurrentUserId();
      return likeRepository.findByUserIdAndCommentIdIn(currentUserId, commentIds).stream()
          .map(Like::getCommentId)
          .toList();
    } catch (Exception ex) {
      log.debug("Could not fetch batch likes: {}", ex.getMessage());
      return List.of();
    }
  }

  private void applyLikedStatus(List<CommentResponse> responses, List<String> likedCommentIds) {
    responses.forEach(
        response -> response.setAlreadyLiked(likedCommentIds.contains(response.getId())));
  }

  private List<CommentResponse> mapCommentsWithAlreadyLiked(List<Comment> comments) {
    if (comments.isEmpty()) {
      return List.of();
    }

    List<CommentResponse> responses =
        comments.stream().map(this::mapCommentToResponseWithUser).toList();

    List<String> commentIds = comments.stream().map(Comment::getId).toList();
    List<String> likedCommentIds = fetchLikedCommentIds(commentIds);
    applyLikedStatus(responses, likedCommentIds);

    return responses;
  }

  @Override
  @Transactional(readOnly = true)
  public ApiResponseDTO<List<CommentResponse>> getRepliesByCommentId(String commentId) {
    Comment parentComment = commentRepository.findById(commentId).orElse(null);
    if (parentComment == null) {
      return ApiResponseUtil.buildApiResponse(ApiResponseStatus.NOT_FOUND, COMMENT_NOT_FOUND, null);
    }

    List<Comment> replies = commentRepository.findByParentCommentId(commentId);
    List<CommentResponse> replyResponses = mapCommentsWithAlreadyLiked(replies);

    return ApiResponseUtil.buildApiResponse(
        ApiResponseStatus.SUCCESS, "Replies retrieved successfully", replyResponses);
  }

  /**
   * Sends a REPLY notification to the parent comment owner. Skips if replier is the comment owner.
   */
  private void sendReplyNotification(Long replierId, Comment parentComment, String replyContent) {
    try {
      if (parentComment.getUserId().equals(replierId)) return;

      UserDTO actor = fetchUser(replierId);
      String actorName =
          actor != null && actor.getFullName() != null ? actor.getFullName() : "Someone";
      String actorAvatar = actor != null ? actor.getAvatar() : null;

      String preview =
          replyContent != null && replyContent.length() > 100
              ? replyContent.substring(0, 100) + "..."
              : replyContent;

      notificationProducer.sendNotification(
          FeedNotificationEvent.builder()
              .actorId(replierId)
              .receiverId(parentComment.getUserId())
              .type(FeedNotificationType.REPLY.name())
              .targetId(parentComment.getId())
              .title("New Reply")
              .message(actorName + " replied to your comment: " + preview)
              .actorName(actorName)
              .actorAvatar(actorAvatar)
              .build());
    } catch (Exception e) {
      log.error(
          "Failed to send reply notification for comment {}: {}",
          parentComment.getId(),
          e.getMessage());
    }
  }

  /** Sends a COMMENT notification to the post owner. Skips if commenter is the post owner. */
  private void sendCommentNotification(Long commenterId, Post post, String commentContent) {
    try {
      // Don't notify yourself
      if (post.getUserId().equals(commenterId)) return;

      UserDTO actor = fetchUser(commenterId);
      String actorName =
          actor != null && actor.getFullName() != null ? actor.getFullName() : "Someone";
      String actorAvatar = actor != null ? actor.getAvatar() : null;

      String preview =
          commentContent != null && commentContent.length() > 100
              ? commentContent.substring(0, 100) + "..."
              : commentContent;

      notificationProducer.sendNotification(
          FeedNotificationEvent.builder()
              .actorId(commenterId)
              .receiverId(post.getUserId())
              .type(FeedNotificationType.COMMENT.name())
              .targetId(post.getId())
              .title("New Comment")
              .message(actorName + " commented on your post: " + preview)
              .actorName(actorName)
              .actorAvatar(actorAvatar)
              .build());
    } catch (Exception e) {
      log.error(
          "Failed to send comment notification for post {}: {}", post.getId(), e.getMessage());
    }
  }

  private UserDTO fetchUser(Long userId) {
    try {
      ResponseEntity<ApiResponseDTO<UserDTO>> response = userClient.getUserById(userId);
      return response.getBody() != null ? response.getBody().getData() : null;
    } catch (Exception e) {
      log.error("Failed to fetch user {}: {}", userId, e.getMessage());
      return null;
    }
  }
}
