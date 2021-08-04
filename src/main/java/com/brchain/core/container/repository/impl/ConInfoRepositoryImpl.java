package com.brchain.core.container.repository.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.brchain.core.channel.entitiy.QChannelInfoPeerEntity;
import com.brchain.core.container.dto.OrgInfoDto;
import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.container.entitiy.QConInfoEntity;
import com.brchain.core.container.repository.custom.ConInfoCustomRepository;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;

@Transactional(readOnly = true)
public class ConInfoRepositoryImpl extends QuerydslRepositorySupport implements ConInfoCustomRepository {

	final QConInfoEntity         conInfoEntity         = QConInfoEntity.conInfoEntity;
	final QConInfoEntity         c                     = new QConInfoEntity("c");
	final QChannelInfoPeerEntity channelInfoPeerEntity = QChannelInfoPeerEntity.channelInfoPeerEntity;

	public ConInfoRepositoryImpl() {
		super(ConInfoEntity.class);
	}

	@Override
	public List<ConInfoEntity> findMemberByOrgName(String orgName) {

		return from(conInfoEntity).where(conInfoEntity.orgName.eq(orgName)
			.and((conInfoEntity.conType.eq("peer")
				.or(conInfoEntity.conType.eq("orderer")))))
			.fetch();
	}

	@Override
	public Optional<ConInfoEntity> findCaInfoByOrgName(String orgName) {

		return Optional.ofNullable(from(conInfoEntity).where(eqConType("ca"), eqOrgName(orgName))
			.fetchOne());
	}
	
	@Override
	public List<ConInfoEntity> findByConTypeAndOrgTypeAndOrgName(String conType, String orgType, String orgName) {

		return from(conInfoEntity).where(eqConType(conType), eqOrgType(orgType), eqOrgName(orgName))
			.fetch();
	}

	@Override
	public boolean portCheck(String conPort) {

		return from(conInfoEntity).where(conInfoEntity.conPort.eq(conPort)
			.and(conInfoEntity.conType.notLike("setup")))
			.fetchFirst() != null;
	}

	@Override
	public List<String> findOrgsByChannelName(String channelName) {

		return from(conInfoEntity).select(conInfoEntity.orgName)
			.join(channelInfoPeerEntity)
			.on(conInfoEntity.conName.eq(channelInfoPeerEntity.conInfoEntity.conName))
			.where(channelInfoPeerEntity.channelInfoEntity.channelName.eq(channelName))
			.groupBy(conInfoEntity.orgName)
			.fetch();
	}

	@Override
	public List<OrgInfoDto> findOrgInfo(String orgType) {

		return from(conInfoEntity)
			.select(Projections.constructor(OrgInfoDto.class, conInfoEntity.orgName, conInfoEntity.orgType,
					ExpressionUtils.as(JPAExpressions.select(c.count())
						.from(c)
						.where(conInfoEntity.orgName.eq(c.orgName)
							.and(c.conType.in(createTypeList(orgType)))), "memberCnt")))
			.where(conInfoEntity.orgType.in(createTypeList(orgType)))
			.groupBy(conInfoEntity.orgName)
			.fetch();
	}

//	@Override
//	public String findAllOrgs() {
//
//		return from(conInfoEntity).distinct()
//			.select(Expressions.stringTemplate("group_concat(DISTINCT {0} SEPARATOR ' ' )", conInfoEntity.orgName))
//			.where(conInfoEntity.conType.eq("ca"))
//			.fetch()
//			.get(0);
//	}

	private List<String> createTypeList(String type) {

		List<String> result = new ArrayList<String>();

		result.add("peer");
		result.add("orderer");

		if (StringUtils.isEmpty(type)) {
			return result;
		} else if (type.equals("peer")) {
			result.remove("orderer");
			return result;
		} else if (type.equals("orderer")) {
			result.remove("peer");
			return result;
		} else {
			return null;
		}

	}

	private BooleanExpression eqConType(String conType) {
		if (StringUtils.isEmpty(conType)) {
			return null;
		}
		return conInfoEntity.conType.eq(conType);
	}

	private BooleanExpression eqOrgType(String orgType) {
		if (StringUtils.isEmpty(orgType)) {
			return null;
		}
		return conInfoEntity.orgType.eq(orgType);
	}

	private BooleanExpression eqOrgName(String orgName) {
		if (StringUtils.isEmpty(orgName)) {
			return null;
		}
		return conInfoEntity.orgName.eq(orgName);
	}

}
