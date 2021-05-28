package com.brchain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LoginDto {

	@Schema(example="testid",description="사용자 ID")
	private String userId;       // 사용자 ID
	@Schema(example="1111",description="사용자 비밀번호")
	private String userPassword; // 사용자 비밀번호
}
