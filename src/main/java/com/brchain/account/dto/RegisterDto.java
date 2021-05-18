package com.brchain.account.dto;

import lombok.Data;

@Data
public class RegisterDto {

	private String userId;       // 사용자 ID
	private String userPassword; // 사용자 비밀번호
	private String userEmail;    // 사용자 이메일
	private String userName;     // 사용자 이름
	private boolean adminYn;
}
