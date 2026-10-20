import Highlight from './Highlight'

// 섹션 공통 제목: 영문 eyebrow(accent 부분 파란색) + 제목 + 설명(선택)
function SectionHeading({ eyebrow, accent, title, description, titleId }) {
  return (
    <header className="section-heading">
      <p className="section-heading__eyebrow">
        <Highlight text={eyebrow} word={accent} as="span" />
      </p>
      <h2 id={titleId} className="section-heading__title">{title}</h2>
      {description && <p className="section-heading__description">{description}</p>}
    </header>
  )
}

export default SectionHeading
