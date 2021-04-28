package com.brchain.core.chaincode.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.core.chaincode.entitiy.CcInfoChannelEntity;
import com.brchain.core.chaincode.repository.custom.CcInfoChannelCustomRepository;

public interface CcInfoChannelRepository extends JpaRepository<CcInfoChannelEntity, Long>, CcInfoChannelCustomRepository {

}
