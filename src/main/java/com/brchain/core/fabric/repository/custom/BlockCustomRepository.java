package com.brchain.core.fabric.repository.custom;

import java.util.List;

import com.brchain.core.fabric.dto.BlockAndTxDto;

public interface BlockCustomRepository {
	List<BlockAndTxDto> findByChannelName(String channelName);
	long countByChannelName(String channelName);
}
