package com.brchain.core.dto;

import java.util.Map;

import lombok.Data;

@Data
public class ChannelInfoDto {
	
	private String channelName;							                  // 조직명
	private String[] orgToJoin;						                      // 가입할 피어조직 배열
	private String ordererToJoin;						                  // 사용오더러 조직
	private Map<String,String> [] anchorPeerSetting; 					  // 오더러 포트(오더러)
	
//	private String gossipBootAddress; 						          // 가쉽 부트스트랩 주소(피어	
//	private int conNum; 						                      // 피어,오더러 번호
//	private int conCnt;							                      // 피어,오더러 개수
//	private boolean couchdbYn;                                        // 카우치디비 사용여부(피어)
}
