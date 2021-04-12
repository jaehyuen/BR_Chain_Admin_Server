package com.brchain.core.chaincode.repository.impl;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.core.chaincode.entitiy.CcInfoPeerEntity;
import com.brchain.core.chaincode.entitiy.QCcInfoPeerEntity;
import com.brchain.core.chaincode.repository.custom.CcInfoPeerCustomRepository;
import com.brchain.core.channel.entitiy.QChannelInfoPeerEntity;

@Transactional(readOnly = true)
public class CcInfoPeerRepositoryImpl extends QuerydslRepositorySupport implements CcInfoPeerCustomRepository {

	public CcInfoPeerRepositoryImpl() {
		super(CcInfoPeerEntity.class);
	}

	@Override
	public List<CcInfoPeerEntity> findCcInfoPeerToActive(String channelName) {
		final QCcInfoPeerEntity      ccInfoPeerlEntity     = QCcInfoPeerEntity.ccInfoPeerEntity;
		final QChannelInfoPeerEntity channelInfoPeerEntity = QChannelInfoPeerEntity.channelInfoPeerEntity;

		return from(ccInfoPeerlEntity)
			.join(channelInfoPeerEntity)
			.on(ccInfoPeerlEntity.conInfoEntity.conName.eq(channelInfoPeerEntity.conInfoEntity.conName))
			.where(channelInfoPeerEntity.channelInfoEntity.channelName.eq(channelName))
			.groupBy(ccInfoPeerlEntity.ccInfoEntity)
			.fetch();
	}

}
