package com.bitetogether.feed.mapper;

import com.bitetogether.feed.dto.request.FeedRequest;
import com.bitetogether.feed.dto.response.FeedResponse;
import com.bitetogether.feed.model.Feed;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface FeedMapper {
  Feed toFeed(FeedRequest feed);

  FeedResponse toFeedResponse(Feed feed);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateFeedFromFeedRequest(FeedRequest feedRequest, @MappingTarget Feed feed);
}
