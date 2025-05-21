package com.mogatshoo.dev.hair_loss_test.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.mogatshoo.dev.hair_loss_test.entity.PictureEntity;
import com.mogatshoo.dev.hair_loss_test.entity.StageEntity;
import com.mogatshoo.dev.hair_loss_test.repository.PictureRepository;
import com.mogatshoo.dev.hair_loss_test.repository.StageRepository;

@Service
public class HairLossTestServiceImpl implements HairLossTestService {

	@Autowired
	private PictureRepository pictureRepository;

	@Autowired
	private StageRepository stageRepository;

	@Value("${file.upload-dir:/uploads}")
	private String uploadDir;

	/**
	 * 이미지를 저장하고 결과를 데이터베이스에 저장합니다.
	 * 
	 * @param memberId  회원 ID
	 * @param imageFile 업로드된 이미지 파일
	 * @param hairStage 탈모 단계 결과
	 * @return 저장된 파일 경로
	 * @throws IOException 파일 저장 실패 시 발생
	 */
	@Transactional
	public String saveHairLossTestResult(String memberId, MultipartFile imageFile, String hairStage)
			throws IOException {
		// 1. 이미지 파일 저장
		String fileName = saveImageFile(imageFile, memberId);

		// 2. 기존 데이터 확인 - 있으면 업데이트, 없으면 새로 생성
		PictureEntity pictureEntity = pictureRepository.findById(memberId).orElse(new PictureEntity());
		pictureEntity.setMemberId(memberId);
		pictureEntity.setHairPicture(fileName);

		// 최초 생성 시간은 새 엔티티일 경우에만 설정
		if (pictureEntity.getCreatedAt() == null) {
			pictureEntity.setCreatedAt(LocalDateTime.now());
		}

		pictureEntity.setUpdatedAt(LocalDateTime.now());
		pictureRepository.save(pictureEntity);

		// 3. StageEntity 생성 및 저장
		StageEntity stageEntity = stageRepository.findById(memberId).orElse(new StageEntity());
		stageEntity.setMemberId(memberId);
		stageEntity.setHairStage(hairStage);

		// 최초 생성 시간은 새 엔티티일 경우에만 설정
		if (stageEntity.getCreateAt() == null) {
			stageEntity.setCreateAt(LocalDateTime.now());
		}

		stageEntity.setUpdatedAt(LocalDateTime.now());
		stageRepository.save(stageEntity);

		return fileName;
	}

	/**
	 * 이미지 파일을 서버에 저장합니다.
	 * 
	 * @param imageFile 업로드된 이미지 파일
	 * @param memberId  회원 ID
	 * @return 저장된 파일 경로
	 * @throws IOException 파일 저장 실패 시 발생
	 */
	public String saveImageFile(MultipartFile imageFile, String memberId) throws IOException {
		// 기본 업로드 디렉토리 생성
		Path baseUploadPath = Paths.get(uploadDir);
		if (!Files.exists(baseUploadPath)) {
			Files.createDirectories(baseUploadPath);
		}

		// 사용자별 폴더 생성
		Path userUploadPath = baseUploadPath.resolve(memberId);
		if (!Files.exists(userUploadPath)) {
			Files.createDirectories(userUploadPath);
		}

		// 디버깅 로그 추가
		System.out.println("사용자 폴더 경로: " + userUploadPath.toAbsolutePath().toString());

		// 파일명 생성 (고유한 파일명 보장)
		String originalFileName = imageFile.getOriginalFilename();
		String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
		String fileName = UUID.randomUUID().toString() + fileExtension;

		// 파일 저장
		Path filePath = userUploadPath.resolve(fileName);
		Files.copy(imageFile.getInputStream(), filePath);

		System.out.println("파일 저장 완료: " + filePath.toAbsolutePath().toString());

		// 데이터베이스에 저장할 상대 경로 (memberId/파일명)
		return "/uploads/"+memberId + "/" + fileName;
	}

	/**
	 * 회원 ID로 저장된 탈모 단계 정보를 조회합니다.
	 * 
	 * @param memberId 회원 ID
	 * @return 탈모 단계 정보 또는 null
	 */
	public StageEntity getHairStageByMemberId(String memberId) {
		return stageRepository.findById(memberId).orElse(null);
	}

	/**
	 * 회원 ID로 저장된 사진 정보를 조회합니다.
	 * 
	 * @param memberId 회원 ID
	 * @return 사진 정보 또는 null
	 */
	public PictureEntity getPictureByMemberId(String memberId) {
		return pictureRepository.findById(memberId).orElse(null);
	}

	/**
	 * 랜덤하게 지정된 개수의 사진을 가져옵니다.
	 * 
	 * @param count 가져올 사진 개수
	 * @return 랜덤하게 선택된 사진 목록
	 */
	public List<PictureEntity> getRandomPictures(int count) {
		// 방법 1: JPA에서 모든 사진을 가져와서 자바에서 랜덤 선택
		List<PictureEntity> allPictures = pictureRepository.findAll();

		// 사진이 count보다 적으면 있는 만큼만 반환
		if (allPictures.size() <= count) {
			return allPictures;
		}

		// 랜덤 선택을 위한 리스트 섞기
		Collections.shuffle(allPictures);

		// 앞에서부터 count개 선택
		return allPictures.subList(0, count);

		/*
		 * // 방법 2: 데이터베이스에서 직접 랜덤 선택 (MySQL 예시) // 이 방법을 사용하려면 PictureRepository에 해당
		 * 메소드 추가 필요 return pictureRepository.findRandomPictures(count);
		 */
	}

	@Override
	public Map<String, Object> hairMypage(String memberId) {
		PictureEntity pictureEntity = pictureRepository.findById(memberId).orElse(null);
		StageEntity stageEntity = stageRepository.findById(memberId).orElse(null);
		
		Map<String, Object> map = new HashMap<>();
		map.put("picture", pictureEntity);
		map.put("stage", stageEntity);
		return map;
	}
}
