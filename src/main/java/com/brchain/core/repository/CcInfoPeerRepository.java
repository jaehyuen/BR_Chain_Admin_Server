package com.brchain.core.repository;


import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.CcInfoEntity;
import com.brchain.core.entity.CcInfoPeerEntity;

public interface CcInfoPeerRepository extends JpaRepository<CcInfoPeerEntity,Long>{
	
	ArrayList<CcInfoPeerEntity> findByConName(String conName);
	
	CcInfoPeerEntity findByCcNameAndCcVersion(String ccName, String ccVersion);

}
