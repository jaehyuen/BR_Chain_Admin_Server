package com.brchain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RefreshTokenDto {
	@Schema(example="access token value",description="JWT 토큰 재발급시 필요한 리프레시 토큰")
	private String refreshToken; // JWT 토큰 재발급시 필요한 토큰
	@Schema(example="testid",description="사용자 ID")
	private String userId; // 사용자 ID
}
