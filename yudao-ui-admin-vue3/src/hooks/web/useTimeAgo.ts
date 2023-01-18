import { useTimeAgo as useTimeAgoCore, UseTimeAgoMessages } from '@vueuse/core'
import { useLocaleStoreWithOut } from '@/store/modules/locale'

const TIME_AGO_MESSAGE_MAP: {
  'zh-CN': UseTimeAgoMessages
  en: UseTimeAgoMessages
} = {
  'zh-CN': {
    justNow: '刚刚',
    past: (n) => (n.match(/\d/) ? `${n}前` : n),
    future: (n) => (n.match(/\d/) ? `${n}后` : n),
    month: (n, past) => (n === 1 ? (past ? '上个月' : '下个月') : `${n} 个月`),
    year: (n, past) => (n === 1 ? (past ? '去年' : '明年') : `${n} 年`),
    day: (n, past) => (n === 1 ? (past ? '昨天' : '明天') : `${n} 天`),
    week: (n, past) => (n === 1 ? (past ? '上周' : '下周') : `${n} 周`),
    hour: (n) => `${n} 小时`,
    minute: (n) => `${n} 分钟`,
    second: (n) => `${n} 秒`
  },
  en: {
    justNow: 'just now',
    past: (n) => (n.match(/\d/) ? `${n} ago` : n),
    future: (n) => (n.match(/\d/) ? `in ${n}` : n),
    month: (n, past) =>
      n === 1 ? (past ? 'last month' : 'next month') : `${n} month${n > 1 ? 's' : ''}`,
    year: (n, past) =>
      n === 1 ? (past ? 'last year' : 'next year') : `${n} year${n > 1 ? 's' : ''}`,
    day: (n, past) => (n === 1 ? (past ? 'yesterday' : 'tomorrow') : `${n} day${n > 1 ? 's' : ''}`),
    week: (n, past) =>
      n === 1 ? (past ? 'last week' : 'next week') : `${n} week${n > 1 ? 's' : ''}`,
    hour: (n) => `${n} hour${n > 1 ? 's' : ''}`,
    minute: (n) => `${n} minute${n > 1 ? 's' : ''}`,
    second: (n) => `${n} second${n > 1 ? 's' : ''}`
  }
}

export const useTimeAgo = (time: Date | number | string) => {
  const localeStore = useLocaleStoreWithOut()

  const currentLocale = computed(() => localeStore.getCurrentLocale)

  const timeAgo = useTimeAgoCore(time, {
    messages: TIME_AGO_MESSAGE_MAP[unref(currentLocale).lang]
  })

  return timeAgo
}
