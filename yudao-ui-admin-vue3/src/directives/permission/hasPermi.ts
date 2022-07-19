import type { App } from 'vue'
import { useCache } from '@/hooks/web/useCache'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n() // 国际化

export function hasPermi(app: App<Element>) {
  app.directive('hasPermi', (el, binding) => {
    const { wsCache } = useCache()
    const { value } = binding
    const all_permission = '*:*:*'
    const permissions = wsCache.get('user').permissions

    if (value && value instanceof Array && value.length > 0) {
      const permissionFlag = value

      const hasPermissions = permissions.some((permission: string) => {
        return all_permission === permission || permissionFlag.includes(permission)
      })

      if (!hasPermissions) {
        el.parentNode && el.parentNode.removeChild(el)
      }
    } else {
      throw new Error(t('permission.hasPermission'))
    }
  })
}
