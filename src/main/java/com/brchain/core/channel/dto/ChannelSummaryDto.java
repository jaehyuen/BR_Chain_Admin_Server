package com.brchain.core.channel.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
//@NoArgsConstructor
//@AllArgsConstructor

public class ChannelSummaryDto {

	private String  channelName;  // 채널 이름
	private int     channelBlock; // 채널 블럭수
	private int     channelTx;    // 채널 트렌젝션수
	private Long     preTxCnt;     // 채널 블럭수
	private Long     nowTxCnt;     // 채널 트렌젝션수
	private Long     percent;
	private boolean index;
	
	public ChannelSummaryDto(String channelName, int channelBlock, int channelTx, Long preTxCnt, Long nowTxCnt) {

		this.channelName  = channelName;
		this.channelBlock = channelBlock;
		this.channelTx    = channelTx;
		this.preTxCnt     = preTxCnt;
		this.nowTxCnt     = nowTxCnt;
		
	}

}
