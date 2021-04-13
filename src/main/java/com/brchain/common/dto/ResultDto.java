package com.brchain.common.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ResultDto<T> {

	private String  resultCode;    // 결과 코드
	private String  resultMessage; // 결과 메시지
	private T       resultData;    // 결과 데이터
	private boolean resultFlag;    // 결과 플래그
}
