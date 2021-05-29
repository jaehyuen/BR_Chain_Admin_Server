package com.brchain.core.chaincode.dto;

import java.time.LocalDateTime;

import com.brchain.core.container.dto.ConInfoDto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description="피어에 설치된 체인코드  정보 dto")
public class CcInfoPeerDto {

	@Schema(example = "1", description = "체인코드 정보 (피어) 시퀀스")
	private Long id; // id

	@Schema(example = "1", description = "설치된 체인코드 버전")
	private String ccVersion; // 체인코드 버전

	private ConInfoDto conInfoDto; // 컨테이너 정보
	private CcInfoDto ccInfoDto; // 체인코드 정보
	private LocalDateTime createdAt; // 생성 시간

}
