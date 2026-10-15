// 본문 속 사진 묶음(1~3장 + 캡션) TipTap 노드.
// HTML: <figure class="post-photos"><img data-image-key="…">…<figcaption>…</figcaption></figure>
// 저장할 때는 src를 빼고 키만 남긴다 (서버가 응답 때 src를 채운다)
import { Node, ReactNodeViewRenderer } from '@tiptap/react'
import PhotoGroupView from './PhotoGroupView'

const PhotoGroup = Node.create({
  name: 'photoGroup',
  group: 'block',
  atom: true,
  draggable: true,

  // 사진 검사·업로드·오류 표시는 편집기 컴포넌트가 setPhotoHandler로 등록한다
  addStorage() {
    let handler = null
    return {
      setPhotoHandler(next) {
        handler = next
      },
      // (files, room) => Promise<[{ imageKey, src }] | null>
      addPhotos(files, room) {
        return handler ? handler(files, room) : Promise.resolve(null)
      },
    }
  },

  addAttributes() {
    return {
      images: {
        default: [],
        rendered: false,
        parseHTML: (element) =>
          Array.from(element.querySelectorAll(':scope > img[data-image-key]')).map((image) => ({
            imageKey: image.getAttribute('data-image-key'),
            src: image.getAttribute('src'),
          })),
      },
      caption: {
        default: '',
        rendered: false,
        parseHTML: (element) => element.querySelector(':scope > figcaption')?.textContent ?? '',
      },
    }
  },

  parseHTML() {
    return [{ tag: 'figure.post-photos' }]
  },

  renderHTML({ node }) {
    const images = node.attrs.images.map(({ imageKey }) => ['img', { 'data-image-key': imageKey }])
    const caption = node.attrs.caption.trim() ? [['figcaption', {}, node.attrs.caption.trim()]] : []
    return ['figure', { class: 'post-photos' }, ...images, ...caption]
  },

  addNodeView() {
    return ReactNodeViewRenderer(PhotoGroupView)
  },
})

export default PhotoGroup
