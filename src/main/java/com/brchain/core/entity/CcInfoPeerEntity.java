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
@Table(name = "CCINFO_PEER")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CcInfoPeerEntity extends BaseEntity{


	@Id
	@GeneratedValue( strategy = GenerationType.IDENTITY)
	@Column(name ="ID")
	private Long id;
	
    @Column(name ="CC_NAME" , nullable = false)
	private String ccName;
    
    @Column(name ="CC_VERSION" , nullable = false)
	private String ccVersion;
    
    @Column(name ="CC_LANG" , nullable = false)
	private String ccLang;
    
    @Column(name ="CON_NAME" , nullable = false)
	private String conName;
    
	@Column(name ="CON_NUM" , nullable = false)
	private int conNum;
	
	@Column(name ="ORG_NAME" , nullable = false)
	private String orgName;

	

    
 
}
