package com.brchain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RefreshTokenDto {

	private String refreshToken; // JWT 토큰 재발급시 필요한 토큰
	private String userId;       // 사용자 ID
}
