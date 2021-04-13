package com.brchain.core.container.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.brchain.core.container.entitiy.ConInfoEntity;

public interface ConInfoRepository extends JpaRepository<ConInfoEntity, String> {

	ConInfoEntity findByConId(String conId);

	List<ConInfoEntity> findByConTypeAndOrgType(String conType, String orgType);

	List<ConInfoEntity> findByConTypeAndOrgName(String conType, String orgName);

	List<ConInfoEntity> findByConTypeAndOrgTypeAndOrgName(String conType, String orgType, String orgName);

	List<ConInfoEntity> findByConType(String conType);

	List<ConInfoEntity> findByConPort(String conPort);

	List<ConInfoEntity> findByOrgName(String orgName);

	@Query(value = "SELECT DISTINCT CONINFO.ORG_NAME FROM CHANNELINFO JOIN CHANNELINFO_PEER on CHANNELINFO.CHANNEL_NAME = CHANNELINFO_PEER.CHANNELINFO_CHANNEL_NAME JOIN CONINFO on CHANNELINFO_PEER.CONINFO_CON_NAME = CONINFO.CON_NAME WHERE CHANNELINFO.CHANNEL_NAME=:channelName", nativeQuery = true)
	List<String> findOrgsByChannelName(@Param("channelName")String channelName);

}
