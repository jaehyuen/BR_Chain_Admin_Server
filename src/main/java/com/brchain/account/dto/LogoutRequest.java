package com.brchain.account.dto;

import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LogoutRequest {

	@NotBlank
	private String refreshToken; // 리프레시 토큰
}
