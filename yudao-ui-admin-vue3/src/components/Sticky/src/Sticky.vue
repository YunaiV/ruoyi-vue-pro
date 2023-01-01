<script setup lang="ts">
import { propTypes } from '@/utils/propTypes'
import { ref, onMounted, onActivated, shallowRef } from 'vue'
import { useEventListener, useWindowSize, isClient } from '@vueuse/core'
import type { CSSProperties } from 'vue'
const props = defineProps({
  // 距离顶部或者底部的距离(单位px)
  offset: propTypes.number.def(0),
  // 设置元素的堆叠顺序
  zIndex: propTypes.number.def(999),
  // 设置指定的class
  className: propTypes.string.def(''),
  // 定位方式，默认为(top)，表示距离顶部位置，可以设置为top或者bottom
  position: {
    type: String,
    validator: function (value: string) {
      return ['top', 'bottom'].indexOf(value) !== -1
    },
    default: 'top'
  }
})
const width = ref('auto' as string)
const height = ref('auto' as string)
const isSticky = ref(false)
const refSticky = shallowRef<HTMLElement>()
const scrollContainer = shallowRef<HTMLElement | Window>()
const { height: windowHeight } = useWindowSize()
onMounted(() => {
  height.value = refSticky.value?.getBoundingClientRect().height + 'px'

  scrollContainer.value = getScrollContainer(refSticky.value!, true)
  useEventListener(scrollContainer, 'scroll', handleScroll)
  useEventListener('resize', handleReize)
  handleScroll()
})
onActivated(() => {
  handleScroll()
})

const camelize = (str: string): string => {
  return str.replace(/-(\w)/g, (_, c) => (c ? c.toUpperCase() : ''))
}

const getStyle = (element: HTMLElement, styleName: keyof CSSProperties): string => {
  if (!isClient || !element || !styleName) return ''

  let key = camelize(styleName)
  if (key === 'float') key = 'cssFloat'
  try {
    const style = element.style[styleName]
    if (style) return style
    const computed = document.defaultView?.getComputedStyle(element, '')
    return computed ? computed[styleName] : ''
  } catch {
    return element.style[styleName]
  }
}
const isScroll = (el: HTMLElement, isVertical?: boolean): boolean => {
  if (!isClient) return false
  const key = (
    {
      undefined: 'overflow',
      true: 'overflow-y',
      false: 'overflow-x'
    } as const
  )[String(isVertical)]!
  const overflow = getStyle(el, key)
  return ['scroll', 'auto', 'overlay'].some((s) => overflow.includes(s))
}

const getScrollContainer = (
  el: HTMLElement,
  isVertical: boolean
): Window | HTMLElement | undefined => {
  if (!isClient) return
  let parent = el
  while (parent) {
    if ([window, document, document.documentElement].includes(parent)) return window
    if (isScroll(parent, isVertical)) return parent
    parent = parent.parentNode as HTMLElement
  }
  return parent
}

const handleScroll = () => {
  width.value = refSticky.value!.getBoundingClientRect().width! + 'px'
  if (props.position === 'top') {
    const offsetTop = refSticky.value?.getBoundingClientRect().top
    if (offsetTop !== undefined && offsetTop < props.offset) {
      sticky()
      return
    }
    reset()
  } else {
    const offsetBottom = refSticky.value?.getBoundingClientRect().bottom

    if (offsetBottom !== undefined && offsetBottom > windowHeight.value - props.offset) {
      sticky()
      return
    }
    reset()
  }
}
const handleReize = () => {
  if (isSticky.value && refSticky.value) {
    width.value = refSticky.value.getBoundingClientRect().width + 'px'
  }
}
const sticky = () => {
  if (isSticky.value) {
    return
  }
  isSticky.value = true
}
const reset = () => {
  if (!isSticky.value) {
    return
  }
  width.value = 'auto'
  isSticky.value = false
}
</script>
<template>
  <div :style="{ height: height, zIndex: zIndex }" ref="refSticky">
    <div
      :class="className"
      :style="{
        top: position === 'top' ? offset + 'px' : '',
        bottom: position !== 'top' ? offset + 'px' : '',
        zIndex: zIndex,
        position: isSticky ? 'fixed' : 'static',
        width: width,
        height: height
      }"
    >
      <slot>
        <div>sticky</div>
      </slot>
    </div>
  </div>
</template>
