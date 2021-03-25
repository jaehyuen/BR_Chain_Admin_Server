package com.brchain.core.chaincode.entitiy;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.brchain.common.entity.BaseEntity;
import com.brchain.core.channel.entitiy.ChannelInfoEntity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "CCINFO_CHANNEL")
@NoArgsConstructor
//@AllArgsConstructor
//@Builder
public class CcInfoChannelEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "CC_VERSION", nullable = false)
	private String ccVersion;

	@ManyToOne(targetEntity = ChannelInfoEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "CHANNELINFO_CHANNEL_NAME")
	private ChannelInfoEntity channelInfoEntity;

	@ManyToOne(targetEntity = CcInfoEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "CCINFO_CC_ID")
	private CcInfoEntity ccInfoEntity;

	@Builder
	public CcInfoChannelEntity(Long id, String ccVersion, ChannelInfoEntity channelInfoEntity,
			CcInfoEntity ccInfoEntity, LocalDateTime createdAt) {
		this.id = id;
		this.ccVersion = ccVersion;
		this.channelInfoEntity = channelInfoEntity;
		this.ccInfoEntity = ccInfoEntity;
		super.setCreatedAt(createdAt);

	}

}
