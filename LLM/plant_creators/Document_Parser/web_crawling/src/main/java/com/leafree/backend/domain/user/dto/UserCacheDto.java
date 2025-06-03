package com.leafree.backend.domain.user.dto;

import java.io.Serializable;

public record UserCacheDto(Long id, String email, String nickname) implements Serializable {}
