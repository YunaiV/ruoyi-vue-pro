import { store } from '../index'
import { defineStore } from 'pinia'
import { getAccessToken } from '@/utils/auth'
import { useCache } from '@/hooks/web/useCache'

const { wsCache } = useCache()

interface UserInfoVO {
  permissions: []
  roles: []
  user: {
    avatar: string
    id: number
    nickname: string
  }
}

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
    async getUserInfoAction(userInfo: UserInfoVO) {
      if (!getAccessToken()) {
        this.resetState()
        return null
      }
      this.permissions = userInfo.permissions
      this.roles = userInfo.roles
      this.user = userInfo.user
      wsCache.set('user', userInfo)
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
