package com.brchain.core.entity;

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
public class CcInfoPeerEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long id;

	@Column(name = "CC_VERSION", nullable = false)
	private String ccVersion;

	@ManyToOne(targetEntity = ConInfoEntity.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "CONINFO_CON_NAME")
	private ConInfoEntity conInfoEntity;

	@ManyToOne(targetEntity = CcInfoEntity.class, fetch = FetchType.EAGER)
	@JoinColumn(name = "CCINFO_CC_NAME")
	private CcInfoEntity ccInfoEntity;

}
