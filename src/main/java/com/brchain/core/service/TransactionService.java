package com.brchain.core.service;

import org.hyperledger.fabric.sdk.BlockInfo.EnvelopeInfo;
import org.hyperledger.fabric.sdk.BlockInfo.EnvelopeType;
import org.hyperledger.fabric.sdk.BlockInfo.TransactionEnvelopeInfo;
import org.hyperledger.fabric.sdk.BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo;
import org.json.simple.JSONObject;
import org.springframework.stereotype.Service;

import com.brchain.core.dto.BlockDto;
import com.brchain.core.dto.TransactionDto;
import com.brchain.core.dto.channel.ChannelInfoDto;
import com.brchain.core.entity.TransactionEntity;
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

	public TransactionDto findBlockById(Long id) {

		return util.toDto(transactionRepository.findById(id).orElseThrow(IllegalArgumentException::new));
	}

	public TransactionDto findBlockByTxId(String txId) {

		TransactionEntity transactionEntity = transactionRepository.findByTxId(txId);
		if (transactionEntity == null) {
			throw new IllegalArgumentException();
		}
		return util.toDto(transactionEntity);
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

	public void inspectTransaction(EnvelopeInfo envelopeInfo, ChannelInfoDto channelInfoDto, BlockDto blockDto) {
		String txId = null;

		try {
			// 이벤트로 받은 txID가 있는지 조회
			txId = envelopeInfo.getTransactionID();
			if (txId.isEmpty()) {
				throw new IllegalArgumentException();
			}
			findBlockByTxId(txId);

		} catch (IllegalArgumentException e) {

			// 조회가 안되면 트렌젝션 정보 저장
			TransactionDto transactionDto = new TransactionDto();
			transactionDto.setTxId(txId.isEmpty() ? null : txId);
			transactionDto.setCreatorId(envelopeInfo.getCreator().getMspid());
			transactionDto.setTxType(envelopeInfo.getType().toString());
			transactionDto.setTimestamp(envelopeInfo.getTimestamp());
			transactionDto.setBlockDto(blockDto);
			transactionDto.setChannelInfoDto(channelInfoDto);

			if (envelopeInfo.getType() == EnvelopeType.TRANSACTION_ENVELOPE) {
				TransactionEnvelopeInfo transactionEnvelopeInfo = (TransactionEnvelopeInfo) envelopeInfo;

				for (TransactionActionInfo transactionActionInfo : transactionEnvelopeInfo
						.getTransactionActionInfos()) {

					transactionDto.setCcName(transactionActionInfo.getChaincodeIDName());
					transactionDto.setCcVersion(transactionActionInfo.getChaincodeIDVersion());

					JSONObject argsJson = new JSONObject();

					for (int j = 1; j < transactionActionInfo.getChaincodeInputArgsCount(); j++) {

						argsJson.put("arg" + j, new String(transactionActionInfo.getChaincodeInputArgs(j)));

					}

					transactionDto.setCcArgs(argsJson.toString());

				}

			}
			saveTransaction(transactionDto);
		}

	}

}
