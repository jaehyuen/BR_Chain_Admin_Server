package com.brchain.core.channel.entitiy;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.brchain.common.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
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

	@Builder
	public ChannelHandleEntity(String channelName, String handle, LocalDateTime createdAt) {
		this.channelName = channelName;
		this.handle = handle;
		super.setCreatedAt(createdAt);

	}
}
