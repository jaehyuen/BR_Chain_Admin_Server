package com.brchain.core.chaincode.repository.custom;

import java.util.List;
import java.util.Optional;

import org.springframework.data.repository.query.Param;

import com.brchain.core.chaincode.entitiy.CcInfoChannelEntity;

public interface CcInfoChannelCustomRepository {

	List<CcInfoChannelEntity> findByChannelName(@Param("channelName") String channelName);

	Optional<CcInfoChannelEntity> findByChannelNameAndCcName(@Param("channelName") String channelName,@Param("ccName") String ccName);

}
