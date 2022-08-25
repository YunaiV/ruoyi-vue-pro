import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

type Callback = (error?: string | Error | undefined) => void

interface LengthRange {
  min: number
  max: number
  message: string
}

export const useValidator = () => {
  const required = (message?: string) => {
    return {
      required: true,
      message: message || t('common.required')
    }
  }

  const lengthRange = (val: any, callback: Callback, options: LengthRange) => {
    const { min, max, message } = options
    if (val.length < min || val.length > max) {
      callback(new Error(message))
    } else {
      callback()
    }
  }

  const notSpace = (val: any, callback: Callback, message: string) => {
    // 用户名不能有空格
    if (val.indexOf(' ') !== -1) {
      callback(new Error(message))
    } else {
      callback()
    }
  }

  const notSpecialCharacters = (val: any, callback: Callback, message: string) => {
    // 密码不能是特殊字符
    if (/[`~!@#$%^&*()_+<>?:"{},.\/;'[\]]/gi.test(val)) {
      callback(new Error(message))
    } else {
      callback()
    }
  }

  // 两个字符串是否想等
  const isEqual = (val1: string, val2: string, callback: Callback, message: string) => {
    if (val1 === val2) {
      callback()
    } else {
      callback(new Error(message))
    }
  }

  return {
    required,
    lengthRange,
    notSpace,
    notSpecialCharacters,
    isEqual
  }
}
