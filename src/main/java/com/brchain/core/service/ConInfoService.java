package com.brchain.core.service;

import java.util.ArrayList;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.brchain.core.dto.ConInfoDto;
import com.brchain.core.dto.FabricMemberDto;
import com.brchain.core.entity.ConInfoEntity;
import com.brchain.core.repository.ConInfoRepository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
@Service
public class ConInfoService {

	@NonNull
	private ConInfoRepository conInfoRepository;
	
	@Value("${brchain.ip}")
	String ip;
	

	public ConInfoEntity saveConInfo(ConInfoDto conInfoDto) {

		return conInfoRepository.save(conInfoDto.toEntity());
		
	}

	public String removeConInfo(String conId) {

		Optional<ConInfoEntity> conInfoEntityWrapper = conInfoRepository.findById(conId);
		ConInfoEntity conInfoEntity = conInfoEntityWrapper.get();
//		//		
//		ConInfoDto dto = ConInfoDto.builder().conId(conInfoEntity.getConId()).conName(conInfoEntity.getConName())
//				.conType(conInfoEntity.getConType()).conNum(conInfoEntity.getConNum()).conCnt(conInfoEntity.getConCnt())
//				.conPort(conInfoEntity.getConPort()).orgName(conInfoEntity.getOrgName())
//				.orgType(conInfoEntity.getOrgType()).couchdbYn(conInfoEntity.isCouchdbYn())
//				.gossipBootAddress(conInfoEntity.getGossipBootAddr()).ordererPorts(conInfoEntity.getOrdererPorts())
//				.build();

		conInfoRepository.deleteById(conInfoEntity.getConId());

//		return dto.getOrgName();
		return conInfoEntity.getOrgName();
	}

	public ConInfoDto selectByConName(String conName) {

		ConInfoEntity conInfoEntity = conInfoRepository.findByConName(conName);
		
//		ConInfoDto conInfoDto =new ConInfoDto();
//		
//		conInfoDto.setConId(conInfoEntity.getConId());
//		conInfoDto.setConName(conInfoEntity.getConName());
//		conInfoDto.setConType(conInfoEntity.getConType());
//		conInfoDto.setConNum(conInfoEntity.getConNum());
//		conInfoDto.setConCnt(conInfoEntity.getConCnt());
//		conInfoDto.setConPort(conInfoEntity.getConPort());
//		conInfoDto.setOrgName(conInfoEntity.getOrgName());
//		conInfoDto.setOrgType(conInfoEntity.getOrgType());
//		conInfoDto.setConsoOrgs(conInfoEntity.getConsoOrgs());
//		conInfoDto.setCouchdbYn(conInfoEntity.isCouchdbYn());
//		conInfoDto.setGossipBootAddress(conInfoEntity.getGossipBootAddr());
//		conInfoDto.setOrdererPorts(conInfoEntity.getOrdererPorts());
		
		return ConInfoDto.builder().conId(conInfoEntity.getConId()).conName(conInfoEntity.getConName())
				.conType(conInfoEntity.getConType()).conNum(conInfoEntity.getConNum()).conCnt(conInfoEntity.getConCnt())
				.conPort(conInfoEntity.getConPort()).orgName(conInfoEntity.getOrgName())
				.orgType(conInfoEntity.getOrgType()).couchdbYn(conInfoEntity.isCouchdbYn())
				.gossipBootAddress(conInfoEntity.getGossipBootAddr()).ordererPorts(conInfoEntity.getOrdererPorts()).build();
		
	}

	public String selectByConType(String conType, String orgType) {

		ArrayList<ConInfoEntity> conInfoEntity = conInfoRepository.findByConTypeAndOrgType(conType, orgType);
		String result = "";
		for (ConInfoEntity entity : conInfoEntity) {

			ConInfoDto conInfoDto = ConInfoDto.builder().conId(entity.getConId()).conName(entity.getConName())
					.conType(entity.getConType()).conNum(entity.getConNum()).conCnt(entity.getConCnt())
					.conPort(conInfoEntity.get(0).getConPort()).orgName(entity.getOrgName())
					.orgType(entity.getOrgType()).couchdbYn(entity.isCouchdbYn())
					.gossipBootAddress(entity.getGossipBootAddr()).ordererPorts(entity.getOrdererPorts()).build();

			result = result + conInfoDto.getOrgName() + " ";
		}

		return result;

	}

	public FabricMemberDto createPeerVo(String orgName) {

//		String ip ="192.168.65.169";
		
		FabricMemberDto result = new FabricMemberDto();
		ArrayList<ConInfoEntity> conInfoEntity = conInfoRepository.findByConTypeAndOrgName("ca", orgName);

		result.setCaUrl("http://"+ip+":" + conInfoEntity.get(0).getConPort());

		conInfoEntity = conInfoRepository.findByConTypeAndOrgName("peer", orgName);

		ConInfoDto peerDto = new ConInfoDto();
		peerDto = ConInfoDto.builder().conId(conInfoEntity.get(0).getConId()).conName(conInfoEntity.get(0).getConName())
				.conType(conInfoEntity.get(0).getConType()).conNum(conInfoEntity.get(0).getConNum())
				.conCnt(conInfoEntity.get(0).getConCnt()).conPort(conInfoEntity.get(0).getConPort())
				.orgName(conInfoEntity.get(0).getOrgName()).orgType(conInfoEntity.get(0).getOrgType())
				.couchdbYn(conInfoEntity.get(0).isCouchdbYn())
				.gossipBootAddress(conInfoEntity.get(0).getGossipBootAddr())
				.ordererPorts(conInfoEntity.get(0).getOrdererPorts()).build();

		result.setConName(peerDto.getConName());
		result.setConNum(peerDto.getConNum());
		result.setConPort(peerDto.getConPort());
		result.setConUrl("grpcs://"+ip+":" + peerDto.getConPort());
		result.setOrgMspId(peerDto.getOrgName() + "MSP");
		result.setOrgName(peerDto.getOrgName());
		result.setOrgType(peerDto.getOrgType());

		return result;

	}
	
	public ArrayList<FabricMemberDto> createMemberDtoArr(String orgType,String orgName) {

//		String ip ="192.168.65.169";
		
		ArrayList<FabricMemberDto> resultArr = new ArrayList<FabricMemberDto>();
		ArrayList<ConInfoEntity> conInfoArr = conInfoRepository.findByConTypeAndOrgTypeAndOrgName("ca",orgType, orgName);

		String caUrl = "http://"+ip+":" + conInfoArr.get(0).getConPort();

		conInfoArr = conInfoRepository.findByConTypeAndOrgName(orgType, orgName);

		
		for(ConInfoEntity conInfo:conInfoArr) {
			FabricMemberDto memberDto = new FabricMemberDto();
			
			memberDto.setConName(conInfo.getConName());
			memberDto.setConNum(conInfo.getConNum());
			memberDto.setConPort(conInfo.getConPort());
			memberDto.setConUrl("grpcs://"+ip+":" + conInfo.getConPort());
			memberDto.setOrgMspId(conInfo.getOrgName() + "MSP");
			memberDto.setOrgName(conInfo.getOrgName());
			memberDto.setOrgType(conInfo.getOrgType());
			memberDto.setCaUrl(caUrl);
			
			resultArr.add(memberDto);
			
		}
		


		return resultArr;

	}

	public FabricMemberDto createOrdererVo(String orgName) {

//		String ip ="192.168.65.169";
		
		FabricMemberDto result = new FabricMemberDto();
		ArrayList<ConInfoEntity> conInfoEntity = conInfoRepository.findByConTypeAndOrgName("ca", orgName);

		result.setCaUrl("http://"+ip+":" + conInfoEntity.get(0).getConPort());

		conInfoEntity = conInfoRepository.findByConTypeAndOrgName("orderer", orgName);

		ConInfoDto peerDto = new ConInfoDto();
		peerDto = ConInfoDto.builder().conId(conInfoEntity.get(0).getConId()).conName(conInfoEntity.get(0).getConName())
				.conType(conInfoEntity.get(0).getConType()).conNum(conInfoEntity.get(0).getConNum())
				.conCnt(conInfoEntity.get(0).getConCnt()).conPort(conInfoEntity.get(0).getConPort())
				.orgName(conInfoEntity.get(0).getOrgName()).orgType(conInfoEntity.get(0).getOrgType())
				.couchdbYn(conInfoEntity.get(0).isCouchdbYn())
				.gossipBootAddress(conInfoEntity.get(0).getGossipBootAddr())
				.ordererPorts(conInfoEntity.get(0).getOrdererPorts()).build();

		result.setConName(peerDto.getConName());
		result.setConNum(peerDto.getConNum());
		result.setConPort(peerDto.getConPort());
		result.setConUrl("grpcs://"+ip+":" + peerDto.getConPort());
		result.setOrgMspId(peerDto.getOrgName() + "MSP");
		result.setOrgName(peerDto.getOrgName());
		result.setOrgType(peerDto.getOrgType());

		return result;

	}
	
	public boolean isMemOfConso(String orgName,String peerOrgName) {
		
		ArrayList<ConInfoEntity> conInfoEntityArr = conInfoRepository.findByConTypeAndOrgName("orderer", orgName);
		
		String[] consoList = conInfoEntityArr.get(0).getConsoOrgs().split(" ");
		
		for (int i = 0; i < consoList.length; i++) {

			if(consoList[i].equals(peerOrgName)) {
				
			return true;	
			
			};
		}
		
		return false;
		
	}
	
	public void updateConsoOrgs(String orgName,String org) {
		ArrayList<ConInfoEntity> conInfoArr = conInfoRepository.findByConTypeAndOrgName("orderer", orgName);
		
		for(ConInfoEntity conInfo:conInfoArr) {
			conInfo.setConsoOrgs(conInfo.getConsoOrgs()+org+" ");
			
			conInfoRepository.save(conInfo);
		}
		
	}

}
