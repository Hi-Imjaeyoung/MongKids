# MongKids

방문 유아체육 전문 브랜드 **몽키즈**의 웹 서비스입니다. 프론트엔드와 백엔드를 이 저장소 하나에서 관리합니다.

**홈페이지: https://mongkiz.com**

## 프로젝트 소개

몽키즈는 어린이집·유치원, 가정, 태권도장, 복지센터 등으로 강사가 직접 찾아가는 유아체육 브랜드입니다.
이 서비스는 보호자와 기관이 프로그램을 살펴보고 바로 상담을 신청할 수 있게 하고, 대표와 직원이 수업 소식과 상담을 한곳에서 관리할 수 있게 합니다.

### 주요 기능

| 대상 | 기능 |
|---|---|
| 방문자 | 몽키즈 소개, 체육/행사 프로그램 안내(어린이집·유치원, 1:1 가정 방문, 태권도장, 운동회, 복지센터), 자주 묻는 질문 |
| 방문자 | 기관·개인 상담 신청 (로그인 없이 신청, 개인정보·건강정보 동의) |
| 회원 | 카카오·네이버 소셜 로그인, 수업 현장 게시물 댓글·공감, 수업 후기 작성·수정·삭제 |
| 직원 | 수업 현장·몽키즈 연구소 게시물 작성 (HTML 편집기, 사진 업로드) |
| 관리자 | 상담 신청 조회·처리 상태 관리, 신규 상담 건수 실시간 배지, 후기 숨김·답글, 직원 권한 관리 |

요구사항 전체와 구현 상태는 [요구사항](docs/requirements/requirement_homepage.md)에서, 진행 상황은 [진행 사항](docs/progress.md)에서 볼 수 있습니다.

## 구성

| 폴더 | 내용 | 스택 |
|---|---|---|
| [`back/`](back/) | REST API 서버 | Java 17, Spring Boot 4, Spring Security(OAuth2 + JWT 쿠키), JPA, MariaDB |
| [`front/`](front/) | 웹 프론트엔드 (SPA) | React 19, react-router-dom 7, Vite 8, TipTap |
| [`docs/`](docs/) | 공유 문서 | 요구사항, 진행 사항, Git 규칙, 배포 점검, ADR |

### 운영 구성

```
브라우저 ─ CloudFront (mongkiz.com, HTTPS)
             ├─ 기본 경로 → S3 (front/dist)
             └─ /api/*, /oauth2/*, /login/oauth2/* → Caddy (EC2) → Spring Boot → RDS MariaDB
사진: S3 Presigned URL로 브라우저가 직접 업로드, 별도 CloudFront로 제공
```

- `main`에 반영되면 GitHub Actions가 바뀐 영역만 배포합니다 (프론트: S3 업로드·CloudFront 무효화, 백엔드: jar 교체·헬스 체크·실패 시 롤백).
- 자세한 리소스와 점검 항목은 [배포 점검 체크리스트](docs/deploy-checklist.md)에 있습니다.

## 로컬 실행

### 백엔드

```bash
cd back
./gradlew bootRun --args='--spring.profiles.active=local'    # Windows: gradlew.bat
```

- 로컬 MariaDB(`mongkids`)와 `back/.env`가 필요합니다. 소셜 로그인 키(`KAKAO_*`, `NAVER_*`), `JWT_SECRET`, 사진 저장소(`AWS_S3_BUCKET`, `STORAGE_PUBLIC_BASE_URL`)가 없으면 기동에 실패합니다. `.env`는 커밋하지 않습니다.
- 테스트는 H2로 돌아 DB 없이 실행할 수 있습니다: `./gradlew build`

### 프론트엔드

```bash
cd front
npm install
npm run dev    # http://localhost:5173, /api·로그인 경로는 백엔드(8080)로 프록시
```

## 문서

- [요구사항](docs/requirements/requirement_homepage.md) · [진행 사항](docs/progress.md)
- [Git 워크플로](docs/git-workflow.md) · [배포 점검 체크리스트](docs/deploy-checklist.md)
- [아키텍처 결정 기록(ADR)](docs/adr/)
- 개발 규칙: [백엔드](back/CLAUDE.md), [프론트엔드](front/CLAUDE.md)
