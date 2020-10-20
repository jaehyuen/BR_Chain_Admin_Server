package com.brchain.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.brchain.common.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "CHANNEL_HANDLER")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelHandlerEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "HANDLER", nullable = false)
	private String handler;

	@OneToOne(targetEntity = ChannelInfoEntity.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "CHANNELINFO_CHANNEL_NAME")
	private ChannelInfoEntity channelInfoEntity;

}
