package com.brchain.core.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.ChannelInfoEntity;
import com.brchain.core.entity.ConInfoEntity;

public interface ChannelInfoRepository extends JpaRepository<ChannelInfoEntity,String>{
	
//	ChannelInfoEntity findByConName(String channelName);
	

}
