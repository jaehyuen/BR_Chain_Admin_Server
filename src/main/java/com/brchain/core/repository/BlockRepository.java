package com.brchain.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.BlockEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;

public interface BlockRepository extends JpaRepository<BlockEntity, String> {
	
	int countByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);


}
