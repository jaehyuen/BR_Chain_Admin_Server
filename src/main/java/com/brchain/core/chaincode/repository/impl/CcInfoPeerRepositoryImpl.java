package com.brchain.core.chaincode.repository.impl;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.core.chaincode.dto.CcSummaryDto;
import com.brchain.core.chaincode.entitiy.CcInfoPeerEntity;
import com.brchain.core.chaincode.entitiy.QCcInfoPeerEntity;
import com.brchain.core.chaincode.repository.custom.CcInfoPeerCustomRepository;
import com.brchain.core.channel.entitiy.QChannelInfoPeerEntity;
import com.brchain.core.container.entitiy.QConInfoEntity;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.JPAExpressions;

@Transactional(readOnly = true)
public class CcInfoPeerRepositoryImpl extends QuerydslRepositorySupport implements CcInfoPeerCustomRepository {

	final QCcInfoPeerEntity      ccInfoPeerEntity     = QCcInfoPeerEntity.ccInfoPeerEntity;
	final QChannelInfoPeerEntity channelInfoPeerEntity = QChannelInfoPeerEntity.channelInfoPeerEntity;
	final QConInfoEntity         conInfoEntity         = QConInfoEntity.conInfoEntity;

	public CcInfoPeerRepositoryImpl() {
		super(CcInfoPeerEntity.class);
	}

	@Override
	public List<CcInfoPeerEntity> findCcInfoPeerToActive(String channelName) {

		return from(ccInfoPeerEntity).join(channelInfoPeerEntity)
			.on(ccInfoPeerEntity.conInfoEntity.conName.eq(channelInfoPeerEntity.conInfoEntity.conName))
			.where(channelInfoPeerEntity.channelInfoEntity.channelName.eq(channelName))
			.groupBy(ccInfoPeerEntity.ccInfoEntity)
			.fetch();
	}

	@Override
	public List<CcSummaryDto> findChaincodeSummary() {

		return from(conInfoEntity).select(Projections.constructor(CcSummaryDto.class, conInfoEntity.conName, ExpressionUtils.as(JPAExpressions.select(ccInfoPeerEntity.count())
			.from(ccInfoPeerEntity)
			.where(ccInfoPeerEntity.conInfoEntity.eq(conInfoEntity)), "ccCnt")))
			.where(conInfoEntity.conType.eq("peer"))
			.fetch();
	}

	@Override
	public List<CcInfoPeerEntity> findByCcId(Long id) {

		return from(ccInfoPeerEntity).leftJoin(ccInfoPeerEntity.conInfoEntity)
				.fetchJoin()
				.leftJoin(ccInfoPeerEntity.ccInfoEntity)
				.fetchJoin().fetch();
	}

}
