package com.brchain.core.channel.repository.custom;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;

public interface ChannelInfoPeerCustomRepository {

	List<ChannelInfoPeerEntity> findByChannelNameOrConName(@Param("channelName") String channelName, @Param("conName") String conName);

//	@Query("select a,b,c from ChannelInfoPeerEntity a left join fetch a.channelInfoEntity b left join fetch a.conInfoEntity c where c.conName=:conName ")
//	List<ChannelInfoPeerEntity> findByConInfoEntityConName(@Param("conName") String conName);

//	@Query("select a,b,c from ChannelInfoPeerEntity a left join fetch a.channelInfoEntity b left join fetch a.conInfoEntity c where b.channelName=:channelName ")
//	List<ChannelInfoPeerEntity> findByChannelName(@Param("channelName") String channelName);

//	ArrayList<ChannelInfoPeerEntity> findByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);

//	ArrayList<ChannelInfoPeerEntity> findByChannelInfoEntityAndConInfoEntity(ChannelInfoEntity channelInfoEntity,ConInfoEntity conInfoEntity);
//	@Query("select a,b,c from ChannelInfoPeerEntity a left join fetch a.channelInfoEntity b left join fetch a.conInfoEntity c where b.channelName=:channelName and c.conName=:conName ")
//	List<ChannelInfoPeerEntity> findByChannelNameAndConName(@Param("channelName") String channelName, @Param("conName") String conName);

}
