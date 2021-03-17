package com.brchain.account.dto;

import lombok.Data;

@Data
public class LoginDto {

	private String userId;       // 사용자 ID
	private String userPassword; // 사용자 비밀번호
}
