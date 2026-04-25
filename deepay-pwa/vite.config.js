import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    vue(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['icon.svg', 'robots.txt'],
      manifest: {
        name: 'Deepay · AI时尚设计',
        short_name: 'Deepay',
        description: 'AI时尚设计助手，图库/模特库/设计库/AI销售一站式平台',
        theme_color: '#0a0a0a',
        background_color: '#0a0a0a',
        display: 'standalone',
        orientation: 'portrait-primary',
        start_url: '/',
        scope: '/',
        lang: 'zh-CN',
        icons: [
          { src: 'icon.svg', sizes: 'any', type: 'image/svg+xml', purpose: 'any maskable' }
        ],
        categories: ['productivity', 'lifestyle'],
        shortcuts: [
          { name: '图库', url: '/image-library', icons: [{ src: 'icon.svg', sizes: 'any' }] },
          { name: 'AI销售', url: '/ai-sales', icons: [{ src: 'icon.svg', sizes: 'any' }] }
        ]
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        cleanupOutdatedCaches: true,
        skipWaiting: true,
        clientsClaim: true,
        runtimeCaching: [
          {
            urlPattern: /^\/api\//,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'deepay-api',
              networkTimeoutSeconds: 10,
              expiration: { maxEntries: 100, maxAgeSeconds: 300 },
              cacheableResponse: { statuses: [0, 200] }
            }
          },
          {
            urlPattern: /^https:\/\/fonts\.(googleapis|gstatic)\.com\/.*/,
            handler: 'CacheFirst',
            options: {
              cacheName: 'deepay-fonts',
              expiration: { maxEntries: 10, maxAgeSeconds: 31536000 },
              cacheableResponse: { statuses: [0, 200] }
            }
          },
          {
            urlPattern: /\.(?:png|jpg|jpeg|webp|svg|gif)$/,
            handler: 'StaleWhileRevalidate',
            options: {
              cacheName: 'deepay-images',
              expiration: { maxEntries: 60, maxAgeSeconds: 2592000 }
            }
          }
        ]
      },
      devOptions: { enabled: true }
    })
  ],
  resolve: { alias: { '@': resolve(__dirname, 'src') } },
  server: { port: 3100, proxy: { '/api': { target: 'http://localhost:48080', changeOrigin: true } } }
})
