package com.brchain.core.chaincode.repository;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.brchain.core.chaincode.entitiy.CcInfoChannelEntity;
import com.brchain.core.chaincode.entitiy.CcInfoEntity;
import com.brchain.core.channel.entitiy.ChannelInfoEntity;

public interface CcInfoChannelRepository extends JpaRepository<CcInfoChannelEntity, Long> {

//	ArrayList<CcInfoChannelEntity> findByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);
	
	@Query("select a,b,c from CcInfoChannelEntity a left join fetch a.channelInfoEntity b left join fetch a.ccInfoEntity c where b.channelName=:channelName ")
	ArrayList<CcInfoChannelEntity> findByChannelName(@Param("channelName") String channelName);
	
//	Optional<CcInfoChannelEntity> findByChannelInfoEntityAndCcInfoEntity(ChannelInfoEntity channelInfoEntity,CcInfoEntity ccInfoEntity);
	
	@Query("select a,b,c from CcInfoChannelEntity a left join fetch a.channelInfoEntity b left join fetch a.ccInfoEntity c where b.channelName=:channelName and c.id=:id")
	Optional<CcInfoChannelEntity> findByChannelNameAndCcName(@Param("channelName") String channelName, @Param("id") Long id);
	
//	long countByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);
}
