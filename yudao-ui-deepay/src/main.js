import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './index.css'

// Capture referral code from URL before router runs
;(function captureRefFromUrl() {
  try {
    const ref = new URLSearchParams(location.search).get('ref')
    if (ref && !localStorage.getItem('deepay_ref')) {
      const myUid = localStorage.getItem('deepay_uid')
      if (!myUid || ref !== myUid) {
        localStorage.setItem('deepay_ref', ref)
      }
    }
  } catch (_) {}
})()

const app   = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)

// Initialize feature store
import { useFeatureStore } from '@/store/modules/feature'
const featureStore = useFeatureStore()
featureStore.load()

app.mount('#app')
