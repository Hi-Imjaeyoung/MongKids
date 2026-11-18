import { useCallback, useEffect, useState } from 'react'
import { getMyDays } from '../api/attendance'

// 기간 안의 내 하루 기록 (EMP-FR-015). from·to는 'YYYY-MM-DD'. enabled가 false면 부르지 않는다.
// days가 null이면 불러오는 중. 기간이 바뀌면 다시 불러온다
function useMyDays(from, to, enabled = true) {
  // 결과가 지금 기간·version 것이 아니면 불러오는 중이다
  const [version, setVersion] = useState(0)
  const [state, setState] = useState({ key: null, days: null, error: null })
  const key = `${from}~${to}#${version}`

  useEffect(() => {
    if (!enabled) return undefined
    let ignore = false
    getMyDays(from, to)
      .then((days) => {
        if (!ignore) setState({ key, days, error: null })
      })
      .catch((error) => {
        if (!ignore) setState({ key, days: null, error: error.message })
      })
    return () => {
      ignore = true
    }
  }, [enabled, from, to, key])

  const reload = useCallback(() => setVersion((current) => current + 1), [])

  const loaded = state.key === key
  return { days: loaded ? state.days : null, error: loaded ? state.error : null, reload }
}

export default useMyDays
