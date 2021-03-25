package com.brchain.core.container.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.brchain.core.container.entitiy.ConInfoEntity;

public interface ConInfoRepository extends JpaRepository<ConInfoEntity, String> {

	ConInfoEntity findByConId(String conId);

	ArrayList<ConInfoEntity> findByConTypeAndOrgType(String conType, String orgType);

	ArrayList<ConInfoEntity> findByConTypeAndOrgName(String conType, String orgName);

	ArrayList<ConInfoEntity> findByConTypeAndOrgTypeAndOrgName(String conType, String orgType, String orgName);

	ArrayList<ConInfoEntity> findByConType(String conType);

	ArrayList<ConInfoEntity> findByConPort(String conPort);

	ArrayList<ConInfoEntity> findByOrgName(String orgName);

	@Query(value = "SELECT DISTINCT CONINFO.ORG_NAME FROM CHANNELINFO JOIN CHANNELINFO_PEER on CHANNELINFO.CHANNEL_NAME = CHANNELINFO_PEER.CHANNELINFO_CHANNEL_NAME JOIN CONINFO on CHANNELINFO_PEER.CONINFO_CON_NAME = CONINFO.CON_NAME WHERE CHANNELINFO.CHANNEL_NAME=:channelName", nativeQuery = true)
	ArrayList<String> findOrgsByChannelName(@Param("channelName")String channelName);

}
