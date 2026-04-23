declare module 'virtual:pwa-register/vue' {
  import type { RegisterSWOptions } from 'vite-plugin-pwa/types'
  // eslint-disable-next-line ts/ban-ts-comment
  // @ts-ignore ignore when vue is not installed
  import type { Ref } from 'vue'

  export type { RegisterSWOptions }

  export function useRegisterSW(options?: RegisterSWOptions): {
    needRefresh: Ref<boolean>
    offlineReady: Ref<boolean>
    /**
     * Reloads the current window to allow the service worker take the control.
     *
     * @param reloadPage From version 0.13.2+ this param is not used anymore.
     */
    updateServiceWorker: (reloadPage?: boolean) => Promise<void>
  }
}
