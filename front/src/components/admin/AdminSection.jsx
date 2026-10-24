import './AdminSection.css'

// 메뉴 한 칸의 제목·설명과 필터 줄(toolbar)
function AdminSection({ title, description, toolbar, children }) {
  return (
    <section className="admin-section" aria-labelledby="admin-section-title">
      <div className="admin-section__head">
        <h1 id="admin-section-title" className="admin-section__title">
          {title}
        </h1>
        <p className="admin-section__description">{description}</p>
      </div>
      {toolbar && <div className="admin-section__toolbar">{toolbar}</div>}
      {children}
    </section>
  )
}

export default AdminSection
