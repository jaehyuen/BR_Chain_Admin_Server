package com.brchain.controller;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.brchain.account.dto.LoginDto;
import com.brchain.account.dto.RefreshTokenDto;
import com.brchain.account.dto.RegisterDto;
import com.brchain.account.dto.TokenDto;
import com.brchain.common.dto.ResultDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@AutoConfigureMockMvc
@SpringBootTest
@RunWith(SpringRunner.class)
@Transactional
//@WebMvcTest(controllers =AuthController.class)
class AuthControllerTest {
	@Autowired
	private MockMvc      mvc;

	@Autowired
	private ObjectMapper objectMapper;

	@Test
	public void 회원가입_테스트() throws Exception {

		System.out.println("************************ 회원가입_테스트 시작 ************************");
		MvcResult         mvcResult = 회원가입();

		ResultDto<String> result    = objectMapper.readValue(mvcResult.getResponse()
			.getContentAsString(), new TypeReference<ResultDto<String>>() {
			});

		System.out.println("resultDto : " + result);

		assertTrue(result.getResultCode()
			.equals("0000"));

		System.out.println("************************ 회원가입_테스트 종료 ************************");

	}

	@Test
	public void 로그인_테스트() throws Exception {

		System.out.println("************************ 로그인_테스트 시작 ************************");

		MvcResult           mvcResult = 로그인();

		ResultDto<TokenDto> result    = objectMapper.readValue(mvcResult.getResponse()
			.getContentAsString(), new TypeReference<ResultDto<TokenDto>>() {
			});

		System.out.println("resultDto : " + result);

		assertTrue(result.getResultCode()
			.equals("0000"));

		System.out.println("************************ 로그인_테스트 종료 ************************");

	}

	@Test
	public void 토큰_재발급_테스트() throws Exception {

		System.out.println("************************ 토큰_재발급_테스트 시작 ************************");

		MvcResult           mvcResult = 로그인();

		ResultDto<TokenDto> result    = objectMapper.readValue(mvcResult.getResponse()
			.getContentAsString(), new TypeReference<ResultDto<TokenDto>>() {
			});

		assertTrue(result.getResultCode()
			.equals("0000"));

		RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
		refreshTokenDto.setRefreshToken(result.getResultData()
			.getRefreshToken());
		refreshTokenDto.setUserId((result.getResultData()
			.getUserId()));

		String content = objectMapper.writeValueAsString(refreshTokenDto);

		mvcResult = mvc.perform(post("/api/auth/refresh").content(content)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		result    = objectMapper.readValue(mvcResult.getResponse()
			.getContentAsString(), new TypeReference<ResultDto<TokenDto>>() {
			});

		assertTrue(result.getResultCode()
			.equals("0000"));
		System.out.println("resultDto : " + result);
		System.out.println("************************ 토큰_재발급_테스트 종료 ************************");

	}

	@Test
	public void 로그아웃_테스트() throws Exception {

		System.out.println("************************ 로그아웃_테스트 시작 ************************");

		MvcResult           mvcResult = 로그인();

		ResultDto<TokenDto> result    = objectMapper.readValue(mvcResult.getResponse()
			.getContentAsString(), new TypeReference<ResultDto<TokenDto>>() {
			});

		assertTrue(result.getResultCode()
			.equals("0000"));

		RefreshTokenDto refreshTokenDto = new RefreshTokenDto();
		refreshTokenDto.setRefreshToken(result.getResultData()
			.getRefreshToken());
		refreshTokenDto.setUserId((result.getResultData()
			.getUserId()));

		String content = objectMapper.writeValueAsString(refreshTokenDto);

		mvcResult = mvc.perform(post("/api/auth/logout").content(content)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

		result    = objectMapper.readValue(mvcResult.getResponse()
			.getContentAsString(), new TypeReference<ResultDto<TokenDto>>() {
			});
		
		System.out.println("resultDto : " + result);
		
		assertTrue(result.getResultCode()
			.equals("0000"));

		System.out.println("************************ 로그아웃_테스트 종료 ************************");

	}

	private MvcResult 회원가입() throws Exception {

		RegisterDto registerDto = new RegisterDto();

		registerDto.setUserEmail("test@test.com");
		registerDto.setUserPassword("1111");
		registerDto.setUserName("테스트");
		registerDto.setUserId("testid");
		registerDto.setAdminYn(true);

		String content = objectMapper.writeValueAsString(registerDto);

		return mvc.perform(post("/api/auth/register").content(content)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();
	}

	private MvcResult 로그인() throws Exception {

		회원가입();
		LoginDto loginDto = new LoginDto();

		loginDto.setUserPassword("1111");
		loginDto.setUserId("testid");
		String content = objectMapper.writeValueAsString(loginDto);
		return mvc.perform(post("/api/auth/login").content(content)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk())
			.andReturn();

	}

}
