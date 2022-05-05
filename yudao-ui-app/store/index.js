import Vue from 'vue'
import Vuex from 'vuex'
import { logout } from '@/api/auth'
import { getUserInfo } from '@/api/user'
import { passwordLogin, smsLogin, socialLogin } from '@/api/auth'

const TokenKey = 'App-Token'

Vue.use(Vuex) // vue的插件机制

// Vuex.Store 构造器选项
const store = new Vuex.Store({
  state: {
    openExamine: false, // 是否开启审核状态。用于小程序、App 等审核时，关闭部分功能。TODO 芋艿：暂时没找到刷新的地方
    token: uni.getStorageSync(TokenKey), // 用户身份 Token
    userInfo: {}, // 用户基本信息
    timerIdent: false // 全局 1s 定时器，只在全局开启一个，所有需要定时执行的任务监听该值即可，无需额外开启 TODO 芋艿：需要看看
  },
  getters: {
    token: state => state.token,
    userInfo: state => state.userInfo,
    hasLogin: state => !!state.token
  },
  mutations: {
    // 更新 state 的通用方法
    SET_STATE_ATTR(state, param) {
      if (param instanceof Array) {
        for (let item of param) {
          state[item.key] = item.val
        }
      } else {
        state[param.key] = param.val
      }
    },
    // 更新token
    SET_TOKEN(state, data) {
      // 设置 Token
      const { token } = data
      state.token = token
      uni.setStorageSync(TokenKey, token)

      // 加载用户信息
      this.dispatch('ObtainUserInfo')
    },
    // 更新用户信息
    SET_USER_INFO(state, data) {
      state.userInfo = data
    },
    // 清空 Token 和 用户信息
    CLEAR_LOGIN_INFO(state) {
      uni.removeStorageSync(TokenKey)
      state.token = ''
      state.userInfo = {}
    }
  },
  actions: {
    //账号登录
    Login({ state, commit }, { type, data }) {
      if (type === 0) {
        return passwordLogin(data).then(res => {
          commit('SET_TOKEN', res.data)
        })
      } else if (type === 1) {
        return smsLogin(data).then(res => {
          commit('SET_TOKEN', res.data)
        })
      } else {
        return socialLogin(data).then(res => {
          commit('SET_TOKEN', res.data)
        })
      }
    },
    // 退出登录
    async Logout({ state, commit }) {
      commit('CLEAR_LOGIN_INFO')
      await logout()
    },
    // 获得用户基本信息
    async ObtainUserInfo({ state, commit }) {
      const res = await getUserInfo()
      commit('SET_USER_INFO', res.data)
    }
  }
})

export default store
