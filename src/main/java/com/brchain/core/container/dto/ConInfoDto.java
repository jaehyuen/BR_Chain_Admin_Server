package com.brchain.core.container.dto;

import java.time.LocalDateTime;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "컨테이너 정보 dto")
public class ConInfoDto {

	@Schema(example = "dqbwuidyny18yqwdy8y1g2uk", description = "컨테이너 아이디")
	private String        conId;             // 컨테이너 아이디
	
	@Schema(example = "peer0.orgtest.com", description = "컨테이너 이름")
	private String        conName;           // 컨테이너 이름
	
	@Schema(example = "peer", description = "컨테이너 타입 (ca, setup, peer, couchdb, orderer)")
	private String        conType;           // 컨테이너 타입 (ca, setup, peer, couchdb, orderer)
	
	@Schema(example = "7051", description = "사용 포트")
	private String        conPort;           // 사용 포트
	
	@Schema(example = "test", description = "조직명")
	private String        orgName;           // 조직명
	
	@Schema(example = "peer", description = "조직 타입 (ca, setup, peer, couchdb, orderer)")
	private String        orgType;           // 조직 타입 (ca, setup, peer, couchdb, orderer)
	
	@Schema(example = "7050", description = "오더러 포트(오더러")
	private String        ordererPorts;      // 오더러 포트(오더러)
	
	@Schema(example = "peer0.orgtest.com:7051 peer1.orgtest.com:7052", description = "가쉽 부트스트랩 주소(피어만 해당)")
	private String        gossipBootAddr;    // 가쉽 부트스트랩 주소(피어)
	
	@Schema(example = "??", description = "앵커피어 정보(setup_channel만 해당)")
	private String        anchorPeerSetting; // 앵커피어 정보(setup_channel)
	
	@Schema(example = "test", description = "참가조직 정보")
	private String        peerOrgs;          // 참가조직 정보
	
	@Schema(example = "test apeer", description = "컨소시움 참가 조직 정보(오더러만 해당")
	private String        consoOrgs;         // 컨소시움 참가 조직 정보(orderer)
	
	@Schema(example = "1", description = "피어,오더러 번호")
	private int           conNum;            // 피어,오더러 번호
	
	@Schema(example = "1", description = "피어,오더러 개수")
	private int           conCnt;            // 피어,오더러 개수
	
	@Schema(example = "true", description = "카우치디비 사용여부(피어만 해당)")
	private boolean       couchdbYn;         // 카우치디비 사용여부(피어)
	private LocalDateTime createdAt;         // 생성 시간

}
