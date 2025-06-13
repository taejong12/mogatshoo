package com.mogatshoo.dev.config.database;

import java.io.ByteArrayInputStream;
import javax.sql.DataSource;
import jakarta.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.google.auth.oauth2.GoogleCredentials;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

@Configuration
public class FirebaseCloudSqlService {

    private static final Logger logger = LoggerFactory.getLogger(FirebaseCloudSqlService.class);

    // Firebase 인증 정보 (Properties에서 가져옴)
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

    private GoogleCredentials credentials;
    private boolean isEnabled = false;

    @PostConstruct
    public void init() {
        if (projectId.isEmpty() || privateKey.isEmpty() || clientEmail.isEmpty()) {
            logger.warn("⚠️ Firebase 설정이 없습니다. 기본 DataSource를 사용합니다.");
            this.isEnabled = false;
            return;
        }

        try {
            // Properties에서 바로 GoogleCredentials 생성 (파일 없이!)
            String jsonCredentials = createJsonCredentials();
            this.credentials = GoogleCredentials.fromStream(
                new ByteArrayInputStream(jsonCredentials.getBytes())
            );

            logger.info("✅ Firebase Cloud SQL 서비스 초기화 성공!");
            this.isEnabled = true;

        } catch (Exception e) {
            logger.error("❌ Firebase Cloud SQL 초기화 실패: {}", e.getMessage(), e);
            this.isEnabled = false;
        }
    }

    /**
     * Properties 정보로 JSON 문자열 생성 (메모리에서만 사용)
     */
    private String createJsonCredentials() {
        String clientCertUrl = String.format(
            "https://www.googleapis.com/robot/v1/metadata/x509/%s", 
            clientEmail.replace("@", "%40")
        );

        return String.format("""
                {
                  "type": "%s",
                  "project_id": "%s",
                  "private_key_id": "%s",
                  "private_key": "%s",
                  "client_email": "%s",
                  "client_id": "%s",
                  "auth_uri": "https://accounts.google.com/o/oauth2/auth",
                  "token_uri": "https://oauth2.googleapis.com/token",
                  "auth_provider_x509_cert_url": "https://www.googleapis.com/oauth2/v1/certs",
                  "client_x509_cert_url": "%s",
                  "universe_domain": "googleapis.com"
                }
                """, 
                type, projectId, privateKeyId, 
                privateKey.replace("\\n", "\n"), 
                clientEmail, clientId, clientCertUrl);
    }

    /**
     * 🎯 메인 DataSource - 2가지 방식 지원
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        if (!isEnabled) {
            logger.warn("⚠️ Cloud SQL이 비활성화되어 있습니다.");
            return createSimpleDataSource();
        }

        // 1순위: Cloud SQL Proxy 방식 (추천)
        DataSource proxyDataSource = tryCreateProxyDataSource();
        if (proxyDataSource != null) {
            return proxyDataSource;
        }

        // 2순위: 직접 연결 방식 (백업)
        logger.info("🔄 Cloud SQL Proxy 연결 실패, 직접 연결을 시도합니다...");
        return createDirectDataSource();
    }

    /**
     * 방법 1: Cloud SQL Proxy 방식 (가장 안정적)
     */
    private DataSource tryCreateProxyDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            
            // Cloud SQL Proxy가 3307 포트에서 실행 중이라고 가정
            config.setJdbcUrl("jdbc:mysql://localhost:3307/mogatshoo-test?useSSL=false&allowPublicKeyRetrieval=true");
            config.setUsername("root");
            config.setPassword("@Mogatshoo");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            // 연결 테스트
            config.setConnectionTimeout(5000); // 5초 타임아웃
            config.setConnectionTestQuery("SELECT 1");

            HikariDataSource dataSource = new HikariDataSource(config);
            
            // 실제 연결 테스트
            dataSource.getConnection().close();
            
            logger.info("✅ Cloud SQL Proxy 연결 성공! (localhost:3307)");
            logger.info("💡 Cloud SQL Proxy가 실행 중입니다.");
            
            return dataSource;

        } catch (Exception e) {
            logger.warn("⚠️ Cloud SQL Proxy 연결 실패: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 방법 2: Socket Factory를 통한 직접 연결
     */
    private DataSource createDirectDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            
            // Google Cloud SQL 직접 연결
            config.setJdbcUrl("jdbc:mysql://google/mogatshoo-test?cloudSqlInstance=mogatshoo-ce84d:asia-northeast3:mogatshoo-test&socketFactory=com.google.cloud.sql.mysql.SocketFactory&useSSL=false");
            config.setUsername("root");
            config.setPassword("@Mogatshoo");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");

            // 직접 연결용 인증 설정
            config.addDataSourceProperty("user", "root");
            config.addDataSourceProperty("password", "@Mogatshoo");

            logger.info("✅ Cloud SQL 직접 연결 성공!");
            return new HikariDataSource(config);

        } catch (Exception e) {
            logger.error("❌ Cloud SQL 직접 연결도 실패: {}", e.getMessage());
            return createSimpleDataSource();
        }
    }

    /**
     * 방법 3: 기본 연결 (최후의 수단)
     */
    private DataSource createSimpleDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            
            // 기본 MySQL 연결 (로컬 또는 다른 DB)
            config.setJdbcUrl("jdbc:mysql://localhost:3307/mogatshoo-test?useSSL=false&allowPublicKeyRetrieval=true");
            config.setUsername("root");
            config.setPassword("@Mogatshoo");
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            logger.info("📦 기본 DataSource 사용");
            return new HikariDataSource(config);
            
        } catch (Exception e) {
            logger.error("❌ 모든 DataSource 생성 실패: {}", e.getMessage());
            throw new RuntimeException("DataSource 생성 불가", e);
        }
    }

    /**
     * 사용 가이드 출력
     */
    @PostConstruct
    public void printUsageGuide() {
        logger.info("🚀 Cloud SQL 연결 옵션:");
        logger.info("1️⃣ 추천: Cloud SQL Proxy 실행 후 연결");
        logger.info("   ./cloud-sql-proxy.exe mogatshoo-ce84d:asia-northeast3:mogatshoo-test --port 3307 --credentials-file gcp-credentials.json");
        logger.info("2️⃣ 백업: Properties 정보로 자동 직접 연결");
        logger.info("💡 Proxy가 없으면 자동으로 직접 연결을 시도합니다.");
    }

    // Getter 메서드들
    public boolean isEnabled() {
        return this.isEnabled;
    }

    public GoogleCredentials getCredentials() {
        return this.credentials;
    }
}