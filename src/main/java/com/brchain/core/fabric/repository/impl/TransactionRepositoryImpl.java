package com.brchain.core.fabric.repository.impl;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.core.fabric.entity.QTransactionEntity;
import com.brchain.core.fabric.entity.TransactionEntity;
import com.brchain.core.fabric.repository.custom.TransactionCustomRepository;

@Transactional(readOnly = true)
public class TransactionRepositoryImpl extends QuerydslRepositorySupport implements TransactionCustomRepository {


	final QTransactionEntity transactionEntity = QTransactionEntity.transactionEntity;

	public TransactionRepositoryImpl() {
		super(TransactionEntity.class);
	}


	@Override
	public long countByChannelName(String channelName) {
		// TODO Auto-generated method stub
		return from(transactionEntity)
			.where(transactionEntity.channelInfoEntity.channelName.eq(channelName)).fetchCount();
	}
	
	@Override
	public List<TransactionEntity> findByChannelName(String channelName) {
		// TODO Auto-generated method stub
		return from(transactionEntity)
			.where(transactionEntity.channelInfoEntity.channelName.eq(channelName)).fetch();
	}

}
