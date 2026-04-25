import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import Components from '@uni-helper/vite-plugin-uni-components'
import { WotResolver } from 'wot-design-uni/auto-imports'
import AutoImport from 'unplugin-auto-import/vite'

export default defineConfig({
  plugins: [
    // Components auto-import (Wot Design Uni)
    Components({
      resolvers: [WotResolver()],
      dts: false,
    }),
    AutoImport({
      imports: ['vue', 'uni-app'],
      dts: false,
    }),
    uni(),
  ],
})
