import { Link } from 'react-router-dom'
import NewsNotice from '../news/NewsNotice'
import NewsTabs from '../news/NewsTabs'
import LabCard from './LabCard'
import Pagination from './Pagination'
import { labCategories, labList, labNotice } from '../../data/labContent'
import '../common/Button.css'
import './LabList.css'

// labPosts가 null이면 불러오는 중. keyword: 현재 검색어 (없으면 ''). canWrite: 글쓰기 버튼 노출 여부
function LabList({
  category,
  onCategoryChange,
  keyword,
  onClearSearch,
  labPosts,
  total,
  page,
  totalPages,
  onPageChange,
  error,
  canWrite,
}) {
  return (
    <section id="lab-list" className="lab-list container" aria-labelledby="lab-list-title">
      <div className="lab-list__heading">
        <h2 id="lab-list-title" className="lab-list__title">
          {labList.title}
        </h2>
        {canWrite && (
          <Link to="/lab/write" className="btn btn--blue btn--md">
            {labList.write}
          </Link>
        )}
      </div>

      <div className="lab-list__toolbar">
        <NewsTabs active={category} onChange={onCategoryChange} categories={labCategories} />
        {labPosts && (
          <p className="lab-list__total">
            {keyword ? (
              <>
                <strong className="lab-list__keyword">‘{keyword}’</strong> {labList.searchResult}{' '}
                <strong>{total}</strong>
                {labList.totalSuffix}
              </>
            ) : (
              <>
                {labList.totalPrefix} <strong>{total}</strong>
                {labList.totalSuffix}
              </>
            )}
          </p>
        )}
      </div>

      {error && <NewsNotice message={error} tone="error" />}
      {!error && !labPosts && <NewsNotice message={labList.loading} />}
      {labPosts?.length === 0 &&
        (keyword ? (
          <div className="lab-list__empty" role="status">
            <svg
              width="40"
              height="40"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              strokeWidth="1.8"
              strokeLinecap="round"
              strokeLinejoin="round"
              aria-hidden="true"
            >
              <circle cx="11" cy="11" r="7" />
              <path d="M20 20l-3.5-3.5" />
              <path d="M8.5 11h5" />
            </svg>
            <p className="lab-list__empty-title">{labList.searchEmpty(keyword)}</p>
            <p className="lab-list__empty-hint">{labList.searchEmptyHint}</p>
            <button type="button" className="lab-list__clear" onClick={onClearSearch}>
              {labList.clearSearch}
            </button>
          </div>
        ) : (
          <NewsNotice message={labList.empty} />
        ))}

      {labPosts?.length > 0 && (
        <>
          <ul className="lab-list__grid">
            {labPosts.map((labPost) => (
              <li key={labPost.id}>
                <LabCard labPost={labPost} />
              </li>
            ))}
          </ul>
          <Pagination page={page} totalPages={totalPages} onChange={onPageChange} />
        </>
      )}

      <p className="lab-list__notice">{labNotice}</p>
    </section>
  )
}

export default LabList
