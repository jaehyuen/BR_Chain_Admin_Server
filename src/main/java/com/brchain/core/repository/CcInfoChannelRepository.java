package com.brchain.core.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.CcInfoChannelEntity;
import com.brchain.core.entity.ChannelInfoEntity;

public interface CcInfoChannelRepository extends JpaRepository<CcInfoChannelEntity, Long> {

	ArrayList<CcInfoChannelEntity> findByChannelInfoEntity(ChannelInfoEntity channelInfoEntity);

}
