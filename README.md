# Noomit

통합 A/S 접수 및 처리 플랫폼

## 추진 배경

현재 다수의 A/S 센터에서는 고객 요청과 수리 처리 내역을 전화, 메모, 엑셀 등 비정형적인 방식으로 관리하고 있다. 이로 인해 접수 누락, 담당자 배정 지연, 처리 이력 추적 곤란, 입금 확인 오류 등의 문제가 반복적으로 발생하고 있으며, 업무 효율성과 고객 응대 품질 저하로 이어지고 있다.

## 목적

- A/S 접수 → 처리 → 완료까지의 핵심 흐름을 하나의 플랫폼으로 관리
- 담당자 배정 및 처리 상태를 실시간으로 추적하여 업무 누락 방지
- 처리 이력 데이터를 축적하여 통계 기반의 운영 개선 근거 확보

## 현재 범위

초기 세팅 단계로, 아래만 구현되어 있다. A/S 접수·처리 도메인은 이후 단계에서 추가된다.

- 이메일/비밀번호 회원가입·로그인 (세션 기반)
- 로그인 사용자 정보 조회 (`/api/me`)
- 관리자 회원 권한 관리 API (권한 부여/조회) — 관리 화면은 자리표시만 있음

## 구성

| 디렉터리 | 내용 |
|---|---|
| [`backend/`](backend) | Spring Boot, Spring Modulith, PostgreSQL, Flyway |
| [`frontend/`](frontend) | Next.js App Router, Redux Toolkit, RTK Query |

각 프로젝트의 상세 구조와 컨벤션은 `backend/AGENTS.md`(작성 예정), [`frontend/AGENTS.md`](frontend/AGENTS.md)를 참고한다.

## 로컬 실행

```bash
# 1. 환경 변수
cp .env.example .env
cp frontend/.env.example frontend/.env.development.local

# 2. DB (로컬 전용, docker-compose.local.yml)
cd backend && docker compose -f docker-compose.local.yml up -d

# 3. 백엔드
./gradlew bootRun

# 4. 프론트엔드 (새 터미널)
cd ../frontend
npm ci
npm run dev
```

백엔드는 `http://localhost:8080`, 프론트엔드는 `http://localhost:3000`에서 뜬다.

## 명명 규칙

- 서비스 이름: `noomit`
- Java 기본 패키지: `com.noomit.backend`
