package com.brchain.core.fabric.repository.custom;

public interface TransactionCustomRepository {
	long countByChannelName(String channelName);
}
