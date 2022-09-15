import {login, logout, getInfo, socialLogin, socialBindLogin, smsLogin} from '@/api/login'
import {setToken, removeToken} from '@/utils/auth'

const user = {
  state: {
    id: 0, // 用户编号
    name: '',
    avatar: '',
    roles: [],
    permissions: []
  },

  mutations: {
    SET_ID: (state, id) => {
      state.id = id
    },
    SET_NAME: (state, name) => {
      state.name = name
    },
    SET_NICKNAME: (state, nickname) => {
      state.nickname = nickname
    },
    SET_AVATAR: (state, avatar) => {
      state.avatar = avatar
    },
    SET_ROLES: (state, roles) => {
      state.roles = roles
    },
    SET_PERMISSIONS: (state, permissions) => {
      state.permissions = permissions
    }
  },

  actions: {
    // 登录
    Login({ commit }, userInfo) {
      const username = userInfo.username.trim()
      const password = userInfo.password
      const captchaVerification = userInfo.captchaVerification
      const socialCode = userInfo.socialCode
      const socialState = userInfo.socialState
      const socialType = userInfo.socialType
      return new Promise((resolve, reject) => {
        login(username, password, captchaVerification, socialType, socialCode, socialState).then(res => {
          res = res.data;
          // 设置 token
          setToken(res)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 社交登录
    SocialLogin({ commit }, userInfo) {
      const code = userInfo.code
      const state = userInfo.state
      const type = userInfo.type
      return new Promise((resolve, reject) => {
        socialLogin(type, code, state).then(res => {
          res = res.data;
          // 设置 token
          setToken(res)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 短信登录
    SmsLogin({ commit }, userInfo) {
      const mobile = userInfo.mobile.trim()
      const mobileCode = userInfo.mobileCode
      return new Promise((resolve, reject) => {
        smsLogin(mobile,mobileCode).then(res => {
          res = res.data;
          // 设置 token
          setToken(res)
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    },
    // 获取用户信息
    GetInfo({ commit, state }) {
      return new Promise((resolve, reject) => {
        getInfo().then(res => {
          // 没有 data 数据，赋予个默认值
          if (!res) {
            res = {
              data: {
                roles: [],
                user: {
                  id: '',
                  avatar: '',
                  userName: '',
                  nickname: ''
                }
              }
            }
          }

          res = res.data; // 读取 data 数据
          const user = res.user
          const avatar = ( user.avatar === "" || user.avatar == null ) ? require("@/assets/images/profile.jpg") : user.avatar;
          if (res.roles && res.roles.length > 0) { // 验证返回的roles是否是一个非空数组
            commit('SET_ROLES', res.roles)
            commit('SET_PERMISSIONS', res.permissions)
          } else {
            commit('SET_ROLES', ['ROLE_DEFAULT'])
          }
          commit('SET_ID', user.id)
          commit('SET_NAME', user.userName)
          commit('SET_NICKNAME', user.nickname)
          commit('SET_AVATAR', avatar)
          resolve(res)
        }).catch(error => {
          reject(error)
        })
      })
    },

    // 退出系统
    LogOut({ commit, state }) {
      return new Promise((resolve, reject) => {
        logout(state.token).then(() => {
          commit('SET_ROLES', [])
          commit('SET_PERMISSIONS', [])
          removeToken()
          resolve()
        }).catch(error => {
          reject(error)
        })
      })
    }
  }
}

export default user
