package com.brchain.core.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConInfoDto {

	private String conId; // 컨테이너 아이디
	private String conName; // 컨테이너 이름
	private String conType; // 컨테이너 타입 (ca, setup, peer, couchdb, orderer)
	private String conPort; // 사용 포트
	private String orgName; // 조직명
	private String orgType; // 조직 타입 (ca, setup, peer, couchdb, orderer)
	private String ordererPorts; // 오더러 포트(오더러)
	private String gossipBootAddr; // 가쉽 부트스트랩 주소(피어)
	private String anchorPeerSetting; // 앵커피어 정보(setup_channel)
	private String peerOrgs; // 참가조직 정보
	private String consoOrgs; // 컨소시움 참가 조직 정보(orderer)
	private int conNum; // 피어,오더러 번호
	private int conCnt; // 피어,오더러 개수
	private boolean couchdbYn; // 카우치디비 사용여부(피어)
	private LocalDateTime createdAt; // 생성 시간

}
