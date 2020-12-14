package com.brchain.core.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.TransactionEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {
	
	int countByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);

}
