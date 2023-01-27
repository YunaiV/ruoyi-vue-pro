<template>
  <el-row v-show="getShow" style="maring-left: -10px; maring-right: -10px">
    <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
      <LoginFormTitle style="width: 100%" />
    </el-col>
    <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
      <el-card shadow="hover" class="mb-10px text-center">
        <Qrcode :logo="logoImg" />
      </el-card>
    </el-col>
    <el-divider class="enter-x">{{ t('login.qrcode') }}</el-divider>
    <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
      <div class="w-[100%] mt-15px">
        <XButton class="w-[100%]" :title="t('login.backLogin')" @click="handleBackLogin()" />
      </div>
    </el-col>
  </el-row>
</template>
<script setup lang="ts">
import logoImg from '@/assets/imgs/logo.png'

import LoginFormTitle from './LoginFormTitle.vue'
import { useLoginState, LoginStateEnum } from './useLogin'

const { t } = useI18n()
const { handleBackLogin, getLoginState } = useLoginState()
const getShow = computed(() => unref(getLoginState) === LoginStateEnum.QR_CODE)
</script>
