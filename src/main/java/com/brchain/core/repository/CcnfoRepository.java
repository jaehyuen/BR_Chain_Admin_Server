package com.brchain.core.repository;


import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.CcInfoEntity;

public interface CcnfoRepository extends JpaRepository<CcInfoEntity,String>{
	
//	ChannelInfoEntity findByConName(String channelName);
	

}
