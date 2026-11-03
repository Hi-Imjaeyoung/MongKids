import { useEffect, useState } from 'react'
import { privacySections, privacyTocLabel } from '../../data/privacyContent'
import PrivacySection from './PrivacySection'
import PrivacyToc from './PrivacyToc'
import './PrivacyPolicy.css'

// 개인정보처리방침 목차 + 조항 본문 (SCR-023). 화면 위쪽에 걸린 조항을 목차에 표시한다
function PrivacyPolicy() {
  const [activeId, setActiveId] = useState(privacySections[0].id)

  useEffect(() => {
    const targets = privacySections.map((section) => document.getElementById(section.id)).filter(Boolean)
    // 화면 위 20% 줄을 지나는 조항을 지금 보는 조항으로 본다
    const observer = new IntersectionObserver(
      (entries) => {
        const visible = entries.find((entry) => entry.isIntersecting)
        if (visible) setActiveId(visible.target.id)
      },
      { rootMargin: '-20% 0px -79% 0px' },
    )
    targets.forEach((target) => observer.observe(target))
    return () => observer.disconnect()
  }, [])

  return (
    <div className="privacy-policy container">
      <PrivacyToc label={privacyTocLabel} sections={privacySections} activeId={activeId} />
      <article className="privacy-policy__body">
        {privacySections.map((section) => (
          <PrivacySection key={section.id} section={section} />
        ))}
      </article>
    </div>
  )
}

export default PrivacyPolicy
