import { useCallback, useEffect, useState } from 'react'
import { getToday } from '../api/attendance'

// 오늘 하루 (EMP-FR-020). enabled가 false면(직원이 아니면) 부르지 않는다.
// day가 null이면 불러오는 중. setDay로 저장 응답의 하루를 바꿔 끼운다
function useToday(enabled) {
  // version: reload할 때마다 올려 다시 불러온다. 결과가 지금 version 것이 아니면 불러오는 중이다
  const [version, setVersion] = useState(0)
  const [state, setState] = useState({ version: null, day: null, error: null })

  useEffect(() => {
    if (!enabled) return undefined
    let ignore = false
    getToday()
      .then((day) => {
        if (!ignore) setState({ version, day, error: null })
      })
      .catch((error) => {
        if (!ignore) setState({ version, day: null, error: error.message })
      })
    return () => {
      ignore = true
    }
  }, [enabled, version])

  const reload = useCallback(() => setVersion((current) => current + 1), [])

  const setDay = useCallback((day) => {
    setState((current) => ({ ...current, day }))
  }, [])

  const loaded = state.version === version
  return { day: loaded ? state.day : null, error: loaded ? state.error : null, reload, setDay }
}

export default useToday
