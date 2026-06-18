package com.peerforge.auth.mapper;

import com.peerforge.auth.dto.request.RegisterRequest;
import com.peerforge.user.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AuthMapper {

    User toUser(RegisterRequest request);
}