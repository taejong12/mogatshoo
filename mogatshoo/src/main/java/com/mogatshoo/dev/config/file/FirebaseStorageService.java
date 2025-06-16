package com.mogatshoo.dev.config.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemEntity;
import com.mogatshoo.dev.admin.point.item.entity.AdminPointItemImgEntity;
import com.mogatshoo.dev.admin.point.send.entity.PointItemSendLogEntity;

@Service
public class FirebaseStorageService {

	private static final Logger logger = LoggerFactory.getLogger(FirebaseStorageService.class);

	@Value("${firebase.type:}")
	private String type;

	@Value("${firebase.project.id:}")
	private String projectId;

	@Value("${firebase.private.key.id:}")
	private String privateKeyId;

	@Value("${firebase.private.key:}")
	private String privateKey;

	@Value("${firebase.client.email:}")
	private String clientEmail;

	@Value("${firebase.client.id:}")
	private String clientId;

	@Value("${firebase.auth.uri:}")
	private String authUri;

	@Value("${firebase.token.uri:}")
	private String tokenUri;

	@Value("${firebase.auth.provider.x509.cert.url:}")
	private String authProviderCertUrl;

	@Value("${firebase.client.x509.cert.url:}")
	private String clientCertUrl;

	@Value("${firebase.universe.domain:}")
	private String universeDomain;

	@Value("${firebase.storage.bucket:}")
	private String storageBucket;

	private Storage storage;
	private boolean isEnabled = false;

	@PostConstruct
	public void init() {
		if (projectId.isEmpty() || privateKey.isEmpty() || clientEmail.isEmpty()) {
			logger.info("[FirebaseStorageService] 설정이 없습니다. 로컬 저장만 사용합니다.");
			this.isEnabled = false;
			return;
		}

		logger.debug("프로젝트 ID: {}", projectId);
		logger.debug("클라이언트 이메일: {}", clientEmail);
		logger.debug("스토리지 버킷: {}", storageBucket);

		try {
			// Properties에서 읽은 정보로 JSON 생성
			String jsonCredentials = createJsonCredentials();

			GoogleCredentials credentials = GoogleCredentials
					.fromStream(new ByteArrayInputStream(jsonCredentials.getBytes()));

			FirebaseOptions options = FirebaseOptions.builder().setCredentials(credentials)
					.setStorageBucket(storageBucket).build();

			if (FirebaseApp.getApps().isEmpty()) {
				FirebaseApp.initializeApp(options);
			}

			this.storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(projectId).build()
					.getService();

			logger.info("Firebase Storage 서비스 생성 성공!");

			this.isEnabled = true;

		} catch (Exception e) {
			logger.info("[DEBUG] 초기화 실패: {}", e.getMessage());
			this.isEnabled = false;
			this.storage = null;
		}
	}

	/**
	 * Properties 정보를 사용해서 JSON 문자열 생성
	 */
	private String createJsonCredentials() {
		return String.format("""
				{
				  "type": "%s",
				  "project_id": "%s",
				  "private_key_id": "%s",
				  "private_key": "%s",
				  "client_email": "%s",
				  "client_id": "%s",
				  "auth_uri": "%s",
				  "token_uri": "%s",
				  "auth_provider_x509_cert_url": "%s",
				  "client_x509_cert_url": "%s",
				  "universe_domain": "%s"
				}
				""", type, projectId, privateKeyId, privateKey.replace("\\n", "\n"), clientEmail, clientId, authUri,
				tokenUri, authProviderCertUrl, clientCertUrl, universeDomain);
	}

	public boolean isEnabled() {
		return this.isEnabled;
	}

	/**
	 * 사용자별 폴더에 파일을 업로드합니다.
	 */
	public String uploadFileToUserFolder(MultipartFile file, String memberId, String fileName) throws IOException {
		if (!isEnabled) {
			throw new IOException("Firebase Storage 서비스가 비활성화되어 있습니다.");
		}

		try {
			String filePath = "member/" + memberId + "/" + fileName;

			logger.info("Firebase Storage 업로드 시작: {}", filePath);

			// 실제 Firebase Storage에 업로드
			BlobId blobId = BlobId.of(storageBucket, filePath);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(file.getContentType()).build();

			// 파일 업로드 실행
			Blob blob = storage.create(blobInfo, file.getBytes());

			logger.info("Firebase Storage 업로드 성공: {}", filePath);
			logger.info("업로드된 파일 크기: {} bytes", blob.getSize());

			return filePath;

		} catch (Exception e) {
			logger.error("Firebase Storage 업로드 실패: memberId={}, fileName={}", memberId, fileName, e);
			throw new IOException("사용자 폴더 파일 업로드 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * 파일 URL 생성
	 */
	public String getFileUrl(String filePath) {
		if (filePath == null || filePath.isEmpty()) {
			return null;
		}
		// Firebase Storage 다운로드 URL 형식
		return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", storageBucket,
				filePath.replace("/", "%2F"));
	}

	/**
	 * 파일 삭제
	 */
	public void deleteFile(String filePath) throws IOException {
		if (!isEnabled || filePath == null || filePath.isEmpty()) {
			return;
		}

		try {
			BlobId blobId = BlobId.of(storageBucket, filePath);
			boolean deleted = storage.delete(blobId);

			if (deleted) {
				logger.info("[FirebaseStorageService] 파일 삭제 성공: {}", filePath);
			} else {
				logger.info("[FirebaseStorageService] 파일이 존재하지 않음: {}", filePath);
			}
		} catch (Exception e) {
			logger.info("[FirebaseStorageService] 파일 삭제 실패: {}", e.getMessage());
		}
	}

	/**
	 * 사용자 폴더 전체 삭제
	 */
	public void deleteUserFolder(String memberId) throws IOException {
		if (!isEnabled || memberId == null || memberId.isEmpty()) {
			return;
		}

		try {
			String folderPrefix = "member/" + memberId + "/";

			// 해당 접두사를 가진 모든 파일 조회 및 삭제
			storage.list(storageBucket, Storage.BlobListOption.prefix(folderPrefix)).iterateAll().forEach(blob -> {
				try {
					storage.delete(blob.getBlobId());
					logger.info("[FirebaseStorageService] 파일 삭제: {}", blob.getName());
				} catch (Exception e) {
					logger.info("[FirebaseStorageService] 파일 삭제 실패: {}", blob.getName() + " - " + e.getMessage());
				}
			});

			logger.info("[FirebaseStorageService] 사용자 폴더 삭제 완료: {}", memberId);

		} catch (Exception e) {
			logger.info("[FirebaseStorageService] 사용자 폴더 삭제 실패: {}", e.getMessage());
			throw new IOException("사용자 폴더 삭제 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * 포인트 상품 이미지 업로드
	 */
	public String uploadFileToPointItem(MultipartFile imgFile, String pointCategoryName, String newFileName) {
		try {
			if (!isEnabled) {
				throw new IOException("Firebase Storage 서비스가 비활성화되어 있습니다.");
			}

			// 포인트 아이템 경로: point-items/{category}/{fileName}
			String filePath = "point-items/" + pointCategoryName + "/" + newFileName;

			BlobId blobId = BlobId.of(storageBucket, filePath);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(imgFile.getContentType()).build();

			storage.create(blobInfo, imgFile.getBytes());

			logger.info("파일 업로드 완료 - 경로: {}", filePath);
			logger.debug("생성된 파일 URL: {}", getFileUrl(filePath));

			return filePath;

		} catch (Exception e) {
			logger.error("포인트 아이템 파일 업로드 실패", e);
			throw new RuntimeException("포인트 아이템 파일 업로드 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * 포인트 상품 이미지 삭제
	 */
	public void deletePointItemImg(String pointItemImgFilePath) {
		try {
			if (!isEnabled) {
				throw new IOException("Firebase Storage 서비스가 비활성화되어 있습니다.");
			}

			if (pointItemImgFilePath == null || pointItemImgFilePath.isEmpty()) {
				logger.warn("삭제할 파일 경로가 null 또는 비어 있어 삭제를 건너뜁니다.");
				return;
			}

			BlobId blobId = BlobId.of(storageBucket, pointItemImgFilePath);
			boolean deleted = storage.delete(blobId);

			if (deleted) {
				logger.info("포인트 아이템 이미지 삭제 완료: 파일 경로={}", pointItemImgFilePath);
			} else {
				logger.warn("삭제할 파일이 존재하지 않음: 파일 경로={}", pointItemImgFilePath);
			}
		} catch (Exception e) {
			logger.error("포인트 아이템 이미지 삭제 실패: 파일 경로={}", pointItemImgFilePath, e);
		}
	}

	/**
	 * 파일을 새 카테고리로 이동
	 */
	public void moveImgToNewCategory(String oldFilePath, String newCategoryName, String oldCategoryName) {
		try {
			if (!isEnabled) {
				throw new IOException("Firebase Storage 서비스가 비활성화되어 있습니다.");
			}

			// 기존 파일에서 파일명 추출
			String fileName = oldFilePath.substring(oldFilePath.lastIndexOf("/") + 1);
			String newFilePath = "point-items/" + newCategoryName + "/" + fileName;

			// 기존 파일 내용 복사
			BlobId oldBlobId = BlobId.of(storageBucket, oldFilePath);
			BlobId newBlobId = BlobId.of(storageBucket, newFilePath);

			Blob oldBlob = storage.get(oldBlobId);
			if (oldBlob != null) {
				BlobInfo newBlobInfo = BlobInfo.newBuilder(newBlobId).setContentType(oldBlob.getContentType()).build();

				storage.create(newBlobInfo, oldBlob.getContent());
				storage.delete(oldBlobId);

				logger.info("파일이 성공적으로 카테고리 이동됨. {} → {}", oldFilePath, newFilePath);
			}

		} catch (Exception e) {
			logger.error("moveImgToNewCategory: 파일 이동 중 오류 발생. oldPath: {}, {} → {}", oldFilePath, oldCategoryName,
					newCategoryName, e);
		}
	}

	/**
	 * 카테고리 폴더에 이미지가 존재하는지 확인
	 */
	public boolean pointCategoryImgCheck(String pointCategoryName) {
		try {
			if (!isEnabled) {
				return false;
			}

			String folderPrefix = "point-items/" + pointCategoryName + "/";

			// 해당 접두사를 가진 파일이 하나라도 있는지 확인
			boolean hasFiles = storage.list(storageBucket, Storage.BlobListOption.prefix(folderPrefix),
					Storage.BlobListOption.pageSize(1)).iterateAll().iterator().hasNext();

			logger.info("pointCategoryImgCheck: 카테고리 '{}' 폴더에 이미지 존재 여부: {}", pointCategoryName, hasFiles);
			return hasFiles;

		} catch (Exception e) {
			logger.error("pointCategoryImgCheck 중 오류 발생: 카테고리명: {}", pointCategoryName, e);
			return false;
		}
	}

	/**
	 * 포인트 상품 카테고리 폴더 삭제 (해당 카테고리의 모든 파일 삭제)
	 */
	public void deletePointItemFolder(String categoryName) {
		try {
			if (!isEnabled) {
				return;
			}

			String folderPrefix = "point-items/" + categoryName + "/";

			// 해당 카테고리의 모든 파일 삭제
			storage.list(storageBucket, Storage.BlobListOption.prefix(folderPrefix)).iterateAll().forEach(blob -> {
				try {
					storage.delete(blob.getBlobId());
					logger.debug("파일 삭제: {}", blob.getName());
				} catch (Exception e) {
					logger.error("파일 삭제 실패: {}", blob.getName(), e);
				}
			});

			logger.info("deletePointItemFolder: 폴더 삭제 완료. 카테고리명: {}", categoryName);

		} catch (Exception e) {
			logger.error("deletePointItemFolder: 오류 발생 - 카테고리명: {}", categoryName, e);
		}
	}

	/**
	 * 카테고리 이름 변경 (모든 파일을 새 카테고리로 이동)
	 * 
	 * @param pointItemImgList2
	 * @return
	 */
	public List<AdminPointItemImgEntity> updateCategoryName(String newCategoryName, String oldCategoryName,
			List<AdminPointItemImgEntity> oldImgList) {
		List<AdminPointItemImgEntity> newImgList = new ArrayList<>();

		try {
			if (!isEnabled) {
				throw new IOException("Firebase Storage 서비스가 비활성화되어 있습니다.");
			}

			String oldFolderPrefix = "point-items/" + oldCategoryName + "/";
			String newFolderPrefix = "point-items/" + newCategoryName + "/";

			// DB 이미지 경로를 빠르게 찾기 위한 Map 구성
			Map<String, AdminPointItemImgEntity> imgPathMap =
					// 리스트를 하나하나 반복할 수 있게 스트림으로 변경
					oldImgList.stream()
							// 스트림을 다른 형태(여기선 Map)으로 변경
							// Key: imgEntity.getPointItemImgPath()
							// Value: imgEntity 객체 자체
							.collect(Collectors.toMap(AdminPointItemImgEntity::getPointItemImgPath,
									Function.identity()));

			// 스트림(Stream)이란?
			// 데이터를 하나씩 흘려보내면서 처리하는 구조

			// collect()란?
			// 스트림으로 처리한 결과를 List, Set, Map 등 원하는 컬렉션으로 다시 모을 때 사용
			// 리스트를 스트림으로 변환 -> 결과를 Map으로 모음

			// Collectors.toMap(keyMapper, valueMapper)
			// keyMapper: 스트림의 각 요소에서 "키"로 사용할 값을 뽑는 함수
			// valueMapper: 스트림의 각 요소에서 "값"으로 사용할 값을 뽑는 함수

			// Function.identity()는 그냥 그 객체 자체를 뜻함 (x -> x와 같음)

			// 기존 카테고리의 모든 파일 처리
			storage.list(storageBucket, Storage.BlobListOption.prefix(oldFolderPrefix)).iterateAll().forEach(blob -> {
				try {
					String fileName = blob.getName().substring(oldFolderPrefix.length());
					String oldImgPath = blob.getName();
					String newImgPath = newFolderPrefix + fileName;

					// 복사
					BlobId newBlobId = BlobId.of(storageBucket, newImgPath);
					BlobInfo newBlobInfo = BlobInfo.newBuilder(newBlobId).setContentType(blob.getContentType()).build();
					storage.create(newBlobInfo, blob.getContent());

					// 삭제
					storage.delete(blob.getBlobId());

					// URL 생성
					String newImgURL = getFileUrl(newImgPath);
					logger.debug("[파일 이동] '{}' → '{}'", blob.getName(), newImgPath);

					// 해당 이미지 객체 정보 갱신
					AdminPointItemImgEntity imgEntity = imgPathMap.get(oldImgPath);
					if (imgEntity != null) {
						imgEntity.setPointItemImgPath(newImgPath);
						imgEntity.setPointItemImgURL(newImgURL);
						newImgList.add(imgEntity);
					}

				} catch (Exception e) {
					logger.error("파일 이동 실패: {}", blob.getName(), e);
				}
			});

			logger.info("카테고리 이름 변경 완료: '{}' → '{}'", oldCategoryName, newCategoryName);
			return newImgList;

		} catch (Exception e) {
			logger.error("카테고리 이름 변경 중 오류 발생: old='{}', new='{}'", oldCategoryName, newCategoryName, e);
			// 전체 실패 시 빈 리스트 반환
			return Collections.emptyList();
		}
	}

	/**
	 * Storage 인스턴스 반환 (필요한 경우)
	 */
	public Storage getStorage() {
		return this.storage;
	}

	// 기프티콘 이미지 저장
	public PointItemSendLogEntity saveGiftImg(PointItemSendLogEntity pointItemSendLogEntity) {
		try {
			if (!isEnabled) {
				throw new IllegalStateException("Firebase Storage 서비스가 비활성화되어 있습니다.");
			}

			MultipartFile imgFile = pointItemSendLogEntity.getImgFile();

			if (imgFile == null || imgFile.isEmpty()) {
				throw new IllegalArgumentException("업로드할 이미지 파일이 없습니다.");
			}

			String originalFilename = imgFile.getOriginalFilename();
			if (originalFilename == null || originalFilename.trim().isEmpty()) {
				throw new IllegalArgumentException("파일 이름이 유효하지 않습니다.");
			}

			// 파일명 생성
			String newFileName = UUID.randomUUID().toString() + "_" + originalFilename;

			// 업로드 경로 지정
			String memberEmail = pointItemSendLogEntity.getMemberEmail();
			String filePath = "point/item/send/" + memberEmail + "/" + newFileName;

			// Firebase 업로드
			BlobId blobId = BlobId.of(storageBucket, filePath);
			BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType(imgFile.getContentType()).build();

			storage.create(blobInfo, imgFile.getBytes());

			logger.info("파일 업로드 완료 - 경로: {}", filePath);
			logger.debug("생성된 파일 URL: {}", getFileUrl(filePath));

			// 엔티티에 정보 설정
			pointItemSendLogEntity.setImgName(newFileName);
			pointItemSendLogEntity.setImgPath(filePath);
			pointItemSendLogEntity.setImgURL(getFileUrl(filePath));

			return pointItemSendLogEntity;

		} catch (Exception e) {
			logger.error("기프티콘 이미지 업로드 중 오류 발생: {}", e.getMessage(), e);
			throw new RuntimeException("기프티콘 이미지 업로드에 실패했습니다.", e);
		}
	}

}