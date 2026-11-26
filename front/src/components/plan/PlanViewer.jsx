import { useState } from 'react'
import { classPlanFileUrl, classPlanOriginalFileUrl } from '../../api/classPlans'
import { plansText } from '../../data/plansContent'
import Button from '../common/Button'
import PdfPages from './PdfPages'
import { LockIcon } from './PlanList'
import './PlanViewer.css'

const ZOOM_MIN = 50
const ZOOM_MAX = 200
const ZOOM_STEP = 25

// 수업 계획안 보기 (SCR-027). PDF를 쪽마다 그려 확대·축소해 본다.
// 연간·월간 모두 관리자가 올린 블러본을 보여 주고, 위에 잠김 안내를 작게 둔다 (원본은 서버가 권한을 확인한 뒤에만 내려줌, FR-032).
// 잠김 안내는 블러 처리되지 않은 부분(월간 1주차 등)을 가리지 않도록 PDF 위가 아닌 판 위쪽에 둔다 (2026-11-26).
// canManage(관리자)이면 잠김 안내 대신 블러본·원본 고르기와 수정·삭제 버튼을 둔다.
// 부모가 key={plan.id}로 렌더링해 계획안을 바꾸면 확대 비율·보기 선택이 처음으로 돌아간다
function PlanViewer({ plan, member, canManage, onLoginClick, onEdit, onDelete }) {
  const [zoom, setZoom] = useState(100)
  const [showOriginal, setShowOriginal] = useState(false)
  const locked = !canManage
  // 2026-11-26 이전에 올린 연간은 원본이 없을 수 있다. 그때는 원본 버튼을 막고 수정에서 올리도록 안내한다
  const original = canManage && showOriginal && plan.hasOriginal
  const url = original ? classPlanOriginalFileUrl(plan) : classPlanFileUrl(plan)
  const { lock } = plansText

  return (
    <div className="plan-viewer">
      <div className="plan-viewer__head">
        <div className="plan-viewer__heading">
          <h2 className="plan-viewer__title">{plan.title}</h2>
          <p className="plan-viewer__period">{plan.period}</p>
        </div>
        <div className="plan-viewer__toolbar" role="toolbar" aria-label={plansText.toolbarLabel}>
          <button
            type="button"
            className="plan-viewer__zoom"
            aria-label={plansText.zoomOut}
            disabled={zoom <= ZOOM_MIN}
            onClick={() => setZoom((value) => Math.max(ZOOM_MIN, value - ZOOM_STEP))}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
              <path d="M5 12h14" />
            </svg>
          </button>
          <span className="plan-viewer__zoom-value" aria-live="polite">
            {zoom}%
          </span>
          <button
            type="button"
            className="plan-viewer__zoom"
            aria-label={plansText.zoomIn}
            disabled={zoom >= ZOOM_MAX}
            onClick={() => setZoom((value) => Math.min(ZOOM_MAX, value + ZOOM_STEP))}
          >
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" aria-hidden="true">
              <path d="M5 12h14M12 5v14" />
            </svg>
          </button>
        </div>
      </div>

      {canManage && (
        <div className="plan-viewer__admin">
          <div className="plan-viewer__versions" role="group" aria-label={plansText.admin.versionLabel}>
            <button
              type="button"
              className={`plan-viewer__version ${!original ? 'plan-viewer__version--selected' : ''}`}
              aria-pressed={!original}
              onClick={() => setShowOriginal(false)}
            >
              {plansText.admin.previewVersion}
            </button>
            <button
              type="button"
              className={`plan-viewer__version ${original ? 'plan-viewer__version--selected' : ''}`}
              aria-pressed={original}
              disabled={!plan.hasOriginal}
              onClick={() => setShowOriginal(true)}
            >
              {plansText.admin.originalVersion}
            </button>
          </div>
          <div className="plan-viewer__admin-buttons">
            <button type="button" className="plan-viewer__admin-button" onClick={onEdit}>
              {plansText.admin.edit}
            </button>
            <button type="button" className="plan-viewer__admin-button plan-viewer__admin-button--danger" onClick={onDelete}>
              {plansText.admin.delete}
            </button>
          </div>
          <p className="plan-viewer__admin-note">
            {!plan.hasOriginal ? plansText.admin.noOriginalNote : original ? plansText.admin.originalNote : plansText.admin.previewNote}
          </p>
        </div>
      )}

      {locked && (
        <div className="plan-viewer__lock">
          <span className="plan-viewer__lock-icon">
            <LockIcon />
          </span>
          <div className="plan-viewer__lock-body">
            <h3 className="plan-viewer__lock-title">{lock.title}</h3>
            <p className="plan-viewer__lock-text">{member ? lock.member : lock.guest}</p>
          </div>
          {!member && (
            <Button variant="blue" size="md" className="plan-viewer__lock-button" onClick={onLoginClick}>
              {lock.login}
            </Button>
          )}
        </div>
      )}

      <div className="plan-viewer__stage">
        <div className="plan-viewer__page" style={{ '--zoom': zoom / 100 }}>
          <PdfPages
            key={url}
            url={url}
            label={original ? plan.title : plansText.previewAlt(plan.title)}
          />
        </div>
      </div>

      <p className="plan-viewer__note">{plansText.viewNote}</p>
    </div>
  )
}

export default PlanViewer
