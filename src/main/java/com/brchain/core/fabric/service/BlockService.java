package com.brchain.core.fabric.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.common.Common.BlockData;
import org.hyperledger.fabric.protos.common.Common.ChannelHeader;
import org.hyperledger.fabric.protos.common.Common.Header;
import org.hyperledger.fabric.protos.common.Common.SignatureHeader;
import org.hyperledger.fabric.protos.msp.Identities.SerializedIdentity;
import org.hyperledger.fabric.protos.peer.ProposalResponsePackage.ProposalResponse;
import org.hyperledger.fabric.protos.peer.TransactionPackage.ChaincodeActionPayload;
import org.hyperledger.fabric.protos.peer.TransactionPackage.Transaction;
import org.hyperledger.fabric.protos.peer.TransactionPackage.TransactionAction;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.BlockInfo.EnvelopeInfo;
import org.springframework.stereotype.Service;

import com.brchain.common.dto.ResultDto;
import com.brchain.core.channel.dto.ChannelInfoDto;
import com.brchain.core.fabric.dto.BlockAndTxDto;
import com.brchain.core.fabric.dto.BlockDto;
import com.brchain.core.fabric.entity.BlockEntity;
import com.brchain.core.fabric.repository.BlockRepository;
import com.brchain.core.util.Util;
import com.google.protobuf.ByteString;
import com.google.protobuf.Descriptors.FieldDescriptor;
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

		return util.toDto(blockRepository.findById(blockDataHash)
			.orElseThrow(IllegalArgumentException::new));
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
		int      txCnt = block.getBlock()
			.getData()
			.getDataCount();
		try {

			// 이벤트로 받은 blockDataHash이 있는지 조회
			blockDto = findBlockByBlockDataHash(Hex.encodeHexString(block.getDataHash()));

		} catch (IllegalArgumentException e) {

			// 조회가 안되면 리슨받은 블록 정보 저장
//			block.getBlock().sh
			blockDto = new BlockDto();
			blockDto.setBlockDataHash(Hex.encodeHexString(block.getDataHash()));
			blockDto.setBlockNum((int) block.getBlockNumber());
			blockDto.setPrevDataHash(Hex.encodeHexString(block.getPreviousHash()));
			blockDto.setTimestamp(block.getEnvelopeInfo(0)
				.getTimestamp());
			blockDto.setTxCount(block.getBlock()
				.getData()
				.getDataCount());
			blockDto.setChannelInfoDto(channelInfoDto);

			saveBLock(blockDto);
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
			transactionService.inspectTransaction(envelopeInfo, channelInfoDto, blockDto);

		}

	}

	public ResultDto getBlockListByChannel(String channelName) {
//		List<BlockEntity> blockEntityList = blockRepository.findByChannelName(channelName);
		List<BlockAndTxDto> blockEntityList = blockRepository.findByChannelName(channelName);

		if (blockEntityList.isEmpty()) {
			return util.setResult("0000", true, "Success get block by channel name", new ArrayList<BlockAndTxDto>());
		} else {
			return util.setResult("0000", true, "Success get block by channel name", blockEntityList);
		}

	}

	public ResultDto getBlockByBlockDataHash(String blockDataHash) {

		return util.setResult("0000", true, "Success get block by channel name", findBlockByBlockDataHash(blockDataHash));

	}

}
