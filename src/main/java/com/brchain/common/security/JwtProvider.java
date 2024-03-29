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
import com.brchain.core.util.BrchainStatusCode;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

@Service
public class JwtProvider {

	private KeyStore   keyStore;
	private final Long jwtExpirationInMillis = (long) (1000	* 3600); 


	@PostConstruct
	public void init() {
		try {
			keyStore = KeyStore.getInstance("JKS");
			InputStream resourceAsStream = getClass().getResourceAsStream("/brchain.jks");
			keyStore.load(resourceAsStream, "Asdf!234".toCharArray());
		} catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
			throw new BrchainException(e, BrchainStatusCode.JWT_ERROR);
		}
	}

	public String generateToken(Authentication authentication) {
		User principal = (User) authentication.getPrincipal();
		return Jwts.builder()
			.setSubject(principal.getUsername())
			.setIssuedAt(from(Instant.now()))
			.signWith(getPrivateKey())
			.setExpiration(Date.from(Instant.now()
				.plusMillis(jwtExpirationInMillis)))
			.compact();
	}

	public String generateTokenWithUserName(String userName) {
		return Jwts.builder()
			.setSubject(userName)
			.setIssuedAt(from(Instant.now()))
			.signWith(getPrivateKey())
			.setExpiration(Date.from(Instant.now()
				.plusMillis(jwtExpirationInMillis)))
			.compact();
	}

	private PrivateKey getPrivateKey() {
		try {
			return (PrivateKey) keyStore.getKey("brchain", "Asdf!234".toCharArray());
		} catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
			throw new BrchainException(e, BrchainStatusCode.JWT_ERROR);
		}
	}

	public boolean validateToken(String token) {

		try {
			Jwts.parserBuilder()
			.setSigningKey(getPublicKey())
			.build()
			.parseClaimsJws(token);
			return true;
		} catch (Exception e) {

			return false;
		}

	}

	private PublicKey getPublicKey() {
		try {
			return keyStore.getCertificate("brchain")
				.getPublicKey();
		} catch (KeyStoreException e) {
			throw new BrchainException(e, BrchainStatusCode.JWT_ERROR);
		}
	}

	public String getUsernameFromJwt(String token) {
		
		Jws<Claims> jwt    = Jwts.parserBuilder()
			.setSigningKey(getPublicKey())
			.build()
			.parseClaimsJws(token);

		Claims      claims = jwt.getBody();

		return claims.getSubject();
	}

	public Long getJwtExpirationInMillis() {
		return jwtExpirationInMillis;
	}
}
