package com.brchain.core.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.ChannelHandlerEntity;
import com.brchain.core.entity.ChannelInfoEntity;

public interface ChannelHandlerRepository extends JpaRepository<ChannelHandlerEntity, Long> {

	ArrayList<ChannelHandlerEntity> findByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);

}
