import { Link } from 'react-router-dom'
import ChevronIcon from '../common/ChevronIcon'
import './PrivacySection.css'

// 표 칸: 문자열이거나 줄 배열({ label, text, muted })
function CellContent({ value }) {
  if (typeof value === 'string') return value
  return value.map((line) => (
    <span key={line.text} className={`privacy-section__line ${line.muted ? 'privacy-section__line--muted' : ''}`}>
      {line.label && <strong>{line.label} </strong>}
      {line.text}
    </span>
  ))
}

function Block({ block }) {
  switch (block.type) {
    case 'text':
      return <p className="privacy-section__text">{block.text}</p>
    case 'note':
      return <p className="privacy-section__note">{block.text}</p>
    case 'list':
      return (
        <ul className="privacy-section__list">
          {block.items.map((item) => (
            <li key={item.id}>
              {item.label && <strong>{item.label}: </strong>}
              {item.text}
            </li>
          ))}
        </ul>
      )
    case 'callout':
      return (
        <ul className="privacy-section__callout">
          {block.items.map((text) => (
            <li key={text}>{text}</li>
          ))}
        </ul>
      )
    case 'table':
      // 모바일은 행마다 카드로 쌓고 칸 이름(data-label)을 앞에 붙인다
      return (
        <div className="privacy-section__table-wrap">
          <table className="privacy-section__table">
            <thead>
              <tr>
                {block.columns.map((column) => (
                  <th key={column.key} scope="col">
                    {column.label}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {block.rows.map((row) => (
                <tr key={row.id}>
                  {block.columns.map((column, index) =>
                    index === 0 ? (
                      <th key={column.key} scope="row" data-label={column.label}>
                        <CellContent value={row[column.key]} />
                      </th>
                    ) : (
                      <td key={column.key} data-label={column.label}>
                        <CellContent value={row[column.key]} />
                      </td>
                    ),
                  )}
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )
    case 'contact':
      return (
        <dl className="privacy-section__contact">
          {block.items.map((item) => (
            <div key={item.id} className="privacy-section__contact-row">
              <dt>{item.label}</dt>
              <dd>{item.href ? <a href={item.href}>{item.value}</a> : item.value}</dd>
            </div>
          ))}
        </dl>
      )
    case 'agencies':
      return (
        <ul className="privacy-section__agencies">
          {block.items.map((item) => (
            <li key={item.id}>
              <span>{item.name}</span>
              <a href={`tel:${item.phone}`}>{item.phone}</a>
            </li>
          ))}
        </ul>
      )
    case 'link':
      return (
        <Link to={block.to} className="privacy-section__link">
          {block.label}
          <ChevronIcon />
        </Link>
      )
    default:
      return null
  }
}

// 개인정보처리방침 조항 하나 (SCR-023). blocks 종류는 privacyContent.js 참고
function PrivacySection({ section }) {
  const titleId = `${section.id}-title`

  return (
    <section id={section.id} className="privacy-section" aria-labelledby={titleId}>
      <h2 id={titleId} className="privacy-section__title">
        {section.title}
      </h2>
      {section.blocks.map((block, index) => (
        // 블록은 순서만 있고 id가 없는 고정 문구라 순서를 key로 쓴다
        <Block key={`${block.type}-${index}`} block={block} />
      ))}
    </section>
  )
}

export default PrivacySection
