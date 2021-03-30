package com.brchain.core.chaincode.entitiy;

import java.time.LocalDateTime;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import org.hibernate.annotations.NamedNativeQuery;

import com.brchain.common.entity.BaseEntity;
import com.brchain.core.chaincode.dto.CcSummaryDto;
import com.brchain.core.container.entitiy.ConInfoEntity;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@SqlResultSetMapping(
        name="ChaincodeSummaryDtoMapping",
        classes = @ConstructorResult(
                targetClass = CcSummaryDto.class,
                columns = {
                        @ColumnResult(name="conName", type = String.class),
                        @ColumnResult(name="ccCnt", type = Integer.class),
                })
)


@NamedNativeQuery( resultSetMapping = "ChaincodeSummaryDtoMapping",
							  query = "SELECT c.CON_NAME AS conName,\n"
									+ "       (SELECT Count(*)\n"
									+ "        FROM   CCINFO_PEER\n"
									+ "        WHERE  c.CON_NAME = CONINFO_CON_NAME) AS ccCnt\n"
									+ "FROM   CONINFO c\n"
									+ "WHERE  c.CON_TYPE = 'peer';",
							   name = "CcInfoPeerEntity.findChaincodeSummary")

@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "CCINFO_PEER")
@NoArgsConstructor
public class CcInfoPeerEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ID")
	private Long          id;

	@Column(name = "CC_VERSION", nullable = false)
	private String        ccVersion;

	@ManyToOne(targetEntity = ConInfoEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "CONINFO_CON_NAME")
	private ConInfoEntity conInfoEntity;

	@ManyToOne(targetEntity = CcInfoEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "CCINFO_ID")
	private CcInfoEntity  ccInfoEntity;

	@Builder
	public CcInfoPeerEntity(Long id, String ccVersion, ConInfoEntity conInfoEntity, CcInfoEntity ccInfoEntity, LocalDateTime createdAt) {
		this.id            = id;
		this.ccVersion     = ccVersion;
		this.ccInfoEntity  = ccInfoEntity;
		this.conInfoEntity = conInfoEntity;
		super.setCreatedAt(createdAt);

	}
}
