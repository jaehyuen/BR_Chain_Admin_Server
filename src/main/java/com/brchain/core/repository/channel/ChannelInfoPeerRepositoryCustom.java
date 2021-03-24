package com.brchain.core.repository.channel;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.entity.channel.ChannelInfoPeerEntity;

public interface ChannelInfoPeerRepositoryCustom {

	List<ChannelInfoPeerEntity> test(String test);

}
