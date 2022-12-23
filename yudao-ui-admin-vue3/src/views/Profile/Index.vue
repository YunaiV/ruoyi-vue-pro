<template>
  <div class="flex">
    <el-card class="w-1/3 user" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ t('profile.user.title') }}</span>
        </div>
      </template>
      <ProfileUser />
    </el-card>
    <el-card class="w-2/3 user" style="margin-left: 10px" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>{{ t('profile.info.title') }}</span>
        </div>
      </template>
      <div>
        <el-tabs v-model="activeName" tab-position="top" style="height: 400px" class="profile-tabs">
          <el-tab-pane :label="t('profile.info.basicInfo')" name="basicInfo">
            <BasicInfo />
          </el-tab-pane>
          <el-tab-pane :label="t('profile.info.resetPwd')" name="resetPwd">
            <ResetPwd />
          </el-tab-pane>
          <el-tab-pane :label="t('profile.info.userSocial')" name="userSocial">
            <UserSocial />
          </el-tab-pane>
        </el-tabs>
      </div>
    </el-card>
  </div>
</template>
<script setup lang="ts" name="Profile">
import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { ElCard, ElTabs, ElTabPane } from 'element-plus'
import { BasicInfo, ProfileUser, ResetPwd, UserSocial } from './components/'
const { t } = useI18n()

const activeName = ref('basicInfo')
</script>
<style scoped>
.user {
  max-height: 960px;
  padding: 15px 20px 20px 20px;
}
.card-header {
  display: flex;
  justify-content: center;
  align-items: center;
}
:deep(.el-card .el-card__header, .el-card .el-card__body) {
  padding: 15px !important;
}
.profile-tabs > .el-tabs__content {
  padding: 32px;
  color: #6b778c;
  font-weight: 600;
}
.el-tabs--left .el-tabs__content {
  height: 100%;
}
</style>
