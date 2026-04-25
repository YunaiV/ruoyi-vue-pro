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
        name: 'Deepay · AI时尚设计平台',
        short_name: 'Deepay',
        description: 'AI驱动的时尚设计平台，一站式图库、模特试衣、AI开店、模板库',
        theme_color: '#0a0a0a',
        background_color: '#0a0a0a',
        display: 'standalone',
        orientation: 'portrait-primary',
        start_url: '/',
        scope: '/',
        lang: 'zh-CN',
        id: 'com.deepay.pwa',
        icons: [
          { src: 'icon.svg', sizes: 'any', type: 'image/svg+xml', purpose: 'any maskable' }
        ],
        categories: ['productivity', 'lifestyle', 'shopping'],
        shortcuts: [
          { name: '图库',   short_name: '图库',   url: '/image-library',    icons: [{ src: 'icon.svg', sizes: 'any' }] },
          { name: 'AI开店', short_name: 'AI开店', url: '/ai-sales',         icons: [{ src: 'icon.svg', sizes: 'any' }] },
          { name: '模板库', short_name: '模板库', url: '/template-library', icons: [{ src: 'icon.svg', sizes: 'any' }] }
        ],
        related_applications: [],
        prefer_related_applications: false
      },
      workbox: {
        globPatterns: ['**/*.{js,css,html,ico,png,svg,woff2}'],
        cleanupOutdatedCaches: true,
        skipWaiting: true,
        clientsClaim: true,
        navigateFallback: '/index.html',
        navigateFallbackDenylist: [/^\/api\//, /^\/admin-api\//, /^\/actuator\//],
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
  server: {
    port: 3100,
    proxy: {
      '/api':       { target: 'http://localhost:48080', changeOrigin: true },
      '/admin-api': { target: 'http://localhost:48080', changeOrigin: true }
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          vendor: ['vue', 'vue-router', 'pinia'],
          vueuse: ['@vueuse/core']
        }
      }
    }
  }
})
