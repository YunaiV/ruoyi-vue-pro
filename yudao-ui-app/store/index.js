import Vue from 'vue'
import Vuex from 'vuex'
import { logout } from '@/api/auth'
import { getUserInfo } from '@/api/user'
import { passwordLogin, smsLogin, weixinMiniAppLogin } from '@/api/auth'

const AccessTokenKey = 'ACCESS_TOKEN'
const RefreshTokenKey = 'REFRESH_TOKEN'

Vue.use(Vuex) // vue的插件机制

// Vuex.Store 构造器选项
const store = new Vuex.Store({
  state: {
    accessToken: uni.getStorageSync(AccessTokenKey), // 访问令牌
    refreshToken: uni.getStorageSync(RefreshTokenKey), // 刷新令牌
    userInfo: {}
  },
  getters: {
    accessToken: state => state.accessToken,
    refreshToken: state => state.refreshToken,
    userInfo: state => state.userInfo,
    hasLogin: state => !!state.accessToken
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
    // 更新令牌
    SET_TOKEN(state, data) {
      // 设置令牌
      const { accessToken, refreshToken } = data
      state.accessToken = accessToken
      state.refreshToken = refreshToken
      uni.setStorageSync(AccessTokenKey, accessToken)
      uni.setStorageSync(RefreshTokenKey, refreshToken)

      // 加载用户信息
      this.dispatch('ObtainUserInfo')
    },
    // 更新用户信息
    SET_USER_INFO(state, data) {
      state.userInfo = data
    },
    // 清空令牌 和 用户信息
    CLEAR_LOGIN_INFO(state) {
      uni.removeStorageSync(AccessTokenKey)
      uni.removeStorageSync(RefreshTokenKey)
      state.accessToken = ''
      state.refreshToken = ''
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
        return weixinMiniAppLogin(data).then(res => {
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
