package com.brchain.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.ToString;


@Data
public class ResultDto<T> {

	@Schema(example = "0000",description="결과 코드")
	private String  resultCode;    // 결과 코드
	@Schema(example = "success message",description="결과 메시지")
	private String  resultMessage; // 결과 메시지
	@Schema(description="결과 데이터")
	private T       resultData;    // 결과 데이터
	@Schema(description="결과 플래그")
	private boolean resultFlag;    // 결과 플래그
}
