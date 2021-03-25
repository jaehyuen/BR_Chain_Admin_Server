package com.brchain.core.channel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.container.entitiy.ConInfoEntity;

public interface ChannelInfoPeerRepositoryCustom {

	List<ChannelInfoPeerEntity> test(String test);

}
