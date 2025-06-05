package com.mogatshoo.dev.config.file;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

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

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .setStorageBucket(storageBucket)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            this.storage = StorageOptions.newBuilder()
                    .setCredentials(credentials)
                    .setProjectId(projectId)
                    .build()
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
                """, type, projectId, privateKeyId, privateKey.replace("\\n", "\n"),
                clientEmail, clientId, authUri, tokenUri, authProviderCertUrl,
                clientCertUrl, universeDomain);
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
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(file.getContentType())
                    .build();

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
        return String.format("https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media", 
                storageBucket, filePath.replace("/", "%2F"));
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
            storage.list(storageBucket, Storage.BlobListOption.prefix(folderPrefix))
                    .iterateAll()
                    .forEach(blob -> {
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
            BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                    .setContentType(imgFile.getContentType())
                    .build();

            Blob blob = storage.create(blobInfo, imgFile.getBytes());

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
                BlobInfo newBlobInfo = BlobInfo.newBuilder(newBlobId)
                        .setContentType(oldBlob.getContentType())
                        .build();
                
                storage.create(newBlobInfo, oldBlob.getContent());
                storage.delete(oldBlobId);

                logger.info("파일이 성공적으로 카테고리 이동됨. {} → {}", oldFilePath, newFilePath);
            }

        } catch (Exception e) {
            logger.error("moveImgToNewCategory: 파일 이동 중 오류 발생. oldPath: {}, {} → {}", 
                    oldFilePath, oldCategoryName, newCategoryName, e);
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
            boolean hasFiles = storage.list(storageBucket, 
                    Storage.BlobListOption.prefix(folderPrefix),
                    Storage.BlobListOption.pageSize(1))
                    .iterateAll()
                    .iterator()
                    .hasNext();

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
            storage.list(storageBucket, Storage.BlobListOption.prefix(folderPrefix))
                    .iterateAll()
                    .forEach(blob -> {
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
     */
    public void updateCategoryName(String newCategoryName, String oldCategoryName) {
        try {
            if (!isEnabled) {
                throw new IOException("Firebase Storage 서비스가 비활성화되어 있습니다.");
            }

            String oldFolderPrefix = "point-items/" + oldCategoryName + "/";
            String newFolderPrefix = "point-items/" + newCategoryName + "/";

            // 기존 카테고리의 모든 파일 조회
            storage.list(storageBucket, Storage.BlobListOption.prefix(oldFolderPrefix))
                    .iterateAll()
                    .forEach(blob -> {
                        try {
                            String fileName = blob.getName().substring(oldFolderPrefix.length());
                            String newFilePath = newFolderPrefix + fileName;
                            
                            // 새 위치에 파일 복사
                            BlobId newBlobId = BlobId.of(storageBucket, newFilePath);
                            BlobInfo newBlobInfo = BlobInfo.newBuilder(newBlobId)
                                    .setContentType(blob.getContentType())
                                    .build();
                            
                            storage.create(newBlobInfo, blob.getContent());
                            
                            // 기존 파일 삭제
                            storage.delete(blob.getBlobId());
                            
                            logger.debug("[파일 이동] '{}' → '{}' 완료", blob.getName(), newFilePath);
                            
                        } catch (Exception e) {
                            logger.error("파일 이동 실패: {}", blob.getName(), e);
                        }
                    });

            logger.info("카테고리 이름 변경 완료: '{}' → '{}'", oldCategoryName, newCategoryName);

        } catch (Exception e) {
            logger.error("카테고리 이름 변경 중 오류 발생: old='{}', new='{}'", oldCategoryName, newCategoryName, e);
        }
    }

    /**
     * Storage 인스턴스 반환 (필요한 경우)
     */
    public Storage getStorage() {
        return this.storage;
    }
}