# 🏫 서일대학교 통합 좌석 관리 시스템 (Campus Seat Management System)
> **"일상의 불편함을 기술로 해결하며, 백엔드 엔지니어링의 본질을 담아내다."**
> 
> 단순한 예약을 넘어, **트래픽 폭주 시의 동시성 제어**와 **데이터 무결성**까지 완벽하게 통제하는 백엔드 아키텍처 프로젝트입니다.

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

### 1. 트래픽 폭주 시 초과 예약(Double Booking) 100% 방어
- **Problem:** 시험 기간 등 특정 좌석에 다수의 예약 요청이 몰릴 경우, 찰나의 시간 차이로 여러 명이 동일 좌석을 예약해버리는 동시성 이슈 발생 위험.
- **Solution:** DB 트랜잭션 병목을 유발하는 비관적 락 대신, **JPA 낙관적 락(Optimistic Lock, `@Version`)을 도입**하여 서버 성능 저하 없이 초과 예약을 완벽하게 차단.
- **Verification:** 멀티스레드 환경을 구축하여 다중 접속 충돌 및 예외 처리 로직을 수학적으로 검증 완료.

### 2. 스냅샷(Snapshot) 패턴을 통한 데이터 무결성 보장
- **Problem:** 관리자가 좌석을 삭제(Soft Delete)하거나 번호를 수정할 경우, 과거에 해당 좌석을 이용했던 사용자들의 '이용 내역(Usage History)'까지 연관관계가 훼손되는 데이터 유실 문제 발생.
- **Solution:** 예약이 확정되는 순간, 원본 좌석 데이터의 강한 연관관계를 끊어내고 당시의 상태를 복제하여 저장하는 **스냅샷 패턴**을 도입.
- **Result:** 운영자가 원본 데이터를 어떻게 수정/삭제하든, 사용자의 과거 이용 기록은 영구적으로 무결성을 유지.

### 3. CQS 패턴 및 QueryDSL 최적화
- **Problem:** 복잡한 동적 검색 조건(건물, 층, 공간 유형 등) 처리 한계 및 JPA 특유의 N+1 쿼리 병목으로 인한 조회 성능 저하 우려.
- **Solution:** **CQS(Command Query Separation) 패턴**을 적용하여 읽기/쓰기 서비스 레이어를 분리하고, **QueryDSL의 Fetch Join**을 통해 N+1 문제를 원천 차단하여 조회 응답 속도 최적화 달성.

### 4. 지속적 통합(CI) 및 접근 제어
- **GitHub Actions**를 활용하여 코드 Push/Merge 시 자동으로 빌드 및 테스트 코드가 실행되는 CI 파이프라인을 구축하여 결함 있는 코드의 배포를 방지.
- **HandlerInterceptor**를 활용하여 비로그인 사용자 및 권한에 따른 API 접근을 서버 단에서 원천 차단.

<br>

## 📌 주요 비즈니스 기능
| 구분 | 핵심 기능 | 설명 |
|---|---|---|
| **User** | 실시간 예약 및 조회 | 복합 조건으로 실시간 좌석 현황을 검색하고 원클릭으로 예약 및 반납 |
| | 즐겨찾기 및 내역 | 자주 찾는 좌석을 등록하고, 과거 이용 내역을 안전하게 조회 |
| | 고장/불편 신고 | 좌석의 물리적 문제 발생 시 즉시 관리자에게 신고 |
| **Admin** | 동적 레이아웃 배치 | 다중 그리드(행/열) 알고리즘을 활용한 시각적인 좌석 일괄 등록 및 배치 |
| | 실시간 모니터링 | 현재 좌석 이용자를 파악하고 문제 발생 시 강제 퇴실 조치 |

<br>

## 📂 프로젝트 아키텍처 및 산출물

* [<img width="1107" height="650" alt="스크린샷 2026-06-15 212129" src="https://github.com/user-attachments/assets/838eba33-1215-4f79-9ea2-8d0e05a896af" />](#) 
* [<img width="1438" height="1257" alt="졸작 ERD" src="https://github.com/user-attachments/assets/b6f7b406-503d-4a2d-ab45-146b77fdac7a" />](#)
* 자세한 동작화면 , 정보는 보고서에 자세하게 설명되어있습니다. [커넥트팀_결과 보고서 양식.pdf](https://github.com/user-attachments/files/29452945/_.pdf)

### 🎥 프로젝트 시연 영상
[![통합 좌석 관리 시스템 시연 영상](https://img.youtube.com/vi/6_mDB6uiwzQ/maxresdefault.jpg)](https://www.youtube.com/watch?v=6_mDB6uiwzQ)
> 👆 **위 이미지를 클릭하시면 유튜브 시연 영상으로 이동합니다.**

## 📂 프로젝트 아키텍처 및 산출물

* 🔗 **[상세 포트폴리오 (Notion) 바로가기]**(https://app.notion.com/p/3067e2c22350828a8ff901212bb86576?source=copy_link)
  > 시스템의 상세 아키텍처, 기술 스택, 그리고 핵심 트러블슈팅 과정이 모두 담긴 통합 포트폴리오입니다.
* 📝 **[개발 기술 블로그 (Tistory) 바로가기]**(https://xodnrok.tistory.com/)
  > 백엔드 학습에 대한 정리 블로그입니다.
