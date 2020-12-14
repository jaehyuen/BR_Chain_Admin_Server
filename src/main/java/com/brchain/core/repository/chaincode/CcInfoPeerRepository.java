package com.brchain.core.repository.chaincode;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.entity.chaincode.CcInfoPeerEntity;

public interface CcInfoPeerRepository extends JpaRepository<CcInfoPeerEntity, Long> {

	ArrayList<CcInfoPeerEntity> findByConInfoEntity(ConInfoEntity conInfoEntity);

}
