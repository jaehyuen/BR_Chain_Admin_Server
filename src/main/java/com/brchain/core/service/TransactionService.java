package com.brchain.core.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.brchain.core.dto.BlockDto;
import com.brchain.core.dto.TransactionDto;
import com.brchain.core.entity.BlockEntity;
import com.brchain.core.entity.TransactionEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;
import com.brchain.core.repository.TransactionRepository;
import com.brchain.core.util.Util;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionService {

	private final TransactionRepository transactionRepository;

	private final Util util;
	/**
	 * 트렌젝션 저장 서비스
	 * 
	 * @param transactionDto 트렌젝션 정보 DTO
	 * 
	 * @return 저장한 트렌젝션 정보 DTO
	 */
	public TransactionDto saveTransaction(TransactionDto transactionDto) {

		return util.toDto(transactionRepository.save(util.toEntity(transactionDto)));

	}

	/**
	 * 트렌젝션 아이디로 트렌젝션 정보 조회 서비스
	 * 
	 * @param txId 트렌젝션 아이디
	 * 
	 * @return 조회한 트렌젝션 정보 DTO
	 */

	public TransactionDto findBlockByTxId(String txId) {

		return util.toDto(transactionRepository.findById(txId).orElseThrow(IllegalArgumentException::new));
	}

	/**
	 * 채널의 트렌젝션 개수 카운트 서비스
	 * 
	 * @param channelInfoEntity 채널정보 엔티티
	 * 
	 * @return 카운트한 트렌젝션 개수
	 * 
	 * TODO 채널 엔티티로 조회를 해도 될까?
	 */
	public int countBychannelTransaction(ChannelInfoEntity channelInfoEntity) {
		return transactionRepository.countByChannelInfoEntity(channelInfoEntity);

	}

//	/**
//	 * DTO 변경 서비스
//	 * 
//	 * @param TransactionEntity 변경할 엔티티
//	 * 
//	 * @return 변경한 DTO
//	 */
//	public TransactionDto toTransactionDto(TransactionEntity transactionEntity) {
//		return TransactionDto.builder().txID(transactionEntity.getTxID()).creatorId(transactionEntity.getCreatorId())
//				.txID(transactionEntity.getTxType()).timestamp(transactionEntity.getTimestamp())
//				.ccName(transactionEntity.getCcName()).ccVersion(transactionEntity.getCcVersion())
//				.ccArgs(transactionEntity.getCcArgs()).blockEntity(transactionEntity.getBlockEntity())
//				.channelInfoEntity(transactionEntity.getChannelInfoEntity()).build();
//	}

}
