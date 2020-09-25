package com.brchain.core.dto;

import lombok.Data;

@Data
public class ResultDto {

	private String resultCode; // 결과 코드
	private String resultMessage; // 결과 메시지
	private Object resultData; // 결과 데이터
	private boolean resultFlag; // 결과 플래그
}
