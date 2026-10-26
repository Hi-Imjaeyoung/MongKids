import { useEffect, useState } from 'react'
import { subscribeNewConsultationCount } from '../api/admin'

// 관리자 화면의 신규 상담 건수 (FR-005). 헤더 "관리자" 링크와 상담 신청 메뉴가 함께 쓴다.
// enabled(관리자 로그인)일 때만 구독하고, 로그아웃하면 연결을 닫는다. 건수를 받기 전에는 null
function useNewConsultationCount(enabled) {
  const [count, setCount] = useState(null)

  useEffect(() => {
    if (!enabled) return
    const unsubscribe = subscribeNewConsultationCount(setCount)
    return () => {
      unsubscribe()
      setCount(null)
    }
  }, [enabled])

  return count
}

export default useNewConsultationCount
