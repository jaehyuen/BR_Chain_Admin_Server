package com.brchain.common.security;

import static io.jsonwebtoken.Jwts.parser;
import static java.util.Date.from;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Date;
import java.time.Instant;

import javax.annotation.PostConstruct;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

import com.brchain.common.exception.BrchainException;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

@Service
public class JwtProvider {

	private KeyStore keyStore;
	private final Long jwtExpirationInMillis = (long) (1000 * 3600); //테스트
//	private final Long jwtExpirationInMillis = (long) (1000);

	@PostConstruct
	public void init() {
		try {
			keyStore = KeyStore.getInstance("JKS");
			InputStream resourceAsStream = getClass().getResourceAsStream("/brchain.jks");
			keyStore.load(resourceAsStream, "Asdf!234".toCharArray());
		} catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
			throw new BrchainException("Exception occurred while loading keystore", e);
		}
	}

	public String generateToken(Authentication authentication) {
		User principal = (User) authentication.getPrincipal();
		return Jwts.builder().setSubject(principal.getUsername()).setIssuedAt(from(Instant.now()))
				.signWith(getPrivateKey()).setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationInMillis)))
				.compact();
	}

	public String generateTokenWithUserName(String userName) {
		return Jwts.builder().setSubject(userName).setIssuedAt(from(Instant.now())).signWith(getPrivateKey())
				.setExpiration(Date.from(Instant.now().plusMillis(jwtExpirationInMillis))).compact();
	}

	private PrivateKey getPrivateKey() {
		try {
			return (PrivateKey) keyStore.getKey("brchain", "Asdf!234".toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
			throw new BrchainException("Exception occured while retrieving public key from keystore", e);
		}
	}

	public boolean validateToken(String jwt) {

		try {
			parser().setSigningKey(getPublickey()).parseClaimsJws(jwt);
			return true;
		} catch (Exception e) {

			return false;
		}

	}

	private PublicKey getPublickey() {
		try {
			return keyStore.getCertificate("brchain").getPublicKey();
		} catch (KeyStoreException e) {
			throw new BrchainException("Exception occured while retrieving public key from keystore", e);
		}
	}

	public String getUsernameFromJwt(String token) {
		Claims claims = parser().setSigningKey(getPublickey()).parseClaimsJws(token).getBody();

		return claims.getSubject();
	}

	public Long getJwtExpirationInMillis() {
		return jwtExpirationInMillis;
	}
}
