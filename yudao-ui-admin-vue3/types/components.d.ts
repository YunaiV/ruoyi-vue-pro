declare module 'vue' {
  export interface GlobalComponents {
    Icon: typeof import('../components/Icon/src/Icon.vue')['default']
  }
}

export {}
