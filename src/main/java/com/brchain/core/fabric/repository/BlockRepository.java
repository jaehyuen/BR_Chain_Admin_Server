package com.brchain.core.fabric.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.fabric.dto.BlockAndTxDto;
import com.brchain.core.fabric.entity.BlockEntity;

public interface BlockRepository extends JpaRepository<BlockEntity, String> {

	int countByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);

//	@Query(value = "SELECT * FROM BLOCK a WHERE a.CHANNELINFO_CHANNEL_NAME = :channelName ORDER BY a.BLOCK_NUM DESC", nativeQuery = true)
	@Query(nativeQuery = true)
	List<BlockAndTxDto> findByChannelName(String channelName);

}
