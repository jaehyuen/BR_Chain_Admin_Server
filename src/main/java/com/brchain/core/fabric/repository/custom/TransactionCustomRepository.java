package com.brchain.core.fabric.repository.custom;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.brchain.core.fabric.entity.TransactionEntity;

public interface TransactionCustomRepository {
	
	long countByChannelName(String channelName);
	
	List<TransactionEntity> findByChannelName(@Param("channelName") String channelName);
}
