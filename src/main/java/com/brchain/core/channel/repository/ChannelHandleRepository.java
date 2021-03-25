package com.brchain.core.channel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.channel.entitiy.ChannelHandleEntity;

public interface ChannelHandleRepository extends JpaRepository<ChannelHandleEntity, String> {

//	ArrayList<ChannelHandlerEntity> findByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);

}
