import { useEffect, useState } from 'react'
import {
  correctClassEnd,
  endClass,
  saveStartNote,
  startClassByLocation,
  startClassManually,
  uploadAttendanceImages,
} from '../api/attendance'
import AttendanceLayout from '../components/attendance/AttendanceLayout'
import AttendanceNotice from '../components/attendance/AttendanceNotice'
import ClassEndForm from '../components/attendance/ClassEndForm'
import ClassList from '../components/attendance/ClassList'
import LocationFail from '../components/attendance/LocationFail'
import LocationGuide from '../components/attendance/LocationGuide'
import ManualStartForm from '../components/attendance/ManualStartForm'
import StartConfirmed from '../components/attendance/StartConfirmed'
import { canUseAttendance } from '../components/attendance/attendanceFormat'
import Button from '../components/common/Button'
import { attendancePage } from '../data/attendanceContent'
import useCurrentPosition from './useCurrentPosition'
import useTodayClasses from './useTodayClasses'

const LIST = { name: 'list' }

// 사진 목록에서 새로 고른 사진만 올리고, 화면 순서대로 객체 키를 돌려준다
async function toImageKeys(photos) {
  const newPhotos = photos.filter((photo) => photo.file)
  const uploadedKeys = await uploadAttendanceImages(newPhotos.map((photo) => photo.file))
  const keyByPhotoId = new Map(newPhotos.map((photo, index) => [photo.id, uploadedKeys[index]]))
  return photos.map((photo) => photo.imageKey ?? keyByPhotoId.get(photo.id))
}

// 수업 시작·종료 체크 (EMP-SCR-001). 오늘 수업 목록(①)에서 하위 화면(②~⑦)으로 바꿔 가며 보여 준다.
// member: undefined면 로그인 상태 확인 중, null이면 비로그인. 직원(STAFF·ADMIN)이 아니면 API를 부르지 않는다
function AttendancePage({ member, onLoginClick }) {
  const allowed = canUseAttendance(member)
  const { classes, error, reload, replaceClass } = useTodayClasses(allowed)
  const { locate } = useCurrentPosition()
  const [today] = useState(() => new Date())
  // view.name: list | fail | guide | manual | started | end | edit
  const [view, setView] = useState(LIST)
  const [locatingId, setLocatingId] = useState(null)
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
  const backToList = () => show(LIST)

  // 위치를 받아 서버에 수업 시작을 확인한다 (EMP-FR-001). 위치를 못 받으면 서버에 묻지 않고 ②·③ 화면으로 안내한다
  const startClass = async (classId) => {
    setActionError(null)
    setLocatingId(classId)
    let position
    try {
      position = await locate()
    } catch ({ kind }) {
      setLocatingId(null)
      if (kind === 'denied' || kind === 'unsupported') show({ name: 'guide', classId, unsupported: kind === 'unsupported' })
      else show({ name: 'fail', classId, reason: kind })
      return
    }
    try {
      const result = await startClassByLocation(classId, position)
      replaceClass(result.classSession)
      if (result.confirmed) {
        show({ name: 'started', classId })
      } else {
        show({
          name: 'fail',
          classId,
          reason: 'outOfRange',
          distance: result.distanceMeters,
          radius: result.radiusMeters,
          position,
        })
      }
    } catch (startError) {
      setActionError(startError.message)
      backToList()
    } finally {
      setLocatingId(null)
    }
  }

  // 저장 요청을 보내고, 성공하면 바뀐 수업을 목록에 반영한 뒤 목록으로 돌아간다
  const save = async (task) => {
    setSubmitting(true)
    setFormError(null)
    try {
      replaceClass(await task())
      backToList()
    } catch (saveError) {
      setFormError(saveError.message)
    } finally {
      setSubmitting(false)
    }
  }

  const classSession = view.classId != null ? classes?.find((item) => item.id === view.classId) : null
  const formProps = { classSession, submitting, error: formError, onBack: backToList }
  let content
  if (view.name === 'fail' && classSession) {
    content = (
      <LocationFail
        reason={view.reason}
        distance={view.distance}
        radius={view.radius}
        classSession={classSession}
        retrying={locatingId != null}
        onRetry={() => startClass(classSession.id)}
        onManual={() => show({ name: 'manual', classId: classSession.id, position: view.position, distance: view.distance })}
        onGuide={() => show({ name: 'guide', classId: classSession.id })}
        onBack={backToList}
      />
    )
  } else if (view.name === 'guide' && classSession) {
    content = (
      <LocationGuide
        unsupported={view.unsupported}
        retrying={locatingId != null}
        onRetry={() => startClass(classSession.id)}
        onManual={() => show({ name: 'manual', classId: classSession.id })}
        onBack={backToList}
      />
    )
  } else if (view.name === 'manual' && classSession) {
    content = (
      <ManualStartForm
        {...formProps}
        measured={view.position ? { distance: view.distance, accuracy: view.position.accuracy } : null}
        onSubmit={(reason) => save(() => startClassManually(classSession.id, reason, view.position))}
      />
    )
  } else if (view.name === 'started' && classSession?.start) {
    content = (
      <StartConfirmed
        classSession={classSession}
        saving={submitting}
        error={formError}
        onSave={(note) => save(() => saveStartNote(classSession.id, note))}
        onBack={backToList}
      />
    )
  } else if ((view.name === 'end' || view.name === 'edit') && classSession) {
    const editing = view.name === 'edit'
    content = (
      <ClassEndForm
        {...formProps}
        editing={editing}
        onSubmit={({ note, photos }) =>
          save(async () => {
            const body = { note, imageKeys: await toImageKeys(photos) }
            return editing ? correctClassEnd(classSession.id, body) : endClass(classSession.id, body)
          })
        }
      />
    )
  } else {
    content = (
      <ClassList
        classes={classes}
        today={today}
        error={error}
        actionError={actionError}
        locatingId={locatingId}
        onReload={reload}
        onStart={(item) => startClass(item.id)}
        onEnd={(item) => show({ name: 'end', classId: item.id })}
        onCorrect={(item) => show({ name: 'edit', classId: item.id })}
      />
    )
  }

  return <AttendanceLayout>{content}</AttendanceLayout>
}

export default AttendancePage
