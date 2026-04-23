import { createApp } from 'vue'
import { createPinia } from 'pinia'
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

const app   = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// ── 启动时加载功能配置（后台改完立刻生效）──────────────────────────
import { useFeatureStore } from '@/store/modules/feature'
const featureStore = useFeatureStore()
featureStore.load()   // 非阻塞：load 失败会用兜底数据，不影响首屏渲染

app.mount('#app')
