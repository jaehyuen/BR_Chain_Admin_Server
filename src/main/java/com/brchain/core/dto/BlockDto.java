package com.brchain.core.dto;

import java.time.LocalDateTime;

import com.brchain.core.entity.BlockEntity;
import com.brchain.core.entity.BlockEntity.BlockEntityBuilder;
import com.brchain.core.entity.channel.ChannelInfoEntity;
import com.brchain.core.repository.BlockRepository;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlockDto {

	private String blockDataHash;
	private int blockNum;
	private int txCount;
	private String prevDataHash;
	private ChannelInfoEntity channelInfoEntity;
	private LocalDateTime createdAt;

//	public BlockEntity toEntity() {
//
//		BlockEntityBuilder blockEntityBuilder = BlockEntity.builder().blockDataHash(blockDataHash).blockNum(blockNum)
//				.txCount(txCount).prevDataHash(prevDataHash).channelInfoEntity(channelInfoEntity);
//		if (createdAt == null) {
//			return blockEntityBuilder.build();
//		} else {
//			return blockEntityBuilder.createdAt(createdAt).build();
//		}
//	}
}
