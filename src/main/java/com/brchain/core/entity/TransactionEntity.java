package com.brchain.core.entity;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.brchain.common.entity.BaseEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "TRANSACTION")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID", nullable = false)
	private Long id;

	@Column(name = "TX_ID", nullable = true)
	private String txId;

	@Column(name = "CREATOR_ID", nullable = true)
	private String creatorId;

	@Column(name = "TX_TYPE", nullable = true)
	private String txType;

	@Column(name = "TIMESTAMP", nullable = false)
	private Date timestamp;

	@Column(name = "CC_NAME", nullable = true)
	private String ccName;

	@Column(name = "CC_VERSION", nullable = true)
	private String ccVersion;

	@Column(name = "CC_ARGS", nullable = true, columnDefinition = "LONGTEXT")
	private String ccArgs;

	@ManyToOne(targetEntity = BlockEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "BLOCK_BLOCK_DATA_HASH", nullable = false)
	private BlockEntity blockEntity;

	@ManyToOne(targetEntity = ChannelInfoEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "CHANNELINFO_CHANNEL_NAME")
	private ChannelInfoEntity channelInfoEntity;

	@Builder
	public TransactionEntity(Long id, String txId, String creatorId, String txType, Date timestamp, String ccName,
			String ccVersion, String ccArgs, BlockEntity blockEntity, ChannelInfoEntity channelInfoEntity,
			LocalDateTime createdAt) {
		this.id = id;
		this.txId = txId;
		this.creatorId = creatorId;
		this.txType = txType;
		this.timestamp = timestamp;
		this.ccName = ccName;
		this.ccVersion = ccVersion;
		this.ccArgs = ccArgs;
		this.blockEntity = blockEntity;
		this.channelInfoEntity = channelInfoEntity;
		super.setCreatedAt(createdAt);

	}

}
