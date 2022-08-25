import { nextTick, unref } from 'vue'
import type { NProgressOptions } from 'nprogress'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { useCssVar } from '@vueuse/core'

const primaryColor = useCssVar('--el-color-primary', document.documentElement)

export const useNProgress = () => {
  NProgress.configure({ showSpinner: false } as NProgressOptions)

  const initColor = async () => {
    await nextTick()
    const bar = document.getElementById('nprogress')?.getElementsByClassName('bar')[0] as ElRef
    if (bar) {
      bar.style.background = unref(primaryColor.value)
    }
  }

  initColor()

  const start = () => {
    NProgress.start()
  }

  const done = () => {
    NProgress.done()
  }

  return {
    start,
    done
  }
}
