import { useLayoutEffect, useRef, useState } from 'react'
import { reviewList } from '../../data/reviewContent'
import ChevronIcon from '../common/ChevronIcon'
import './ReviewContent.css'

// 후기 본문 (SCR-012). 접혀 있을 때 잘리는 내용이 있으면 "더보기"로 펼친다.
// 자르는 기준은 CSS가 정한다: 데스크톱 사진 있는 후기는 사진 칸 높이, 사진 없는 후기는 6줄, 모바일은 5줄
// expanded·onToggle: 펼침 상태는 카드가 가진다 (사진 칸 높이에 맞출지 카드에서 정해야 해서)
function ReviewContent({ id, content, expanded, onToggle }) {
  const textRef = useRef(null)
  const [clipped, setClipped] = useState(false)

  // 접혀 있는 동안 실제로 잘렸는지 잰다. 창 크기·사진 로드로 칸 크기가 바뀌면 다시 잰다
  useLayoutEffect(() => {
    if (expanded) return
    const text = textRef.current
    const measure = () => setClipped(text.scrollHeight > text.clientHeight + 1)
    measure()
    const observer = new ResizeObserver(measure)
    observer.observe(text)
    return () => observer.disconnect()
  }, [expanded, content])

  const folded = !expanded

  return (
    <div className="review-content">
      <p
        id={id}
        className={`review-content__text ${folded ? 'review-content__text--folded' : ''} ${
          folded && clipped ? 'review-content__text--clipped' : ''
        }`}
        ref={textRef}
      >
        {content}
      </p>
      {(clipped || expanded) && (
        <button
          type="button"
          className={`review-content__toggle ${expanded ? 'review-content__toggle--open' : ''}`}
          aria-expanded={expanded}
          aria-controls={id}
          onClick={onToggle}
        >
          {expanded ? reviewList.less : reviewList.more}
          <ChevronIcon />
        </button>
      )}
    </div>
  )
}

export default ReviewContent
