package com.mogatshoo.dev.config.file;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;

@Service
public class GoogleDriveService {
    
    @Value("${google.drive.credentials.file.path:}")
    private String credentialsFilePath;
    
    @Value("${google.drive.folder.id:}")
    private String folderId;
    
    private Drive driveService;
    private boolean isEnabled = false;
    
    @PostConstruct
    public void init() {
        if (credentialsFilePath.isEmpty() || folderId.isEmpty()) {
            System.out.println("[GoogleDriveService] 설정이 없습니다.");
            this.isEnabled = false;
            return;
        }
        
        System.out.println("[DEBUG] Credentials path: " + credentialsFilePath);
        System.out.println("[DEBUG] Folder ID: " + folderId);
        
        try {
            GoogleCredential credential = GoogleCredential
                .fromStream(new FileInputStream(credentialsFilePath))
                .createScoped(Collections.singleton(DriveScopes.DRIVE));
            
            System.out.println("[DEBUG] 인증 성공!");
            
            this.driveService = new Drive.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    JacksonFactory.getDefaultInstance(),
                    credential)
                    .setApplicationName("Hair Loss Test Application")
                    .build();
            
            System.out.println("[DEBUG] Drive 서비스 생성 성공!");
            
            // 파일 목록 조회로 연결 확인 (폴더 체크 대신)
            try {
                var result = driveService.files().list()
                        .setPageSize(1)
                        .execute();
                System.out.println("[DEBUG] 구글 드라이브 연결 확인 성공!");
                
            } catch (Exception listError) {
                System.err.println("[DEBUG] 연결 확인 실패: " + listError.getMessage());
            }
            
            // 서비스 활성화 (폴더 체크 생략)
            System.out.println("[DEBUG] 서비스를 활성화합니다");
            this.isEnabled = true;
            
        } catch (Exception e) {
            System.err.println("[DEBUG] 초기화 실패: " + e.getMessage());
            this.isEnabled = false;
            this.driveService = null;
        }
    }
    
    /**
     * 구글 드라이브 서비스가 사용 가능한지 확인
     */
    public boolean isEnabled() {
        return this.isEnabled;
    }
    
    /**
     * 파일을 구글 드라이브에 업로드합니다.
     */
    public String uploadFile(MultipartFile file, String fileName) throws IOException {
        if (!isEnabled) {
            throw new IOException("구글 드라이브 서비스가 비활성화되어 있습니다.");
        }
        
        try {
            // 1. 파일 메타데이터 설정
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(folderId));
            
            // 2. 파일 컨텐츠 설정
            InputStreamContent mediaContent = new InputStreamContent(
                    file.getContentType(),
                    file.getInputStream());
            
            // 3. 파일 업로드
            File uploadedFile = driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id,name")
                    .execute();
            
            System.out.println("[GoogleDriveService] 파일 업로드 성공: " + uploadedFile.getName() 
                             + " (ID: " + uploadedFile.getId() + ")");
            
            // 4. 파일을 공개로 설정
            makeFilePublic(uploadedFile.getId());
            
            return uploadedFile.getId();
            
        } catch (Exception e) {
            System.err.println("[GoogleDriveService] 파일 업로드 실패: " + e.getMessage());
            throw new IOException("구글 드라이브 업로드 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 파일을 공개로 설정합니다.
     */
    private void makeFilePublic(String fileId) {
        try {
            Permission permission = new Permission()
                    .setType("anyone")
                    .setRole("reader");
            
            driveService.permissions()
                    .create(fileId, permission)
                    .execute();
            
            System.out.println("[GoogleDriveService] 파일 공개 설정 완료: " + fileId);
            
        } catch (Exception e) {
            System.err.println("[GoogleDriveService] 파일 공개 설정 실패: " + e.getMessage());
        }
    }
    
    /**
     * 파일 ID로 공개 URL을 생성합니다.
     */
    public String getFileUrl(String fileId) {
        if (fileId == null || fileId.isEmpty()) {
            return null;
        }
        return "https://drive.google.com/uc?id=" + fileId;
    }
    
    /**
     * 구글 드라이브에서 파일을 삭제합니다.
     */
    public void deleteFile(String fileId) throws IOException {
        if (!isEnabled || fileId == null || fileId.isEmpty()) {
            System.out.println("[GoogleDriveService] 파일 삭제를 건너뜁니다.");
            return;
        }
        
        try {
            driveService.files().delete(fileId).execute();
            System.out.println("[GoogleDriveService] 파일 삭제 성공: " + fileId);
            
        } catch (Exception e) {
            System.err.println("[GoogleDriveService] 파일 삭제 실패: " + e.getMessage());
        }
    }
    
    
    /**
     * 사용자별 폴더를 생성하거나 찾습니다.
     */
    public String createOrFindUserFolder(String memberId) throws IOException {
        if (!isEnabled) {
            throw new IOException("구글 드라이브 서비스가 비활성화되어 있습니다.");
        }
        
        try {
            // 1. 기존에 해당 이름의 폴더가 있는지 확인
            var result = driveService.files().list()
                    .setQ("name='" + memberId + "' and mimeType='application/vnd.google-apps.folder' and parents in '" + folderId + "'")
                    .setFields("files(id, name)")
                    .execute();
            
            // 2. 이미 폴더가 있으면 해당 폴더 ID 반환
            if (result.getFiles() != null && !result.getFiles().isEmpty()) {
                String userFolderId = result.getFiles().get(0).getId();
                System.out.println("[GoogleDriveService] 기존 사용자 폴더 사용: " + memberId + " (ID: " + userFolderId + ")");
                return userFolderId;
            }
            
            // 3. 폴더가 없으면 새로 생성
            File folderMetadata = new File();
            folderMetadata.setName(memberId);
            folderMetadata.setMimeType("application/vnd.google-apps.folder");
            folderMetadata.setParents(Collections.singletonList(folderId));
            
            File folder = driveService.files()
                    .create(folderMetadata)
                    .setFields("id, name")
                    .execute();
            
            System.out.println("[GoogleDriveService] 새 사용자 폴더 생성: " + memberId + " (ID: " + folder.getId() + ")");
            return folder.getId();
            
        } catch (Exception e) {
            System.err.println("[GoogleDriveService] 사용자 폴더 생성/조회 실패: " + e.getMessage());
            throw new IOException("사용자 폴더 처리 실패: " + e.getMessage(), e);
        }
    }
    
    
    /**
     * 사용자 폴더에 파일을 업로드합니다.
     */
    public String uploadFileToUserFolder(MultipartFile file, String memberId, String fileName) throws IOException {
        if (!isEnabled) {
            throw new IOException("구글 드라이브 서비스가 비활성화되어 있습니다.");
        }
        
        try {
            // 1. 사용자 폴더 생성/조회
            String userFolderId = createOrFindUserFolder(memberId);
            
            // 2. 파일 메타데이터 설정 (사용자 폴더에 저장)
            File fileMetadata = new File();
            fileMetadata.setName(fileName);
            fileMetadata.setParents(Collections.singletonList(userFolderId)); // 사용자 폴더에 저장
            
            // 3. 파일 컨텐츠 설정
            InputStreamContent mediaContent = new InputStreamContent(
                    file.getContentType(),
                    file.getInputStream());
            
            // 4. 파일 업로드
            File uploadedFile = driveService.files()
                    .create(fileMetadata, mediaContent)
                    .setFields("id,name")
                    .execute();
            
            System.out.println("[GoogleDriveService] 사용자 폴더에 파일 업로드 성공: " + uploadedFile.getName() 
                             + " (폴더: " + memberId + ", 파일ID: " + uploadedFile.getId() + ")");
            
            // 5. 파일을 공개로 설정
            makeFilePublic(uploadedFile.getId());
            
            return uploadedFile.getId();
            
        } catch (Exception e) {
            System.err.println("[GoogleDriveService] 사용자 폴더 파일 업로드 실패: " + e.getMessage());
            throw new IOException("사용자 폴더 파일 업로드 실패: " + e.getMessage(), e);
        }
    }
    
    
    /**
     * 구글 드라이브 연결 상태를 테스트합니다.
     */
    public String testConnection() {
        if (!isEnabled) {
            return "구글 드라이브가 비활성화되어 있습니다. 설정을 확인해주세요.";
        }
        
        try {
            File folder = driveService.files().get(folderId).execute();
            return "연결 성공! 폴더: " + folder.getName() + " (ID: " + folderId + ")";
            
        } catch (Exception e) {
            return "연결 실패: " + e.getMessage();
        }
    }
}