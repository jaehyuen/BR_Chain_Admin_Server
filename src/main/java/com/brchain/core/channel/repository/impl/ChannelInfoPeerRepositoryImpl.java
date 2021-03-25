package com.brchain.core.channel.repository.impl;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.channel.entitiy.QChannelInfoPeerEntity;
import com.brchain.core.channel.repository.custom.ChannelInfoPeerCustomRepository;
import com.querydsl.core.BooleanBuilder;

@Transactional(readOnly = true)
public class ChannelInfoPeerRepositoryImpl extends QuerydslRepositorySupport implements ChannelInfoPeerCustomRepository {

	public ChannelInfoPeerRepositoryImpl() {
		super(ChannelInfoPeerEntity.class);
	}

	@Override
	public List<ChannelInfoPeerEntity> findByChannelNameOrConName(String channelName, String conName) {
		final QChannelInfoPeerEntity channelInfoPeerEntity = QChannelInfoPeerEntity.channelInfoPeerEntity;

		BooleanBuilder               builder               = new BooleanBuilder();

		if (!StringUtils.isEmpty(channelName)) {
			builder.and(channelInfoPeerEntity.channelInfoEntity.channelName.eq(channelName));
		}
		if (!StringUtils.isEmpty(conName)) {
			builder.and(channelInfoPeerEntity.conInfoEntity.conName.eq(conName));
		}

		return from(channelInfoPeerEntity).leftJoin(channelInfoPeerEntity.conInfoEntity)
			.fetchJoin()
			.leftJoin(channelInfoPeerEntity.channelInfoEntity)
			.fetchJoin()
			.where(builder)
			.fetch();

	}


}
