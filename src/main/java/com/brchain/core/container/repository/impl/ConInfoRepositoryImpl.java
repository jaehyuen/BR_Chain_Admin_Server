package com.brchain.core.container.repository.impl;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.container.entitiy.QConInfoEntity;
import com.brchain.core.container.repository.custom.ConInfoCustomRepository;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;

@Transactional(readOnly = true)
public class ConInfoRepositoryImpl extends QuerydslRepositorySupport implements ConInfoCustomRepository {

	final QConInfoEntity conInfoEntity = QConInfoEntity.conInfoEntity;

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
	public List<ConInfoEntity> findByTest(String conType, String orgType, String orgName) {

		return from(conInfoEntity).where(eqConType(conType), eqOrgType(orgType), eqOrgName(orgName))
			.fetch();
	}

	@Override
	public boolean portCheck(String conPort) {

		return from(conInfoEntity).where(conInfoEntity.conPort.eq(conPort)
			.and(conInfoEntity.conType.notLike("setup")))
			.fetchFirst() != null;
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
