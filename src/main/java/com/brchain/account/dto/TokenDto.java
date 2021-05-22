package com.brchain.account.dto;

import java.time.Instant;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenDto {
	
	@Schema(example = "json web token value")
	private String  accessToken;  // JWT 토큰
	@Schema(example = "access token value")
	private String  refreshToken; // JWT 토큰 재발급시 필요한 토큰
	private Instant expiresAt;    // JWT 토큰 만료 시간
	@Schema(example = "user id")
	private String  userId;       // 사용자 ID
}
