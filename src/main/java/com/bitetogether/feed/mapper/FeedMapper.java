package com.bitetogether.feed.mapper;

import com.bitetogether.feed.dto.FeedDTO;
import com.bitetogether.feed.model.Feed;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface FeedMapper {
  Feed toFeed(FeedDTO feedDTO);

  FeedDTO toFeedDTO(Feed feed);

  @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
  void updateFeedFromDTO(FeedDTO feedDTO, @MappingTarget Feed feed);
}
