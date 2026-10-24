import { useEffect, useId, useRef, useState } from 'react'
import { NodeViewWrapper } from '@tiptap/react'
import { MAX_CAPTION_LENGTH, MAX_PHOTOS_PER_GROUP } from './photoGroupRules'
import { newsEditor } from '../../data/newsContent'
import { postImageRules } from '../../api/posts'
import './PhotoGroupView.css'

const text = newsEditor.photoGroup

// 편집기 안의 사진 묶음: 사진 추가·빼기·순서, 캡션 입력
function PhotoGroupView({ node, updateAttributes, deleteNode, editor, selected }) {
  const { images, caption } = node.attrs
  const inputId = useId()
  const [uploading, setUploading] = useState(false)
  const editable = editor.isEditable

  // 노드뷰는 속성 변경을 비동기로 다시 그린다. 노드 속성만 value로 쓰면 React가 입력값을 되돌려
  // 한글 조합이 끊기므로 입력값은 로컬 상태로 들고, 실행 취소 등 밖에서 바뀐 캡션만 따라간다
  const [draft, setDraft] = useState(caption)
  const [prevCaption, setPrevCaption] = useState(caption)
  if (caption !== prevCaption) {
    setPrevCaption(caption)
    setDraft(caption)
  }

  const changeCaption = (event) => {
    setDraft(event.target.value)
    updateAttributes({ caption: event.target.value })
  }

  // 업로드가 끝났을 때 그사이 바뀐 사진 목록 위에 덧붙이기 위해 최신 노드를 들고 있는다
  const nodeRef = useRef(node)
  useEffect(() => {
    nodeRef.current = node
  }, [node])

  const handleFiles = async (event) => {
    const files = Array.from(event.target.files)
    event.target.value = ''
    if (files.length === 0) return
    setUploading(true)
    const added = await editor.storage.photoGroup.addPhotos(files, MAX_PHOTOS_PER_GROUP - images.length)
    setUploading(false)
    if (added) updateAttributes({ images: [...nodeRef.current.attrs.images, ...added] })
  }

  const move = (index, step) => {
    const next = [...images]
    ;[next[index], next[index + step]] = [next[index + step], next[index]]
    updateAttributes({ images: next })
  }

  const remove = (index) => {
    const next = images.filter((_, i) => i !== index)
    if (next.length === 0) deleteNode()
    else updateAttributes({ images: next })
  }

  return (
    <NodeViewWrapper as="figure" className={`photo-group ${selected ? 'photo-group--selected' : ''}`}>
      <div className={`photo-group__photos photo-group__photos--${images.length}`} contentEditable={false}>
        {images.map((image, index) => (
          <div key={image.imageKey} className="photo-group__photo">
            <img src={image.src} alt="" />
            {editable && (
              <div className="photo-group__controls">
                <button
                  type="button"
                  className="photo-group__control"
                  aria-label={`${index + 1}번 사진 ${text.moveBefore}`}
                  onClick={() => move(index, -1)}
                  disabled={index === 0}
                >
                  ‹
                </button>
                <button
                  type="button"
                  className="photo-group__control"
                  aria-label={`${index + 1}번 사진 ${text.removePhoto}`}
                  onClick={() => remove(index)}
                >
                  ×
                </button>
                <button
                  type="button"
                  className="photo-group__control"
                  aria-label={`${index + 1}번 사진 ${text.moveAfter}`}
                  onClick={() => move(index, 1)}
                  disabled={index === images.length - 1}
                >
                  ›
                </button>
              </div>
            )}
          </div>
        ))}
      </div>

      <div className="photo-group__footer" contentEditable={false}>
        <input
          className="photo-group__caption"
          value={draft}
          maxLength={MAX_CAPTION_LENGTH}
          placeholder={text.captionPlaceholder}
          aria-label={text.captionPlaceholder}
          onChange={changeCaption}
          disabled={!editable}
        />
        {editable && images.length < MAX_PHOTOS_PER_GROUP && (
          <>
            <input
              id={inputId}
              className="photo-group__file"
              type="file"
              accept={postImageRules.types.join(',')}
              multiple
              onChange={handleFiles}
              disabled={uploading}
            />
            <label htmlFor={inputId} className="photo-group__button">
              {text.add}
            </label>
          </>
        )}
        {editable && (
          <button type="button" className="photo-group__button photo-group__button--danger" onClick={deleteNode}>
            {text.removeGroup}
          </button>
        )}
      </div>
      {uploading && <p className="photo-group__status">{text.uploading}</p>}
    </NodeViewWrapper>
  )
}

export default PhotoGroupView
