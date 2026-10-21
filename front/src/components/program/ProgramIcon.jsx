// 프로그램별 선 아이콘 (시안의 인라인 SVG). 색은 currentColor를 따른다.
const paths = {
  school: (
    <>
      <path d="M3 21h18" />
      <path d="M5 21V9l7-5 7 5v12" />
      <path d="M10 21v-5h4v5" />
    </>
  ),
  home: (
    <>
      <path d="M3 11l9-7 9 7" />
      <path d="M5 10v10h14V10" />
      <path d="M10 20v-5h4v5" />
    </>
  ),
  taekwondo: (
    <>
      <path d="M8 4h8l3 5-4 2v9H9v-9L5 9z" />
      <path d="M9 13h6" />
      <path d="M12 4v4" />
    </>
  ),
  sportsday: (
    <>
      <path d="M12 3c3 0 5 2.4 5 5.5S14.5 15 12 15s-5-3.4-5-6.5S9 3 12 3z" />
      <path d="M12 15l-1 2h2l-1-2z" />
      <path d="M12 17c0 2-2 2-2 4" />
    </>
  ),
  public: (
    <>
      <path d="M3 21h18" />
      <path d="M12 3 4 8h16z" />
      <path d="M6 10v8M10 10v8M14 10v8M18 10v8" />
    </>
  ),
}

function ProgramIcon({ programId, size = 64 }) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="1.6"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      {paths[programId]}
    </svg>
  )
}

export default ProgramIcon
