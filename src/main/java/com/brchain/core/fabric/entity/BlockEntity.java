package com.brchain.core.fabric.entity;

import java.time.LocalDateTime;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ColumnResult;
import javax.persistence.ConstructorResult;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SqlResultSetMapping;
import javax.persistence.Table;

import org.hibernate.annotations.NamedNativeQuery;

import com.brchain.common.entity.BaseEntity;
import com.brchain.core.channel.entitiy.ChannelInfoEntity;
import com.brchain.core.fabric.dto.BlockAndTxDto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@SqlResultSetMapping(
        name="BlockAndTxDtoMapping",
        classes = @ConstructorResult(
                targetClass = BlockAndTxDto.class,
                columns = {
                        @ColumnResult(name="blockDataHash", type = String.class),
                        @ColumnResult(name="blockNum", type = Integer.class),
                        @ColumnResult(name="txCount", type = Integer.class),
                        @ColumnResult(name="timestamp", type = Date.class),
                        @ColumnResult(name="prevDataHash", type = String.class),
                        @ColumnResult(name="txList", type = String.class),
                })
)


@NamedNativeQuery( resultSetMapping = "BlockAndTxDtoMapping",
							  query = "SELECT a.BLOCK_DATA_HASH     AS blockdatahash,\n"
							  		+ "       a.BLOCK_NUM           AS blocknum,\n"
							  		+ "       a.TX_COUNT            AS txcount,\n"
							  		+ "       a.`TIMESTAMP`         AS timestamp,\n"
							  		+ "       a.PREV_DATA_HASH      AS prevdatahash,\n"
							  		+ "       Group_concat(b.TX_ID) AS txList\n"
							  		+ "FROM   BLOCK a\n"
							  		+ "       JOIN `TRANSACTION` b\n"
							  		+ "         ON a.BLOCK_DATA_HASH = b.BLOCK_BLOCK_DATA_HASH\n"
							  		+ "WHERE  a.CHANNELINFO_CHANNEL_NAME = :channelName\n"
							  		+ "GROUP  BY b.BLOCK_BLOCK_DATA_HASH\n"
							  		+ "ORDER  BY a.BLOCK_NUM DESC;",
							   name = "BlockEntity.findByChannelName")



@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "BLOCK")
@NoArgsConstructor
public class BlockEntity extends BaseEntity {

	@Id
	@Column(name = "BLOCK_DATA_HASH", nullable = false)
	private String            blockDataHash;

	@Column(name = "BLOCK_NUM", nullable = false)
	private int               blockNum;

	@Column(name = "TX_COUNT", nullable = false)
	private int               txCount;

	@Column(name = "TIMESTAMP", nullable = false)
	private Date              timestamp;

	@Column(name = "PREV_DATA_HASH", nullable = false)
	private String            prevDataHash;

	@ManyToOne(targetEntity = ChannelInfoEntity.class, fetch = FetchType.EAGER, cascade = CascadeType.REMOVE)
	@JoinColumn(name = "CHANNELINFO_CHANNEL_NAME")
	private ChannelInfoEntity channelInfoEntity;

	@Builder
	public BlockEntity(String blockDataHash, int blockNum, int txCount, Date timestamp, String prevDataHash, ChannelInfoEntity channelInfoEntity, LocalDateTime createdAt) {
		this.blockDataHash     = blockDataHash;
		this.blockNum          = blockNum;
		this.txCount           = txCount;
		this.timestamp         = timestamp;
		this.prevDataHash      = prevDataHash;
		this.channelInfoEntity = channelInfoEntity;
		super.setCreatedAt(createdAt);

	}
}
