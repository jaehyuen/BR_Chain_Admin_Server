package com.brchain.core.channel.repository.Impl;

import java.util.List;

import org.hibernate.annotations.Where;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.channel.entitiy.QChannelInfoEntity;
import com.brchain.core.channel.entitiy.QChannelInfoPeerEntity;
import com.brchain.core.channel.repository.custom.ChannelInfoPeerCustomRepository;
import com.brchain.core.container.entitiy.QConInfoEntity;

@Transactional(readOnly = true)
public class ChannelInfoPeerRepositoryImpl extends QuerydslRepositorySupport implements ChannelInfoPeerCustomRepository {

	public ChannelInfoPeerRepositoryImpl() {
		super(ChannelInfoPeerEntity.class);
	}

	@Override
	public List<ChannelInfoPeerEntity> test(String test) {
		final QChannelInfoPeerEntity channelInfoPeerEntity = QChannelInfoPeerEntity.channelInfoPeerEntity;
		return from(channelInfoPeerEntity).leftJoin(channelInfoPeerEntity.conInfoEntity)
			.fetchJoin()
			.leftJoin(channelInfoPeerEntity.channelInfoEntity)
			.fetchJoin()
			.where(channelInfoPeerEntity.channelInfoEntity.channelName.eq("testchannel"))
			.fetch();

	}

	@Override
	public List<ChannelInfoPeerEntity> findByConInfoEntityConName(String conName) {
		final QChannelInfoPeerEntity channelInfoPeerEntity = QChannelInfoPeerEntity.channelInfoPeerEntity;
		return from(channelInfoPeerEntity).leftJoin(channelInfoPeerEntity.conInfoEntity)
			.fetchJoin()
			.leftJoin(channelInfoPeerEntity.channelInfoEntity)
			.fetchJoin()
			.where(channelInfoPeerEntity.conInfoEntity.conName.eq(conName))
			.fetch();

	}

	@Override
	public List<ChannelInfoPeerEntity> findByChannelName(String channelName) {
		final QChannelInfoPeerEntity channelInfoPeerEntity = QChannelInfoPeerEntity.channelInfoPeerEntity;
		return from(channelInfoPeerEntity).leftJoin(channelInfoPeerEntity.conInfoEntity)
			.fetchJoin()
			.leftJoin(channelInfoPeerEntity.channelInfoEntity)
			.fetchJoin()
			.where(channelInfoPeerEntity.channelInfoEntity.channelName.eq(channelName))
			.fetch();
	}

	@Override
	public List<ChannelInfoPeerEntity> findByChannelNameAndConName(String channelName, String conName) {
		final QChannelInfoPeerEntity channelInfoPeerEntity = QChannelInfoPeerEntity.channelInfoPeerEntity;
		return from(channelInfoPeerEntity).leftJoin(channelInfoPeerEntity.conInfoEntity)
			.fetchJoin()
			.leftJoin(channelInfoPeerEntity.channelInfoEntity)
			.fetchJoin()
			.where(channelInfoPeerEntity.channelInfoEntity.channelName.eq(channelName)
				.and(channelInfoPeerEntity.conInfoEntity.conName.eq(conName)))
			.fetch();
	}

}
