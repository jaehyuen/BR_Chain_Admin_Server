package com.brchain.core.fabric.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.fabric.entity.BlockEntity;

public interface BlockRepository extends JpaRepository<BlockEntity, String> {
	

	int countByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);


}
