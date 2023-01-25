<template>
  <el-table :data="socialUsers" :show-header="false">
    <el-table-column type="seq" title="序号" width="60" fixed="left" />
    <el-table-column label="社交平台" align="left" width="120">
      <template #default="{ row }">
        <img class="h-5 align-middle" :src="row.img" alt="" />
        <p class="mr-5">{{ row.title }}</p>
      </template>
    </el-table-column>
    <el-table-column label="操作" align="center">
      <template #default="{ row }">
        <template v-if="row.openid">
          已绑定
          <XTextButton type="primary" class="mr-5" @click="unbind(row)" title="(解绑)" />
        </template>
        <template v-else>
          未绑定
          <XTextButton type="primary" class="mr-5" @click="bind(row)" title="(绑定)" />
        </template>
      </template>
    </el-table-column>
  </el-table>
</template>
<script setup lang="ts">
import { SystemUserSocialTypeEnum } from '@/utils/constants'
import { getUserProfileApi, ProfileVO } from '@/api/system/user/profile'
import { socialAuthRedirect, socialUnbind } from '@/api/system/user/socialUser'

const message = useMessage()
const socialUsers = ref<any[]>([])
const userInfo = ref<ProfileVO>()

const initSocial = async () => {
  const res = await getUserProfileApi()
  userInfo.value = res
  for (const i in SystemUserSocialTypeEnum) {
    const socialUser = { ...SystemUserSocialTypeEnum[i] }
    socialUsers.value.push(socialUser)
    if (userInfo.value?.socialUsers) {
      for (const j in userInfo.value.socialUsers) {
        if (socialUser.type === userInfo.value.socialUsers[j].type) {
          socialUser.openid = userInfo.value.socialUsers[j].openid
          break
        }
      }
    }
  }
}
const bind = (row) => {
  const redirectUri = location.origin + '/user/profile?type=' + row.type
  // 进行跳转
  socialAuthRedirect(row.type, encodeURIComponent(redirectUri)).then((res) => {
    window.location.href = res.data
  })
}
const unbind = async (row) => {
  const res = await socialUnbind(row.type, row.openid)
  if (res) {
    row.openid = undefined
  }
  message.success('解绑成功')
}

onMounted(async () => {
  await initSocial()
})
</script>
