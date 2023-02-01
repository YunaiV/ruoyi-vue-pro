import { store } from '../index'
import { defineStore } from 'pinia'
import { getAccessToken, removeToken } from '@/utils/auth'
import { CACHE_KEY, useCache } from '@/hooks/web/useCache'
import { getInfoApi, loginOutApi } from '@/api/login'

const { wsCache } = useCache()

interface UserVO {
  id: number
  avatar: string
  nickname: string
}
interface UserInfoVO {
  permissions: string[]
  roles: string[]
  isSetUser: boolean
  user: UserVO
}

export const useUserStore = defineStore('admin-user', {
  state: (): UserInfoVO => ({
    permissions: [],
    roles: [],
    isSetUser: false,
    user: {
      id: 0,
      avatar: '',
      nickname: ''
    }
  }),
  getters: {
    getPermissions(): string[] {
      return this.permissions
    },
    getRoles(): string[] {
      return this.roles
    },
    getIsSetUser(): boolean {
      return this.isSetUser
    },
    getUser(): UserVO {
      return this.user
    }
  },
  actions: {
    async setUserInfoAction() {
      if (!getAccessToken()) {
        this.resetState()
        return null
      }
      let userInfo = wsCache.get(CACHE_KEY.USER)
      if (!userInfo) {
        userInfo = await getInfoApi()
      }
      this.permissions = userInfo.permissions
      this.roles = userInfo.roles
      this.user = userInfo.user
      this.isSetUser = true
      wsCache.set(CACHE_KEY.USER, userInfo)
    },
    async loginOut() {
      await loginOutApi()
      removeToken()
      wsCache.clear()
      this.resetState()
    },
    resetState() {
      this.permissions = []
      this.roles = []
      this.isSetUser = false
      this.user = {
        id: 0,
        avatar: '',
        nickname: ''
      }
    }
  }
})

export const useUserStoreWithOut = () => {
  return useUserStore(store)
}
