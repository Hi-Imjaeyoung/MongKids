import './PageHero.css'

// 화면 상단 소개 영역 (크림 배경): eyebrow + 제목(h1) + 설명 + 버튼(children, 선택)
// tag: eyebrow 옆 뱃지(선택)
function PageHero({ eyebrow, tag, title, description, titleId, children }) {
  return (
    <section className="page-hero" aria-labelledby={titleId}>
      <div className="page-hero__inner container">
        <div className="page-hero__eyebrow-row">
          <p className="page-hero__eyebrow">{eyebrow}</p>
          {tag && <span className="page-hero__tag">{tag}</span>}
        </div>
        <h1 id={titleId} className="page-hero__title">
          {title}
        </h1>
        {description && <p className="page-hero__description">{description}</p>}
        {children && <div className="page-hero__actions">{children}</div>}
      </div>
    </section>
  )
}

export default PageHero
