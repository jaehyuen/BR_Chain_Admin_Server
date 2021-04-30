package com.brchain.core.channel.repository.impl;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.core.channel.dto.ChannelSummaryDto;
import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.channel.entitiy.QChannelInfoEntity;
import com.brchain.core.channel.repository.custom.ChannelInfoCustomRepository;
import com.brchain.core.fabric.entity.QBlockEntity;
import com.brchain.core.fabric.entity.QTransactionEntity;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;

@Transactional(readOnly = true)
public class ChannelInfoRepositoryImpl extends QuerydslRepositorySupport implements ChannelInfoCustomRepository {

	final QChannelInfoEntity channelInfoEntity = QChannelInfoEntity.channelInfoEntity;
	final QBlockEntity       blockEntity       = QBlockEntity.blockEntity;
	final QTransactionEntity transactionEntity = QTransactionEntity.transactionEntity;

	public ChannelInfoRepositoryImpl() {
		super(ChannelInfoEntity.class);
	}

	@Override
	public List<ChannelSummaryDto> findChannelSummary(String preMonth, String nowMonth) {
		// TODO Auto-generated method stub
		return from(channelInfoEntity)
			.select(Projections.constructor(ChannelSummaryDto.class, 
											channelInfoEntity.channelName,
											channelInfoEntity.channelBlock,
											channelInfoEntity.channelTx ,
											createBlockSubQuery(preMonth), 
											createBlockSubQuery(nowMonth),
											createTxSubQuery(preMonth),
											createTxSubQuery(nowMonth)
											))
			.fetch();

	}

	private JPQLQuery<Long> createBlockSubQuery(String month) {
		return JPAExpressions.select(blockEntity.count()).from(blockEntity)
			.where(Expressions.stringTemplate("DATE_FORMAT({0}, {1})", blockEntity.timestamp, ConstantImpl.create("%Y%m"))
				.eq(month)
				.and(blockEntity.channelInfoEntity.eq(channelInfoEntity))
				);
	}

	private JPQLQuery<Long> createTxSubQuery(String month) {
		return JPAExpressions.select(transactionEntity.count()).from(transactionEntity)
			.where(Expressions.stringTemplate("DATE_FORMAT({0}, {1})", transactionEntity.timestamp, ConstantImpl.create("%Y%m"))
				.eq(month)
				.and(transactionEntity.channelInfoEntity.eq(channelInfoEntity))
				);
	}

}
