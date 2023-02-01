<script setup lang="ts">
import { ElMessageBox } from 'element-plus'

import { CACHE_KEY, useCache } from '@/hooks/web/useCache'
import { useDesign } from '@/hooks/web/useDesign'
import avatarImg from '@/assets/imgs/avatar.gif'
import { useUserStore } from '@/store/modules/user'
import { useTagsViewStore } from '@/store/modules/tagsView'

const { t } = useI18n()

const { wsCache } = useCache()

const { push, replace } = useRouter()

const userStore = useUserStore()

const tagsViewStore = useTagsViewStore()

const { getPrefixCls } = useDesign()

const prefixCls = getPrefixCls('user-info')

const user = wsCache.get(CACHE_KEY.USER)

const avatar = user.user.avatar ? user.user.avatar : avatarImg

const userName = user.user.nickname ? user.user.nickname : 'Admin'

const loginOut = () => {
  ElMessageBox.confirm(t('common.loginOutMessage'), t('common.reminder'), {
    confirmButtonText: t('common.ok'),
    cancelButtonText: t('common.cancel'),
    type: 'warning'
  })
    .then(async () => {
      await userStore.loginOut()
      tagsViewStore.delAllViews()
      replace('/login?redirect=/index')
    })
    .catch(() => {})
}
const toProfile = async () => {
  push('/user/profile')
}
const toDocument = () => {
  window.open('https://doc.iocoder.cn/')
}
</script>

<template>
  <ElDropdown :class="prefixCls" trigger="click">
    <div class="flex items-center">
      <img :src="avatar" alt="" class="w-[calc(var(--logo-height)-25px)] rounded-[50%]" />
      <span class="<lg:hidden text-14px pl-[5px] text-[var(--top-header-text-color)]">
        {{ userName }}
      </span>
    </div>
    <template #dropdown>
      <ElDropdownMenu>
        <ElDropdownItem>
          <Icon icon="ep:tools" />
          <div @click="toProfile">{{ t('common.profile') }}</div>
        </ElDropdownItem>
        <ElDropdownItem>
          <Icon icon="ep:menu" />
          <div @click="toDocument">{{ t('common.document') }}</div>
        </ElDropdownItem>
        <ElDropdownItem divided>
          <Icon icon="ep:switch-button" />
          <div @click="loginOut">{{ t('common.loginOut') }}</div>
        </ElDropdownItem>
      </ElDropdownMenu>
    </template>
  </ElDropdown>
</template>
