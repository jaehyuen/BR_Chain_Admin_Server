package com.brchain.core.channel.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ChannelSummaryDto {

	@Schema(example = "test-channel", description = "채널 이름")
	private String channelName; // 채널 이름

	@Schema(example = "10", description = "채널 총 블록 개수")
	private int channelBlock; // 채널 블럭수

	@Schema(example = "20", description = "채널 총 트렌젝션 개수")
	private int channelTx; // 채널 트랜잭션수

	@Schema(example = "2", description = "지난달 채널 블록 개수")
	private Double preBlockCnt; // 지난달 채널 블럭수

	@Schema(example = "7", description = "이번달 채널 블록 개수")
	private Double nowBlockCnt; // 이번달 채널 블럭수

	@Schema(example = "5", description = "지난달 채널 트랜잭션수 개수")
	private Double preTxCnt; // 지난달 채널 트랜잭션수

	@Schema(example = "15", description = "이번달 채널 트랜잭션수 개수")
	private Double nowTxCnt; // 이번달 채널 트랜잭션수

	@Schema(example = "300", description = "전월 대비 트랜잭션 증감율")
	private Long percent; // 트랜잭션 증감율

	@Schema(example = "true", description = "음수 여부")
	private boolean flag; // 증가감소 플래그

	public ChannelSummaryDto(String channelName, int channelBlock, int channelTx, Long preBlockCnt, Long nowBlockCnt,
			Long preTxCnt, Long nowTxCnt) {

		this.channelName = channelName;
		this.channelBlock = channelBlock;
		this.channelTx = channelTx;
		this.preBlockCnt = (double) preBlockCnt;
		this.nowBlockCnt = (double) nowBlockCnt;
		this.preTxCnt = (double) preTxCnt;
		this.nowTxCnt = (double) nowTxCnt;

	}

	public ChannelSummaryDto(String channelName, int channelBlock, int channelTx, Long preBlockCnt) {

		this.channelName = channelName;
		this.channelBlock = channelBlock;
		this.channelTx = channelTx;
		this.preBlockCnt = (double) preBlockCnt;
		this.nowBlockCnt = (double) 0;
		this.preTxCnt = (double) 0;
		this.nowTxCnt = (double) 0;

	}

}
