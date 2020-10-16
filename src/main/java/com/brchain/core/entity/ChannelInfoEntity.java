package com.brchain.core.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.brchain.common.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import lombok.NoArgsConstructor;


@Data
@Entity
@Table(name = "CHANNELINFO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelInfoEntity extends BaseEntity {


    @Id
    @Column(name ="CHANNEL_NAME" ,length = 100, nullable = false)
	private String channelName;
    
    @Column(name ="CHANNEL_BLOCK" ,length = 100, nullable = false)
	private int channelBlock;
    
    @Column(name ="CHANNEL_TX" ,length = 100, nullable = false)
	private int channelTx;;
	
    @Column(name ="ORDERING_ORG" ,length = 100, nullable = false)
	private String orderingOrg;;
	
    @Column(name ="ACTIVE_CC" ,length = 100, nullable = true)
    private String activeCc;

    
 
}
