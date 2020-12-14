package com.brchain.core.entity.channel;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.brchain.common.entity.BaseEntity;
import com.brchain.core.entity.BlockEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "CHANNELINFO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelInfoEntity extends BaseEntity {

	@Id
	@Column(name = "CHANNEL_NAME", nullable = false)
	private String channelName;

	@Column(name = "CHANNEL_BLOCK", nullable = false)
	private int channelBlock;

	@Column(name = "CHANNEL_TX", nullable = false)
	private int channelTx;

	@Column(name = "ORDERING_ORG", nullable = false)
	private String orderingOrg;

	@Column(name = "APP_ADMIN_POLICY_TYPE", nullable = false)
	private String appAdminPolicyType;

	@Column(name = "APP_ADMIN_POLICY_VALUE", nullable = false)
	private String appAdminPolicyValue;

	@Column(name = "ORDERER_ADMIN_POLICY_TYPE", nullable = false)
	private String ordererAdminPolicyType;

	@Column(name = "ORDERER_ADMIN_POLICY_VALUE", nullable = false)
	private String ordererAdminPolicyValue;

	@Column(name = "CHANNEL_ADMIN_POLICY_TYPE", nullable = false)
	private String channelAdminPolicyType;

	@Column(name = "CHANNEL_ADMIN_POLICY_VALUE", nullable = false)
	private String channelAdminPolicyValue;

	@Column(name = "BATCH_TIMEOUT", nullable = false)
	private String batchTimeout;

	@Column(name = "BATCH_SIZE_ABSOL_MAX", nullable = false)
	private long batchSizeAbsolMax;

	@Column(name = "BATCH_SIZE_MAX_MSG", nullable = false)
	private long batchSizeMaxMsg;

	@Column(name = "BATCH_SIZE_PREFER_MAX", nullable = false)
	private long batchSizePreferMax;

	@Builder
	public ChannelInfoEntity(String channelName, int channelBlock, int channelTx, String orderingOrg,
			String appAdminPolicyType, String appAdminPolicyValue, String ordererAdminPolicyType,
			String ordererAdminPolicyValue, String channelAdminPolicyType, String channelAdminPolicyValue,
			String batchTimeout, long batchSizeAbsolMax, long batchSizeMaxMsg, long batchSizePreferMax,
			LocalDateTime createdAt) {

		this.channelName = channelName;
		this.channelBlock = channelBlock;
		this.channelTx = channelTx;
		this.orderingOrg = orderingOrg;
		this.appAdminPolicyType = appAdminPolicyType;
		this.appAdminPolicyValue = appAdminPolicyValue;
		this.ordererAdminPolicyType = ordererAdminPolicyType;
		this.ordererAdminPolicyValue = ordererAdminPolicyValue;
		this.channelAdminPolicyType = channelAdminPolicyType;
		this.channelAdminPolicyValue = channelAdminPolicyValue;
		this.batchTimeout = batchTimeout;
		this.batchSizeAbsolMax = batchSizeAbsolMax;
		this.batchSizeMaxMsg = batchSizeMaxMsg;
		this.batchSizePreferMax = batchSizePreferMax;
		super.setCreatedAt(createdAt);

	}
}
