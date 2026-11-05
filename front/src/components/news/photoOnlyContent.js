// 사진만 게시물(카드뉴스, SCR-006·SCR-015). 저장은 일반 본문과 같은 사진 묶음 HTML을 쓴다
import { MAX_PHOTOS_PER_GROUP } from './photoGroupRules'

// 사진만 게시물의 최대 사진 수 (게시물 전체 20장 제한보다 작다)
export const MAX_CARD_PHOTOS = 10

// 본문에 글(캡션 포함)이 없고 사진만 있으면 [{ imageKey, url }], 아니면 null
export function parsePhotoOnly(html) {
  if (!html) return null
  const body = new DOMParser().parseFromString(html, 'text/html').body
  if (body.textContent.trim() !== '') return null
  const images = [...body.querySelectorAll('img')].map((image) => ({
    imageKey: image.getAttribute('data-image-key'),
    url: image.getAttribute('src'),
  }))
  return images.length > 0 ? images : null
}

// 사진 키 목록 → 3장씩 나눈 캡션 없는 사진 묶음 HTML (photoGroupExtension의 저장 형식)
export function toPhotoOnlyHtml(imageKeys) {
  const groups = []
  for (let start = 0; start < imageKeys.length; start += MAX_PHOTOS_PER_GROUP) {
    const images = imageKeys
      .slice(start, start + MAX_PHOTOS_PER_GROUP)
      .map((imageKey) => `<img data-image-key="${imageKey.replace(/[&"<>]/g, '')}">`)
    groups.push(`<figure class="post-photos">${images.join('')}</figure>`)
  }
  return groups.join('')
}
