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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(HairLossTestServiceImpl.class);
	
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
	 */
	@Transactional
	public String saveHairLossTestResult(String memberId, MultipartFile imageFile, String hairStage)
	        throws IOException {
		
		logger.info("탈모 테스트 결과 저장 시작: memberId={}, fileName={}, hairStage={}", 
		           memberId, imageFile.getOriginalFilename(), hairStage);
		
		try {
			// 사진 새로 등록될때 기존 사진이 있는지 확인하고 삭제
			PictureEntity existingPicture = pictureRepository.findById(memberId).orElse(null);
			if (existingPicture != null) {
				logger.info("기존 사진 발견, 삭제 시작: memberId={}", memberId);
				// 기존 로컬 파일과 Firebase Storage 파일 삭제
				deleteImageFile(existingPicture.getHairPicture(), existingPicture.getFirebaseStoragePath());
				logger.info("기존 사진 삭제 완료: memberId={}", memberId);
			}
		    
		    // 로컬 + Firebase Storage 동시 저장
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
		    logger.debug("PictureEntity 저장 완료: memberId={}", memberId);

		    // StageEntity 저장 로직
		    StageEntity stageEntity = stageRepository.findById(memberId).orElse(new StageEntity());
		    stageEntity.setMemberId(memberId);
		    stageEntity.setHairStage(hairStage);

		    if (stageEntity.getCreateAt() == null) {
		        stageEntity.setCreateAt(LocalDateTime.now());
		    }
		    stageEntity.setUpdatedAt(LocalDateTime.now());
		    stageRepository.save(stageEntity);
		    logger.debug("StageEntity 저장 완료: memberId={}", memberId);

		    logger.info("탈모 테스트 결과 저장 완료: memberId={}, fileName={}", memberId, fileName);
		    return fileName;
		    
		} catch (Exception e) {
			logger.error("탈모 테스트 결과 저장 실패: memberId={}, error={}", memberId, e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 이미지 파일을 서버에 저장합니다.
	 */
	public Map<String, String> saveImageFile(MultipartFile imageFile, String memberId) throws IOException {
		logger.info("이미지 파일 저장 시작: memberId={}, fileName={}, size={}bytes", 
		           memberId, imageFile.getOriginalFilename(), imageFile.getSize());
		
		try {
			// 입력값 검증
			if (imageFile == null || imageFile.isEmpty()) {
				throw new IllegalArgumentException("업로드된 파일이 없습니다");
			}
			if (memberId == null || memberId.trim().isEmpty()) {
				throw new IllegalArgumentException("회원 ID가 없습니다");
			}
			
		    // 1. 로컬 저장
		    Path baseUploadPath = Paths.get(uploadDir);
		    if (!Files.exists(baseUploadPath)) {
		        Files.createDirectories(baseUploadPath);
		        logger.debug("기본 업로드 디렉토리 생성: {}", baseUploadPath);
		    }

		    Path userUploadPath = baseUploadPath.resolve(memberId);
		    if (!Files.exists(userUploadPath)) {
		        Files.createDirectories(userUploadPath);
		        logger.debug("사용자 업로드 디렉토리 생성: {}", userUploadPath);
		    }

		    String originalFileName = imageFile.getOriginalFilename();
		    String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
		    String fileName = UUID.randomUUID().toString() + fileExtension;

		    Path filePath = userUploadPath.resolve(fileName);
		    Files.copy(imageFile.getInputStream(), filePath);
		    
		    String localPath = "/uploads/" + memberId + "/" + fileName;
		    logger.info("로컬 파일 저장 완료: {}", filePath.toAbsolutePath());

		    // 2. Firebase Storage 저장
		    String firebaseStorageUrl = null;
		    String firebaseStoragePath = null;
		    
		    try {
		        if (firebaseStorageService.isEnabled()) {
		            String firebaseFileName = System.currentTimeMillis() + "_" + imageFile.getOriginalFilename();
		            firebaseStoragePath = firebaseStorageService.uploadFileToUserFolder(imageFile, memberId, firebaseFileName);
		            firebaseStorageUrl = firebaseStorageService.getFileUrl(firebaseStoragePath);
		            logger.info("Firebase Storage 업로드 성공: path={}, url={}", firebaseStoragePath, firebaseStorageUrl);
		        } else {
		            logger.warn("Firebase Storage가 비활성화되어 있어 로컬에만 저장됨");
		        }
		    } catch (Exception e) {
		        logger.error("Firebase Storage 업로드 실패 (로컬 저장은 성공): memberId={}, error={}", 
		                    memberId, e.getMessage(), e);
		        // Firebase 실패해도 로컬 저장은 성공했으므로 계속 진행
		    }

		    // 3. 결과 반환
		    Map<String, String> result = new HashMap<>();
		    result.put("localPath", localPath);
		    result.put("firebaseStorageUrl", firebaseStorageUrl);
		    result.put("firebaseStoragePath", firebaseStoragePath);
		    
		    logger.debug("이미지 파일 저장 완료: memberId={}, localPath={}", memberId, localPath);
		    return result;
		    
		} catch (Exception e) {
			logger.error("이미지 파일 저장 실패: memberId={}, fileName={}, error={}", 
			            memberId, imageFile.getOriginalFilename(), e.getMessage(), e);
			throw e;
		}
	}

	/**
	 * 회원 ID로 저장된 탈모 단계 정보를 조회합니다.
	 */
	public StageEntity getHairStageByMemberId(String memberId) {
		logger.debug("탈모 단계 정보 조회: memberId={}", memberId);
		
		try {
			if (memberId == null || memberId.trim().isEmpty()) {
				logger.warn("유효하지 않은 memberId로 탈모 단계 조회 시도: {}", memberId);
				return null;
			}
			
			StageEntity result = stageRepository.findById(memberId).orElse(null);
			logger.debug("탈모 단계 정보 조회 결과: memberId={}, found={}", memberId, result != null);
			return result;
			
		} catch (Exception e) {
			logger.error("탈모 단계 정보 조회 실패: memberId={}, error={}", memberId, e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 회원 ID로 저장된 사진 정보를 조회합니다.
	 */
	public PictureEntity getPictureByMemberId(String memberId) {
		logger.debug("사진 정보 조회: memberId={}", memberId);
		
		try {
			if (memberId == null || memberId.trim().isEmpty()) {
				logger.warn("유효하지 않은 memberId로 사진 정보 조회 시도: {}", memberId);
				return null;
			}
			
			PictureEntity result = pictureRepository.findById(memberId).orElse(null);
			logger.debug("사진 정보 조회 결과: memberId={}, found={}", memberId, result != null);
			return result;
			
		} catch (Exception e) {
			logger.error("사진 정보 조회 실패: memberId={}, error={}", memberId, e.getMessage(), e);
			return null;
		}
	}

	/**
	 * 랜덤하게 지정된 개수의 사진을 가져옵니다.
	 */
	public List<PictureEntity> getRandomPictures(int count) {
		logger.info("랜덤 사진 조회 시작: count={}", count);
		
		try {
			if (count <= 0) {
				logger.warn("유효하지 않은 count 값: {}", count);
				return Collections.emptyList();
			}
			
			List<PictureEntity> allPictures = pictureRepository.findAll();
			logger.debug("전체 사진 개수: {}", allPictures.size());

			// 사진이 count보다 적으면 있는 만큼만 반환
			if (allPictures.size() <= count) {
				logger.info("요청한 개수보다 적은 사진 반환: 요청={}, 실제={}", count, allPictures.size());
				return allPictures;
			}

			// 랜덤 선택을 위한 리스트 섞기
			Collections.shuffle(allPictures);
			List<PictureEntity> result = allPictures.subList(0, count);
			
			logger.info("랜덤 사진 조회 완료: count={}", result.size());
			return result;
			
		} catch (Exception e) {
			logger.error("랜덤 사진 조회 실패: count={}, error={}", count, e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	@Override
	public Map<String, Object> hairMypage(String memberId) {
		logger.info("탈모 마이페이지 데이터 조회: memberId={}", memberId);
		
		try {
			if (memberId == null || memberId.trim().isEmpty()) {
				logger.warn("유효하지 않은 memberId로 마이페이지 조회: {}", memberId);
				return new HashMap<>();
			}
			
			PictureEntity pictureEntity = pictureRepository.findById(memberId).orElse(null);
			StageEntity stageEntity = stageRepository.findById(memberId).orElse(null);
			
			Map<String, Object> map = new HashMap<>();
			map.put("picture", pictureEntity);
			map.put("stage", stageEntity);
			
			logger.info("탈모 마이페이지 데이터 조회 완료: memberId={}, hasPicture={}, hasStage={}", 
			           memberId, pictureEntity != null, stageEntity != null);
			return map;
			
		} catch (Exception e) {
			logger.error("탈모 마이페이지 데이터 조회 실패: memberId={}, error={}", memberId, e.getMessage(), e);
			return new HashMap<>();
		}
	}

	@Override
	public boolean loginMemberHairCheck(String memberId) {
		logger.info("탈모 테스트 데이터 확인 시작: memberId={}", memberId);
		
		try {
			// 입력값 검증
			if (memberId == null || memberId.trim().isEmpty()) {
				logger.warn("유효하지 않은 memberId: {}", memberId);
				return false;
			}
			
			// 사진 데이터 존재 확인
			boolean hasPicture = pictureRepository.existsById(memberId);
			logger.debug("사진 데이터 존재 여부: memberId={}, exists={}", memberId, hasPicture);
			
			// 단계 데이터 존재 확인  
			boolean hasStage = stageRepository.existsById(memberId);
			logger.debug("단계 데이터 존재 여부: memberId={}, exists={}", memberId, hasStage);
			
			boolean result = hasPicture && hasStage;
			
			if (result) {
				logger.info("탈모 테스트 데이터 확인 완료: memberId={}, 데이터 존재함", memberId);
			} else {
				logger.info("탈모 테스트 데이터 확인 완료: memberId={}, 데이터 없음 (사진:{}, 단계:{})", 
				           memberId, hasPicture, hasStage);
			}
			
			return result;
			
		} catch (Exception e) {
			logger.error("탈모 테스트 데이터 확인 중 오류 발생: memberId={}, error={}", 
			            memberId, e.getMessage(), e);
			return false;
		}
	}

	@Override
	public void memberDelete(String memberId) {
		logger.info("회원 탈퇴 처리 시작: memberId={}", memberId);
		
		try {
			if (memberId == null || memberId.trim().isEmpty()) {
				logger.warn("유효하지 않은 memberId로 회원 탈퇴 시도: {}", memberId);
				return;
			}
			
		    PictureEntity pictureEntity = pictureRepository.findById(memberId).orElse(null);
		    
		    if (pictureEntity != null) {
		        logger.info("회원의 사진 데이터 발견, 파일 삭제 시작: memberId={}", memberId);
		        // 로컬 파일과 Firebase Storage 파일 모두 삭제
		        deleteImageFile(pictureEntity.getHairPicture(), pictureEntity.getFirebaseStoragePath());
		    } else {
		        logger.info("회원의 사진 데이터 없음: memberId={}", memberId);
		    }
		    
		    // Firebase Storage 사용자 폴더 전체 삭제
		    try {
		        if (firebaseStorageService.isEnabled()) {
		            firebaseStorageService.deleteUserFolder(memberId);
		            logger.info("Firebase Storage 사용자 폴더 삭제 완료: memberId={}", memberId);
		        }
		    } catch (Exception e) {
		        logger.error("Firebase Storage 사용자 폴더 삭제 실패: memberId={}, error={}", 
		                    memberId, e.getMessage(), e);
		    }
		    
		    // 로컬 사용자 폴더 삭제
		    deleteLocalUserFolder(memberId);
		    
		    // 데이터베이스에서 삭제
		    try {
		        stageRepository.deleteById(memberId);
		        logger.debug("StageEntity 삭제 완료: memberId={}", memberId);
		        
		        pictureRepository.deleteById(memberId);
		        logger.debug("PictureEntity 삭제 완료: memberId={}", memberId);
		        
		        logger.info("회원 탈퇴 처리 완료: memberId={}", memberId);
		    } catch (Exception e) {
		        logger.error("데이터베이스 삭제 실패: memberId={}, error={}", memberId, e.getMessage(), e);
		        throw e;
		    }
		    
		} catch (Exception e) {
			logger.error("회원 탈퇴 처리 실패: memberId={}, error={}", memberId, e.getMessage(), e);
			throw new RuntimeException("회원 탈퇴 처리 중 오류가 발생했습니다", e);
		}
	}

	// 회원탈퇴시 이미지 삭제
	public void deleteImageFile(String imagePath, String firebaseStoragePath) {
		logger.debug("이미지 파일 삭제 시작: imagePath={}, firebaseStoragePath={}", imagePath, firebaseStoragePath);
		
		// 로컬 파일 삭제
	    try {
	        if (imagePath != null && !imagePath.isEmpty()) {
	            String fullPath = uploadDir + imagePath.replace("/uploads", "");
	            Path filePath = Paths.get(fullPath);
	            
	            if (Files.exists(filePath)) {
	                Files.delete(filePath);
	                logger.info("로컬 파일 삭제 성공: {}", filePath);
	            } else {
	                logger.warn("삭제하려는 로컬 파일이 존재하지 않음: {}", filePath);
	            }
	        }
	    } catch (Exception e) {
	        logger.error("로컬 파일 삭제 실패: imagePath={}, error={}", imagePath, e.getMessage(), e);
	    }
	    
	    // Firebase Storage 파일 삭제
	    try {
	        if (firebaseStoragePath != null && !firebaseStoragePath.isEmpty() && firebaseStorageService.isEnabled()) {
	            firebaseStorageService.deleteFile(firebaseStoragePath);
	            logger.info("Firebase Storage 파일 삭제 성공: {}", firebaseStoragePath);
	        }
	    } catch (Exception e) {
	        logger.error("Firebase Storage 파일 삭제 실패: firebaseStoragePath={}, error={}", 
	                    firebaseStoragePath, e.getMessage(), e);
	    }
	}
	
	public void deleteLocalUserFolder(String memberId) {
		logger.info("로컬 사용자 폴더 삭제 시작: memberId={}", memberId);
		
	    try {
	        if (memberId == null || memberId.trim().isEmpty()) {
	            logger.warn("유효하지 않은 memberId로 폴더 삭제 시도: {}", memberId);
	            return;
	        }
	        
	        Path userFolderPath = Paths.get(uploadDir, memberId);
	        if (Files.exists(userFolderPath)) {
	            logger.debug("사용자 폴더 발견, 삭제 진행: {}", userFolderPath);
	            
	            // 폴더 안의 모든 파일과 폴더 삭제
	            Files.walk(userFolderPath)
	                 .sorted((a, b) -> b.compareTo(a)) // 파일 먼저, 폴더 나중에 삭제
	                 .forEach(path -> {
	                     try {
	                         Files.delete(path);
	                         logger.debug("파일/폴더 삭제 성공: {}", path);
	                     } catch (Exception e) {
	                         logger.error("파일/폴더 삭제 실패: path={}, error={}", path, e.getMessage());
	                     }
	                 });
	            logger.info("로컬 사용자 폴더 삭제 완료: {}", userFolderPath);
	        } else {
	            logger.info("삭제할 사용자 폴더가 존재하지 않음: {}", userFolderPath);
	        }
	    } catch (Exception e) {
	        logger.error("로컬 사용자 폴더 삭제 실패: memberId={}, error={}", memberId, e.getMessage(), e);
	    }
	}
}