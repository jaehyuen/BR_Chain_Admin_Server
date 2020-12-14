package com.brchain.core.entity;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.brchain.common.entity.BaseEntity;
import com.brchain.core.entity.chaincode.CcInfoChannelEntity;
import com.brchain.core.entity.chaincode.CcInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "BLOCK")
@NoArgsConstructor
public class BlockEntity extends BaseEntity {

	@Id
	@Column(name = "BLOCK_DATA_HASH", nullable = false)
	private String blockDataHash;

	@Column(name = "BLOCK_NUM", nullable = false)
	private int blockNum;

	@Column(name = "TX_COUNT", nullable = false)
	private int txCount;

	@Column(name = "PREV_DATA_HASH", nullable = false)
	private String prevDataHash;

	@ManyToOne(targetEntity = ChannelInfoEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "CHANNELINFO_CHANNEL_NAME")
	private ChannelInfoEntity channelInfoEntity;

	@Builder
	public BlockEntity(String blockDataHash, int blockNum, int txCount, String prevDataHash,
			ChannelInfoEntity channelInfoEntity, LocalDateTime createdAt) {
		this.blockDataHash = blockDataHash;
		this.blockNum = blockNum;
		this.txCount = txCount;
		this.prevDataHash = prevDataHash;
		this.channelInfoEntity = channelInfoEntity;
		super.setCreatedAt(createdAt);

	}
}
