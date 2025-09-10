package com.bitetogether.feed.mapper;

import com.bitetogether.feed.dto.FeedDTO;
import com.bitetogether.feed.model.Feed;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FeedMapper {
    Feed toFeed(FeedDTO feedDTO);
    FeedDTO toFeedDTO(Feed feed);
}
