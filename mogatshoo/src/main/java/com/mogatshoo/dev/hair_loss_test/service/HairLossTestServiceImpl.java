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

import com.mogatshoo.dev.config.file.FirebaseStorageService;
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
	
	 @Autowired
	 private FirebaseStorageService firebaseStorageService;

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
		
		// 사진 새로 등록될때 기존 사진이 있는지 확인하고 삭제
		PictureEntity existingPicture = pictureRepository.findById(memberId).orElse(null);
		if (existingPicture != null) {
			System.out.println("[사진 재등록] 기존 사진 삭제 시작: " + memberId);
			// 기존 로컬 파일과 Firebase Storage 파일 삭제
			deleteImageFile(existingPicture.getHairPicture(), existingPicture.getFirebaseStoragePath());
			System.out.println("[사진 재등록] 기존 사진 삭제 완료");
		}
	    
	    // 로컬 + Firebase Storage 동시 저장 (saveImageFile에서 처리)
	    Map<String, String> uploadResult = saveImageFile(imageFile, memberId);
	    String fileName = uploadResult.get("localPath");
	    String firebaseStorageUrl = uploadResult.get("firebaseStorageUrl");
	    String firebaseStoragePath = uploadResult.get("firebaseStoragePath");

	    // PictureEntity 저장 로직
	    PictureEntity pictureEntity = pictureRepository.findById(memberId).orElse(new PictureEntity());
	    pictureEntity.setMemberId(memberId);
	    pictureEntity.setHairPicture(fileName);
	    pictureEntity.setFirebaseStorageUrl(firebaseStorageUrl);
	    pictureEntity.setFirebaseStoragePath(firebaseStoragePath);
	    

	    if (pictureEntity.getCreatedAt() == null) {
	        pictureEntity.setCreatedAt(LocalDateTime.now());
	    }
	    pictureEntity.setUpdatedAt(LocalDateTime.now());
	    pictureRepository.save(pictureEntity);

	    // 3. StageEntity 저장 로직
	    StageEntity stageEntity = stageRepository.findById(memberId).orElse(new StageEntity());
	    stageEntity.setMemberId(memberId);
	    stageEntity.setHairStage(hairStage);

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
	public Map<String, String> saveImageFile(MultipartFile imageFile, String memberId) throws IOException {
	    // 1. 로컬 저장 (기존 로직)
	    Path baseUploadPath = Paths.get(uploadDir);
	    if (!Files.exists(baseUploadPath)) {
	        Files.createDirectories(baseUploadPath);
	    }

	    Path userUploadPath = baseUploadPath.resolve(memberId);
	    if (!Files.exists(userUploadPath)) {
	        Files.createDirectories(userUploadPath);
	    }

	    String originalFileName = imageFile.getOriginalFilename();
	    String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
	    String fileName = UUID.randomUUID().toString() + fileExtension;

	    Path filePath = userUploadPath.resolve(fileName);
	    Files.copy(imageFile.getInputStream(), filePath);
	    
	    String localPath = "/uploads/" + memberId + "/" + fileName;
	    System.out.println("로컬 파일 저장 완료: " + filePath.toAbsolutePath().toString());

	    // 2. Firebase Storage 저장 (사용자 폴더에)
	    String firebaseStorageUrl = null;
	    String firebaseStoragePath = null;
	    
	    try {
	        // Firebase Storage에 사용자별 폴더 생성 후 파일 저장
	        String firebaseFileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
	        firebaseStoragePath = firebaseStorageService.uploadFileToUserFolder(imageFile, memberId, firebaseFileName);
	        firebaseStorageUrl = firebaseStorageService.getFileUrl(firebaseStoragePath);
	        System.out.println("Firebase Storage 사용자 폴더에 업로드 성공: " + firebaseStorageUrl);
	    } catch (Exception e) {
	        System.err.println("Firebase Storage 업로드 실패: " + e.getMessage());
	    }

	    // 3. 결과 반환
	    Map<String, String> result = new HashMap<>();
	    result.put("localPath", localPath);
	    result.put("firebaseStorageUrl", firebaseStorageUrl);
	    result.put("firebaseStoragePath", firebaseStoragePath);
	    
	    return result;
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

	@Override
	public boolean loginMemberHairCheck(String memberId) {
		System.out.println("memberId: "+memberId);
		System.out.println("pictureRepository.existsById(memberId): "+pictureRepository.existsById(memberId));
		System.out.println("stageRepository.existsById(memberId): "+stageRepository.existsById(memberId));
		return pictureRepository.existsById(memberId) && stageRepository.existsById(memberId);
	}

	@Override
	public void memberDelete(String memberId) {
	    
	    PictureEntity pictureEntity = pictureRepository.findById(memberId).orElse(null);
	    
	    if(pictureEntity != null) {
	        // 로컬 파일과 Firebase Storage 파일 모두 삭제
	        deleteImageFile(pictureEntity.getHairPicture(), pictureEntity.getFirebaseStoragePath());
	    }
	    
	    // Firebase Storage 사용자 폴더 전체 삭제
	    try {
	        if (firebaseStorageService.isEnabled()) {
	            firebaseStorageService.deleteUserFolder(memberId);
	            System.out.println("[회원 탈퇴] Firebase Storage 사용자 폴더 삭제 완료: " + memberId);
	        }
	    } catch (Exception e) {
	        System.err.println("[회원 탈퇴] Firebase Storage 사용자 폴더 삭제 실패: " + e.getMessage());
	    }
	    
	    // 로컬 사용자 폴더 삭제
	    deleteLocalUserFolder(memberId);
	    
	    // 데이터베이스에서 삭제
	    stageRepository.deleteById(memberId);
	    pictureRepository.deleteById(memberId);
	}

	// 회원탈퇴시 이미지 삭제
	public void deleteImageFile(String imagePath, String firebaseStoragePath) {
	    try {
	        if (imagePath != null && !imagePath.isEmpty()) {
	            String fullPath = uploadDir + imagePath.replace("/uploads", "");
	            Path filePath = Paths.get(fullPath);
	            Files.delete(filePath);
	        }
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	    
	    // Firebase Storage 파일 삭제
	    try {
	        if (firebaseStoragePath != null && !firebaseStoragePath.isEmpty() && firebaseStorageService.isEnabled()) {
	            firebaseStorageService.deleteFile(firebaseStoragePath);
	        }
	    } catch (Exception e) {
	        System.err.println("Firebase Storage 파일 삭제 실패: " + e.getMessage());
	        e.printStackTrace();
	    }
	}
	
	public void deleteLocalUserFolder(String memberId) {
	    try {
	        Path userFolderPath = Paths.get(uploadDir, memberId);
	        if (Files.exists(userFolderPath)) {
	            // 폴더 안의 모든 파일과 폴더 삭제
	            Files.walk(userFolderPath)
	                 .sorted((a, b) -> b.compareTo(a)) // 파일 먼저, 폴더 나중에 삭제
	                 .forEach(path -> {
	                     try {
	                         Files.delete(path);
	                     } catch (Exception e) {
	                         System.err.println("파일 삭제 실패: " + path + " - " + e.getMessage());
	                     }
	                 });
	            System.out.println("[회원 탈퇴] 로컬 사용자 폴더 삭제 완료: " + userFolderPath);
	        }
	    } catch (Exception e) {
	        System.err.println("[회원 탈퇴] 로컬 사용자 폴더 삭제 실패: " + e.getMessage());
	    }
	}
}