import { useState } from 'react'
import { Link } from 'react-router-dom'
import { resetAboutCurriculumMonths, saveAboutCurriculumMonths } from '../../api/aboutCurriculum'
import { aboutCurriculumEdit } from '../../data/aboutContent'
import ChevronIcon from '../common/ChevronIcon'
import './AboutCurriculum.css'

// "연령별 맞춤 커리큘럼"의 월별 커리큘럼 예시 패널 (SCR-002, FR-034).
// savedMonths: undefined면 불러오는 중, null·빈 배열이면 기본 문구(curriculum.rows), 3줄이면 관리자가 저장한 값.
// 한 줄에 월 / 활동 주제(볼드) / 목표를 나란히 둔다.
// canEdit(관리자)이면 그 자리에서 3줄의 월·활동 주제·목표를 고칠 수 있다. 저장·되돌리면 onSavedMonthsChange를 부른다
function AboutCurriculum({ curriculum, savedMonths, canEdit, onSavedMonthsChange }) {
  const [draft, setDraft] = useState(null)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)

  const loading = savedMonths === undefined
  const hasSaved = Array.isArray(savedMonths) && savedMonths.length > 0
  const rows = hasSaved ? savedMonths : curriculum.rows
  const editing = draft !== null

  const startEdit = () => {
    setDraft(rows.map(({ month, topic, goal }) => ({ month, topic, goal: goal ?? '' })))
    setError(null)
  }

  const changeDraft = (index, field, value) => {
    setDraft((current) => current.map((row, i) => (i === index ? { ...row, [field]: value } : row)))
  }

  const run = async (action) => {
    setSaving(true)
    setError(null)
    try {
      await action()
      setDraft(null)
    } catch (saveError) {
      setError(saveError.message)
    } finally {
      setSaving(false)
    }
  }

  const submit = (event) => {
    event.preventDefault()
    if (saving) return
    const months = draft.map(({ month, topic, goal }) => ({ month: month.trim(), topic: topic.trim(), goal: goal.trim() }))
    if (months.some(({ month, topic, goal }) => !month || !topic || !goal)) {
      setError(aboutCurriculumEdit.emptyError)
      return
    }
    run(async () => onSavedMonthsChange(await saveAboutCurriculumMonths(months)))
  }

  const reset = () => {
    if (saving || !window.confirm(aboutCurriculumEdit.resetConfirm)) return
    run(async () => {
      await resetAboutCurriculumMonths()
      onSavedMonthsChange([])
    })
  }

  return (
    <div className="about-principle__curriculum">
      <div className="about-curriculum__head">
        <h3 className="about-principle__curriculum-title">{curriculum.title}</h3>
        {canEdit && !editing && !loading && (
          <button type="button" className="about-curriculum__edit" onClick={startEdit}>
            {aboutCurriculumEdit.edit}
          </button>
        )}
      </div>

      {editing ? (
        <form className="about-curriculum__form" onSubmit={submit}>
          <ul className="about-principle__months">
            {draft.map((row, index) => (
              <li key={index} className="about-principle__month about-curriculum__row">
                <input
                  className="about-curriculum__input about-curriculum__input--month"
                  aria-label={aboutCurriculumEdit.monthLabel(index)}
                  placeholder={aboutCurriculumEdit.monthPlaceholder}
                  maxLength={aboutCurriculumEdit.monthMaxLength}
                  value={row.month}
                  onChange={(event) => changeDraft(index, 'month', event.target.value)}
                />
                <div className="about-curriculum__body">
                  <input
                    className="about-curriculum__input about-curriculum__input--topic"
                    aria-label={aboutCurriculumEdit.topicLabel(index)}
                    placeholder={aboutCurriculumEdit.topicPlaceholder}
                    maxLength={aboutCurriculumEdit.topicMaxLength}
                    value={row.topic}
                    onChange={(event) => changeDraft(index, 'topic', event.target.value)}
                  />
                  <input
                    className="about-curriculum__input"
                    aria-label={aboutCurriculumEdit.goalLabel(index)}
                    placeholder={aboutCurriculumEdit.goalPlaceholder}
                    maxLength={aboutCurriculumEdit.goalMaxLength}
                    value={row.goal}
                    onChange={(event) => changeDraft(index, 'goal', event.target.value)}
                  />
                </div>
              </li>
            ))}
          </ul>
          {error && (
            <p className="about-curriculum__error" role="alert">
              {error}
            </p>
          )}
          <div className="about-curriculum__buttons">
            {hasSaved && (
              <button type="button" className="about-curriculum__reset" disabled={saving} onClick={reset}>
                {aboutCurriculumEdit.reset}
              </button>
            )}
            <button type="button" className="about-curriculum__cancel" disabled={saving} onClick={() => setDraft(null)}>
              {aboutCurriculumEdit.cancel}
            </button>
            <button type="submit" className="about-curriculum__save" disabled={saving}>
              {saving ? aboutCurriculumEdit.saving : aboutCurriculumEdit.save}
            </button>
          </div>
        </form>
      ) : (
        // 불러오는 동안은 자리만 잡아 두어 기본 문구 → 저장 문구로 바뀌는 깜빡임을 줄인다
        <ul className={`about-principle__months ${loading ? 'about-curriculum__months--loading' : ''}`} aria-busy={loading}>
          {rows.map((row, index) => (
            <li key={index} className="about-principle__month">
              <strong>{row.month}</strong>
              {/* 목표 컬럼 추가 전에 저장된 줄은 목표가 비어 있어 활동 주제만 보인다 */}
              <div className="about-curriculum__body">
                <span className="about-curriculum__topic">{row.topic}</span>
                {row.goal && <span className="about-curriculum__goal">{row.goal}</span>}
              </div>
            </li>
          ))}
        </ul>
      )}

      <div className="about-curriculum__links">
        <Link to={curriculum.link.to} className="about-principle__link">
          {curriculum.link.label}
          <ChevronIcon />
        </Link>
      </div>
    </div>
  )
}

export default AboutCurriculum
