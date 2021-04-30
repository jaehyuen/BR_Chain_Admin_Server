package com.brchain.core.channel.repository.custom;

import java.util.List;

import org.springframework.data.repository.query.Param;

import com.brchain.core.channel.dto.ChannelSummaryDto;
import com.brchain.core.channel.entitiy.ChannelInfoPeerEntity;

public interface ChannelInfoCustomRepository {

	List<ChannelSummaryDto> findChannelSummary(@Param(value = "preMonth") String preMonth, @Param(value = "nowMonth") String nowMonth);


}
