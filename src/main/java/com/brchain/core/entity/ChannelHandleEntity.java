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
@Table(name = "CHANNEL_HANDLE")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelHandleEntity extends BaseEntity {

	@Id
	@Column(name = "CHANNEL_NAME", nullable = false)
	private String channelName;


	@Column(name = "HANDLE", nullable = false)
	private String handle;

	
}
