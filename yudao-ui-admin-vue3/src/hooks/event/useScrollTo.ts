import { ref, unref } from 'vue'

export interface ScrollToParams {
  el: HTMLElement
  to: number
  position: string
  duration?: number
  callback?: () => void
}

const easeInOutQuad = (t: number, b: number, c: number, d: number) => {
  t /= d / 2
  if (t < 1) {
    return (c / 2) * t * t + b
  }
  t--
  return (-c / 2) * (t * (t - 2) - 1) + b
}
const move = (el: HTMLElement, position: string, amount: number) => {
  el[position] = amount
}

export function useScrollTo({
  el,
  position = 'scrollLeft',
  to,
  duration = 500,
  callback
}: ScrollToParams) {
  const isActiveRef = ref(false)
  const start = el[position]
  const change = to - start
  const increment = 20
  let currentTime = 0

  function animateScroll() {
    if (!unref(isActiveRef)) {
      return
    }
    currentTime += increment
    const val = easeInOutQuad(currentTime, start, change, duration)
    move(el, position, val)
    if (currentTime < duration && unref(isActiveRef)) {
      requestAnimationFrame(animateScroll)
    } else {
      if (callback) {
        callback()
      }
    }
  }

  function run() {
    isActiveRef.value = true
    animateScroll()
  }

  function stop() {
    isActiveRef.value = false
  }

  return { start: run, stop }
}
