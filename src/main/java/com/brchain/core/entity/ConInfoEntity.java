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
@Table(name = "CONINFO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConInfoEntity extends BaseEntity {


    @Id
    @Column(name ="CON_ID" ,length = 100, nullable = false)
	private String conId;
    
    @Column(name ="CON_NAME" ,length = 100, nullable = false)
	private String conName;
    
    @Column(name ="CON_TYPE" ,length = 100, nullable = false)
	private String conType;;
	
	@Column(name ="CON_NUM" ,length = 100, nullable = true)
	private int conNum;
	
	@Column(name ="CON_CNT" ,length = 100, nullable = true)
	private int conCnt;
	
	@Column(name ="CON_PORT" ,length = 100, nullable = true)
	private String conPort;
	
	@Column(name ="ORG_NAME" ,length = 100, nullable = false)
	private String orgName;
	
	@Column(name ="ORG_TYPE" ,length = 100, nullable = false)
	private String orgType;
	
	@Column(name ="CONSO_ORGS" ,length = 100, nullable = true)
	private String consoOrgs;
	
	@Column(name ="COUCHDB_YN" ,length = 100, nullable = true)
	private boolean couchdbYn;
	
	@Column(name ="GOSSIP_BOOT_ADDR" , nullable = true)
	private String gossipBootAddr;
	
	@Column(name ="ORDERER_PORTS" ,length = 100, nullable = true)
	private String ordererPorts;


    
 
}
