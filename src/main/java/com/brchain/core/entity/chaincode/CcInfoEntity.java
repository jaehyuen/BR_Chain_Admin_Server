package com.brchain.core.entity.chaincode;

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
@Table(name = "CCINFO")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CcInfoEntity extends BaseEntity {

	@Id
	@Column(name = "CC_NAME", nullable = false)
	private String ccName;

	@Column(name = "CC_PATH", nullable = false)
	private String ccPath;

	@Column(name = "CC_LANG", nullable = false)
	private String ccLang;

	@Column(name = "CC_DESC", nullable = false)
	private String ccDesc;

	@Column(name = "CC_VERSION", nullable = false)
	private String ccVersion;

	@Builder
	public CcInfoEntity(String ccName, String ccPath, String ccLang, String ccDesc, String ccVersion,
			LocalDateTime createdAt) {
		this.ccName = ccName;
		this.ccPath = ccPath;
		this.ccLang = ccLang;
		this.ccDesc = ccDesc;
		this.ccVersion = ccVersion;
		super.setCreatedAt(createdAt);

	}
}
