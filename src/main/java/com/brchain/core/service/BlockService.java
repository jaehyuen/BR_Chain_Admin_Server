package com.brchain.core.service;

import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.protos.common.Common.Block;
import org.hyperledger.fabric.protos.common.Common.BlockData;
import org.hyperledger.fabric.protos.common.Common.Payload;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.BlockInfo.EnvelopeInfo;
import org.springframework.stereotype.Service;

import com.brchain.core.dto.BlockDto;
import com.brchain.core.dto.TransactionDto;
import com.brchain.core.dto.channel.ChannelInfoDto;
import com.brchain.core.repository.BlockRepository;
import com.brchain.core.util.Util;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;

import javassist.bytecode.ByteArray;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlockService {

	// jpa 레파지토리
	private final BlockRepository blockRepository;

	private final TransactionService transactionService;

	private final Util util;

	/**
	 * 블록정보 저장 서비스
	 * 
	 * @param blockDto 블록 정보 DTO
	 * 
	 * @return 저장한 블록 정보 DTO
	 */

	public BlockDto saveBLock(BlockDto blockDto) {

		return util.toDto(blockRepository.save(util.toEntity(blockDto)));

	}

	/**
	 * 테스트중 블록테이터 해쉬값으로 블록정보 조회 서비스
	 * 
	 * @param blockDataHash 블록데이터 해쉬값
	 * 
	 * @return 조회한 블록정보 DTO
	 */

	public BlockDto findBlockByBlockDataHash(String blockDataHash) {

		return util.toDto(blockRepository.findById(blockDataHash).orElseThrow(IllegalArgumentException::new));
	}

	/**
	 * 채널의 블록 개수 카운트 서비스
	 * 
	 * @param channelInfoDto 채널정보 DTO
	 * 
	 * @return 카운트한 채널의 블록 개수
	 */

	public int countBychannelBlock(ChannelInfoDto channelInfoDto) {

		return blockRepository.countByChannelInfoEntity(util.toEntity(channelInfoDto));

	}

	public void inspectBlock(BlockInfo block, ChannelInfoDto channelInfoDto) throws InvalidProtocolBufferException {

		BlockDto blockDto;
		int txCnt = block.getBlock().getData().getDataCount();
		try {

			// 이벤트로 받은 blockDataHash이 있는지 조회
			blockDto = findBlockByBlockDataHash(Hex.encodeHexString(block.getDataHash()));

		} catch (IllegalArgumentException e) {

			// 조회가 안되면 리슨받은 블록 정보 저장
			blockDto = new BlockDto();
			blockDto.setBlockDataHash(Hex.encodeHexString(block.getDataHash()));
			blockDto.setBlockNum((int) block.getBlockNumber());
			blockDto.setPrevDataHash(Hex.encodeHexString(block.getPreviousHash()));
			blockDto.setTimestamp(block.getEnvelopeInfo(0).getTimestamp());
			blockDto.setTxCount(block.getBlock().getData().getDataCount());
			blockDto.setChannelInfoDto(channelInfoDto);

			saveBLock(blockDto);
		}

		// 블록 내 트렌젝션 순회
		for (EnvelopeInfo envelopeInfo : block.getEnvelopeInfos()) {
			transactionService.inspectTransaction(envelopeInfo, channelInfoDto, blockDto);

		}

	}

}
