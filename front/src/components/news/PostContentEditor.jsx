import { useEffect, useId, useRef, useState } from 'react'
import { EditorContent, useEditor, useEditorState } from '@tiptap/react'
import StarterKit from '@tiptap/starter-kit'
import PhotoGroup from './photoGroupExtension'
import { MAX_PHOTOS_PER_GROUP, countPhotos } from './photoGroupRules'
import { newsEditor } from '../../data/newsContent'
import { postImageRules, uploadPostImages } from '../../api/posts'
import './PostBody.css'
import './PostContentEditor.css'

const text = newsEditor.toolbar
const HTTP_URL = /^https?:\/\//i

function isAllowedFile(file) {
  return postImageRules.types.includes(file.type) && file.size <= postImageRules.maxSize
}

// 본문 HTML 편집기 (FR-008). 허용 서식: 굵게·기울임·밑줄·링크·목록·인용·구분선 + 사진 묶음.
// onChange({ html, hasContent }), onUploadingChange(boolean), onError(message)
function PostContentEditor({ initialContent = '', onChange, onUploadingChange, onError, disabled = false }) {
  const fileInputId = useId()
  const [uploadingCount, setUploadingCount] = useState(0)

  // 편집기 설정은 한 번만 만들어지므로 onUpdate에는 최신 콜백을 ref로 넘긴다
  const onChangeRef = useRef(onChange)
  useEffect(() => {
    onChangeRef.current = onChange
  }, [onChange])

  const editor = useEditor({
    extensions: [
      StarterKit.configure({
        heading: false,
        code: false,
        codeBlock: false,
        strike: false,
        link: {
          openOnClick: false,
          defaultProtocol: 'https',
          isAllowedUri: (url) => HTTP_URL.test(url),
          HTMLAttributes: { target: null, rel: null },
        },
      }),
      PhotoGroup,
    ],
    content: initialContent,
    editorProps: {
      attributes: { class: 'post-body post-content-editor__body', 'aria-label': newsEditor.content },
    },
    onUpdate: ({ editor: current }) => {
      onChangeRef.current({
        html: current.getHTML(),
        hasContent: current.getText().trim() !== '' || countPhotos(current.state.doc) > 0,
      })
    },
  })

  // 사진 묶음에 넣을 사진을 검사하고 S3에 올린다. 실패하면 오류를 알리고 null
  const addPhotos = async (files, room) => {
    const report = (message) => {
      onError(message)
      return null
    }
    if (files.length > room) return report(newsEditor.errors.tooManyInGroup)
    if (countPhotos(editor.state.doc) + files.length > postImageRules.maxCount) {
      return report(newsEditor.errors.tooMany)
    }
    if (!files.every(isAllowedFile)) return report(newsEditor.errors.invalidFile)

    setUploadingCount((count) => count + 1)
    try {
      const imageKeys = await uploadPostImages(files)
      onError(null)
      // 미리보기는 내려받기 주소 대신 로컬 파일로 보여 준다
      return files.map((file, index) => ({ imageKey: imageKeys[index], src: URL.createObjectURL(file) }))
    } catch {
      return report(newsEditor.errors.upload)
    } finally {
      setUploadingCount((count) => count - 1)
    }
  }

  // 사진 묶음 노드(PhotoGroupView)가 쓰도록 편집기 저장소에 등록한다
  useEffect(() => {
    editor?.storage.photoGroup.setPhotoHandler(addPhotos)
  })

  useEffect(() => {
    editor?.setEditable(!disabled)
  }, [editor, disabled])

  useEffect(() => {
    onUploadingChange?.(uploadingCount > 0)
  }, [uploadingCount, onUploadingChange])

  const active = useEditorState({
    editor,
    selector: ({ editor: current }) => ({
      bold: current.isActive('bold'),
      italic: current.isActive('italic'),
      underline: current.isActive('underline'),
      link: current.isActive('link'),
      bulletList: current.isActive('bulletList'),
      orderedList: current.isActive('orderedList'),
      blockquote: current.isActive('blockquote'),
    }),
  })

  const editLink = () => {
    const input = window.prompt(text.linkPrompt, editor.getAttributes('link').href ?? '')
    if (input === null) return
    const url = input.trim()
    const chain = editor.chain().focus().extendMarkRange('link')
    if (url === '') {
      chain.unsetLink().run()
    } else if (HTTP_URL.test(url)) {
      chain.setLink({ href: url }).run()
    } else {
      onError(newsEditor.errors.invalidLink)
    }
  }

  const insertPhotos = async (event) => {
    const files = Array.from(event.target.files)
    event.target.value = ''
    if (files.length === 0) return
    const images = await addPhotos(files, MAX_PHOTOS_PER_GROUP)
    if (images) editor.chain().focus().insertContent({ type: 'photoGroup', attrs: { images } }).run()
  }

  const tools = [
    { id: 'bold', label: text.bold, icon: 'B', run: () => editor.chain().focus().toggleBold().run() },
    { id: 'italic', label: text.italic, icon: 'I', run: () => editor.chain().focus().toggleItalic().run() },
    { id: 'underline', label: text.underline, icon: 'U', run: () => editor.chain().focus().toggleUnderline().run() },
    { id: 'link', label: text.link, icon: '🔗', run: editLink },
    { id: 'bulletList', label: text.bulletList, icon: '•', run: () => editor.chain().focus().toggleBulletList().run() },
    { id: 'orderedList', label: text.orderedList, icon: '1.', run: () => editor.chain().focus().toggleOrderedList().run() },
    { id: 'blockquote', label: text.blockquote, icon: '❝', run: () => editor.chain().focus().toggleBlockquote().run() },
    { id: 'horizontalRule', label: text.horizontalRule, icon: '―', run: () => editor.chain().focus().setHorizontalRule().run() },
  ]

  return (
    <div className={`post-content-editor ${disabled ? 'post-content-editor--disabled' : ''}`}>
      <div className="post-content-editor__toolbar" role="toolbar" aria-label={text.label}>
        {tools.map((tool) => (
          <button
            key={tool.id}
            type="button"
            className={`post-content-editor__tool post-content-editor__tool--${tool.id} ${
              active?.[tool.id] ? 'post-content-editor__tool--active' : ''
            }`}
            aria-label={tool.label}
            aria-pressed={active?.[tool.id] ?? undefined}
            title={tool.label}
            onClick={tool.run}
            disabled={disabled}
          >
            <span aria-hidden="true">{tool.icon}</span>
          </button>
        ))}
        <input
          id={fileInputId}
          className="post-content-editor__file"
          type="file"
          accept={postImageRules.types.join(',')}
          multiple
          onChange={insertPhotos}
          disabled={disabled}
        />
        <label htmlFor={fileInputId} className="post-content-editor__photo">
          <span aria-hidden="true">📷</span> {text.photos}
        </label>
      </div>
      <EditorContent editor={editor} className="post-content-editor__content" />
      {uploadingCount > 0 && <p className="post-content-editor__status">{newsEditor.photoGroup.uploading}</p>}
    </div>
  )
}

export default PostContentEditor
