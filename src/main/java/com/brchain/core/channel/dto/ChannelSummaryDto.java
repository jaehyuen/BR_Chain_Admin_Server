package com.brchain.core.channel.dto;

import lombok.Data;

@Data
public class ChannelSummaryDto {

	private String  channelName;  // 채널 이름
	private int     channelBlock; // 채널 블럭수
	private int     channelTx;    // 채널 트렌젝션수
	private Long    preBlockCnt;  // 채널 블럭수
	private Long    nowBlockCnt;  // 채널 트렌젝션수
	private Long    preTxCnt;     // 채널 블럭수
	private Long    nowTxCnt;     // 채널 트렌젝션수
	private Long    percent;
	private boolean flag;

	public ChannelSummaryDto(String channelName, int channelBlock, int channelTx, Long preBlockCnt, Long nowBlockCnt, Long preTxCnt, Long nowTxCnt) {

		this.channelName  = channelName;
		this.channelBlock = channelBlock;
		this.channelTx    = channelTx;
		this.preBlockCnt  = preBlockCnt;
		this.nowBlockCnt  = nowBlockCnt;
		this.preTxCnt     = preTxCnt;
		this.nowTxCnt     = nowTxCnt;

	}

}
