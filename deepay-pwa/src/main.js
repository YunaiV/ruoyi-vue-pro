import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router/index.js'
import './styles/global.css'
import { initPWA } from './utils/pwa.js'

const app   = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
initPWA()
app.mount('#app')
