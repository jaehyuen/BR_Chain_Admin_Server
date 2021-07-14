package com.brchain.core.fabric.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.BlockInfo.EnvelopeInfo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.channel.dto.ChannelInfoDto;
import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.fabric.dto.BlockAndTxDto;
import com.brchain.core.fabric.dto.BlockDto;
import com.brchain.core.fabric.entity.BlockEntity;
import com.brchain.core.fabric.repository.BlockRepository;
import com.brchain.core.util.BrchainStatusCode;
import com.brchain.core.util.Util;
import com.google.protobuf.InvalidProtocolBufferException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlockService {

	// jpa 레파지토리
	private final BlockRepository    blockRepository;

	private final TransactionService transactionService;

	private final Util               util;

	/**
	 * 블록정보 저장 서비스
	 * 
	 * @param blockEntity 블록 정보 Entity
	 * 
	 * @return 저장한 블록 정보 Entity
	 */

	public BlockEntity saveBlock(BlockEntity blockEntity) {

		return blockRepository.save(blockEntity);

	}

	/**
	 * 테스트중 블록테이터 해쉬값으로 블록정보 조회 서비스
	 * 
	 * @param blockDataHash 블록데이터 해쉬값
	 * 
	 * @return 조회한 블록정보 Entity
	 */

	public BlockEntity findBlockByBlockDataHash(String blockDataHash) {

		return blockRepository.findById(blockDataHash).orElseThrow(IllegalArgumentException::new);
	}

	/**
	 * 채널의 블록 개수 카운트 서비스
	 * 
	 * @param channelInfoDto 채널정보 DTO
	 * 
	 * @return 카운트한 채널의 블록 개수
	 */

	public long countByChannelName(String channelName) {

		return blockRepository.countByChannelName(channelName);

	}

	/**
	 * 이벤트로 받은 블록 분석후 디비에 저장하는 서비스
	 * 
	 * @param block          이벤트로 받은 블록
	 * @param channelInfoDto 블록에 대한 채널 정보 Entity
	 * 
	 * @throws InvalidProtocolBufferException
	 */

	public void inspectBlock(BlockInfo block, ChannelInfoEntity channelInfoEntity) {

		BlockEntity blockEntity;
//		int      txCnt = block.getBlock()
//			.getData()
//			.getDataCount();
		try {

			// 이벤트로 받은 blockDataHash이 있는지 조회
			blockEntity = findBlockByBlockDataHash(Hex.encodeHexString(block.getDataHash()));

		} catch (IllegalArgumentException e) {

			// 조회가 안되면 리슨받은 블록 정보 저장
//			block.getBlock().sh

			blockEntity = new BlockEntity();
			try {
				blockEntity.setBlockDataHash(Hex.encodeHexString(block.getDataHash()));
				blockEntity.setBlockNum((int) block.getBlockNumber());
				blockEntity.setPrevDataHash(Hex.encodeHexString(block.getPreviousHash()));
				blockEntity.setTimestamp(block.getEnvelopeInfo(0).getTimestamp());
				blockEntity.setTxCount(block.getBlock().getData().getDataCount());
				blockEntity.setChannelInfoEntity(channelInfoEntity);
				
			} catch (InvalidProtocolBufferException e1) {
				throw new RuntimeException(channelInfoEntity.getChannelName() + " 채널의 " + blockEntity.getBlockNum() + "번 블록의 프로토콜 메시지가 잘못되었습니다.");
			}
			saveBlock(blockEntity);
		}

		// 디비에 저장할 데이터 테스트중...
//		block.getTransactionCount();
//		block.getTransActionsMetaData();
//		block.getBlock().
//		
//		BlockData test1 =block.getBlock().getData();
//		System.out.println(test1.getDataCount());
//		BlockData test2=  Common.BlockData.parseFrom(test1.getData(0));
//		Transaction test3 = Transaction.parseFrom(test1.getData(0));
//		
//		TransactionAction test4=TransactionAction.parseFrom(test3.toByteString());
//		Header test5= Header.parseFrom(test4.getHeader());
//		ChannelHeader test6= ChannelHeader.parseFrom(test5.getChannelHeader());
//		
////		ChannelHeader test11= ChannelHeader.parseFrom(test6.get);
//		Map<FieldDescriptor, Object> test10 = test6.getAllFields();
////		test6.get
//		SignatureHeader test7=SignatureHeader.parseFrom(test5.getSignatureHeader());
//		
//		SerializedIdentity test8=  SerializedIdentity.parseFrom(test7.getCreator());
//		test8.getMspid();
//		test8.get

//		ByteString test5 = test4.getPayload();
//		ProposalResponse test6=ProposalResponse.parseFrom(test4.toByteString());
//		ChaincodeActionPayload test5= ChaincodeActionPayload.parseFrom(test4.toByteString());
//		test1.get
//		block.getTransActionsMetaData()
		// 블록 내 트렌젝션 순회
		for (EnvelopeInfo envelopeInfo : block.getEnvelopeInfos()) {
			transactionService.inspectTransaction(envelopeInfo, channelInfoEntity, blockEntity);

		}

	}

	/**
	 * 채널명으로 블록 리스트 조회 서비스
	 * 
	 * @param channelName 채널 이름
	 * 
	 * @return 결과 DTO (블록 및 트랜잭션 정보)
	 */

	@Transactional(readOnly = true)
	public ResultDto<List<BlockAndTxDto>> getBlockListByChannel(String channelName) {
		
		return util.setResult(BrchainStatusCode.SUCCESS, blockRepository.findByChannelName(channelName));

	}

	/**
	 * 블록 데이터 해쉬값으로 블록 조회 서비스
	 * 
	 * @param blockDataHash 블록 데이터 해쉬값
	 * 
	 * @return 결과 DTO (블록 정보)
	 */

	@Transactional(readOnly = true)
	public ResultDto<BlockDto> getBlockByBlockDataHash(String blockDataHash) {

		//return util.setResult("0000", true, "Success get block by channel name", findBlockByBlockDataHash(blockDataHash));
		//Success get block by block data hash
		return util.setResult(BrchainStatusCode.SUCCESS, util.toDto(findBlockByBlockDataHash(blockDataHash)));

	}

}
