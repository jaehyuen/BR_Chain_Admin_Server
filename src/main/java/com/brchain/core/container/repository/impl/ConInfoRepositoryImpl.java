package com.brchain.core.container.repository.impl;

import java.util.List;

import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.transaction.annotation.Transactional;

import com.brchain.core.container.entitiy.ConInfoEntity;
import com.brchain.core.container.entitiy.QConInfoEntity;
import com.brchain.core.container.repository.custom.ConInfoCustomRepository;

@Transactional(readOnly = true)
public class ConInfoRepositoryImpl extends QuerydslRepositorySupport implements ConInfoCustomRepository {

	public ConInfoRepositoryImpl() {
		super(ConInfoEntity.class);
	}

	@Override
	public List<ConInfoEntity> findMemberByOrgName(String orgName) {
		final QConInfoEntity conInfoEntity = QConInfoEntity.conInfoEntity;

		return from(conInfoEntity).where(conInfoEntity.orgName.eq(orgName)
			.and((conInfoEntity.conType.eq("peer")
				.or(conInfoEntity.conType.eq("orderer")))))
			.fetch();
	}

}
