package com.brchain.core.fabric.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.fabric.entity.TransactionEntity;
import com.brchain.core.fabric.repository.custom.TransactionCustomRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, Long>, TransactionCustomRepository {

	Optional<TransactionEntity> findByTxId(String txId);


}
