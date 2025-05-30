package com.mogatshoo.dev.config.file;

import java.io.ByteArrayInputStream;
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

	@Value("${google.drive.project.id:}")
	private String projectId;

	@Value("${google.drive.private.key.id:}")
	private String privateKeyId;

	@Value("${google.drive.private.key:}")
	private String privateKey;

	@Value("${google.drive.client.email:}")
	private String clientEmail;

	@Value("${google.drive.client.id:}")
	private String clientId;

	@Value("${google.drive.folder.id:}")
	private String folderId;

	private Drive driveService;
	private boolean isEnabled = false;

	@PostConstruct
	public void init() {
		if (projectId.isEmpty() || privateKey.isEmpty() || clientEmail.isEmpty() || folderId.isEmpty()) {
			System.out.println("[GoogleDriveService] 설정이 없습니다. 로컬 저장만 사용합니다.");
			this.isEnabled = false;
			return;
		}

		System.out.println("[DEBUG] 프로젝트 ID: " + projectId);
		System.out.println("[DEBUG] 클라이언트 이메일: " + clientEmail);
		System.out.println("[DEBUG] 폴더 ID: " + folderId);

		try {
			// Properties에서 읽은 정보로 JSON 생성
			String jsonCredentials = createJsonCredentials();

			GoogleCredential credential = GoogleCredential
					.fromStream(new ByteArrayInputStream(jsonCredentials.getBytes()))
					.createScoped(Collections.singleton(DriveScopes.DRIVE));

			System.out.println("[DEBUG] 인증 성공!");

			this.driveService = new Drive.Builder(GoogleNetHttpTransport.newTrustedTransport(),
					JacksonFactory.getDefaultInstance(), credential).setApplicationName("Hair Loss Test Application")
					.build();

			System.out.println("[DEBUG] Drive 서비스 생성 성공!");

			// 연결 테스트
			try {
				var result = driveService.files().list().setPageSize(1).execute();
				System.out.println("[DEBUG] 구글 드라이브 연결 확인 성공!");
				this.isEnabled = true;
			} catch (Exception e) {
				System.err.println("[DEBUG] 연결 확인 실패: " + e.getMessage());
				this.isEnabled = false;
			}

		} catch (Exception e) {
			System.err.println("[DEBUG] 초기화 실패: " + e.getMessage());
			this.isEnabled = false;
			this.driveService = null;
		}
	}

	/**
	 * Properties 정보를 사용해서 JSON 문자열 생성
	 */
	private String createJsonCredentials() {
		return String.format("""
				{
				  "type": "service_account",
				  "project_id": "%s",
				  "private_key_id": "%s",
				  "private_key": "%s",
				  "client_email": "%s",
				  "client_id": "%s",
				  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
				  "token_uri": "https://oauth2.googleapis.com/token",
				  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
				  "client_x509_cert_url": "https://www.googleapis.com/robot/v1/metadata/x509/%s",
				  "universe_domain": "googleapis.com"
				}
				""", projectId, privateKeyId, privateKey.replace("\\n", "\n"), // \n 문자 복원
				clientEmail, clientId, clientEmail.replace("@", "%40") // URL 인코딩
		);
	}

	public boolean isEnabled() {
		return this.isEnabled;
	}

	/**
	 * 사용자별 폴더를 생성하거나 찾습니다.
	 */
	public String createOrFindUserFolder(String memberId) throws IOException {
		if (!isEnabled) {
			throw new IOException("구글 드라이브 서비스가 비활성화되어 있습니다.");
		}

		try {
			// 기존 폴더 확인
			var result = driveService
					.files().list().setQ("name='" + memberId
							+ "' and mimeType='application/vnd.google-apps.folder' and parents in '" + folderId + "'")
					.setFields("files(id, name)").execute();

			if (result.getFiles() != null && !result.getFiles().isEmpty()) {
				String userFolderId = result.getFiles().get(0).getId();
				System.out.println("[GoogleDriveService] 기존 사용자 폴더 사용: " + memberId);
				return userFolderId;
			}

			// 새 폴더 생성
			File folderMetadata = new File();
			folderMetadata.setName(memberId);
			folderMetadata.setMimeType("application/vnd.google-apps.folder");
			folderMetadata.setParents(Collections.singletonList(folderId));

			File folder = driveService.files().create(folderMetadata).setFields("id, name").execute();

			System.out.println("[GoogleDriveService] 새 사용자 폴더 생성: " + memberId);
			return folder.getId();

		} catch (Exception e) {
			throw new IOException("사용자 폴더 처리 실패: " + e.getMessage(), e);
		}
	}
	/**
	 * 폴더 접근 권한 테스트용 메서드
	 */
	public void testFolderAccess() {
	    if (!isEnabled) {
	        System.out.println("[DEBUG] 구글 드라이브 서비스가 비활성화됨");
	        return;
	    }
	    
	    try {
	        System.out.println("[DEBUG] 폴더 접근 테스트 시작");
	        System.out.println("[DEBUG] 대상 폴더 ID: " + folderId);
	        System.out.println("[DEBUG] 서비스 계정: " + clientEmail);
	        
	        // 폴더 정보 조회 시도
	        var folder = driveService.files().get(folderId).setFields("id,name,owners,permissions").execute();
	        System.out.println("[DEBUG] 폴더명: " + folder.getName());
	        System.out.println("[DEBUG] 폴더 소유자: " + folder.getOwners());
	        
	        // 폴더 내 파일 목록 조회 시도
	        var result = driveService.files().list()
	            .setQ("parents in '" + folderId + "'")
	            .setFields("files(id,name)")
	            .setPageSize(5)
	            .execute();
	            
	        System.out.println("[DEBUG] 폴더 내 파일 개수: " + (result.getFiles() != null ? result.getFiles().size() : 0));
	        
	    } catch (Exception e) {
	        System.err.println("[DEBUG] 폴더 접근 실패: " + e.getMessage());
	        e.printStackTrace();
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
	        String userFolderId = createOrFindUserFolder(memberId);

	        File fileMetadata = new File();
	        fileMetadata.setName(fileName);
	        fileMetadata.setParents(Collections.singletonList(userFolderId));

	        InputStreamContent mediaContent = new InputStreamContent(file.getContentType(), file.getInputStream());

	        File uploadedFile = driveService.files().create(fileMetadata, mediaContent).setFields("id,name").execute();

	        // 🔥 디버깅 로그 추가!
	        System.out.println("=== 구글 드라이브 업로드 디버깅 ===");
	        System.out.println("멤버 ID: " + memberId);
	        System.out.println("파일명: " + fileName);
	        System.out.println("업로드된 파일명: " + uploadedFile.getName());
	        System.out.println("실제 파일 ID: " + uploadedFile.getId());
	        System.out.println("생성될 URL: " + getFileUrl(uploadedFile.getId()));
	        System.out.println("=====================================");

	        makeFilePublic(uploadedFile.getId());

	        return uploadedFile.getId();

	    } catch (Exception e) {
	        throw new IOException("사용자 폴더 파일 업로드 실패: " + e.getMessage(), e);
	    }
	}

	private void makeFilePublic(String fileId) {
		try {
			Permission permission = new Permission().setType("anyone").setRole("reader");

			driveService.permissions().create(fileId, permission).execute();

			System.out.println("[GoogleDriveService] 파일 공개 설정 완료");

		} catch (Exception e) {
			System.err.println("[GoogleDriveService] 파일 공개 설정 실패: " + e.getMessage());
		}
	}

	public String getFileUrl(String fileId) {
	    if (fileId == null || fileId.isEmpty()) {
	        return null;
	    }
	    // 이 형식이 HTML에서 더 잘 작동?
	    return "https://lh3.googleusercontent.com/d/" + fileId;
	}

	public void deleteFile(String fileId) throws IOException {
		if (!isEnabled || fileId == null || fileId.isEmpty()) {
			return;
		}

		try {
			driveService.files().delete(fileId).execute();
			System.out.println("[GoogleDriveService] 파일 삭제 성공");
		} catch (Exception e) {
			System.err.println("[GoogleDriveService] 파일 삭제 실패: " + e.getMessage());
		}
	}
	
	public void deleteUserFolder(String memberId) throws IOException {
	    if (!isEnabled || memberId == null || memberId.isEmpty()) {
	        return;
	    }

	    try {
	        // 1. 사용자 폴더 찾기
	        var result = driveService
	                .files().list()
	                .setQ("name='" + memberId + "' and mimeType='application/vnd.google-apps.folder' and parents in '" + folderId + "'")
	                .setFields("files(id, name)")
	                .execute();

	        if (result.getFiles() == null || result.getFiles().isEmpty()) {
	            System.out.println("[GoogleDriveService] 삭제할 사용자 폴더가 없습니다: " + memberId);
	            return;
	        }

	        String userFolderId = result.getFiles().get(0).getId();
	        
	        // 2. 폴더 안의 모든 파일 조회
	        var filesInFolder = driveService
	                .files().list()
	                .setQ("parents in '" + userFolderId + "'")
	                .setFields("files(id, name)")
	                .execute();

	        // 3. 폴더 안의 모든 파일 삭제
	        if (filesInFolder.getFiles() != null) {
	            for (var file : filesInFolder.getFiles()) {
	                try {
	                    driveService.files().delete(file.getId()).execute();
	                    System.out.println("[GoogleDriveService] 폴더 내 파일 삭제: " + file.getName());
	                } catch (Exception e) {
	                    System.err.println("[GoogleDriveService] 파일 삭제 실패: " + file.getName() + " - " + e.getMessage());
	                }
	            }
	        }

	        // 4. 빈 폴더 삭제
	        driveService.files().delete(userFolderId).execute();
	        System.out.println("[GoogleDriveService] 사용자 폴더 삭제 완료: " + memberId);

	    } catch (Exception e) {
	        System.err.println("[GoogleDriveService] 사용자 폴더 삭제 실패: " + e.getMessage());
	        throw new IOException("사용자 폴더 삭제 실패: " + e.getMessage(), e);
	    }
	}
	
	
	// 프록시로 전달하는 메ㅔ서드추가 ㅎ
	public Drive getDriveService() {
	    return this.driveService;
	}
}