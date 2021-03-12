package com.brchain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TokenDto {
	private String accessToken; // JWT 토큰
	private String refreshToken; // JWT 토큰 재발급시 필요한 토큰
	private Instant expiresAt; // JWT 토큰 만료 시간
	private String userId; // 사용자 ID
}
