<template>
  <el-form
    :model="loginData.loginForm"
    :rules="rules"
    label-position="top"
    class="login-form"
    label-width="120px"
    size="large"
    v-show="getShow"
    ref="formSmsLogin"
  >
    <el-row style="margin-left: -10px; margin-right: -10px">
      <!-- 租户名 -->
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item>
          <LoginFormTitle style="width: 100%" />
        </el-form-item>
      </el-col>
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item prop="tenantName">
          <el-input
            type="text"
            v-model="loginData.loginForm.tenantName"
            :placeholder="t('login.tenantNamePlaceholder')"
            :prefix-icon="iconHouse"
          />
        </el-form-item>
      </el-col>
      <!-- 手机号 -->
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item prop="mobileNumber">
          <el-input
            v-model="loginData.loginForm.mobileNumber"
            :placeholder="t('login.mobileNumberPlaceholder')"
            :prefix-icon="iconCellphone"
          />
        </el-form-item>
      </el-col>
      <!-- 验证码 -->
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item prop="code">
          <el-row justify="space-between" style="width: 100%" :gutter="5">
            <el-col :span="24">
              <el-input
                v-model="loginData.loginForm.code"
                :placeholder="t('login.codePlaceholder')"
                :prefix-icon="iconCircleCheck"
              >
                <!-- <el-button class="w-[100%]"> -->
                <template #append>
                  <span
                    v-if="mobileCodeTimer <= 0"
                    @click="getSmsCode"
                    class="getMobileCode"
                    style="cursor: pointer"
                  >
                    {{ t('login.getSmsCode') }}
                  </span>
                  <span v-if="mobileCodeTimer > 0" class="getMobileCode" style="cursor: pointer">
                    {{ mobileCodeTimer }}秒后可重新获取
                  </span>
                </template>
              </el-input>
              <!-- </el-button> -->
            </el-col>
          </el-row>
        </el-form-item>
      </el-col>
      <!-- 登录按钮 / 返回按钮 -->
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item>
          <el-button :loading="loginLoading" type="primary" class="w-[100%]" @click="signIn">
            {{ t('login.login') }}
          </el-button>
        </el-form-item>
      </el-col>
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item>
          <el-button :loading="loginLoading" class="w-[100%]" @click="handleBackLogin">
            {{ t('login.backLogin') }}
          </el-button>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
<script setup lang="ts">
import { reactive, ref, unref, watch, computed } from 'vue'
import { ElForm, ElFormItem, ElInput, ElRow, ElCol } from 'element-plus'
import { useRouter } from 'vue-router'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { useI18n } from '@/hooks/web/useI18n'
import { useIcon } from '@/hooks/web/useIcon'
import { useCache } from '@/hooks/web/useCache'
import { useMessage } from '@/hooks/web/useMessage'
import { setToken } from '@/utils/auth'
import { required } from '@/utils/formRules'
import { usePermissionStore } from '@/store/modules/permission'
import { getTenantIdByNameApi, sendSmsCodeApi, smsLoginApi } from '@/api/login'
import LoginFormTitle from './LoginFormTitle.vue'
import { useLoginState, LoginStateEnum, useFormValid } from './useLogin'

const { t } = useI18n()
const { wsCache } = useCache()
const message = useMessage()
const permissionStore = usePermissionStore()
const { currentRoute, push } = useRouter()
const formSmsLogin = ref()
const loginLoading = ref(false)
const iconHouse = useIcon({ icon: 'ep:house' })
const iconCellphone = useIcon({ icon: 'ep:cellphone' })
const iconCircleCheck = useIcon({ icon: 'ep:circle-check' })
const { validForm } = useFormValid(formSmsLogin)
const { handleBackLogin, getLoginState } = useLoginState()
const getShow = computed(() => unref(getLoginState) === LoginStateEnum.MOBILE)

const rules = {
  tenantName: [required],
  mobileNumber: [required],
  code: [required]
}
const loginData = reactive({
  codeImg: '',
  tenantEnable: true,
  token: '',
  loading: {
    signIn: false
  },
  loginForm: {
    uuid: '',
    tenantName: '芋道源码',
    mobileNumber: '',
    code: ''
  }
})
const smsVO = reactive({
  smsCode: {
    mobile: '',
    scene: 21
  },
  loginSms: {
    mobile: '',
    code: ''
  }
})
const mobileCodeTimer = ref(0)
const redirect = ref<string>('')
const getSmsCode = async () => {
  await getTenantId()
  smsVO.smsCode.mobile = loginData.loginForm.mobileNumber
  await sendSmsCodeApi(smsVO.smsCode).then(async () => {
    message.success(t('login.SmsSendMsg'))
    // 设置倒计时
    mobileCodeTimer.value = 60
    let msgTimer = setInterval(() => {
      mobileCodeTimer.value = mobileCodeTimer.value - 1
      if (mobileCodeTimer.value <= 0) {
        clearInterval(msgTimer)
      }
    }, 1000)
  })
}
watch(
  () => currentRoute.value,
  (route: RouteLocationNormalizedLoaded) => {
    redirect.value = route?.query?.redirect as string
  },
  {
    immediate: true
  }
)
// 获取租户 ID
const getTenantId = async () => {
  const res = await getTenantIdByNameApi(loginData.loginForm.tenantName)
  wsCache.set('tenantId', res)
}
// 登录
const signIn = async () => {
  await getTenantId()
  const data = await validForm()
  if (!data) return
  loginLoading.value = true
  smsVO.loginSms.mobile = loginData.loginForm.mobileNumber
  smsVO.loginSms.code = loginData.loginForm.code
  await smsLoginApi(smsVO.loginSms)
    .then(async (res) => {
      setToken(res?.token)
      if (!redirect.value) {
        redirect.value = '/'
      }
      push({ path: redirect.value || permissionStore.addRouters[0].path })
    })
    .catch(() => {})
    .finally(() => {
      loginLoading.value = false
    })
}
</script>

<style lang="scss" scoped>
:deep(.anticon) {
  &:hover {
    color: var(--el-color-primary) !important;
  }
}

.smsbtn {
  margin-top: 33px;
}
</style>
