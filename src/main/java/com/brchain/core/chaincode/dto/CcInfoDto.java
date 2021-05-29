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
@Schema(description="체인코드  정보 dto")
public class CcInfoDto {

	@Schema(example = "1", description = "체인코드 정보 시퀀스")
	private Long id; // 아이디

	@Schema(example = "test-cc", description = "체인코드 이름")
	private String ccName; // 체인코드 이름

	@Schema(example = "src/~/~/~.go", description = "체인코드 저장 경로")
	private String ccPath; // 체인코드 경로

	@Schema(example = "golang", description = "체인코드 개발 언어")
	private String ccLang; // 체인코드 언어

	@Schema(example = "this is test chaincode", description = "체인코드 설명")
	private String ccDesc; // 체인코드 설명

	@Schema(example = "1", description = "체인코드 버전")
	private String ccVersion; // 체인코드 버전

	private LocalDateTime createdAt; // 생성 시간

}
