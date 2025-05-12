# 📌 모가슈?
毛(털모) + 갔슈? (‘가다’의 전라도 사투리)
<p align="center">
  <img src="" width="300" alt="Image">
</p>

## 📖 소개
탈모인들을 위한 익명 공감 커뮤니티 앱

## 목표
1. 익명 기반 공감 커뮤니티 구축
탈모에 대한 민감한 고민을 익명으로 질문하고 투표할 수 있는 안전한 공간 제공
2. 탈모 단계별 맞춤 정보 제공
사용자 분석을 통해 탈모 단계, 지역, 성별에 맞는 질문·투표·추천 콘텐츠 제공
3. 유쾌한 위로와 소통
‘탈모’라는 무거운 주제를 유머와 공감으로 풀어가는 커뮤니티 지향
4. 지역 기반 병원 정보와 후기 공유
공공데이터 + 사용자 후기를 통한 병원 정보 제공으로 실제적인 도움 연결


### 회원 기능
<p align="center">
  <img src="https://github.com/user-attachments/assets/3f45523e-659d-4b2a-8f6a-f6d2d9b5aece" width="300" alt="Image">
</p>

### 숙소와 관리자 기능
<p align="center">
  <img src="https://github.com/user-attachments/assets/872ef4a1-5491-4106-814e-6b1300c32e3d" width="300" alt="Image">
</p>

## 🛠️ 기술 스택
### Database ERD 
<p align="center">
  <img src="https://github.com/user-attachments/assets/5e63ed13-4394-4687-98c1-6e705bec1f95" width="300" alt="Image">
</p>


### 백엔드
<img src="https://github.com/user-attachments/assets/098cf956-2b31-4b9c-b796-a2a9c490481a" width="24" height="24"> Java 11: https://www.oracle.com/java/technologies/javase/jdk11-archive-downloads.html<br>
<img src="https://github.com/user-attachments/assets/d713e7ce-f1c9-4b7e-b304-86b1ec4a2377" width="24" height="24"> Spring MVC: https://spring.io/projects/spring-framework<br>
<img src="https://github.com/user-attachments/assets/8ad9bfd9-ed84-44fb-b90a-7ebc580162e7" width="24" height="24"> Spring AOP: https://spring.io/projects/spring-framework<br>
<img src="https://github.com/user-attachments/assets/de769007-690e-4e91-bd1b-305c0f3f7ce5" width="24" height="24"> Spring Security: https://spring.io/projects/spring-security<br>
<img src="https://github.com/user-attachments/assets/fbc5f08e-b5c0-4b23-852a-006458bcc831" width="24" height="24"> MyBatis: https://mybatis.org/mybatis-3/

### DB & WAS
<img src="https://github.com/user-attachments/assets/e8b69b4f-e623-4c5a-ba5e-c5a644282c98" width="24" height="24"> Oracle: https://www.oracle.com/database/technologies/oracle-database-software-downloads.html<br>
<img src="https://github.com/user-attachments/assets/74dcb744-5b38-4df5-86b6-5059eaacf733" width="24" height="24"> Oracle Cloud: https://www.oracle.com/cloud/<br>
<img src="https://github.com/user-attachments/assets/48b60e17-a537-4b78-a534-fa5cf5e4e391" width="24" height="24"> Tomcat 9: https://tomcat.apache.org/download-90.cgi

### 프론트엔드
<img src="https://github.com/user-attachments/assets/4f7fc0b5-6a86-41e4-8cde-1752fb98b0ca" width="24" height="24"> HTML/CSS<br>
<img src="https://github.com/user-attachments/assets/d58810d0-40ef-471b-b952-f712c0f9a279" width="24" height="24"> JavaScript<br>
<img src="https://github.com/user-attachments/assets/9edd1866-b56d-4d44-8655-8c6826437007" width="24" height="24"> jQuery: https://jquery.com/download/

## 📋 개발 환경
```
- 개발 환경
  - Java 1.6
  - Maven (빌드 관리 도구)

- 백엔드
  - Spring Framework 4.1.1.RELEASE
  - Spring MVC (웹 MVC 구현)
  - Spring AOP (관점 지향 프로그래밍)
  - Spring JDBC (데이터베이스 연동)
  - Spring Context Support (이메일 지원)
  - AspectJ 1.6.10 (AOP 구현)

- 데이터베이스
  - Oracle Database (ojdbc8 19.3.0.0)
  - MyBatis 3.4.6 (SQL 매핑 프레임워크)
  - MyBatis-Spring 1.3.2 (MyBatis와 Spring 연동)

- 웹 서버
  - Apache Tomcat (Servlet 2.5 지원)

- 프론트엔드
  - JSP 2.1
  - JSTL 1.2
  - Apache Tiles 2.2.2 (페이지 레이아웃 템플릿)

- 유틸리티
  - Lombok (코드 간소화)
  - Jackson 2.5.4 (JSON 처리)
  - Commons FileUpload 1.2.1 (파일 업로드)
  - Thumbnailator 0.4.8 (이미지 썸네일)
  - JavaMail 1.5.4 (이메일 기능)
  - Google API Client 1.34.0 (구글 로그인)

- 로깅
  - SLF4J 1.6.6
  - Log4j 1.2.15
```

## 📊 프로젝트 구조
```
Urban_Village
├── src/main/java/com.test.Urban_Village
│   ├── accommodation   # 숙소 관련 기능
│   ├── admin           # 관리자 기능
│   ├── apiLogin        # API 로그인(구글 등)
│   ├── cleaner         # 청소 관리자 기능
│   ├── common          # 일반적인 인터셉터 기능
│   ├── email           # 이메일 기능
│   ├── member          # 회원 관리
│   ├── reservation     # 예약 시스템
│   ├── review          # 리뷰 기능
│   ├── wishList        # 위시리스트 기능
│   └── MainController.java
├── src/main/resources
│   └── mybatis         # MyBatis 매핑 설정
├── webapp
│   ├── resources
│   │   ├── css
│   │   ├── helpCenterImage
│   │   └── WebSiteImages
│   └── WEB-INF
│       ├── spring      # Spring 설정
│       ├── tiles       # 타일즈 레이아웃 설정
│       └── views       # JSP 뷰 파일들
└── pom.xml             # Maven 프로젝트 설정
```

## 🔖 사용 API
- 구글 로그인 API: https://developers.google.com/identity/gsi/web/guides/overview
- 카카오 지도 API: https://apis.map.kakao.com/web/guide/
- 포트원 결제 API: https://developers.portone.io/docs/ko/overview
- 도로명주소 API: https://www.juso.go.kr/addrlink/devAddrLinkRequestGuide.do

## 🤝 기여자
### 종로 itkgbank 핀테그 풀스텍 수강자 
- 김대중 : [노션 포트폴리오](https://www.notion.so/1e31366431dd802f8235cef0bca33a5d?pvs=12)
- 김찬영 : 
- 서하나 : [노션 포트폴리오](https://www.notion.so/1b9db822a28e801eb04af2f96ab48d5a)
- 최승필 : [노션 포트폴리오](https://www.notion.so/1e3c0872a5f880ac8e10e228219cd836?pvs=4)

<div align="center">
  <p>⭐️ 이 프로젝트가 마음에 드셨다면 star를 눌러주세요! ⭐️</p>
</div>
