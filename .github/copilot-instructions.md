# Bluesky Boot Crypto - GitHub Copilot Instructions

## 프로젝트 개요

Spring Boot 기반 프로젝트에서 properties의 값을 암호화/복호화하는 기능을 제공하는 라이브러리입니다.

## 주요 기능

- Properties 값 암호화/복호화
- 여러 개의 TextEncryptor 관리
- Spring Security의 DelegatingPasswordEncoder 방식 적용
- 암호화 알고리즘 변경 시에도 기존 암호화 값 유지 가능

## 기술 스택

- Java 17
- Spring Boot 3.1.0+
- Maven

## 암호화 형식

```
{textEncryptorId}암호화된값
```

예시:
```
{cipher}AQBvRFjoe8YJ9dLw5WTlOSqGVIxcGS9x1L831KH5xO75+K/f...
```

## 사용 방법

### 1. Maven Dependency

```xml
<dependency>
    <groupId>io.github.luversof</groupId>
    <artifactId>bluesky-boot-crypto</artifactId>
    <version>${currentVersion}</version>
</dependency>
```

### 2. 자동 설정

Dependency를 추가하면 `DecryptEnvironmentPostProcessor`가 자동으로 설정됩니다.

### 3. Properties 암호화

```properties
# 암호화된 값 사용
spring.datasource.password={cipher}AQBvRFjoe8YJ9dLw5WTlOSqGVIxcGS9x1L831KH5xO75+K/f...
```

## 아키텍처

### DelegatingTextEncryptor

- 여러 TextEncryptor를 관리
- Prefix로 사용할 TextEncryptor 결정
- Spring Security의 DelegatingPasswordEncoder 패턴 적용

### DecryptEnvironmentPostProcessor

- 애플리케이션 시작 시 Environment의 암호화된 값을 자동으로 복호화
- `EnvironmentPostProcessor` 인터페이스 구현

## 사용 프로젝트

- **bluesky-cloud**: Config Server에서 암호화된 설정 값 관리
- **bluesky-project**: 데이터베이스 비밀번호 등 민감 정보 암호화

## 설정 예시

### JKS Keystore 사용 (bluesky-cloud-config-server)

```properties
encrypt.keyStore.location=classpath:/bluesky.jks
encrypt.keyStore.password=blueskyPass
encrypt.keyStore.alias=bluesky-Project
encrypt.keyStore.secret=blueskyPass
```

## 코딩 규칙

### 패키지 구조

```
io.github.luversof.boot.crypto/
├── textencryptor/               # TextEncryptor 구현체
├── config/                      # 설정 클래스
└── processor/                   # EnvironmentPostProcessor
```

### 새로운 TextEncryptor 추가 시

1. `TextEncryptor` 인터페이스 구현
2. DelegatingTextEncryptor에 등록
3. ID 부여 (prefix로 사용)

### 보안 원칙

- 키 정보는 반드시 외부 설정으로 관리
- 소스 코드에 키 정보 하드코딩 금지
- Production 환경에서는 강력한 암호화 알고리즘 사용
