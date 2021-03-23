package com.brchain.core.repository.channel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoPeerEntity;

public interface ChannelInfoPeerRepository extends JpaRepository<ChannelInfoPeerEntity, Long> {

	ArrayList<ChannelInfoPeerEntity> findByConInfoEntity(ConInfoEntity conInfoEntity);
	

	@Query("select a,b,c from ChannelInfoPeerEntity a left join fetch a.channelInfoEntity b left join fetch a.conInfoEntity c where c.conName=:conName ")
	ArrayList<ChannelInfoPeerEntity> findByConInfoEntityConName(@Param("conName")String conName);

	ArrayList<ChannelInfoPeerEntity> findByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);
	
	ArrayList<ChannelInfoPeerEntity> findByChannelInfoEntityAndConInfoEntity(ChannelInfoEntity channelInfoEntity,ConInfoEntity conInfoEntity);

}
