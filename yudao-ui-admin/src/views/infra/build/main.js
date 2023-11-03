import Vue from 'vue'
import App from './App.vue'
import router from '@/router'
import '@/styles/index.scss'
import '@/assets/icons'
import axios from 'axios'
import Tinymce from '@/components/tinymce/index.vue'

Vue.component('tinymce', Tinymce)

Vue.config.productionTip = false
Vue.prototype.$axios = axios

// add by 芋道源码：引用自 https://github.com/JakHuang/form-generator/tree/dev/src/views/index

new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
