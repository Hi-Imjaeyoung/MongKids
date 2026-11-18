import { useEffect, useState } from 'react'
import {
  checkDayEndLocation,
  correctDayEnd,
  endDayByLocation,
  endDayManually,
  startDayByLocation,
  startDayManually,
  uploadAttendanceImages,
} from '../api/attendance'
import AttendanceLayout from '../components/attendance/AttendanceLayout'
import AttendanceNotice from '../components/attendance/AttendanceNotice'
import DayEndForm from '../components/attendance/DayEndForm'
import DayToday from '../components/attendance/DayToday'
import LocationFail from '../components/attendance/LocationFail'
import LocationGuide from '../components/attendance/LocationGuide'
import ManualStartForm from '../components/attendance/ManualStartForm'
import { canUseAttendance, firstInstitution, lastInstitution } from '../components/attendance/attendanceFormat'
import Button from '../components/common/Button'
import { attendancePage } from '../data/attendanceContent'
import AttendanceHistory from './AttendanceHistory'
import useCurrentPosition from './useCurrentPosition'
import useToday from './useToday'
import { pageMeta } from '../data/seoContent'
import usePageMeta from './usePageMeta'

const TODAY = { name: 'today' }

// 사진 목록에서 새로 고른 사진만 올리고, 화면 순서대로 객체 키를 돌려준다
async function toImageKeys(photos) {
  const newPhotos = photos.filter((photo) => photo.file)
  const uploadedKeys = await uploadAttendanceImages(newPhotos.map((photo) => photo.file))
  const keyByPhotoId = new Map(newPhotos.map((photo, index) => [photo.id, uploadedKeys[index]]))
  return photos.map((photo) => photo.imageKey ?? keyByPhotoId.get(photo.id))
}

function toPosition({ latitude, longitude, accuracy }) {
  return { latitude, longitude, accuracy }
}

// 하루 수업 시작·종료 체크 (EMP-SCR-001, EMP-FR-020). 오늘 하루(①)에서 하위 화면과 날짜별 수업 기록(⑧~⑪)으로 바꿔 가며 보여 준다.
// 하루 시작은 첫 수업 기관, 하루 종료는 마지막 수업 기관 위치로 확인한다. 수업 사이 기관 이동은 확인하지 않는다.
// member: undefined면 로그인 상태 확인 중, null이면 비로그인. 직원(STAFF·ADMIN)이 아니면 API를 부르지 않는다
function AttendancePage({ member, onLoginClick }) {
  usePageMeta(pageMeta.attendance.title)
  const allowed = canUseAttendance(member)
  const { day, error, reload, setDay } = useToday(allowed)
  const { locate } = useCurrentPosition()
  const [today] = useState(() => new Date())
  // view.name: today | fail | guide | manualStart | end | manualEnd | edit | history. view.mode: 'start' | 'end'
  const [view, setView] = useState(TODAY)
  const [locating, setLocating] = useState(false)
  const [actionError, setActionError] = useState(null)
  const [formError, setFormError] = useState(null)
  const [submitting, setSubmitting] = useState(false)

  // 화면이 바뀌면 맨 위에서 시작한다
  useEffect(() => {
    window.scrollTo(0, 0)
  }, [view])

  if (member === undefined) return <AttendanceNotice message={attendancePage.checking} />
  if (member === null) {
    return (
      <AttendanceNotice message={attendancePage.loginRequired}>
        <Button variant="blue" onClick={onLoginClick}>
          {attendancePage.login}
        </Button>
      </AttendanceNotice>
    )
  }
  if (!allowed) {
    return (
      <AttendanceNotice message={attendancePage.notStaff}>
        <Button variant="ghost" to="/">
          {attendancePage.goHome}
        </Button>
      </AttendanceNotice>
    )
  }

  const show = (next) => {
    setFormError(null)
    setView(next)
  }
  const backToToday = () => show(TODAY)

  // 위치를 받아 서버에 확인한다. 위치를 못 받으면 서버에 묻지 않고 실패·위치 설정 안내로 보낸다
  // mode 'start': 첫 기관에서 하루 시작(확정되면 저장), 'end': 마지막 기관 반경 안이면 종료 화면으로(저장은 제출할 때)
  const checkLocation = async (mode) => {
    setActionError(null)
    setLocating(true)
    let position
    try {
      position = toPosition(await locate())
    } catch ({ kind }) {
      setLocating(false)
      if (kind === 'denied' || kind === 'unsupported') show({ name: 'guide', mode, unsupported: kind === 'unsupported' })
      else show({ name: 'fail', mode, reason: kind })
      return
    }
    try {
      const result =
        mode === 'start' ? await startDayByLocation(day.date, position) : await checkDayEndLocation(day.date, position)
      setDay(result.day)
      if (!result.confirmed) {
        show({
          name: 'fail',
          mode,
          reason: 'outOfRange',
          distance: result.distanceMeters,
          radius: result.radiusMeters,
          institutionName: result.institutionName,
          position,
        })
      } else if (mode === 'start') {
        backToToday()
      } else {
        show({ name: 'end', position, check: result })
      }
    } catch (checkError) {
      setActionError(checkError.message)
      backToToday()
    } finally {
      setLocating(false)
    }
  }

  // 저장 요청을 보내고, 성공하면 바뀐 하루를 반영한 뒤 오늘 하루로 돌아간다
  const save = async (task) => {
    setSubmitting(true)
    setFormError(null)
    try {
      const result = await task()
      if (result) {
        setDay(result)
        backToToday()
      }
    } catch (saveError) {
      setFormError(saveError.message)
    } finally {
      setSubmitting(false)
    }
  }

  // 하루 종료 제출. 위치로 확인했는데 그사이 반경 밖으로 나갔으면 저장되지 않으므로 실패 화면으로 보낸다
  const submitEnd = ({ photos, notes }) =>
    save(async () => {
      const result = await endDayByLocation(day.date, { ...view.position, imageKeys: await toImageKeys(photos), notes })
      if (result.confirmed) return result.day
      show({
        name: 'fail',
        mode: 'end',
        reason: 'outOfRange',
        distance: result.distanceMeters,
        radius: result.radiusMeters,
        institutionName: result.institutionName,
        position: view.position,
      })
      return null
    })

  const toManual = (mode, position, distance) =>
    show({ name: mode === 'start' ? 'manualStart' : 'manualEnd', mode, position, distance })

  let content
  if (view.name === 'fail' && day) {
    content = (
      <LocationFail
        mode={view.mode}
        institutionName={view.institutionName ?? (view.mode === 'start' ? firstInstitution(day) : lastInstitution(day))}
        reason={view.reason}
        distance={view.distance}
        radius={view.radius}
        retrying={locating}
        onRetry={() => checkLocation(view.mode)}
        onManual={() => toManual(view.mode, view.position, view.distance)}
        onGuide={() => show({ name: 'guide', mode: view.mode })}
        onBack={backToToday}
      />
    )
  } else if (view.name === 'guide' && day) {
    content = (
      <LocationGuide
        mode={view.mode}
        unsupported={view.unsupported}
        retrying={locating}
        onRetry={() => checkLocation(view.mode)}
        onManual={() => toManual(view.mode)}
        onBack={backToToday}
      />
    )
  } else if (view.name === 'manualStart' && day) {
    content = (
      <ManualStartForm
        institutionName={firstInstitution(day)}
        measured={view.position ? { distance: view.distance, accuracy: view.position.accuracy } : null}
        submitting={submitting}
        error={formError}
        onSubmit={(reason) => save(() => startDayManually(day.date, reason, view.position))}
        onBack={backToToday}
      />
    )
  } else if (['end', 'manualEnd', 'edit'].includes(view.name) && day) {
    const mode = view.name === 'manualEnd' ? 'manual' : view.name
    content = (
      <DayEndForm
        key={view.name}
        day={day}
        mode={mode}
        check={view.check}
        measured={view.position ? { distance: view.distance, accuracy: view.position.accuracy } : null}
        submitting={submitting}
        error={formError}
        onSubmit={({ reason, photos, notes }) => {
          if (mode === 'end') return submitEnd({ photos, notes })
          return save(async () => {
            const imageKeys = await toImageKeys(photos)
            if (mode === 'edit') return correctDayEnd(day.date, { imageKeys, notes })
            return endDayManually(day.date, { reason, ...view.position, imageKeys, notes })
          })
        }}
        onBack={backToToday}
      />
    )
  } else if (view.name === 'history') {
    content = <AttendanceHistory today={today} onGoToday={backToToday} />
  } else {
    content = (
      <DayToday
        day={day}
        today={today}
        error={error}
        actionError={actionError}
        locating={locating}
        onReload={reload}
        onStart={() => checkLocation('start')}
        onEnd={() => checkLocation('end')}
        onCorrect={() => show({ name: 'edit' })}
        onHistory={() => show({ name: 'history' })}
      />
    )
  }

  return <AttendanceLayout>{content}</AttendanceLayout>
}

export default AttendancePage
