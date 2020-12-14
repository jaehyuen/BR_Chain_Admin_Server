package com.brchain.core.service;

import java.util.Optional;

import org.springframework.stereotype.Service;

import com.brchain.core.dto.BlockDto;
import com.brchain.core.entity.BlockEntity;
import com.brchain.core.entity.channel.ChannelHandleEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;
import com.brchain.core.repository.BlockRepository;
import com.brchain.core.util.Util;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BlockService {

	private final BlockRepository blockRepository;

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
	 * @param channelInfoEntity 채널정보 엔티티
	 * 
	 * @return 카운트한 채널의 블록 개수
	 * 
	 *         TODO 채널 엔티티로 조회를 해도 될까?
	 */

	public int countBychannelBlock(ChannelInfoEntity channelInfoEntity) {
		return blockRepository.countByChannelInfoEntity(channelInfoEntity);

	}

}
