// 개인정보처리방침(SCR-023) 정적 데이터. 수집 항목·보관 기간은 요구사항 FR-001·002·007·018·019·023·028을 따른다.
// 방침을 바꾸면 effectiveDate를 새 시행일로 바꾸고 10번 조항대로 시행 7일 전부터 알린다.

// 시행일: 운영에 처음 올리는 날 (사용자 결정 2026-11-03). 배포일이 달라지면 함께 고친다
export const effectiveDate = '2026년 11월 3일'

export const privacyHero = {
  eyebrow: 'PRIVACY',
  title: '개인정보처리방침',
  description:
    '몽키즈유아체육(이하 ‘몽키즈’)은 상담과 수업에 꼭 필요한 정보만 받고, 받은 정보를 안전하게 지킵니다. 어떤 정보를 왜 받고 언제 지우는지 알려 드립니다.',
}

export const privacySummary = {
  title: '한눈에 보기',
  dateLabel: '시행일',
  items: [
    { id: 'items', icon: '📋', title: '수집 항목', text: '이름·연락처 등 가입과 상담에 필요한 최소한의 정보' },
    { id: 'purpose', icon: '🎯', title: '이용 목적', text: '상담 연락, 수업 안내, 회원 관리, 강사 채용' },
    { id: 'retention', icon: '⏱️', title: '보관 기간', text: '상담 신청은 상담 완료 후 3년, 회원 정보는 탈퇴하면 바로 삭제' },
    { id: 'health', icon: '💛', title: '건강정보', text: '1:1 상담에서 적어 주시고 따로 동의하신 경우에만 받아요', tone: 'yellow' },
    { id: 'third-party', icon: '🔒', title: '제3자 제공·위탁', text: '동의 없이 외부에 제공하지 않아요' },
    { id: 'contact', icon: '📞', title: '문의', text: '개인정보 보호책임자 임재영 · 010-8984-2757' },
  ],
}

export const privacyTocLabel = '목차'

// 조항 본문. blocks의 type: text(문단), list(목록, label은 굵게), table(표), callout(강조 상자),
// contact(정의 목록, href가 있으면 링크), agencies(신고 기관 전화번호), link(화면 이동 버튼), note(작은 안내)
// 표의 칸 값은 문자열이거나 줄 배열({ label, text })이다
export const privacySections = [
  {
    id: 'privacy-items',
    title: '1. 수집하는 개인정보 항목과 이용 목적',
    tocLabel: '1. 수집 항목과 이용 목적',
    blocks: [
      {
        type: 'text',
        text: '몽키즈는 아래 경우에만 개인정보를 받고, 받을 때마다 항목·목적·보관 기간을 먼저 알려 드린 뒤 동의를 받습니다. 상담 신청과 강사 지원은 회원가입 없이 할 수 있습니다.',
      },
      {
        type: 'table',
        columns: [
          { key: 'category', label: '구분' },
          { key: 'items', label: '수집 항목' },
          { key: 'purpose', label: '이용 목적' },
          { key: 'retention', label: '보관 기간' },
        ],
        rows: [
          {
            id: 'member',
            category: [{ text: '회원가입·로그인' }, { text: '카카오·네이버', muted: true }],
            items: [
              { label: '필수', text: '이름, 이메일, 휴대전화 번호, 소셜 계정 식별값' },
              { label: '선택', text: 'SNS 수신 동의 여부와 동의 시각' },
            ],
            purpose: '회원 확인과 로그인, 후기·댓글·공감 등 회원 서비스, 동의한 경우 소식 안내',
            retention: '회원 탈퇴 시 바로 삭제',
          },
          {
            id: 'institution',
            category: [{ text: '기관 상담 신청' }],
            items: [
              { label: '필수', text: '기관명, 연락받을 전화번호, 희망 수업 일정(요일·시간), 원아 연령·인원' },
              { label: '선택', text: '특이사항' },
            ],
            purpose: '상담 연락, 수업 일정 협의',
            retention: '상담 완료 후 3년',
          },
          {
            id: 'personal',
            category: [{ text: '1:1 방문체육 상담 신청' }],
            items: [
              {
                label: '필수',
                text: '보호자 이름, 휴대전화 번호, 아이 이름·성별, 생년월일 또는 개월 수, 수업받을 주소, 어린이집·유치원 이용 여부',
              },
              {
                label: '선택',
                text: '아이 성향, 좋아하는 놀이·장난감, 좋아하는 캐릭터·노래, 신청 이유·기대하는 점, 건강·알레르기·주의사항(2번 참고)',
              },
            ],
            purpose: '상담 연락, 아이에게 맞는 수업 안내와 방문 준비',
            retention: '상담 완료 후 3년',
          },
          {
            id: 'recruit',
            category: [{ text: '강사 지원' }],
            items: '지원서 파일(지원서에 적은 정보)',
            purpose: '채용 검토와 지원자 연락',
            retention: '채용 절차가 끝난 뒤 6개월',
          },
          {
            id: 'cookie',
            category: [{ text: '자동 수집' }],
            items: '로그인 유지를 위한 쿠키',
            purpose: '로그인 상태 유지',
            retention: '로그아웃하거나 유효 기간이 지나면 삭제',
          },
        ],
      },
    ],
  },
  {
    id: 'privacy-health',
    title: '2. 건강정보(민감정보) 처리',
    tocLabel: '2. 건강정보(민감정보)',
    blocks: [
      {
        type: 'callout',
        items: [
          '1:1 방문체육 상담에서 건강·알레르기·주의사항 칸에 내용을 적으신 경우에만, 개인정보 수집 동의와 따로 건강정보 처리 동의를 받습니다.',
          '안전한 수업 준비에만 쓰며, 보관 기간은 상담 신청과 같습니다(상담 완료 후 3년).',
          '기관 상담 신청은 건강정보를 받지 않습니다. 특이사항 칸에 원아 건강정보는 적지 말아 주세요.',
        ],
      },
    ],
  },
  {
    id: 'privacy-child',
    title: '3. 아이 정보와 보호자 동의',
    tocLabel: '3. 아이 정보와 보호자 동의',
    blocks: [
      {
        type: 'text',
        text: '몽키즈는 만 14세 미만 아동에게 직접 정보를 받지 않습니다. 아이 정보는 보호자가 1:1 방문체육 상담 신청에서 직접 입력하고 동의합니다.',
      },
    ],
  },
  {
    id: 'privacy-retention',
    title: '4. 보관 기간과 파기',
    tocLabel: '4. 보관 기간과 파기',
    blocks: [
      {
        type: 'list',
        items: [
          { id: 'consultation', label: '상담 신청', text: '상담 완료 후 3년이 지나면 삭제합니다.' },
          {
            id: 'member',
            label: '회원',
            text: '탈퇴하면 휴대전화 번호·이메일·소셜 계정 식별값·SNS 수신 동의를 바로 지우고, 이름은 ‘탈퇴 회원’으로 바꿉니다. 작성한 후기·게시물·댓글은 남고 작성자는 ‘탈퇴 회원’으로 표시됩니다.',
          },
          { id: 'recruit', label: '강사 지원서', text: '채용 절차가 끝난 뒤 6개월이 지나면 삭제합니다.' },
          {
            id: 'destroy',
            text: '전자 파일은 되살릴 수 없는 방법으로 지웁니다. 법령에 따라 보관해야 하는 정보는 그 기간 동안 따로 보관합니다.',
          },
        ],
      },
    ],
  },
  {
    id: 'privacy-third-party',
    title: '5. 제3자 제공과 처리 위탁',
    tocLabel: '5. 제3자 제공과 처리 위탁',
    blocks: [
      {
        type: 'text',
        text: '몽키즈는 이용자의 동의가 있거나 법령에 근거가 있는 경우 외에는 개인정보를 외부에 제공하지 않습니다. 서비스 운영을 위해 아래 업무를 맡기고 있습니다.',
      },
      {
        type: 'table',
        columns: [
          { key: 'company', label: '맡기는 곳' },
          { key: 'work', label: '맡기는 업무' },
        ],
        rows: [{ id: 'aws', company: 'Amazon Web Services', work: '웹사이트 운영과 데이터 보관' }],
      },
      { type: 'note', text: '카카오·네이버 로그인은 이용자가 해당 서비스에서 동의한 항목만 몽키즈가 받습니다.' },
    ],
  },
  {
    id: 'privacy-rights',
    title: '6. 이용자의 권리와 행사 방법',
    tocLabel: '6. 이용자의 권리',
    blocks: [
      {
        type: 'text',
        text: '언제든 내 개인정보를 확인하고, 고치거나 지우도록 요청하고, 처리를 멈추도록 요청할 수 있습니다.',
      },
      {
        type: 'list',
        items: [
          { id: 'member', label: '회원', text: '내 정보 화면에서 정보 확인, SNS 수신 동의 변경, 회원 탈퇴를 직접 할 수 있습니다.' },
          { id: 'guest', label: '상담 신청·강사 지원', text: '9번 개인정보 보호책임자에게 연락하시면 확인 후 처리합니다.' },
        ],
      },
      // 내 정보(SCR-025)
      { type: 'link', label: '내 정보 바로가기', to: '/me' },
    ],
  },
  {
    id: 'privacy-cookie',
    title: '7. 쿠키',
    tocLabel: '7. 쿠키',
    blocks: [
      {
        type: 'text',
        text: '로그인 상태를 유지하기 위한 쿠키만 씁니다. 브라우저 설정에서 쿠키를 막을 수 있지만, 그러면 로그인이 필요한 기능을 쓸 수 없습니다.',
      },
    ],
  },
  {
    id: 'privacy-security',
    title: '8. 개인정보를 안전하게 지키는 방법',
    tocLabel: '8. 안전하게 지키는 방법',
    blocks: [
      {
        type: 'list',
        items: [
          { id: 'https', text: '모든 통신을 HTTPS로 암호화합니다.' },
          { id: 'cookie', text: '로그인 정보는 웹페이지 스크립트가 읽을 수 없는 쿠키에 담습니다.' },
          { id: 'admin', text: '상담 신청 내용은 관리자 권한이 있는 사람만 볼 수 있습니다.' },
        ],
      },
    ],
  },
  {
    id: 'privacy-officer',
    title: '9. 개인정보 보호책임자',
    tocLabel: '9. 개인정보 보호책임자',
    blocks: [
      {
        type: 'contact',
        items: [
          { id: 'name', label: '성명', value: '임재영' },
          { id: 'position', label: '직책', value: '개발자' },
          { id: 'phone', label: '전화', value: '010-8984-2757', href: 'tel:010-8984-2757' },
          { id: 'email', label: '이메일', value: 'imjy2757@naver.com', href: 'mailto:imjy2757@naver.com' },
        ],
      },
      { type: 'text', text: '개인정보 침해에 대한 신고나 상담은 아래 기관에도 할 수 있습니다.' },
      {
        type: 'agencies',
        items: [
          { id: 'kisa', name: '개인정보침해신고센터', phone: '118' },
          { id: 'kopico', name: '개인정보분쟁조정위원회', phone: '1833-6972' },
          { id: 'spo', name: '대검찰청', phone: '1301' },
          { id: 'police', name: '경찰청', phone: '182' },
        ],
      },
    ],
  },
  {
    id: 'privacy-change',
    title: '10. 방침의 변경',
    tocLabel: '10. 방침의 변경',
    blocks: [
      { type: 'text', text: '이 방침을 바꾸면 시행 7일 전부터 홈페이지에 알립니다.' },
      { type: 'text', text: `이 방침은 ${effectiveDate}부터 시행합니다.` },
    ],
  },
]
