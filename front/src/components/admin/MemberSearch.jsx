import { useState } from 'react'
import { memberAdmin } from '../../data/adminContent'
import './MemberSearch.css'

// 이름·전화번호 검색 (FR-022). 제출하면 onSearch(검색어)
function MemberSearch({ onSearch }) {
  const [value, setValue] = useState('')

  return (
    <form
      className="member-search"
      role="search"
      onSubmit={(event) => {
        event.preventDefault()
        onSearch(value.trim())
      }}
    >
      <label className="member-search__field">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" aria-hidden="true">
          <circle cx="11" cy="11" r="7" />
          <path d="m20 20-3.5-3.5" strokeLinecap="round" />
        </svg>
        <input
          type="search"
          className="member-search__input"
          aria-label={memberAdmin.searchLabel}
          value={value}
          placeholder={memberAdmin.searchPlaceholder}
          onChange={(event) => setValue(event.target.value)}
        />
      </label>
      <button type="submit" className="member-search__button">
        {memberAdmin.search}
      </button>
    </form>
  )
}

export default MemberSearch
