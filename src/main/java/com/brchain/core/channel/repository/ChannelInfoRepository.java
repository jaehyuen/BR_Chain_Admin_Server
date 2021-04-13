package com.brchain.core.channel.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.brchain.core.channel.dto.ChannelSummaryDto;
import com.brchain.core.channel.entitiy.ChannelInfoEntity;

public interface ChannelInfoRepository extends JpaRepository<ChannelInfoEntity, String> {

	@Query(nativeQuery = true)
	List<ChannelSummaryDto> findChannelSummary(@Param(value = "preMonth") String preMonth, @Param(value = "nowMonth") String nowMonth);

}
