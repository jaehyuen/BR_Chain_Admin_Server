package com.brchain.core.container.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "조직 정보 dto")
public class OrgInfoDto {

	@Schema(example = "test", description = "조직명")
	private String orgName;   // 조직명

	@Schema(example = "peer", description = "조직 타입 (ca, setup, peer, couchdb, orderer)")
	private String orgType;   // 조직 타입 (ca, setup, peer, couchdb, orderer)

	@Schema(example = "2", description = "맴버 개수")
	private Long    memberCnt; // 맴버 개수

}
