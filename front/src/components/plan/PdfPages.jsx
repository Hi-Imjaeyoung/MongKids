import { useEffect, useRef, useState } from 'react'
import { plansText } from '../../data/plansContent'
import './PdfPages.css'

// 저장 메뉴·끌어 놓기를 막는다. 화면 캡처까지 막을 수는 없다
const blockSave = (event) => event.preventDefault()

// 확대해도 흐려지지 않도록 화면 폭보다 넉넉하게 그린다 (CSS로 줄여 보여 준다)
const RENDER_SCALE = 2

let pdfjsPromise = null

// pdf.js는 계획안 화면에서만 쓰므로 처음 필요할 때 불러온다
function loadPdfjs() {
  if (!pdfjsPromise) {
    pdfjsPromise = Promise.all([
      import('pdfjs-dist'),
      import('pdfjs-dist/build/pdf.worker.min.mjs?url'),
    ]).then(([pdfjs, worker]) => {
      pdfjs.GlobalWorkerOptions.workerSrc = worker.default
      return pdfjs
    })
  }
  return pdfjsPromise
}

// PDF 쪽을 캔버스에 그려 이미지처럼 보여 준다 (SCR-027: 화면 안에서만 보기, 내려받기·인쇄 버튼 없음).
// url은 같은 출처 API라 로그인 쿠키가 함께 간다. 쪽 너비는 부모(.plan-viewer__page)가 정한다
function PdfPages({ url, label }) {
  const containerRef = useRef(null)
  const [status, setStatus] = useState('loading')

  useEffect(() => {
    const container = containerRef.current
    let cancelled = false
    let loadingTask = null

    const render = async () => {
      setStatus('loading')
      container.replaceChildren()
      try {
        const pdfjs = await loadPdfjs()
        if (cancelled) return
        // PDF 안의 스크립트·글꼴 코드를 eval로 실행하지 않는다 (악성 PDF 방어)
        loadingTask = pdfjs.getDocument({ url, withCredentials: true, isEvalSupported: false })
        const pdf = await loadingTask.promise
        for (let pageNumber = 1; pageNumber <= pdf.numPages; pageNumber += 1) {
          const page = await pdf.getPage(pageNumber)
          if (cancelled) return
          const viewport = page.getViewport({ scale: RENDER_SCALE })
          const canvas = document.createElement('canvas')
          canvas.className = 'pdf-pages__page'
          canvas.width = viewport.width
          canvas.height = viewport.height
          canvas.setAttribute('role', 'img')
          canvas.setAttribute('aria-label', plansText.pageLabel(label, pageNumber, pdf.numPages))
          canvas.addEventListener('contextmenu', blockSave)
          container.append(canvas)
          await page.render({ canvas, viewport }).promise
          if (pageNumber === 1 && !cancelled) setStatus('ready')
        }
      } catch {
        if (!cancelled) setStatus('error')
      }
    }

    render()
    return () => {
      cancelled = true
      loadingTask?.destroy()
    }
  }, [url, label])

  return (
    <div className="pdf-pages" aria-busy={status === 'loading'}>
      {status === 'loading' && <p className="pdf-pages__status">{plansText.pdfLoading}</p>}
      {status === 'error' && (
        <p className="pdf-pages__status" role="alert">
          {plansText.pdfError}
        </p>
      )}
      <div ref={containerRef} className="pdf-pages__list" onDragStart={blockSave} />
    </div>
  )
}

export default PdfPages
