let nextPhotoId = 0

// 사진 칸 (PhotoUploader): 이미 저장된 사진은 imageKey, 새로 고른 사진은 file을 가진다
export function toPhoto({ imageKey, url, file }) {
  nextPhotoId += 1
  return { id: nextPhotoId, imageKey, url, file }
}
