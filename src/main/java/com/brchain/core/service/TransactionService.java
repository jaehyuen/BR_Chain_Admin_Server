package com.brchain.core.service;

import org.springframework.stereotype.Service;

import com.brchain.core.dto.TransactionDto;
import com.brchain.core.dto.channel.ChannelInfoDto;
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
	 * @param channelInfoDto 채널정보 DTO
	 * 
	 * @return 카운트한 트렌젝션 개수
	 */
	public int countBychannelTransaction(ChannelInfoDto channelInfoDto) {
		return transactionRepository.countByChannelInfoEntity(util.toEntity(channelInfoDto));

	}

}
