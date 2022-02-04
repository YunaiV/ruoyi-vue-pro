import App from './App'

import uView from '@/uni_modules/uview-ui'
Vue.use(uView)

// 全局 Mixin
import mixin from './common/mixin/mixin'
Vue.mixin(mixin) 

// 全局 Util
import {
	msg,
	isLogin,
	debounce,
	throttle,
	prePage,
	date
} from '@/common/js/util'
Vue.prototype.$util = {
	msg,
	isLogin,
	debounce,
	throttle,
	prePage,
	date
}

// 全局 Store
import store from './store'
Vue.prototype.$store = store

// #ifndef VUE3
import Vue from 'vue'
Vue.config.productionTip = false
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