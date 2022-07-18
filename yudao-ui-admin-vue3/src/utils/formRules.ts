import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n()

// 必填项
export const required = {
  required: true,
  message: t('common.required')
}
