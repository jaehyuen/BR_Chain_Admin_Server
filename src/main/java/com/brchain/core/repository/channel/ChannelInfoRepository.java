package com.brchain.core.repository.channel;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.channel.ChannelInfoEntity;

public interface ChannelInfoRepository extends JpaRepository<ChannelInfoEntity, String> {

}
