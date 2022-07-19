import { store } from '../index'
import { defineStore } from 'pinia'
import { getInfoApi } from '@/api/login'
import { getAccessToken } from '@/utils/auth'
import { useCache } from '@/hooks/web/useCache'

interface UserInfoVO {
  permissions: []
  roles: []
  user: {
    avatar: string
    id: number
    nickname: string
  }
}
const { wsCache } = useCache()

export const useUserStore = defineStore({
  id: 'admin-user',
  state: (): UserInfoVO => ({
    permissions: [],
    roles: [],
    user: {
      id: 0,
      avatar: '',
      nickname: ''
    }
  }),
  actions: {
    async getUserInfoAction() {
      if (!getAccessToken()) {
        this.resetState()
        return null
      }
      const res = await getInfoApi()
      this.permissions = res.permissions
      this.roles = res.roles
      this.user = res.user
      wsCache.set('user', res)
    },
    resetState() {
      this.permissions = []
      this.roles = []
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
