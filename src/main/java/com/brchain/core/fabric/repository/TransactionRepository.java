package com.brchain.core.fabric.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.brchain.core.fabric.entity.TransactionEntity;
import com.brchain.core.fabric.repository.custom.TransactionCustomRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long>, TransactionCustomRepository {

	TransactionEntity findByTxId(String txId);
	
	@Query(value ="select * from TRANSACTION where CHANNELINFO_CHANNEL_NAME = :channelName",nativeQuery = true)
	List<TransactionEntity> findByChannelName(@Param("channelName") String channelName);

}
