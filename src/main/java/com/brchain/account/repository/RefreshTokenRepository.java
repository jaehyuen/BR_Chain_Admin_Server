package com.brchain.account.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.brchain.account.entity.RefreshTokenEntity;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {
	Optional<RefreshTokenEntity> findByToken(String token);

	void deleteByToken(String token);
}