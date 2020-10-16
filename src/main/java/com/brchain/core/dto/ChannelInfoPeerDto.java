package com.brchain.core.dto;

import javax.persistence.Column;

import com.brchain.core.entity.CcInfoEntity;
import com.brchain.core.entity.CcInfoPeerEntity;
import com.brchain.core.entity.ChannelInfoEntity;
import com.brchain.core.entity.ChannelInfoPeerEntity;

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
public class ChannelInfoPeerDto {

	private String channelName; // 채널 이름
	private boolean anchorYn; // 앵커피어 여부
	private String conName; // 컨테이너 이름
	private int conNum;// 컨테이너 번호
	private String orgName;// 조직 이름

	public ChannelInfoPeerEntity toEntity() {

		ChannelInfoPeerEntity channelInfoPeerEntity = ChannelInfoPeerEntity.builder().channelName(channelName)
				.anchorYn(anchorYn).conName(conName).conNum(conNum).orgName(orgName).build();
		return channelInfoPeerEntity;
	}

}
