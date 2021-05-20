package com.brchain.controller;

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

import com.brchain.account.dto.RegisterDto;
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
	void contextLoads() {
	}

	@Test
	public void 회원가입_테스트() throws Exception {

		System.out.println("************************ 회원가입_테스트 시작 ************************");
		RegisterDto registerDto = new RegisterDto();
		
		registerDto.setUserEmail("test@test.com");
		registerDto.setUserPassword("1111");
		registerDto.setUserName("테스트");
		registerDto.setUserId("testid");
		registerDto.setAdminYn(true);
		

		String      content     = objectMapper.writeValueAsString(registerDto);

		mvc.perform(post("/api/auth/register").content(content)
			.contentType(MediaType.APPLICATION_JSON))
			.andExpect(status().isOk());
	}

}
