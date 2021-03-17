package com.brchain.core.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.TransactionEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

	int countByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);

	TransactionEntity findByTxId(String txId);

}
