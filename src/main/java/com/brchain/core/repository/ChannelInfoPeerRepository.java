package com.brchain.core.repository;


import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.CcInfoEntity;
import com.brchain.core.entity.CcInfoPeerEntity;
import com.brchain.core.entity.ChannelInfoEntity;
import com.brchain.core.entity.ChannelInfoPeerEntity;

public interface ChannelInfoPeerRepository extends JpaRepository<ChannelInfoPeerEntity,Long>{
//	
	ArrayList<ChannelInfoPeerEntity> findByConName(String conName);
	
	ArrayList<ChannelInfoPeerEntity> findByChannelName(String channelName);

}
