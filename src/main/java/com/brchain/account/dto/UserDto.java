package com.brchain.account.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class UserDto {

	@Schema(example="1",description="사용자 시퀀스")
	private Long    id;           // ID 값
	
	@Schema(example="testid",description="사용자 ID")
	private String  userId;       // 사용자 ID
	
	@Schema(example="1111",description="사용자 비밀번호")
	private String  userPassword; // 사용자 비밀번호
	
	@Schema(example="test@test.com",description="사용자 이메일")
	private String  userEmail;    // 사용자 이메일
	
	@Schema(example="lee",description="사용자 이름")
	private String  userName;     // 사용자 이름
	
	@Schema(example="true",description="계정 활성화 상태")
	private boolean active;       // 계정 활성화 상태

}
