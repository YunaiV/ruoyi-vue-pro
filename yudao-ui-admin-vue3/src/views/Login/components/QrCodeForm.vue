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
        <el-button class="w-[100%]" @click="handleBackLogin">
          {{ t('sys.login.backSignIn') }}
        </el-button>
      </div>
    </el-col>
  </el-row>
</template>
<script setup lang="ts">
import { computed, unref } from 'vue'
import { ElRow, ElCol, ElCard, ElDivider } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { useLoginState, LoginStateEnum } from './useLogin'
import LoginFormTitle from './LoginFormTitle.vue'
import { Qrcode } from '@/components/Qrcode'
import logoImg from '@/assets/imgs/logo.png'

const { t } = useI18n()
const { handleBackLogin, getLoginState } = useLoginState()
const getShow = computed(() => unref(getLoginState) === LoginStateEnum.QR_CODE)
</script>
