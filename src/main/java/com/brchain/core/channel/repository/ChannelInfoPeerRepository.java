package com.brchain.core.channel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;
import com.brchain.core.channel.repository.custom.ChannelInfoPeerCustomRepository;
import com.brchain.core.container.entitiy.ConInfoEntity;

@Repository
public interface ChannelInfoPeerRepository extends JpaRepository<ChannelInfoPeerEntity, Long>, ChannelInfoPeerCustomRepository {

	List<ChannelInfoPeerEntity> findByConInfoEntity(ConInfoEntity conInfoEntity);

}
