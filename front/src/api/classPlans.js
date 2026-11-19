// 수업 계획안(SCR-027, FR-032) API. PDF는 서버가 같은 출처로 내려준다 (ADR-009)
import { request } from './client'

// 백엔드 검증과 같은 PDF 제한 (한 파일 20MB, 가정값)
export const classPlanFileRules = {
  maxSize: 20 * 1024 * 1024,
  type: 'application/pdf',
}

function jsonBody(body) {
  return { headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }
}

// [{ id, kind: 'ANNUAL' | 'MONTHLY', title, period, updatedAt }]. 연간이 먼저, 같은 종류는 올린 순서
export function getClassPlans() {
  return request('/api/v1/class-plans')
}

// 누구나 보는 PDF(연간 계획안, 월간 블러본). 파일을 바꾸면 updatedAt이 바뀌어 새로 받는다
export function classPlanFileUrl(plan) {
  return `/api/v1/class-plans/${plan.id}/file?v=${encodeURIComponent(plan.updatedAt ?? '')}`
}

// 월간 원본 PDF. 기관 인증 방법이 정해지기 전까지 관리자만 받는다
export function classPlanOriginalFileUrl(plan) {
  return `/api/v1/admin/class-plans/${plan.id}/original-file`
}

// files: [{ target: 'PUBLIC' | 'ORIGINAL', file }]. S3에 직접 올리고 순서대로 파일 키를 돌려준다
export async function uploadClassPlanFiles(files) {
  if (files.length === 0) return []

  const uploads = await request('/api/v1/admin/class-plans/file-upload-urls', {
    method: 'POST',
    ...jsonBody({
      files: files.map(({ target, file }) => ({ target, contentType: file.type, size: file.size })),
    }),
  })
  await Promise.all(
    uploads.map(async ({ uploadUrl }, index) => {
      const response = await fetch(uploadUrl, {
        method: 'PUT',
        headers: { 'Content-Type': files[index].file.type },
        body: files[index].file,
      })
      if (!response.ok) throw new Error('PDF를 올리지 못했습니다.')
    }),
  )
  return uploads.map(({ fileKey }) => fileKey)
}

// plan: { kind, title, period, fileKey, originalFileKey }. 월간은 원본 키가 꼭 있어야 한다
export function createClassPlan(plan) {
  return request('/api/v1/admin/class-plans', { method: 'POST', ...jsonBody(plan) })
}

// plan: { title, period, fileKey?, originalFileKey? }. 키를 보낸 파일만 바뀐다
export function updateClassPlan(classPlanId, plan) {
  return request(`/api/v1/admin/class-plans/${classPlanId}`, { method: 'PUT', ...jsonBody(plan) })
}

export function deleteClassPlan(classPlanId) {
  return request(`/api/v1/admin/class-plans/${classPlanId}`, { method: 'DELETE' })
}
