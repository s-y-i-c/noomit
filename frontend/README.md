# Noomit Frontend

Noomit의 사용자·관리자 웹 화면을 제공하는 Next.js 프런트엔드입니다.

## 현재 범위

- 로컬 이메일/비밀번호 회원가입·로그인, 로그인 사용자 정보 표시
- 사용자 홈
- 관리자 레이아웃
- 회원 관리 자리표시 화면 (`업데이트 예정입니다.`)

last-mission 템플릿에서 가져오면서 채팅(메신저), 소켓, 중앙인증(coder-han) 연동은 제외했습니다.
Noomit 백엔드는 자체 세션 기반 로그인만 제공하기 때문입니다.

## 기술 구성

| 영역 | 기술 |
|---|---|
| 프레임워크 | Next.js App Router |
| 상태 관리 | Redux Toolkit, RTK Query |
| UI | React, TypeScript, CSS Modules |
| 런타임 | Node.js 24 |

## 프로젝트 구조

```text
app/
├─ (user)/                 사용자 홈과 공통 셸
├─ admin/                  관리자 셸과 회원 관리 자리표시 화면
└─ login/                  로그인/회원가입 화면
features/
├─ auth/                   로그인·회원가입·현재 사용자 상태
├─ shared/                 공용 API·UI
├─ shell/                  사용자 내비게이션
├─ admin/                  관리자 내비게이션
├─ store/                  Redux 스토어 설정
└─ theme/                  테마 설정
```

## 로컬 실행

```bash
npm ci
npm run dev
```

로컬 개발값은 Git에서 제외되는 `.env.development.local`에 둡니다.

```bash
cp .env.example .env.development.local
```

`NEXT_PUBLIC_*` 값은 브라우저에 공개되고 빌드 결과에 포함되므로 비밀번호, 토큰, 인증서 경로와 Secret을 넣지 않습니다.

## 검증

```bash
npm run lint
npm run build
```
