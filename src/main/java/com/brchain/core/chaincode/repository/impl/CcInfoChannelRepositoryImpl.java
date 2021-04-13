package com.brchain.core.chaincode.repository.impl;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.core.chaincode.entitiy.CcInfoChannelEntity;
import com.brchain.core.chaincode.entitiy.QCcInfoChannelEntity;
import com.brchain.core.chaincode.repository.custom.CcInfoChannelCustomRepository;

@Transactional(readOnly = true)
public class CcInfoChannelRepositoryImpl extends QuerydslRepositorySupport implements CcInfoChannelCustomRepository {

	public CcInfoChannelRepositoryImpl() {
		super(CcInfoChannelEntity.class);
	}

	@Override
	public List<CcInfoChannelEntity> findByChannelName(String channelName) {
		final QCcInfoChannelEntity ccInfoChannelEntity = QCcInfoChannelEntity.ccInfoChannelEntity;

		return from(ccInfoChannelEntity).leftJoin(ccInfoChannelEntity.ccInfoEntity)
			.fetchJoin()
			.leftJoin(ccInfoChannelEntity.channelInfoEntity)
			.fetchJoin()
			.where(ccInfoChannelEntity.channelInfoEntity.channelName.eq(channelName))
			.fetch();
	}

	@Override
	public CcInfoChannelEntity findByChannelNameAndCcName(String channelName, String ccName) {
		final QCcInfoChannelEntity ccInfoChannelEntity = QCcInfoChannelEntity.ccInfoChannelEntity;

		return from(ccInfoChannelEntity).where(ccInfoChannelEntity.ccInfoEntity.ccName.eq(ccName)
			.and(ccInfoChannelEntity.channelInfoEntity.channelName.eq(channelName)))
			.fetchOne();

	}

}
