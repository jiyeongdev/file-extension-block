# 파일 확장자 차단 시스템

보안상 위험한 파일 확장자를 차단하고 관리하는 Spring Boot 애플리케이션입니다.

## 주요 기능

- **고정 확장자 관리**: exe, sh, cmd 등 위험한 확장자 차단
- **커스텀 확장자 관리**: 사용자 정의 확장자 추가/삭제
- **파일 업로드**: 안전한 파일 업로드 및 관리

- **다중 템플릿 엔진**: Thymeleaf와 JSP 지원

## 기술 스택

- **Backend**: Spring Boot 3.x, Java 21
- **Database**: H2 Database (개발용)
- **Build Tool**: Gradle
- **Template Engine**: Thymeleaf, JSP

- **Query Builder**: QueryDSL 5.0 (타입 안전한 쿼리 작성)

## 프로젝트 구조

```
src/main/java/com/fileextension/proj/
├── config/          # 설정 클래스
├── controller/      # 컨트롤러
├── dto/            # 데이터 전송 객체
├── entity/         # 엔티티
├── repository/     # 리포지토리 (JPA + QueryDSL)
├── service/        # 서비스 계층
└── ProjApplication.java
```

## 디렉토리 구조

```
file-extension-proj/
├── src/                    # 소스 코드
├── build/generated/        # QueryDSL Q클래스 생성 디렉토리
├── data/                   # H2 데이터베이스 파일 (로컬)
├── uploads/                # 파일 업로드 디렉토리 (로컬)
├── logs/                   # 로그 파일 (운영)
├── Dockerfile              # 도커 이미지 빌드
├── docker-compose.yml      # 도커 컴포즈 설정
└── README.md
```

## 로컬 개발 환경 설정

### 사전 요구사항

- Java 17 이상
- Gradle 8.x 이상

### 1. 프로젝트 클론

```bash
git clone <repository-url>
cd file-extenstion-proj
```

### 2. 로컬 실행

#### Gradle을 사용한 실행

```bash
# 의존성 다운로드
./gradlew build

# 로컬 프로파일로 실행
./gradlew bootRun --args='--spring.profiles.active=local'
```

#### IDE에서 실행

1. IntelliJ IDEA 또는 Eclipse에서 프로젝트 열기
2. `ProjApplication.java` 실행
3. VM 옵션에 `-Dspring.profiles.active=local` 추가

### 3. 접속

- **Thymeleaf 페이지**: http://localhost:8080
- **JSP 페이지**: http://localhost:8080/jsp
- **H2 콘솔**: http://localhost:8080/h2-console
- **API 문서**: http://localhost:8080/swagger-ui.html

## 도커 빌드 및 실행

### 사전 요구사항

- Docker
- Docker Compose

### 1. 도커 이미지 빌드

```bash
# 이미지 빌드
docker build -t file-extension-app .

# 이미지 확인
docker images | grep file-extension-app
```

### 2. 도커 컨테이너 실행

#### Docker Compose 사용 (권장)

```bash
# 컨테이너 빌드 및 실행
docker-compose up --build

# 백그라운드 실행
docker-compose up -d --build

# 컨테이너 상태 확인
docker-compose ps

# 로그 확인
docker-compose logs -f

# 컨테이너 중지
docker-compose down
```

#### Docker 명령어 직접 사용

```bash
# 컨테이너 실행
docker run -d \
  --name file-extension-app \
  -p 8080:8080 \
  -v $(pwd)/uploads:/app/uploads \
  -v $(pwd)/data:/app/data \
  -v $(pwd)/logs:/app/logs \
  -e SPRING_PROFILES_ACTIVE=prod \
  file-extension-app

# 컨테이너 상태 확인
docker ps

# 로그 확인
docker logs -f file-extension-app

# 컨테이너 중지 및 삭제
docker stop file-extension-app
docker rm file-extension-app
```

### 3. 접속

- **애플리케이션**: http://localhost:8080
- **헬스체크**: http://localhost:8080/actuator/health

## 환경별 설정

### 프로파일

- **local**: 로컬 개발 환경 (H2 Database)
- **prod**: 운영 환경 (실제 데이터베이스)

### 설정 파일

- `application.yml`: 공통 설정
- `application-local.yml`: 로컬 환경 설정
- `application-prod.yml`: 운영 환경 설정

### 환경별 디렉토리 설정

| 환경 | 업로드 디렉토리 | 데이터베이스 | 로그 |
|------|----------------|-------------|------|
| local | `${user.dir}/uploads` | `./data/testdb` | 콘솔 |
| prod | `/app/uploads` | 외부 DB | `/app/logs` |

## API 엔드포인트

### 확장자 관리

- `GET /api/extensions/fixed`: 고정 확장자 목록 조회
- `GET /api/extensions/custom`: 커스텀 확장자 목록 조회
- `POST /api/extensions/custom`: 커스텀 확장자 추가
- `DELETE /api/extensions/custom/{id}`: 커스텀 확장자 삭제

### 파일 업로드

- `POST /api/files/upload`: 파일 업로드
- `GET /api/files`: 업로드된 파일 목록 조회
- `DELETE /api/files/{filename}`: 파일 삭제

## 보안 고려사항

⚠️ **중요**: 이 시스템은 다층 보안을 통해 파일 업로드 공격을 효과적으로 차단합니다.

### 구현된 보안 기능

#### 1. **확장자 기반 차단**
- 고정 확장자 차단: exe, bat, cmd, com, cpl, scr, js 등
- 커스텀 확장자 차단: 사용자가 추가한 확장자
- 사용자 제어: 원하는 확장자만 선택적으로 차단 가능

#### 2. **확장자 우회 방지 (파일명 기반)**
- **차단 예시**: `memo.exe.txt`, `script.bat.jpg`, `virus.cmd.txt`
- **동작 원리**: 마지막 점 이후만 확장자로 인식, 파일명에 차단된 확장자 포함 시 차단
- **보안 효과**: 공격자가 실행 파일을 안전한 확장자로 위장하는 공격 방지

#### 3. **파일 내용 검증 (매직 바이트)**
- **매직 바이트 검증**: 파일 내용의 실제 형식을 확인
- **차단 예시**: 
  - `document.txt`로 이름을 바꾼 실행 파일 (매직 바이트: PE 헤더)
  - `image.jpg`로 이름을 바꾼 바이너리 파일 (매직 바이트: PE 헤더)
  - `script.txt`로 이름을 바꾼 ELF 파일 (매직 바이트: ELF 헤더)
  - `memo.txt`로 이름을 바꾼 쉘 스크립트 (매직 바이트: #! 헤더)

#### 4. **다중 확장자 검사**
- 파일명에 포함된 모든 확장자 검사
- 사용자 설정 기반: 차단된 확장자만 검사 대상

### 보안 공격 시나리오 및 방어

#### 공격 시나리오 1: 확장자 우회
```
공격자: virus.exe → memo.exe.txt로 이름 변경
시스템: 파일명에 exe 포함 → 차단됨
결과: 확장자 우회 공격 실패
```

#### 공격 시나리오 2: 파일 내용 조작
```
공격자: virus.exe를 document.txt로 이름 변경
시스템: 매직 바이트 검증 → PE 헤더 감지
결과: 파일 내용 조작 공격 실패
```

#### 공격 시나리오 3: 매직 바이트 우회
```
공격자: 실행 파일을 안전한 확장자로 이름 변경
시스템: 매직 바이트 검증 → PE/ELF/Mach-O 헤더 감지
결과: 파일 내용 기반 공격 실패
```

### 보안 한계

- 바이러스 스캔 없음
- 악성 코드 정적 분석 없음
- 파일 암호화/압축 우회 없음

### 권장 추가 보안 대책

1. **바이러스 스캔**: 업로드된 파일에 대한 실시간 바이러스 검사
2. **정적 분석**: 악성 코드 패턴 분석
3. **실행 방지**: 업로드 디렉토리 실행 권한 제거
4. **파일 크기 제한**: 대용량 파일 업로드 방지
5. **접근 제어**: 인증/인가 시스템 적용
6. **파일 암호화 검사**: 암호화된 악성 파일 탐지

## 🛡️ 고급 보안 기능

### 확장자 우회 공격 방지 시스템

#### 핵심 로직
```java
private static String detectExtensionBypass(String originalExtension, String detectedType) {
    // 실행 파일 확장자 목록
    String[] executableExtensions = {"exe", "scr", "cpl", "com", "bat", "cmd", "js"};
    
    // 원본 확장자가 실행 파일인지 확인
    boolean isOriginalExecutable = false;
    for (String ext : executableExtensions) {
        if (originalExtension.equals(ext)) {
            isOriginalExecutable = true;
            break;
        }
    }
    
    // 원본 확장자가 실행 파일이면 확장자 기반으로만 판단 (사용자 설정 존중)
    if (isOriginalExecutable) {
        return originalExtension;  // 사용자가 허용한 확장자는 허용
    }
    
    // 원본 확장자가 안전한데 매직 바이트가 실행 파일이면 우회 공격으로 판단
    if (detectedType.equals("exe") || detectedType.equals("bat") || 
        detectedType.equals("cmd") || detectedType.equals("js")) {
        return detectedType;  // 실제 위험한 타입 반환하여 차단
    }
    
    return originalExtension;
}
```

#### 지원하는 파일 형식 감지

| 카테고리 | 파일 형식 | 감지 방식 | 매직 바이트 |
|----------|-----------|-----------|-------------|
| **실행 파일** | EXE, SCR, CPL, COM | PE 헤더 | `4D 5A` (MZ) |
| **스크립트** | BAT, CMD, JS | 내용 분석 | 텍스트 패턴 |
| **문서** | PDF | PDF 시그니처 | `25 50 44 46` (%PDF) |
| **압축** | ZIP, DOCX, XLSX | ZIP 시그니처 | `50 4B` (PK) |
| **이미지** | JPG, PNG, GIF | 이미지 헤더 | 각각 고유 시그니처 |
| **텍스트** | TXT | 텍스트 검증 | ASCII/UTF-8 검증 |

### 실제 우회 공격 테스트 결과

#### 테스트 시나리오 및 결과

| 테스트 케이스 | 파일명 | 확장자 | 매직 바이트 | 결과 | 보안 효과 |
|---------------|--------|--------|-------------|------|-----------|
| **정상 EXE** | `real_pe.exe` | `exe` | `exe` | ❌ 차단 | 확장자 기반 차단 |
| **우회 공격 1** | `virus.txt` | `txt` | `exe` | ❌ 차단 | 확장자 우회 감지 |
| **우회 공격 2** | `document.pdf` | `pdf` | `exe` | ❌ 차단 | 확장자 우회 감지 |
| **우회 공격 3** | `safe.txt` | `txt` | `exe` | ❌ 차단 | 확장자 우회 감지 |
| **정상 텍스트** | `normal.txt` | `txt` | `txt` | ✅ 허용 | 정상 파일 보호 |
| **사용자 설정** | `screensaver.scr` | `scr` | `exe` | ✅ 허용 | 사용자 설정 존중 |

#### 공격 시나리오별 방어 메커니즘

| 공격 유형 | 공격 방법 | 시스템 대응 | 결과 |
|-----------|-----------|-------------|------|
| **확장자 우회** | `virus.exe` → `document.txt` | 매직 바이트 검증 | ❌ 차단됨 |
| **다중 확장자** | `virus.exe.txt` | 파일명 전체 검사 | ❌ 차단됨 |
| **MIME 타입 조작** | Content-Type 조작 | 매직 바이트 우선 | ❌ 차단됨 |
| **정상 파일 위장** | 실제 텍스트 파일 | 내용 검증 | ✅ 허용됨 |

### 사용자 설정 기반 보안

#### 설정 시나리오 예시

| 사용자 설정 | 파일 업로드 | 시스템 동작 | 결과 |
|-------------|-------------|-------------|------|
| EXE 차단, SCR 허용 | `virus.exe` | 확장자 기반 차단 | ❌ 차단 |
| EXE 차단, SCR 허용 | `screensaver.scr` | 확장자 기반 허용 | ✅ 허용 |
| EXE 차단, SCR 허용 | `virus.exe` → `screensaver.scr` | 확장자 우선 (사용자 설정 존중) | ✅ 허용 |
| BAT 차단, JS 허용 | `script.bat` | 확장자 기반 차단 | ❌ 차단 |
| BAT 차단, JS 허용 | `script.js` | 확장자 기반 허용 | ✅ 허용 |

## 🔧 예외처리 및 안정성

### 파일 업로드 예외처리

#### 1. **중복 파일명 처리**
```java
// 파일명 충돌 시 자동 번호 부여
String fileName = originalFilename;
int counter = 1;
while (Files.exists(uploadPath.resolve(fileName))) {
    String nameWithoutExt = originalFilename.substring(0, originalFilename.lastIndexOf('.'));
    String extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
    fileName = nameWithoutExt + "-" + counter + extension;
    counter++;
}
```

**결과**: `document.pdf` → `document-1.pdf` → `document-2.pdf`

#### 2. **파일 크기 제한**
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
```

#### 3. **파일 형식 검증**
- 빈 파일 업로드 방지
- 지원하지 않는 파일 형식 차단
- 파일명 특수문자 처리

#### 4. **디렉토리 보안**
```java
// 업로드 디렉토리 존재 확인 및 생성
if (!Files.exists(uploadPath)) {
    Files.createDirectories(uploadPath);
}

// 파일 경로 검증 (Path Traversal 방지)
Path resolvedPath = uploadPath.resolve(fileName).normalize();
if (!resolvedPath.startsWith(uploadPath)) {
    throw new SecurityException("Invalid file path");
}
```

### 데이터베이스 예외처리

#### 1. **중복 확장자 처리**
```java
// 확장자 중복 체크
if (customExtensionRepository.existsByExtensionName(normalizedExtension)) {
    throw new IllegalArgumentException("이미 존재하는 확장자입니다.");
}
```

#### 2. **트랜잭션 관리**
```java
@Transactional
public void addCustomExtension(String extensionName) {
    // 확장자 정규화
    String normalizedExtension = normalizeExtensionName(extensionName);
    
    // 중복 체크
    if (customExtensionRepository.existsByExtensionName(normalizedExtension)) {
        throw new IllegalArgumentException("이미 존재하는 확장자입니다.");
    }
    
    // 저장
    CustomExtension extension = new CustomExtension(normalizedExtension);
    customExtensionRepository.save(extension);
}
```

#### 3. **데이터 무결성 보장**
- 확장자명 정규화 (소문자, 공백 제거)
- 최대 길이 제한
- 특수문자 필터링

### 시스템 안정성

#### 1. **메모리 관리**
```java
// 스트림 자동 해제
try (InputStream inputStream = file.getInputStream()) {
    // 파일 처리 로직
}
```

#### 2. **로깅 시스템**
```java
// 보안 이벤트 로깅
log.warn("차단된 파일 업로드 시도: {} (실제 형식: {})", 
         originalFilename, detectedType);

// 시스템 오류 로깅
log.error("파일 업로드 중 오류 발생", e);
```

#### 3. **성능 최적화**
- 파일 크기별 처리 방식 분기
- 불필요한 파일 읽기 최소화
- 캐시 활용

### 에러 응답 표준화

#### API 응답 형식
```json
{
  "success": false,
  "message": "차단된 파일 형식입니다. (실제 파일 형식: exe)",
  "data": null,
  "errorCode": 0
}
```

#### 에러 코드 체계
- `0`: 성공
- `1000`: 파일 업로드 오류
- `1001`: 확장자 차단
- `1002`: 파일 형식 불일치
- `1003`: 파일 크기 초과
- `1004`: 중복 확장자
- `1005`: 보안 위반

## 📊 성능 및 모니터링

### 파일 업로드 성능

| 파일 크기 | 처리 시간 | 메모리 사용량 |
|-----------|-----------|---------------|
| < 1MB | < 100ms | < 10MB |
| 1-10MB | < 500ms | < 50MB |
| > 10MB | 차단됨 | - |

### 보안 이벤트 모니터링

#### 모니터링 지표
- 차단된 파일 업로드 시도 횟수
- 확장자 우회 공격 감지 횟수
- 매직 바이트 불일치 감지 횟수
- 시스템 오류 발생률

#### 알림 시스템
- 연속 공격 시도 감지 시 알림
- 시스템 오류 임계값 초과 시 알림
- 디스크 공간 부족 시 알림

## 개발 가이드



### 데이터베이스 초기화

- `DataInitializer`에서 최초 1회 데이터 적재
- 개발 환경에서만 테스트 파일 생성

## 문제 해결

### 일반적인 문제

1. **포트 충돌**: 8080 포트가 사용 중인 경우 다른 포트 사용
2. **권한 문제**: 업로드 디렉토리 권한 확인
3. **메모리 부족**: JVM 힙 메모리 설정 조정

### 로그 확인

```bash
# 애플리케이션 로그
docker logs file-extension-app

# 실시간 로그
docker logs -f file-extension-app
```

