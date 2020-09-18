package com.brchain.core.dto;



import lombok.Data;

@Data
public class FabricMemberDto {
	
	private String orgName;							                  // 조직명
	private String orgType;							                  // 조직 타입 (ca, setup, peer, couchdb, orderer)
	private String orgMspId;							              // 조직 타입 (ca, setup, peer, couchdb, orderer)
	private String conName;						                      // 컨테이너명
	private String conPort;						                      // 사용 포트
	private String conUrl;						                      // 사용 Url
	private String caUrl;						                      // ca Url
	private int conNum; 						                      // 피어,오더러 번호

}
