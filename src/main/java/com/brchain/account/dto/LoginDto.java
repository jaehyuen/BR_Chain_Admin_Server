package com.brchain.account.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {

	private String userId; // 사용자 ID
	private String userPassword; // 사용자 비밀번호
}
