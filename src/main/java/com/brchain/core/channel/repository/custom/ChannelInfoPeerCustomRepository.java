package com.brchain.core.channel.repository.custom;

import java.util.List;

import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;

public interface ChannelInfoPeerCustomRepository {

	List<ChannelInfoPeerEntity> findByChannelNameOrConName(String channelName, String conName);
	
	

}
