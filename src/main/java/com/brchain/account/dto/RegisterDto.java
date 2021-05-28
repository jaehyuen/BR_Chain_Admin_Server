package com.brchain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class RegisterDto {

	@Schema(example="testid",description="사용자 ID")
	private String userId;       // 사용자 ID
	
	@Schema(example="1111",description="사용자 비밀번호")
	private String userPassword; // 사용자 비밀번호
	
	@Schema(example="test@test.com",description="사용자 이메일")
	private String userEmail;    // 사용자 이메일
	
	@Schema(example="lee",description="사용자 이름")
	private String userName;     // 사용자 이름
	
	@Schema(example="true",description="관리자 권한 여부")
	private boolean adminYn;
}
