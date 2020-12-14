package com.brchain.core.repository.channel;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoPeerEntity;

public interface ChannelInfoPeerRepository extends JpaRepository<ChannelInfoPeerEntity, Long> {

	ArrayList<ChannelInfoPeerEntity> findByConInfoEntity(ConInfoEntity conInfoEntity);

	ArrayList<ChannelInfoPeerEntity> findByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);
	
	ArrayList<ChannelInfoPeerEntity> findByChannelInfoEntityAndConInfoEntity(ChannelInfoEntity channelInfoEntity,ConInfoEntity conInfoEntity);

}
