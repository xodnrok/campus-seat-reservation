# 🏫 헛걸음 ZERO (Campus Seat Management System)
> **"교내 공용 공간의 잔여 좌석을 검색하고 예약·이용 현황을 관리하는 캠퍼스 좌석 관리 플랫폼입니다."**
> 
> 동시 예약 요청 처리, 과거 이용 기록 보존, 조회·명령 로직 분리 등을 통해 데이터 일관성과 유지보수성을 고려한 백엔드 시스템을 구현했습니다.

<br>

## 🛠 Tech Stack
**Backend**
- **Java 21** / **Spring Boot 3.5.8**
- **Spring Data JPA** / **QueryDSL**
- **MySQL 8.0** (운영) / **H2 Database** (테스트)

**DevOps & QA**
- **GitHub Actions** (CI Pipeline 자동화)
- **JUnit5** (60+ 단위 및 통합 테스트)
- **Swagger (SpringDoc)** (API 명세 자동화)

**Frontend**
- HTML5, CSS3, JavaScript (ES6), Bootstrap, SweetAlert2

<br>

## 🔥 Core Engineering (핵심 트러블슈팅 및 아키텍처)
이 프로젝트는 단순 CRUD 구현을 넘어, 실제 서비스 운영 시 발생할 수 있는 치명적인 시스템 결함을 예측하고 아키텍처 레벨에서 방어했습니다.

### 1. 낙관적 락 기반 동시 예약 제어
- **Problem:** 여러 사용자가 동시에 동일 좌석을 예약할 때, 중복 예약이 발생할 수 있는 문제
- **Solution:** JPA의 낙관적 락(@Version)을 적용해 동시 업데이트 충돌을 감지하고, 중복 예약을 방지하도록 구현했습니다.
- **Solution:** 충돌 상황에서 발생하는 예외를 처리하여 예약 데이터의 일관성을 유지하도록 설계했습니다.
- **Verification:** 멀티스레드 기반 동시성 테스트를 통해 여러 예약 요청이 충돌하는 상황을 검증했습니다.

### 2. Snapshot Pattern을 활용한 과거 이용 기록 보존
- **Problem:** 관리자가 좌석 정보를 변경하거나 삭제할 때, 과거 이용 내역의 좌석 정보가 훼손될 수 있는 문제
- **Solution:** 이용 기록에 필요한 좌석 정보를 스냅샷 형태로 보존하여 현재 좌석 데이터의 변경·삭제와 과거 기록을 분리했습니다.
- **Solution:** Soft Delete를 적용해 삭제된 좌석 정보를 논리적으로 관리하고, 과거 이용 내역의 데이터 정합성을 고려했습니다.

### 3. CQS 및 QueryDSL을 활용한 조회 로직 개선
- **Problem:** 건물, 층, 공간 유형 등 복수의 검색 조건을 조합해야 하고, 연관 엔티티 조회 시 N+1 문제가 발생할 수 있는 구조
- **Solution:** CQS(Command Query Separation)를 적용해 데이터 변경 로직과 조회 로직의 책임을 분리했습니다.
- **Solution:** QueryDSL을 활용해 여러 검색 조건을 조합하는 동적 쿼리를 구현했습니다.
- **Solution:** 필요한 조회 경로에 Fetch Join을 적용해 연관 데이터 조회 시 발생하는 N+1 문제를 방지하도록 개선했습니다.

### 4. 인증·인가 및 접근 제어
- **권한 분리:**일반 사용자와 관리자 기능을 구분해 접근 권한을 관리했습니다.
- **HandlerInterceptor:**비로그인 사용자와 권한이 없는 사용자의 보호된 기능 접근을 서버 측에서 제어했습니다.

### 5. 테스트 자동화 및 API 명세
- **JUnit 5:**단위 테스트 및 통합 테스트를 작성해 주요 비즈니스 로직을 검증했습니다.
- **GitHub Actions:**코드 변경 시 빌드 및 테스트가 자동 실행되도록 CI 파이프라인을 구성했습니다.
- **SpringDoc OpenAPI:**Swagger UI를 통해 API 명세를 확인할 수 있도록 구성했습니다.
<br>

## 📌 주요 비즈니스 기능
| 구분 | 핵심 기능 | 설명 |
|---|---|---|
| **User** | 공간 검색 및 예약  | 건물·층·공간 유형 등의 조건으로 공간을 검색하고 좌석을 예약·반납 |
| | 이용 내역 및 즐겨찾기 | 과거 이용 내역을 조회하고 자주 이용하는 좌석을 즐겨찾기로 관리 |
| | 시설 고장 신고 | 좌석의 고장 및 불편 사항을 관리자에게 신고하고 처리 상태 확인 |
| **Admin** | 공간 및 좌석 관리 | 건물·층·공간 유형을 등록하고 행·열 기반으로 좌석 배치를 설정 |
| | 이용 현황 관리 | 현재 좌석 이용 현황을 확인하고 고장 신고 및 이용 상태를 관리 |

<br>

## 📂 프로젝트 아키텍처 및 산출물

* [<img width="1107" height="650" alt="스크린샷 2026-06-15 212129" src="https://github.com/user-attachments/assets/838eba33-1215-4f79-9ea2-8d0e05a896af" />](#) 
* [<img width="1438" height="1257" alt="졸작 ERD" src="https://github.com/user-attachments/assets/b6f7b406-503d-4a2d-ab45-146b77fdac7a" />](#)
* 자세한 동작화면 , 정보는 보고서에 자세하게 설명되어있습니다. [커넥트팀_결과 보고서 양식.pdf](https://github.com/user-attachments/files/29452945/_.pdf)

### 🎥 프로젝트 시연 영상
[![통합 좌석 관리 시스템 시연 영상](https://img.youtube.com/vi/6_mDB6uiwzQ/maxresdefault.jpg)](https://www.youtube.com/watch?v=6_mDB6uiwzQ)
> 👆 **위 이미지를 클릭하시면 유튜브 시연 영상으로 이동합니다.**

## 📂 프로젝트 아키텍처 및 산출물

* 🔗 **[상세 포트폴리오 (Notion) 바로가기]**(https://app.notion.com/p/3067e2c22350828a8ff901212bb86576?p=2e97e2c2235082479b9c81ede08d8adf&pm=c)
  > 시스템의 상세 아키텍처, 기술 스택, 그리고 핵심 트러블슈팅 과정이 모두 담긴 통합 포트폴리오입니다.
* 📝 **[개발 기술 블로그 (Tistory) 바로가기]**(https://xodnrok.tistory.com/)
  > 백엔드 학습에 대한 정리 블로그입니다.
