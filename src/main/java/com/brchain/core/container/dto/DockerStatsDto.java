package com.brchain.core.container.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "컨테이너 상태 dto")
public class DockerStatsDto {

	@Schema(example = "dqbwuidyny18yqwdy8y1g2uk", description = "컨테이너 아이디")
	private String  conId;      // 컨테이너 아이디
	
	@Schema(example = "peer0.orgtest.com", description = "컨테이너 이름")
	private String  conName;    // 컨테이너 이름
	
	@Schema(example = "container status", description = "컨테이너 상태")
	private String  conStatus;  // 컨테이너 상태
	
	@Schema(example = "container created time", description = "컨테이너 생성시간")
	private String  conCreated; // 컨테이너 생성 시간
	
	@Schema(example = "7051", description = "컨테이너 포트")
	private Integer conPort;    // 컨테이너 포

}
