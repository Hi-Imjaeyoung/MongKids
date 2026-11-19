import { useState } from 'react'
import { plansText } from '../../data/plansContent'
import Button from '../common/Button'
import ImagePlaceholder from '../common/ImagePlaceholder'
import { LockIcon } from './PlanList'
import './PlanViewer.css'

const ZOOM_MIN = 50
const ZOOM_MAX = 200
const ZOOM_STEP = 25

// 이미지 저장 메뉴·끌어 놓기를 막는다. 화면 캡처까지 막을 수는 없다
const blockSave = (event) => event.preventDefault()

// 수업 계획안 보기 (SCR-027). 연간은 이미지를 확대·축소해 보고,
// 월간은 블러 처리한 미리보기 위에 잠김 안내를 띄운다 (원본은 인증 후 서버가 내려줌, FR-032)
// 부모가 key={plan.id}로 렌더링해 계획안을 바꾸면 확대 비율이 처음으로 돌아간다
function PlanViewer({ plan, member, onLoginClick, onShowAnnual }) {
  const [zoom, setZoom] = useState(100)
  const locked = plan.kind === 'monthly'
  const src = locked ? plan.preview : plan.image
  const { lock } = plansText

  return (
    <div className="plan-viewer">
      <div className="plan-viewer__head">
        <div className="plan-viewer__heading">
          <h2 className="plan-viewer__title">{plan.title}</h2>
          <p className="plan-viewer__period">{plan.period}</p>
        </div>
        {!locked && (
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
        )}
      </div>

      <div className={`plan-viewer__stage ${locked ? 'plan-viewer__stage--locked' : ''}`}>
        <div
          className={`plan-viewer__page ${plan.portrait ? 'plan-viewer__page--portrait' : ''}`}
          style={locked ? undefined : { '--zoom': zoom / 100 }}
        >
          {src ? (
            <img
              className="plan-viewer__image"
              src={src}
              alt={locked ? plansText.previewAlt(plan.title) : plan.title}
              draggable={false}
              onContextMenu={blockSave}
              onDragStart={blockSave}
            />
          ) : (
            <ImagePlaceholder label={plansText.imagePlaceholder(plan.title)} className="plan-viewer__placeholder" />
          )}
        </div>

        {locked && (
          <div className="plan-viewer__lock">
            <span className="plan-viewer__lock-icon">
              <LockIcon />
            </span>
            <h3 className="plan-viewer__lock-title">{lock.title}</h3>
            <p className="plan-viewer__lock-text">{member ? lock.member : lock.guest}</p>
            {!member && (
              <Button variant="blue" size="lg" className="plan-viewer__lock-button" onClick={onLoginClick}>
                {lock.login}
              </Button>
            )}
            <Button variant="ghost" size="lg" className="plan-viewer__lock-button" onClick={onShowAnnual}>
              {lock.showAnnual}
            </Button>
          </div>
        )}
      </div>

      {!locked && <p className="plan-viewer__note">{plansText.viewNote}</p>}
    </div>
  )
}

export default PlanViewer
