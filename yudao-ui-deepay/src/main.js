import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import './index.css'

// ── 最早时机捕获 URL 中的 ?ref= 参数（防止路由跳转后丢失）──────────
// 只写入一次：首次访问带 ref 的链接时记录推荐人
;(function captureRefFromUrl() {
  try {
    const ref = new URLSearchParams(location.search).get('ref')
    if (ref && !localStorage.getItem('deepay_ref')) {
      const myUid = localStorage.getItem('deepay_uid')
      if (!myUid || ref !== myUid) {          // 防自刷
        localStorage.setItem('deepay_ref', ref)
      }
    }
  } catch (_) {}
})()

createApp(App).use(router).mount('#app')
