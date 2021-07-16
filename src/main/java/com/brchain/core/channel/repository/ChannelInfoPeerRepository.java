package com.brchain.core.channel.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.channel.repository.custom.ChannelInfoPeerCustomRepository;

@Repository
public interface ChannelInfoPeerRepository extends JpaRepository<ChannelInfoPeerEntity, Long>, ChannelInfoPeerCustomRepository {

}
