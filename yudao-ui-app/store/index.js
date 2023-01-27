import Vue from 'vue'
import Vuex from 'vuex'
import user from './mudules/user'
import cart from './mudules/cart'
import getters from './getters'

Vue.use(Vuex) // vue的插件机制

// Vuex.Store 构造器选项
const store = new Vuex.Store({
  modules: {
    user,
    cart
  },
  getters
})
export default store
