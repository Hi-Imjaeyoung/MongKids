import { labList } from '../../data/labContent'
import './Pagination.css'

// 한 번에 보여 줄 페이지 번호 수
const VISIBLE_PAGES = 5

// page: 현재 페이지(1부터), totalPages, onChange(page)
function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null

  const first = Math.max(1, Math.min(page - Math.floor(VISIBLE_PAGES / 2), totalPages - VISIBLE_PAGES + 1))
  const last = Math.min(totalPages, first + VISIBLE_PAGES - 1)
  const pages = Array.from({ length: last - first + 1 }, (_, index) => first + index)

  return (
    <nav className="pagination" aria-label={labList.pagination}>
      <button
        type="button"
        className="pagination__arrow"
        aria-label={labList.previousPage}
        disabled={page <= 1}
        onClick={() => onChange(page - 1)}
      >
        <ArrowIcon direction="left" />
      </button>
      {pages.map((number) => (
        <button
          key={number}
          type="button"
          className={`pagination__page ${number === page ? 'pagination__page--current' : ''}`}
          aria-current={number === page ? 'page' : undefined}
          onClick={() => onChange(number)}
        >
          {number}
        </button>
      ))}
      <button
        type="button"
        className="pagination__arrow"
        aria-label={labList.nextPage}
        disabled={page >= totalPages}
        onClick={() => onChange(page + 1)}
      >
        <ArrowIcon direction="right" />
      </button>
    </nav>
  )
}

function ArrowIcon({ direction }) {
  return (
    <svg
      width="18"
      height="18"
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth="2.2"
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
    >
      <path d={direction === 'left' ? 'M15 6l-6 6 6 6' : 'M9 6l6 6-6 6'} />
    </svg>
  )
}

export default Pagination
