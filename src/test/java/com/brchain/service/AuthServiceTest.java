package com.brchain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.brchain.account.dto.LoginDto;
import com.brchain.account.dto.RegisterDto;
import com.brchain.account.dto.TokenDto;
import com.brchain.account.entity.RefreshTokenEntity;
import com.brchain.account.repository.RefreshTokenRepository;
import com.brchain.account.repository.UserRepository;
import com.brchain.account.service.AuthService;
import com.brchain.account.service.RefreshTokenService;
import com.brchain.account.service.UserDetailsServiceImpl;
import com.brchain.common.dto.ResultDto;
import com.brchain.common.security.JwtProvider;
import com.brchain.core.util.Util;

//@SpringBootTest
//@RunWith(SpringRunner.class)
@ExtendWith(MockitoExtension.class)
@Transactional
//@RequiredArgsConstructor
class AuthServiceTest {

	@Mock
	private Util                   util;
	@InjectMocks
	private AuthService            authService;

	@Mock
	private PasswordEncoder        passwordEncoder;

	@Mock
	private UserRepository         userRepository;
	@Mock
	private AuthenticationManager  authenticationManager;
	@Mock
	private JwtProvider            jwtProvider;
	@Mock
	private RefreshTokenService    refreshTokenService;
	@Mock
	private RefreshTokenRepository refreshTokenRepository;
	@Mock
	private UserDetailsServiceImpl userDetailsService;

	@Test
	public void 회원가입_서비스_테스트() throws Exception {

		System.out.println("************************ 회원가입_서비스_테스트 시작 ************************");

		// given
		ResultDto<Object> resultDto = new ResultDto<Object>();
		resultDto.setResultCode("0000");
		resultDto.setResultData(null);
		resultDto.setResultFlag(true);
		resultDto.setResultMessage("Success register");

		when(util.setResult("0000", true, "Success register", null)).thenReturn(resultDto);

		RegisterDto registerDto = new RegisterDto();

		registerDto.setUserEmail("test@test.com");
		registerDto.setUserPassword("1111");
		registerDto.setUserName("테스트");
		registerDto.setUserId("testid");
		registerDto.setAdminYn(true);

		// when
		ResultDto<String> result = authService.register(registerDto);
		System.out.println("resultDto : " + result);

		// then
		assertThat(result.getResultCode()).isEqualTo("0000");

		System.out.println("************************ 회원가입_서비스_테스트 종료 ************************");

	}

	@Test
	public void 로그인_서비스_테스트() throws Exception {


	}

}
