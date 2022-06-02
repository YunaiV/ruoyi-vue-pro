import Vue from 'vue'
import App from './App'

// 引入全局uView
import uView from '@/uni_modules/uview-ui'

// vuex
import store from './store'

Vue.config.productionTip = false
Vue.prototype.$store = store



App.mpType = 'app'
Vue.use(uView)

const app = new Vue({
	store,
	...App
})

// 引入请求封装
require('./utils/request/index')(app)

app.$mount()
