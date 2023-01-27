import { h } from 'vue'
import type { VNode } from 'vue'
import { Icon } from '@/components/Icon'
import { IconTypes } from '@/types/icon'

export const useIcon = (props: IconTypes): VNode => {
  return h(Icon, props)
}
