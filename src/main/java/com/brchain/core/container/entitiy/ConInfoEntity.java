package com.brchain.core.container.entitiy;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.brchain.common.entity.BaseEntity;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "CONINFO")
@NoArgsConstructor
public class ConInfoEntity extends BaseEntity {

	@Id
	@Column(name = "CON_NAME", nullable = false, unique = true)
	private String  conName;

	@Column(name = "CON_ID", nullable = false, unique = true)
	private String  conId;

	@Column(name = "CON_TYPE", nullable = false)
	private String  conType;;

	@Column(name = "CON_NUM", nullable = true)
	private int     conNum;

	@Column(name = "CON_CNT", nullable = true)
	private int     conCnt;

	@Column(name = "CON_PORT", nullable = true)
	private String  conPort;

	@Column(name = "ORG_NAME", nullable = false, unique = true)
	private String  orgName;

	@Column(name = "ORG_TYPE", nullable = false)
	private String  orgType;

	@Column(name = "CONSO_ORGS", nullable = true)
	private String  consoOrgs;

	@Column(name = "COUCHDB_YN", nullable = true)
	private boolean couchdbYn;

	@Column(name = "GOSSIP_BOOT_ADDR", nullable = true, columnDefinition = "LONGTEXT")
	private String  gossipBootAddr;

	@Column(name = "ORDERER_PORTS", nullable = true)
	private String  ordererPorts;

	@Builder
	public ConInfoEntity(String conName, String conId, String conType, int conNum, int conCnt, String conPort,
			String orgName, String orgType, String consoOrgs, boolean couchdbYn, String gossipBootAddr,
			String ordererPorts, LocalDateTime createdAt) {

		this.conName        = conName;
		this.conId          = conId;
		this.conType        = conType;
		this.conNum         = conNum;
		this.conCnt         = conCnt;
		this.conPort        = conPort;
		this.orgName        = orgName;
		this.orgType        = orgType;
		this.consoOrgs      = consoOrgs;
		this.couchdbYn      = couchdbYn;
		this.gossipBootAddr = gossipBootAddr;
		this.ordererPorts   = ordererPorts;
		super.setCreatedAt(createdAt);

	}

}
