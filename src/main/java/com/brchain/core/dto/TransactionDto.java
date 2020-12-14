package com.brchain.core.dto;

import java.time.LocalDateTime;
import java.util.Date;

import com.brchain.core.entity.BlockEntity;
import com.brchain.core.entity.TransactionEntity;
import com.brchain.core.entity.TransactionEntity.TransactionEntityBuilder;
import com.brchain.core.entity.channel.ChannelInfoEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {

	private String txID;
	private String creatorId;
	private String txType;
	private Date timestamp;
	private String ccName;
	private String ccVersion;
	private String ccArgs;
	private BlockEntity blockEntity;
	private ChannelInfoEntity channelInfoEntity;
	private LocalDateTime createdAt;

//	public TransactionEntity toEntity() {
//
//		TransactionEntityBuilder transactionEntityBuilder = TransactionEntity.builder().txID(txID).creatorId(creatorId).txType(txType)
//				.timestamp(timestamp).ccName(ccName).ccVersion(ccVersion).ccArgs(ccArgs).blockEntity(blockEntity)
//				.channelInfoEntity(channelInfoEntity);
//		if (createdAt == null) {
//			return transactionEntityBuilder.build();
//		} else {
//			return transactionEntityBuilder.createdAt(createdAt).build();
//		}
//	}
}
