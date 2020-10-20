package com.brchain.core.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.ChannelInfoEntity;
import com.brchain.core.entity.ChannelInfoPeerEntity;
import com.brchain.core.entity.ConInfoEntity;

public interface ChannelInfoPeerRepository extends JpaRepository<ChannelInfoPeerEntity, Long> {

	ArrayList<ChannelInfoPeerEntity> findByConInfoEntity(ConInfoEntity conInfoEntity);

	ArrayList<ChannelInfoPeerEntity> findByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);

}
