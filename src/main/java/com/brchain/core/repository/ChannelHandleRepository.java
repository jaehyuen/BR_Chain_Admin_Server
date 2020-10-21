package com.brchain.core.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.ChannelHandleEntity;
import com.brchain.core.entity.ChannelInfoEntity;

public interface ChannelHandleRepository extends JpaRepository<ChannelHandleEntity, String> {

//	ArrayList<ChannelHandlerEntity> findByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);

}
