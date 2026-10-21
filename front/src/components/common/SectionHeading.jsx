import Highlight from './Highlight'

// 섹션 공통 제목: 영문 eyebrow(accent 부분 파란색) + 제목 + 설명(선택)
// align: 'center' | 'left' (소개·프로그램·FAQ 화면은 왼쪽 정렬)
function SectionHeading({ eyebrow, accent, title, description, titleId, align = 'center' }) {
  return (
    <header className={`section-heading ${align === 'left' ? 'section-heading--left' : ''}`}>
      <p className="section-heading__eyebrow">
        <Highlight text={eyebrow} word={accent} as="span" />
      </p>
      <h2 id={titleId} className="section-heading__title">{title}</h2>
      {description && <p className="section-heading__description">{description}</p>}
    </header>
  )
}

export default SectionHeading
