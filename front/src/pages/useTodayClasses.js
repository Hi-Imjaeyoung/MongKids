import { useCallback, useEffect, useState } from 'react'
import { getTodayClasses } from '../api/attendance'

// 오늘 수업 목록 (EMP-FR-013). enabled가 false면(직원이 아니면) 부르지 않는다.
// classes가 null이면 불러오는 중. replaceClass로 저장 응답의 수업 하나만 바꿔 끼운다
function useTodayClasses(enabled) {
  // version: reload할 때마다 올려 다시 불러온다. 결과가 지금 version 것이 아니면 불러오는 중이다
  const [version, setVersion] = useState(0)
  const [state, setState] = useState({ version: null, classes: null, error: null })

  useEffect(() => {
    if (!enabled) return undefined
    let ignore = false
    getTodayClasses()
      .then((classes) => {
        if (!ignore) setState({ version, classes, error: null })
      })
      .catch((error) => {
        if (!ignore) setState({ version, classes: null, error: error.message })
      })
    return () => {
      ignore = true
    }
  }, [enabled, version])

  const reload = useCallback(() => setVersion((current) => current + 1), [])

  const replaceClass = useCallback((updated) => {
    setState((current) => ({
      ...current,
      classes: current.classes?.map((classSession) => (classSession.id === updated.id ? updated : classSession)),
    }))
  }, [])

  const loaded = state.version === version
  return { classes: loaded ? state.classes : null, error: loaded ? state.error : null, reload, replaceClass }
}

export default useTodayClasses
