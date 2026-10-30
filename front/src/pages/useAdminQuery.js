import { useCallback, useEffect, useState } from 'react'

// 관리자 화면의 목록(배열) 조회. load(params)는 배열 등 데이터를 돌려주는 API 함수이고,
// params가 바뀌거나 reload()를 부르면 다시 불러온다. 고친 뒤에는 setData로 목록에 바로 반영한다.
// params가 null이면 부르지 않는다 (필터 값이 아직 올바르지 않을 때)
function useAdminQuery(load, params) {
  const key = params === null ? null : JSON.stringify(params)
  const [version, setVersion] = useState(0)
  // loadedKey: 지금 보여 주는 결과가 어떤 조건으로 불러온 것인지 (다르면 불러오는 중)
  const [state, setState] = useState({ loadedKey: null, data: null, error: null })
  const requestKey = key === null ? null : `${key}#${version}`

  useEffect(() => {
    if (requestKey === null) return undefined
    let ignore = false
    load(JSON.parse(key))
      .then((data) => {
        if (!ignore) setState({ loadedKey: requestKey, data, error: null })
      })
      .catch((error) => {
        if (!ignore) setState({ loadedKey: requestKey, data: null, error })
      })
    return () => {
      ignore = true
    }
  }, [load, key, requestKey])

  const setData = useCallback((update) => {
    setState((current) => ({ ...current, data: update(current.data) }))
  }, [])
  const reload = useCallback(() => setVersion((current) => current + 1), [])

  return {
    data: state.data,
    error: state.error,
    loading: requestKey !== null && state.loadedKey !== requestKey,
    setData,
    reload,
  }
}

export default useAdminQuery
