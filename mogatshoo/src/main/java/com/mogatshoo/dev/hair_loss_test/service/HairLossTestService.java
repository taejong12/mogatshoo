package com.mogatshoo.dev.hair_loss_test.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.mogatshoo.dev.hair_loss_test.entity.PictureEntity;
import com.mogatshoo.dev.hair_loss_test.entity.StageEntity;

public interface HairLossTestService {

	String saveHairLossTestResult(String memberId, MultipartFile imageFile, String hairStage) throws IOException;

	String saveImageFile(MultipartFile imageFile, String memberId) throws IOException;

	public StageEntity getHairStageByMemberId(String memberId);

	public PictureEntity getPictureByMemberId(String memberId);
	
	public List<PictureEntity> getRandomPictures(int count);

	Map<String, Object> hairMypage(String memberId);
}
