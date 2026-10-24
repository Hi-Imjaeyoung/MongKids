import { useCallback, useEffect, useState } from 'react'

// 관리자 화면의 페이지 목록 조회. load(params)는 PageResponse를 돌려주는 API 함수이고,
// params가 바뀌면 다시 불러온다. 항목을 고친 뒤에는 updateItem으로 목록에 바로 반영한다
function useAdminList(load, params) {
  const key = JSON.stringify(params)
  // key: 지금 보여 주는 결과가 어떤 조건으로 불러온 것인지 (다르면 불러오는 중)
  const [state, setState] = useState({ key: null, page: null, error: null })

  useEffect(() => {
    let ignore = false
    load(JSON.parse(key))
      .then((page) => {
        if (!ignore) setState({ key, page, error: null })
      })
      .catch((error) => {
        if (!ignore) setState({ key, page: null, error })
      })
    return () => {
      ignore = true
    }
  }, [load, key])

  // isTarget(item)이 참인 항목을 update(item)의 결과로 바꾼다
  const updateItem = useCallback((isTarget, update) => {
    setState((current) =>
      current.page
        ? {
            ...current,
            page: {
              ...current.page,
              content: current.page.content.map((item) => (isTarget(item) ? update(item) : item)),
            },
          }
        : current,
    )
  }, [])

  return { page: state.page, error: state.error, loading: state.key !== key, updateItem }
}

export default useAdminList
