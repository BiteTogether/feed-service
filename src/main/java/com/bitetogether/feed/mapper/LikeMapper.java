package com.bitetogether.feed.mapper;

import com.bitetogether.feed.dto.request.LikeRequest;
import com.bitetogether.feed.dto.response.LikeResponse;
import com.bitetogether.feed.model.Like;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LikeMapper {

  Like toLike(LikeRequest request);

  LikeResponse toLikeResponse(Like like);
}
