package com.brchain.core.channel.repository.Impl;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.channel.entitiy.QChannelInfoPeerEntity;
import com.brchain.core.channel.repository.ChannelInfoPeerRepositoryCustom;

@Transactional(readOnly = true)
public class ChannelInfoPeerRepositoryImpl extends QuerydslRepositorySupport implements ChannelInfoPeerRepositoryCustom {

	
	
	public ChannelInfoPeerRepositoryImpl() {
		super(ChannelInfoPeerEntity.class);
	}

	@Override
	public List<ChannelInfoPeerEntity> test(String test) {
		final QChannelInfoPeerEntity channelInfoPeerEntity = QChannelInfoPeerEntity.channelInfoPeerEntity;
		return from(channelInfoPeerEntity).fetch();

	}

}
