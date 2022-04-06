import App from './App'

// #ifndef VUE3
import Vue from 'vue'
Vue.config.productionTip = false

//引入并使用uView的JS库
import uView from '@/uni_modules/uview-ui'
Vue.use(uView)

App.mpType = 'app'
const app = new Vue({
    ...App
})
app.$mount()
// #endif

// #ifdef VUE3
import { createSSRApp } from 'vue'
export function createApp() {
  const app = createSSRApp(App)
  return {
    app
  }
}
// #endif