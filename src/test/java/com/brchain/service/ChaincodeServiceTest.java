//package com.brchain.service;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.mockito.Mockito.when;
//
//import java.time.LocalDateTime;
//import java.util.Optional;
//
//import javax.transaction.Transactional;
//
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import com.brchain.core.chaincode.dto.CcInfoDto;
//import com.brchain.core.chaincode.entitiy.CcInfoEntity;
//import com.brchain.core.chaincode.repository.CcInfoChannelRepository;
//import com.brchain.core.chaincode.repository.CcInfoPeerRepository;
//import com.brchain.core.chaincode.repository.CcInfoRepository;
//import com.brchain.core.chaincode.service.ChaincodeService;
//import com.brchain.core.channel.service.ChannelService;
//import com.brchain.core.container.service.ContainerService;
//import com.brchain.core.util.Util;
//
//@ExtendWith(MockitoExtension.class)
//@Transactional
//class ChaincodeServiceTest {
//
//	@InjectMocks
//	private ChaincodeService        chaincodeService;
//
//	@Mock
//	private CcInfoRepository        ccInfoRepository;
//	@Mock
//	private CcInfoPeerRepository    ccInfoPeerRepository;
//	@Mock
//	private CcInfoChannelRepository ccInfoChannelRepository;
//
//	// 서비스
//	@Mock
//	private ContainerService        containerService;
//	@Mock
//	private ChannelService          channelService;
//
//	@Mock
//	private Util                    util;
//
//	@Test
//	public void 체인코드_정보_저장_서비스_테스트() throws Exception {
//
//		System.out.println("************************ 체인코드_정보_저장_서비스_테스트 시작************************");
//
//		// given
//		CcInfoEntity ccInfoEntity = createCcInfoEntity();
//		CcInfoDto    ccInfoDto    = createCcInfoDto();
//
//		when(ccInfoRepository.save(ccInfoEntity)).thenReturn(ccInfoEntity);
//		when(util.toDto(ccInfoEntity)).thenReturn(ccInfoDto);
//		when(util.toEntity(ccInfoDto)).thenReturn(ccInfoEntity);
//
//		// when
//		CcInfoDto result = chaincodeService.saveCcInfo(ccInfoDto);
//		System.out.println("CcInfoDto : " + result);
//
//		// then
////		assertThat(result.getResultCode()).isEqualTo("0000");
//
//		System.out.println("************************ 체인코드_정보_저장_서비스_테스트 종료 ************************");
//
//	}
//
//	@Test
//	public void 체인코드_정보_조회_서비스_테스트() throws Exception {
//
//		System.out.println("************************ 체인코드_정보_조회_서비스_테스트 시작 ************************");
//
//		// given
//		CcInfoEntity ccInfoEntity = createCcInfoEntity();
//		CcInfoDto    ccInfoDto    = createCcInfoDto();
//
//		when(util.toDto(ccInfoEntity)).thenReturn(ccInfoDto);
//		when(ccInfoRepository.findById(1L)).thenReturn(Optional.ofNullable(ccInfoEntity));
//
//		// when
//
//		CcInfoDto result = chaincodeService.findCcInfoById(1L);
//		System.out.println("CcInfoDto : " + result);
//
//		// then
////		assertThat(result.getId()).is(1L);
//
//		System.out.println("************************ 체인코드_정보_조회_서비스_테스트 종료 ************************");
//
//	}
//
//	@Test
//	public void 로그인_서비스_테스트() throws Exception {
//
//	}
//
//	private CcInfoDto createCcInfoDto() {
//
//		CcInfoDto ccInfoDto = new CcInfoDto();
//
//		ccInfoDto.setId(1L);
//		ccInfoDto.setCcName("test-chaincode");
//		ccInfoDto.setCcPath("/src/test/chaincode/test.go");
//		ccInfoDto.setCcLang("golang");
//		ccInfoDto.setCcDesc("this is test chaincode");
//		ccInfoDto.setCcVersion("1");
//		ccInfoDto.setCreatedAt(LocalDateTime.now());
//
//		return ccInfoDto;
//
//	}
//
//	private CcInfoEntity createCcInfoEntity() {
//
//		CcInfoEntity ccInfoEntity = new CcInfoEntity();
//
//		ccInfoEntity.setId(1L);
//		ccInfoEntity.setCcName("test-chaincode");
//		ccInfoEntity.setCcPath("/src/test/chaincode/test.go");
//		ccInfoEntity.setCcLang("golang");
//		ccInfoEntity.setCcDesc("this is test chaincode");
//		ccInfoEntity.setCcVersion("1");
//		ccInfoEntity.setCreatedAt(LocalDateTime.now());
//
//		return ccInfoEntity;
//
//	}
//
//}
