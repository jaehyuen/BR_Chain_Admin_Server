package com.brchain.core.channel.repository.custom;

import java.util.List;

import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;

public interface ChannelInfoPeerCustomRepository {

	List<ChannelInfoPeerEntity> findByChannelNameOrConName(String channelName, String conName);
	List<ChannelInfoPeerEntity> findByOrgName(String orgName);
	List<String> findOrgExcludedOrgName(String channelName, String orgName);
	

}
