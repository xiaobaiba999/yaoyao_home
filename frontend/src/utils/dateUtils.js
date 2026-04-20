export function calculateDays(dateString) {
  const targetDate = new Date(dateString)
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  targetDate.setHours(0, 0, 0, 0)
  
  const diffTime = today - targetDate
  const diffDays = Math.floor(diffTime / (1000 * 60 * 60 * 24))
  
  return diffDays
}

export function calculateCountdown(month, day) {
  const today = new Date()
  const currentYear = today.getFullYear()
  
  let targetDate = new Date(currentYear, month - 1, day)
  
  if (targetDate < today) {
    targetDate = new Date(currentYear + 1, month - 1, day)
  }
  
  const diffTime = targetDate - today
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24))
  
  return diffDays
}

export function formatDate(dateString) {
  const date = new Date(dateString)
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}.${month}.${day}`
}

export function getAnniversaryInfo() {
  return [
    {
      id: 'meet',
      title: '相识纪念日',
      date: '2023-02-28',
      displayDate: '2023.02.28',
      days: calculateDays('2023-02-28'),
      type: 'passed',
      icon: '💕',
      color: '#ff6b9d'
    },
    {
      id: 'love',
      title: '相爱纪念日',
      date: '2023-05-20',
      displayDate: '2023.05.20',
      days: calculateDays('2023-05-20'),
      type: 'passed',
      icon: '❤️',
      color: '#e74c3c'
    },
    {
      id: 'yaoyao-birthday',
      title: '瑶瑶生日',
      month: 6,
      day: 8,
      displayDate: '6月8日',
      countdown: calculateCountdown(6, 8),
      type: 'countdown',
      icon: '🎂',
      color: '#9b59b6'
    },
    {
      id: 'lulu-birthday',
      title: '璐璐生日',
      month: 7,
      day: 24,
      displayDate: '7月24日',
      countdown: calculateCountdown(7, 24),
      type: 'countdown',
      icon: '🎁',
      color: '#3498db'
    }
  ]
}
