package com.brchain.core.repository;

import java.util.ArrayList;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.entity.CcInfoPeerEntity;
import com.brchain.core.entity.ConInfoEntity;

public interface CcInfoPeerRepository extends JpaRepository<CcInfoPeerEntity, Long> {

	ArrayList<CcInfoPeerEntity> findByConInfoEntity(ConInfoEntity conInfoEntity);

}
