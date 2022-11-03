<script setup lang="ts">
import { reactive, computed, watch, onMounted, unref, toRef, PropType, defineEmits } from 'vue'
import { isNumber } from '@/utils/is'
import { propTypes } from '@/utils/propTypes'
import { useDesign } from '@/hooks/web/useDesign'

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('count-to')

const props = defineProps({
  startVal: propTypes.number.def(0),
  endVal: propTypes.number.def(2021),
  duration: propTypes.number.def(3000),
  autoplay: propTypes.bool.def(true),
  decimals: propTypes.number.validate((value: number) => value >= 0).def(0),
  decimal: propTypes.string.def('.'),
  separator: propTypes.string.def(','),
  prefix: propTypes.string.def(''),
  suffix: propTypes.string.def(''),
  useEasing: propTypes.bool.def(true),
  easingFn: {
    type: Function as PropType<(t: number, b: number, c: number, d: number) => number>,
    default(t: number, b: number, c: number, d: number) {
      return (c * (-Math.pow(2, (-10 * t) / d) + 1) * 1024) / 1023 + b
    }
  }
})

const emit = defineEmits(['mounted', 'callback'])

const formatNumber = (num: number | string) => {
  const { decimals, decimal, separator, suffix, prefix } = props
  num = Number(num).toFixed(decimals)
  num += ''
  const x = num.split('.')
  let x1 = x[0]
  const x2 = x.length > 1 ? decimal + x[1] : ''
  const rgx = /(\d+)(\d{3})/
  if (separator && !isNumber(separator)) {
    while (rgx.test(x1)) {
      x1 = x1.replace(rgx, '$1' + separator + '$2')
    }
  }
  return prefix + x1 + x2 + suffix
}

const state = reactive<{
  localStartVal: number
  printVal: number | null
  displayValue: string
  paused: boolean
  localDuration: number | null
  startTime: number | null
  timestamp: number | null
  rAF: any
  remaining: number | null
}>({
  localStartVal: props.startVal,
  displayValue: formatNumber(props.startVal),
  printVal: null,
  paused: false,
  localDuration: props.duration,
  startTime: null,
  timestamp: null,
  remaining: null,
  rAF: null
})

const displayValue = toRef(state, 'displayValue')

onMounted(() => {
  if (props.autoplay) {
    start()
  }
  emit('mounted')
})

const getCountDown = computed(() => {
  return props.startVal > props.endVal
})

watch([() => props.startVal, () => props.endVal], () => {
  if (props.autoplay) {
    start()
  }
})

const start = () => {
  const { startVal, duration } = props
  state.localStartVal = startVal
  state.startTime = null
  state.localDuration = duration
  state.paused = false
  state.rAF = requestAnimationFrame(count)
}

const pauseResume = () => {
  if (state.paused) {
    resume()
    state.paused = false
  } else {
    pause()
    state.paused = true
  }
}

const pause = () => {
  cancelAnimationFrame(state.rAF)
}

const resume = () => {
  state.startTime = null
  state.localDuration = +(state.remaining as number)
  state.localStartVal = +(state.printVal as number)
  requestAnimationFrame(count)
}

const reset = () => {
  state.startTime = null
  cancelAnimationFrame(state.rAF)
  state.displayValue = formatNumber(props.startVal)
}

const count = (timestamp: number) => {
  const { useEasing, easingFn, endVal } = props
  if (!state.startTime) state.startTime = timestamp
  state.timestamp = timestamp
  const progress = timestamp - state.startTime
  state.remaining = (state.localDuration as number) - progress
  if (useEasing) {
    if (unref(getCountDown)) {
      state.printVal =
        state.localStartVal -
        easingFn(progress, 0, state.localStartVal - endVal, state.localDuration as number)
    } else {
      state.printVal = easingFn(
        progress,
        state.localStartVal,
        endVal - state.localStartVal,
        state.localDuration as number
      )
    }
  } else {
    if (unref(getCountDown)) {
      state.printVal =
        state.localStartVal -
        (state.localStartVal - endVal) * (progress / (state.localDuration as number))
    } else {
      state.printVal =
        state.localStartVal +
        (endVal - state.localStartVal) * (progress / (state.localDuration as number))
    }
  }
  if (unref(getCountDown)) {
    state.printVal = state.printVal < endVal ? endVal : state.printVal
  } else {
    state.printVal = state.printVal > endVal ? endVal : state.printVal
  }
  state.displayValue = formatNumber(state.printVal!)
  if (progress < (state.localDuration as number)) {
    state.rAF = requestAnimationFrame(count)
  } else {
    emit('callback')
  }
}

defineExpose({
  pauseResume,
  reset,
  start,
  pause
})
</script>

<template>
  <span :class="prefixCls">
    {{ displayValue }}
  </span>
</template>
