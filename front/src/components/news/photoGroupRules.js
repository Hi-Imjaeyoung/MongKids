// 사진 묶음 제한과 집계 (백엔드 PostContentSanitizer와 같은 값)
export const MAX_PHOTOS_PER_GROUP = 3
export const MAX_CAPTION_LENGTH = 200

// 문서 안 전체 사진 수
export function countPhotos(doc) {
  let count = 0
  doc.descendants((node) => {
    if (node.type.name === 'photoGroup') count += node.attrs.images.length
  })
  return count
}
