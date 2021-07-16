package com.brchain.core.channel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.channel.repository.custom.ChannelInfoCustomRepository;

public interface ChannelInfoRepository extends JpaRepository<ChannelInfoEntity, String>, ChannelInfoCustomRepository {



}
