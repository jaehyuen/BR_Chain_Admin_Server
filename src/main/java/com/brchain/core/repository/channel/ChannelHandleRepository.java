package com.brchain.core.repository.channel;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.channel.ChannelHandleEntity;

public interface ChannelHandleRepository extends JpaRepository<ChannelHandleEntity, String> {

//	ArrayList<ChannelHandlerEntity> findByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);

}
