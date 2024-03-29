package com.brchain.core.channel.repository.impl;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.channel.entitiy.QChannelInfoPeerEntity;
import com.brchain.core.channel.repository.custom.ChannelInfoPeerCustomRepository;
import com.querydsl.core.types.dsl.BooleanExpression;

@Transactional(readOnly = true)
public class ChannelInfoPeerRepositoryImpl extends QuerydslRepositorySupport
		implements ChannelInfoPeerCustomRepository {

	final QChannelInfoPeerEntity channelInfoPeerEntity = QChannelInfoPeerEntity.channelInfoPeerEntity;

	public ChannelInfoPeerRepositoryImpl() {
		super(ChannelInfoPeerEntity.class);
	}

	@Override
	public List<ChannelInfoPeerEntity> findByChannelNameOrConName(String channelName, String conName) {

		return from(channelInfoPeerEntity).leftJoin(channelInfoPeerEntity.conInfoEntity)
			.fetchJoin()
			.leftJoin(channelInfoPeerEntity.channelInfoEntity)
			.fetchJoin()
			.where(eqChannelName(channelName), eqConName(conName))
			.fetch();

	}

	@Override
	public List<ChannelInfoPeerEntity> findByOrgName(String orgName) {

		return from(channelInfoPeerEntity).where(channelInfoPeerEntity.conInfoEntity.orgName.eq(orgName)).groupBy(channelInfoPeerEntity.channelInfoEntity.channelName)
			.fetch();

	}

	@Override
	public List<String> findOrgExcludedOrgName(String channelName, String orgName) {

		return from(channelInfoPeerEntity).select(channelInfoPeerEntity.conInfoEntity.orgName)
			.where(eqChannelName(channelName)
				.and(channelInfoPeerEntity.conInfoEntity.orgName.ne(orgName)))
			.groupBy(channelInfoPeerEntity.conInfoEntity.orgName)
			.fetch();

	}

	private BooleanExpression eqChannelName(String channelName) {
		if (StringUtils.isEmpty(channelName)) {
			return null;
		}
		return channelInfoPeerEntity.channelInfoEntity.channelName.eq(channelName);
	}

	private BooleanExpression eqConName(String conName) {
		if (StringUtils.isEmpty(conName)) {
			return null;
		}
		return channelInfoPeerEntity.conInfoEntity.conName.eq(conName);
	}

}
