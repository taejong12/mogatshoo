package com.mogatshoo.dev.fortune.service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.mogatshoo.dev.fortune.entity.FortuneEntity;
import com.mogatshoo.dev.fortune.repository.FortuneRepository;
import com.mogatshoo.dev.point.service.PointService;

import jakarta.transaction.Transactional;
@Service
@Transactional
public class FortuneServiceImpl implements FortuneService{

	@Autowired
	private FortuneRepository fortuneRepository;
	
	@Autowired
	private PointService pointService;
	
	@Override
	public Map<String, Object> fortuneMsg(String memberId, String name, LocalDate birth, String gender) {
		
		Map<String, Object> map = new HashMap<>();
		
		List<FortuneEntity> fortuneList = fortuneRepository.findAll();
		
		// 고유 키 생성
	    String key = name + birth.toString() + gender + LocalDate.now().toString();
		
	    // 고정된 랜덤 인덱스 생성 (항상 같은 결과)
	    // 음수 방지
	    int hash = Math.abs(key.hashCode());
	    
	    // 리스트 크기 안에서 인덱스 생성
	    int index = hash % fortuneList.size(); 
	    
		// 운세문구 랜덤
		String fortuneMsg = fortuneList.get(index).getFortuneContent();
		
		// 운세아이템 랜덤
		String fortuneItem = "오늘의 아이템은 "+fortuneList.get(index).getFortuneItem()+" 입니다.";
		
		map.put("fortuneMsg", fortuneMsg);
		map.put("fortuneItem", fortuneItem);
		
		int point = pointService.fortunePointUse(memberId);
		
		map.put("point", point);
		
		return map;
	}
}
