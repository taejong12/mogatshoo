package com.mogatshoo.dev.config.file;

import java.io.ByteArrayOutputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.mogatshoo.dev.config.file.GoogleDriveService;

@RestController
public class ImageProxyController {

    @Autowired
    private GoogleDriveService googleDriveService;

    @GetMapping("/proxy/image/{fileId}")
    public ResponseEntity<byte[]> getImage(@PathVariable("fileId") String fileId) {
        try {
            System.out.println("[ImageProxy] 이미지 요청: " + fileId);

            // 구글 드라이브에서 이미지 다운로드
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            googleDriveService.getDriveService().files().get(fileId).executeMediaAndDownloadTo(outputStream);

            byte[] imageBytes = outputStream.toByteArray();

            System.out.println("[ImageProxy] 이미지 크기: " + imageBytes.length + " bytes");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.IMAGE_JPEG);
            headers.setCacheControl("public, max-age=3600"); // 1시간 캐시

            return ResponseEntity.ok().headers(headers).body(imageBytes);

        } catch (Exception e) {
            System.err.println("[ImageProxy] 이미지 로드 실패: " + e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}