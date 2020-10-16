package com.brchain.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.brchain.common.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;


@Data
@Entity
@Table(name = "CHANNELINFO_PEER")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelInfoPeerEntity extends BaseEntity {



    
	@Id
	@GeneratedValue( strategy = GenerationType.IDENTITY)
	@Column(name ="ID")
	private Long id;
	
    @Column(name ="CHANNEL_NAME" , nullable = false)
	private String channelName;
    
    @Column(name ="ANCHOR_YN" , nullable = false)
	private boolean anchorYn;
    
    @Column(name ="CON_NAME" , nullable = false)
	private String conName;
    
	@Column(name ="CON_NUM" , nullable = false)
	private int conNum;
	
	@Column(name ="ORG_NAME" , nullable = false)
	private String orgName;
    
 
}
