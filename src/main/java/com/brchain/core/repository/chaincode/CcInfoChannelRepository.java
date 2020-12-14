package com.brchain.core.repository.chaincode;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.chaincode.CcInfoChannelEntity;
import com.brchain.core.entity.chaincode.CcInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;

public interface CcInfoChannelRepository extends JpaRepository<CcInfoChannelEntity, Long> {

	ArrayList<CcInfoChannelEntity> findByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);
	
	Optional<CcInfoChannelEntity> findByChannelInfoEntityAndCcInfoEntity(ChannelInfoEntity channelInfoEntity,CcInfoEntity ccInfoEntity);
	
//	long countByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);
}
