import Vue from 'vue'
import Vuex from 'vuex'
import { getUserInfo, logout } from '@/common/api'

Vue.use(Vuex) // vue的插件机制

// Vuex.Store 构造器选项
const store = new Vuex.Store({
  state: {
    openExamine: false, // 是否开启审核状态。用于小程序、App 等审核时，关闭部分功能。TODO 芋艿：暂时没找到刷新的地方
    token: uni.getStorageSync('token'), // 用户身份 Token
    userInfo: uni.getStorageSync('userInfo'), // 用户基本信息
    timerIdent: false // 全局 1s 定时器，只在全局开启一个，所有需要定时执行的任务监听该值即可，无需额外开启 TODO 芋艿：需要看看
  },
  getters: {
    hasLogin(state) {
      return !!state.token
    }
  },
  mutations: {
    // 更新 state 的通用方法
    setStateAttr(state, param) {
      if (param instanceof Array) {
        for (let item of param) {
          state[item.key] = item.val
        }
      } else {
        state[param.key] = param.val
      }
    },
    // 更新token
    setToken(state, data) {
      // 设置 Token
      const { token } = data
      state.token = token
      uni.setStorageSync('token', token)

      // 加载用户信息
      this.dispatch('obtainUserInfo')
    },
    // 更新用户信息
    setUserInfo(state, data) {
      state.userInfo = data
      uni.setStorageSync('userInfo', data)
    },
    // 清空 Token 和 用户信息
    clearLoginInfo(state) {
      uni.removeStorageSync('token')
      state.token = ''
      uni.removeStorageSync('userInfo')
      state.userInfo = {}
    }
  },
  actions: {
    // 获得用户基本信息
    async obtainUserInfo({ state, commit }) {
      const res = await getUserInfo()
      commit('setUserInfo', res.data)
    },
    // 退出登录
    async logout({ state, commit }) {
      commit('clearLoginInfo')
      await logout()
    }
  }
})

export default store
