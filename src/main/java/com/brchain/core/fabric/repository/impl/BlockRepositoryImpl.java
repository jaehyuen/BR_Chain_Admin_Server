package com.brchain.core.fabric.repository.impl;

import java.util.List;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.core.fabric.dto.BlockAndTxDto;
import com.brchain.core.fabric.entity.BlockEntity;
import com.brchain.core.fabric.entity.QBlockEntity;
import com.brchain.core.fabric.entity.QTransactionEntity;
import com.brchain.core.fabric.repository.custom.BlockCustomRepository;

@Transactional(readOnly = true)
public class BlockRepositoryImpl extends QuerydslRepositorySupport implements BlockCustomRepository {

	final QBlockEntity       blockEntity       = QBlockEntity.blockEntity;
	final QTransactionEntity transactionEntity = QTransactionEntity.transactionEntity;

	public BlockRepositoryImpl() {
		super(BlockEntity.class);
	}

	@Override
	public List<BlockAndTxDto> findByChannelName(String channelName) {
		// TODO Auto-generated method stub
		return from(blockEntity)
			.select(Projections.constructor(BlockAndTxDto.class, blockEntity.blockDataHash, blockEntity.blockNum,
					blockEntity.txCount, blockEntity.timestamp, blockEntity.prevDataHash,
					Expressions.stringTemplate("group_concat({0})", transactionEntity.txId)))
			.join(transactionEntity)
			.on(transactionEntity.blockEntity.blockDataHash.eq(blockEntity.blockDataHash))
			.where(blockEntity.channelInfoEntity.channelName.eq(channelName))
			.groupBy(blockEntity.blockDataHash)
			.orderBy(blockEntity.blockNum.desc())
			.fetch();
	}

	@Override
	public long countByChannelName(String channelName) {
		// TODO Auto-generated method stub
		return from(blockEntity).where(blockEntity.channelInfoEntity.channelName.eq(channelName))
			.fetchCount();
	}

}
