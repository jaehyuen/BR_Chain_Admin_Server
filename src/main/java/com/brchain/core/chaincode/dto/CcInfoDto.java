package com.brchain.core.chaincode.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CcInfoDto {

	@Schema(example="1")
	private Long          id;        // 아이디
	@Schema(example="test-cc")
	private String        ccName;    // 체인코드 이름
	@Schema(example="src/~/~/~.go")
	private String        ccPath;    // 체인코드 경로
	@Schema(example="golang")
	private String        ccLang;    // 체인코드 언어
	@Schema(example="this is test chaincode")
	private String        ccDesc;    // 체인코드 설명
	@Schema(example="1")
	private String        ccVersion; // 체인코드 버전
	private LocalDateTime createdAt; // 생성 시간

}
