package com.bitetogether.feed.mapper;

import com.bitetogether.feed.dto.request.PostRequest;
import com.bitetogether.feed.dto.response.PostResponse;
import com.bitetogether.feed.model.Post;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface PostMapper {
  Post toPost(PostRequest post);

  PostResponse toPostResponse(Post post);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updatePostFromPostRequest(PostRequest postRequest, @MappingTarget Post post);
}
