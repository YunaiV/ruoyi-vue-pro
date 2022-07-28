<script setup lang="ts">
import { getUserProfileApi } from '@/api/system/user/profile'
import { onMounted, reactive } from 'vue'
import dayjs from 'dayjs'
import UserAvatar from './UserAvatar.vue'
import { ProfileVO } from '@/api/system/user/profile/types'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
interface userInfoType {
  user: ProfileVO
}
const userInfo = reactive<userInfoType>({
  user: {
    id: 0,
    username: '',
    nickname: '',
    dept: {
      id: 0,
      name: ''
    },
    roles: [],
    posts: [],
    socialUsers: [],
    email: '',
    mobile: '',
    sex: 0,
    avatar: '',
    status: 0,
    remark: '',
    loginIp: '',
    loginDate: new Date(),
    createTime: new Date()
  }
})
const getUserInfo = async () => {
  const users = await getUserProfileApi()
  userInfo.user = users
}
onMounted(async () => {
  await getUserInfo()
})
</script>
<template>
  <div>
    <div class="text-center">
      <UserAvatar :img="userInfo.user.avatar" />
    </div>
    <ul class="list-group list-group-striped">
      <li class="list-group-item">
        <Icon icon="ep:user" class="mr-5px" />{{ t('profile.user.username') }}
        <div class="pull-right">{{ userInfo.user.username }}</div>
      </li>
      <li class="list-group-item">
        <Icon icon="ep:phone" class="mr-5px" />{{ t('profile.user.mobile') }}
        <div class="pull-right">{{ userInfo.user.mobile }}</div>
      </li>
      <li class="list-group-item">
        <Icon icon="fontisto:email" class="mr-5px" />{{ t('profile.user.email') }}
        <div class="pull-right">{{ userInfo.user.email }}</div>
      </li>
      <li class="list-group-item">
        <Icon icon="carbon:tree-view-alt" class="mr-5px" />{{ t('profile.user.dept') }}
        <div class="pull-right" v-if="userInfo.user.dept">{{ userInfo.user.dept.name }}</div>
      </li>
      <li class="list-group-item">
        <Icon icon="ep:suitcase" class="mr-5px" />{{ t('profile.user.posts') }}
        <div class="pull-right" v-if="userInfo.user.posts">
          {{ userInfo.user.posts.map((post) => post.name).join(',') }}
        </div>
      </li>
      <li class="list-group-item">
        <Icon icon="icon-park-outline:peoples" class="mr-5px" />{{ t('profile.user.roles') }}
        <div class="pull-right" v-if="userInfo.user.roles">
          {{ userInfo.user.roles.map((role) => role.name).join(',') }}
        </div>
      </li>
      <li class="list-group-item">
        <Icon icon="ep:calendar" class="mr-5px" />{{ t('profile.user.createTime') }}
        <div class="pull-right">{{ dayjs(userInfo.user.createTime).format('YYYY-MM-DD') }}</div>
      </li>
    </ul>
  </div>
</template>
<style scoped>
.text-center {
  text-align: center;
  position: relative;
  height: 120px;
}
.list-group-striped > .list-group-item {
  border-left: 0;
  border-right: 0;
  border-radius: 0;
  padding-left: 0;
  padding-right: 0;
}

.list-group {
  padding-left: 0px;
  list-style: none;
}

.list-group-item {
  border-bottom: 1px solid #e7eaec;
  border-top: 1px solid #e7eaec;
  margin-bottom: -1px;
  padding: 11px 0px;
  font-size: 13px;
}
.pull-right {
  float: right !important;
}
</style>
