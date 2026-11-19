import { useState } from 'react'
import { classPlanFileRules, createClassPlan, updateClassPlan, uploadClassPlanFiles } from '../../api/classPlans'
import { plansText } from '../../data/plansContent'
import Button from '../common/Button'
import './PlanForm.css'

// 고른 파일이 계획안 PDF로 올릴 수 있는지. 문제가 있으면 안내 문구, 없으면 null
function checkFile(file) {
  if (!file) return null
  if (file.type !== classPlanFileRules.type) return plansText.form.typeError
  if (file.size > classPlanFileRules.maxSize) return plansText.form.sizeError
  return null
}

// 수업 계획안 등록·수정 (SCR-027, FR-032, ADR-009). 관리자만 /plans 화면에서 연다.
// 연간은 공개 PDF 하나, 월간은 관리자가 블러 처리한 공개 PDF와 원본 PDF 두 가지를 구분해 올린다.
// plan이 있으면 수정: 종류는 바꾸지 않고, 새로 고른 파일만 교체한다. 저장하면 onSaved(저장된 계획안)를 부른다
function PlanForm({ plan, onSaved, onCancel }) {
  const editing = Boolean(plan)
  const { form } = plansText
  const [kind, setKind] = useState(plan ? plan.kind : 'monthly')
  const [title, setTitle] = useState(plan?.title ?? '')
  const [period, setPeriod] = useState(plan?.period ?? '')
  const [publicFile, setPublicFile] = useState(null)
  const [originalFile, setOriginalFile] = useState(null)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState(null)
  const monthly = kind === 'monthly'

  const pickFile = (setter) => (event) => {
    const file = event.target.files?.[0] ?? null
    const fileError = checkFile(file)
    setError(fileError)
    setter(fileError ? null : file)
    if (fileError) event.target.value = ''
  }

  const submit = async (event) => {
    event.preventDefault()
    if (saving) return
    const trimmedTitle = title.trim()
    const trimmedPeriod = period.trim()
    if (!trimmedTitle || !trimmedPeriod) {
      setError(form.emptyError)
      return
    }
    // 새로 등록할 때는 파일이 모두 있어야 한다. 수정은 고른 파일만 바꾼다
    if (!editing && (!publicFile || (monthly && !originalFile))) {
      setError(monthly ? form.monthlyFileError : form.annualFileError)
      return
    }

    setSaving(true)
    setError(null)
    try {
      const files = [
        publicFile && { target: 'PUBLIC', file: publicFile },
        monthly && originalFile && { target: 'ORIGINAL', file: originalFile },
      ].filter(Boolean)
      const keys = await uploadClassPlanFiles(files)
      const keyOf = (target) => keys[files.findIndex((item) => item.target === target)] ?? null
      const body = { title: trimmedTitle, period: trimmedPeriod, fileKey: keyOf('PUBLIC'), originalFileKey: keyOf('ORIGINAL') }
      const saved = editing
        ? await updateClassPlan(plan.id, body)
        : await createClassPlan({ ...body, kind: kind.toUpperCase() })
      onSaved(saved)
    } catch (saveError) {
      setError(saveError.message)
      setSaving(false)
    }
  }

  return (
    <form className="plan-form" onSubmit={submit}>
      <h2 className="plan-form__title">{editing ? form.editTitle : form.createTitle}</h2>

      <fieldset className="plan-form__field plan-form__kinds" disabled={editing || saving}>
        <legend className="plan-form__label">{form.kindLabel}</legend>
        {['annual', 'monthly'].map((value) => (
          <label key={value} className={`plan-form__kind ${kind === value ? 'plan-form__kind--selected' : ''}`}>
            <input type="radio" name="plan-kind" value={value} checked={kind === value} onChange={() => setKind(value)} />
            {plansText.groups[value]}
          </label>
        ))}
        {editing && <p className="plan-form__hint">{form.kindFixed}</p>}
      </fieldset>

      <label className="plan-form__field">
        <span className="plan-form__label">{form.titleLabel}</span>
        <input
          className="plan-form__input"
          value={title}
          maxLength={form.titleMaxLength}
          placeholder={monthly ? form.monthlyTitlePlaceholder : form.annualTitlePlaceholder}
          disabled={saving}
          onChange={(event) => setTitle(event.target.value)}
        />
      </label>

      <label className="plan-form__field">
        <span className="plan-form__label">{form.periodLabel}</span>
        <input
          className="plan-form__input"
          value={period}
          maxLength={form.periodMaxLength}
          placeholder={monthly ? form.monthlyPeriodPlaceholder : form.annualPeriodPlaceholder}
          disabled={saving}
          onChange={(event) => setPeriod(event.target.value)}
        />
      </label>

      <label className="plan-form__field plan-form__file">
        <span className="plan-form__label">{monthly ? form.previewFileLabel : form.annualFileLabel}</span>
        <span className="plan-form__hint">{monthly ? form.previewFileHint : form.annualFileHint}</span>
        <input type="file" accept={classPlanFileRules.type} disabled={saving} onChange={pickFile(setPublicFile)} />
        {editing && !publicFile && <span className="plan-form__hint">{form.keepFile}</span>}
      </label>

      {monthly && (
        <label className="plan-form__field plan-form__file plan-form__file--original">
          <span className="plan-form__label">{form.originalFileLabel}</span>
          <span className="plan-form__hint">{form.originalFileHint}</span>
          <input type="file" accept={classPlanFileRules.type} disabled={saving} onChange={pickFile(setOriginalFile)} />
          {editing && !originalFile && <span className="plan-form__hint">{form.keepFile}</span>}
        </label>
      )}

      {error && (
        <p className="plan-form__error" role="alert">
          {error}
        </p>
      )}

      <div className="plan-form__buttons">
        <Button variant="ghost" size="lg" type="button" disabled={saving} onClick={onCancel}>
          {form.cancel}
        </Button>
        <Button variant="blue" size="lg" type="submit" disabled={saving}>
          {saving ? form.saving : form.save}
        </Button>
      </div>
    </form>
  )
}

export default PlanForm
