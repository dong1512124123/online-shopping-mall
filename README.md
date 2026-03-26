# FASHION STORE - 온라인 쇼핑몰

패션 의류 전문 온라인 쇼핑몰 웹 애플리케이션입니다.

## 주요 기능

- **상품 관리**: 카테고리별 상품 조회 / 검색 / 상세보기
- **회원 기능**: 회원가입 / 로그인 / 마이페이지 / 비밀번호 변경
- **장바구니 & 위시리스트**: 상품 담기 / 좋아요
- **주문 & 결제**: 주문하기 / 주문내역 / 주문취소
- **관리자 대시보드**: 매출 현황 / 주문관리 / 회원관리 / 상품관리
- **매출 통계**: 연도별 월별 매출 그래프 (Chart.js)
- **엑셀 다운로드**: 주문 / 회원 / 상품 데이터 내보내기

## 기술 스택

| 구분 | 기술 |
|------|------|
| Backend | Spring Boot 3.2.3 (Java 18) |
| Database | MySQL 8.0 |
| View | Thymeleaf |
| Frontend | Bootstrap 5.3.2 (CDN) |
| Security | Spring Security 6 |
| Build | Maven |

---

## 실행 방법

### 1단계: 사전 준비

아래 프로그램이 설치되어 있어야 합니다.

- **Java 17 이상** - [다운로드](https://adoptium.net/)
- **MySQL 8.0** - [다운로드](https://dev.mysql.com/downloads/installer/)

### 2단계: 프로젝트 다운로드

```bash
git clone https://github.com/dong1512124123/online-shopping-mall.git
cd online-shopping-mall
```

### 3단계: MySQL 데이터베이스 생성

MySQL에 접속하여 데이터베이스를 생성합니다.

```sql
CREATE DATABASE online_shopping_mall CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 4단계: DB 비밀번호 설정

본인의 MySQL root 비밀번호를 환경변수로 설정합니다.

**Windows (CMD):**
```cmd
set DB_PASSWORD=본인의MySQL비밀번호
```

**Windows (PowerShell):**
```powershell
$env:DB_PASSWORD="본인의MySQL비밀번호"
```

**Mac / Linux:**
```bash
export DB_PASSWORD=본인의MySQL비밀번호
```

> 환경변수를 설정하지 않으면 기본값(빈 문자열)이 사용됩니다.

### 5단계: 실행

**Windows:**
```cmd
mvnw.cmd spring-boot:run
```

**Mac / Linux:**
```bash
./mvnw spring-boot:run
```

### 6단계: 접속

브라우저에서 아래 주소로 접속합니다.

```
http://localhost:8082
```

**테스트 계정:**

| 구분 | 아이디 | 비밀번호 |
|------|--------|----------|
| 관리자 | admin | admin1234 |
| 일반회원 | hong123 | test1234 |

---

## 프로젝트 구조

```
online-shopping-mall/
├── src/main/java/com/shop/mall/
│   ├── OnlineShoppingMallApplication.java  <- 메인 실행 클래스
│   ├── config/                             <- 설정
│   │   ├── SecurityConfig.java
│   │   ├── WebConfig.java
│   │   └── DataInitializer.java
│   ├── controller/                         <- 웹 요청 처리
│   │   ├── HomeController.java
│   │   ├── AuthController.java
│   │   ├── ProductController.java
│   │   ├── CartController.java
│   │   ├── OrderController.java
│   │   ├── WishlistController.java
│   │   ├── MyPageController.java
│   │   ├── AdminDashboardController.java
│   │   ├── AdminProductController.java
│   │   ├── AdminOrderController.java
│   │   └── AdminMemberController.java
│   ├── service/                            <- 비즈니스 로직
│   ├── repository/                         <- DB 접근
│   ├── entity/                             <- DB 테이블 매핑
│   └── enums/                              <- 상태값 정의
├── src/main/resources/
│   ├── application.properties              <- 설정 파일
│   └── templates/                          <- HTML 화면
│       ├── fragments/ (header, footer)
│       ├── auth/      (login, register)
│       ├── product/   (list, detail, search)
│       ├── cart/      (list)
│       ├── order/     (checkout, list, detail, complete)
│       ├── wishlist/  (list)
│       ├── mypage/    (index)
│       └── admin/     (dashboard, product, order, member, revenue)
├── pom.xml                                 <- Maven 의존성 설정
└── mvnw.cmd                                <- Maven Wrapper (빌드 도구)
```

---

## 화면 미리보기

| 화면 | 설명 |
|------|------|
| `/` | 메인 - 신상품 / 랭킹 / 세일 |
| `/category/MAN` | 카테고리별 상품 |
| `/product/{id}` | 상품 상세 |
| `/cart` | 장바구니 |
| `/order/checkout` | 주문하기 |
| `/order/list` | 주문내역 |
| `/wishlist` | 좋아요 목록 |
| `/mypage` | 마이페이지 |
| `/admin/dashboard` | 관리자 대시보드 |
| `/admin/revenue` | 매출 현황 그래프 |

---

## 문제 해결

### MySQL 연결 오류
- MySQL 서비스가 실행 중인지 확인하세요.
- `online_shopping_mall` 데이터베이스가 생성되었는지 확인하세요.
- DB 비밀번호가 올바른지 확인하세요.

### 포트 충돌
8082 포트를 다른 프로그램이 사용 중이면 `application.properties`에서 포트를 변경하세요:
```properties
server.port=9090
```
