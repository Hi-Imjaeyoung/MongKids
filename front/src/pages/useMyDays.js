import { useCallback, useEffect, useState } from 'react'
import { getMyClasses } from '../api/attendance'

// 기간 안의 내 수업 (EMP-FR-015). from·to는 'YYYY-MM-DD'. enabled가 false면 부르지 않는다.
// classes가 null이면 불러오는 중. 기간이 바뀌면 다시 불러온다
function useMyClasses(from, to, enabled = true) {
  // 결과가 지금 기간·version 것이 아니면 불러오는 중이다
  const [version, setVersion] = useState(0)
  const [state, setState] = useState({ key: null, classes: null, error: null })
  const key = `${from}~${to}#${version}`

  useEffect(() => {
    if (!enabled) return undefined
    let ignore = false
    getMyClasses(from, to)
      .then((classes) => {
        if (!ignore) setState({ key, classes, error: null })
      })
      .catch((error) => {
        if (!ignore) setState({ key, classes: null, error: error.message })
      })
    return () => {
      ignore = true
    }
  }, [enabled, from, to, key])

  const reload = useCallback(() => setVersion((current) => current + 1), [])

  const loaded = state.key === key
  return { classes: loaded ? state.classes : null, error: loaded ? state.error : null, reload }
}

export default useMyClasses
