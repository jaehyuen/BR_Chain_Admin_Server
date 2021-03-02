package com.brchain.core.entity.channel;

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
import com.brchain.core.entity.ConInfoEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "CHANNELINFO_PEER")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelInfoPeerEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "ANCHOR_YN", nullable = false)
	private boolean anchorYn;

	@ManyToOne(targetEntity = ChannelInfoEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "CHANNELINFO_CHANNEL_NAME")
	private ChannelInfoEntity channelInfoEntity;

	@ManyToOne(targetEntity = ConInfoEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "CONINFO_CON_NAME")
	private ConInfoEntity conInfoEntity;

	@Builder
	public ChannelInfoPeerEntity(Long id, boolean anchorYn, ChannelInfoEntity channelInfoEntity,
			ConInfoEntity conInfoEntity, LocalDateTime createdAt) {

		this.id = id;
		this.anchorYn = anchorYn;
		this.channelInfoEntity = channelInfoEntity;
		this.conInfoEntity = conInfoEntity;
		super.setCreatedAt(createdAt);

	}
}
