package com.brchain.core.dto.channel;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class CreateChannelDto {

	private String channelName; // 조직명
	private List<String> peerOrgs; // 가입할 피어조직 배열
	private String orderingOrg; // 사용오더러 조직
	private Map<String, String>[] anchorPeerSetting; // 앵커피어 설정(테스트중)

}
