package com.brchain.core.container.dto;

import lombok.Data;

@Data

public class CreateOrgConInfoDto {

	private String  conType;  // 컨테이너 타입 (ca, setup, peer, couchdb, orderer)
	private String  conPort;  // 사용 포트
	private String  orgName;  // 조직명
	private String  orgType;  // 조직 타입 (ca, setup, peer, couchdb, orderer)
	private boolean couchdbYn;
	private int     conNum;   // 피어,오더러 번호
	private int     conCnt;   // 피어,오더러 개수

}
