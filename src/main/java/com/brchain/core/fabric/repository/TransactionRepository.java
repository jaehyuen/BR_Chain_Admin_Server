package com.brchain.core.fabric.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.fabric.entity.TransactionEntity;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {

	int countByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);

	TransactionEntity findByTxId(String txId);

}
