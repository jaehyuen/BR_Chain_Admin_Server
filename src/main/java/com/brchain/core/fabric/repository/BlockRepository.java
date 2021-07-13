package com.brchain.core.fabric.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.fabric.entity.BlockEntity;
import com.brchain.core.fabric.repository.custom.BlockCustomRepository;

public interface BlockRepository extends JpaRepository<BlockEntity, String>, BlockCustomRepository {

	//int countByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);

}
