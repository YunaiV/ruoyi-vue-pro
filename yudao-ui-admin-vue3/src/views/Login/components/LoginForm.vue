<script lang="ts" setup>
import { useIcon } from '@/hooks/web/useIcon'
import LoginFormTitle from './LoginFormTitle.vue'
import {
  ElForm,
  ElFormItem,
  ElInput,
  ElCheckbox,
  ElCol,
  ElLink,
  ElRow,
  ElDivider
} from 'element-plus'
import { reactive, ref, unref, onMounted, computed, watch } from 'vue'
import * as LoginApi from '@/api/login'
import {
  setToken,
  setTenantId,
  getUsername,
  getRememberMe,
  getPassword,
  getTenantName
} from '@/utils/auth'
import { usePermissionStore } from '@/store/modules/permission'
import { useRouter } from 'vue-router'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { Icon } from '@/components/Icon'
import { LoginStateEnum, useLoginState, useFormValid } from './useLogin'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { Verify } from '@/components/Verifition'

const { currentRoute, push } = useRouter()
const permissionStore = usePermissionStore()
const formLogin = ref()
const { validForm } = useFormValid(formLogin)
const { setLoginState, getLoginState } = useLoginState()
const getShow = computed(() => unref(getLoginState) === LoginStateEnum.LOGIN)
const iconSize = 30
const iconColor = '#999'
const redirect = ref<string>('')
const { t } = useI18n()
const iconHouse = useIcon({ icon: 'ep:house' })
const iconAvatar = useIcon({ icon: 'ep:avatar' })
const iconLock = useIcon({ icon: 'ep:lock' })
const LoginRules = {
  tenantName: [required],
  username: [required],
  password: [required]
}
const loginLoading = ref(false)
const loginData = reactive({
  isShowPassword: false,
  captchaEnable: import.meta.env.VITE_APP_CAPTCHA_ENABLE,
  tenantEnable: import.meta.env.VITE_APP_TENANT_ENABLE,
  token: '',
  loading: {
    signIn: false
  },
  loginForm: {
    tenantName: '芋道源码',
    username: 'admin',
    password: 'admin123',
    captchaVerification: '',
    rememberMe: false
  }
})
// blockPuzzle 滑块 clickWord 点击文字
const verify = ref()
const captchaType = ref('blockPuzzle')

// 获取验证码
const getCode = async () => {
  // 情况一，未开启：则直接登录
  if (!loginData.captchaEnable) {
    await handleLogin({})
    return
  }

  // 情况二，已开启：则展示验证码；只有完成验证码的情况，才进行登录
  // 弹出验证码
  verify.value.show()
}
//获取租户ID
const getTenantId = async () => {
  const res = await LoginApi.getTenantIdByNameApi(loginData.loginForm.tenantName)
  setTenantId(res)
}
// 记住我
const getCookie = () => {
  const username = getUsername()
  const password = getPassword()
  const rememberMe = getRememberMe()
  const tenantName = getTenantName()
  loginData.loginForm = {
    ...loginData.loginForm,
    username: username ? username : loginData.loginForm.username,
    password: password ? password : loginData.loginForm.password,
    rememberMe: rememberMe ? getRememberMe() : false,
    tenantName: tenantName ? tenantName : loginData.loginForm.tenantName
  }
}
// 登录
const handleLogin = async (params) => {
  loginLoading.value = true
  try {
    await getTenantId()
    const data = await validForm()
    if (!data) {
      return
    }
    loginData.loginForm.captchaVerification = params.captchaVerification
    const res = await LoginApi.loginApi(loginData.loginForm)
    if (!res) {
      return
    }
    setToken(res)
    if (!redirect.value) {
      redirect.value = '/'
    }
    push({ path: redirect.value || permissionStore.addRouters[0].path })
  } finally {
    loginLoading.value = false
  }
}

// 社交登录
const doSocialLogin = async (type: string) => {
  loginLoading.value = true
  // 计算 redirectUri
  const redirectUri =
    location.origin + '/social-login?type=' + type + '&redirect=' + (redirect.value || '/')
  // 进行跳转
  const res = await LoginApi.socialAuthRedirectApi(type, encodeURIComponent(redirectUri))
  window.open = res
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
onMounted(() => {
  getCookie()
})
</script>
<template>
  <el-form
    :model="loginData.loginForm"
    :rules="LoginRules"
    label-position="top"
    class="login-form"
    label-width="120px"
    size="large"
    v-show="getShow"
    ref="formLogin"
  >
    <el-row style="maring-left: -10px; maring-right: -10px">
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
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item prop="username">
          <el-input
            v-model="loginData.loginForm.username"
            :placeholder="t('login.usernamePlaceholder')"
            :prefix-icon="iconAvatar"
          />
        </el-form-item>
      </el-col>
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item prop="password">
          <el-input
            v-model="loginData.loginForm.password"
            type="password"
            :placeholder="t('login.passwordPlaceholder')"
            show-password
            @keyup.enter="getCode()"
            :prefix-icon="iconLock"
          />
        </el-form-item>
      </el-col>
      <el-col
        :span="24"
        style="padding-left: 10px; padding-right: 10px; margin-top: -20px; margin-bottom: -20px"
      >
        <el-form-item>
          <el-row justify="space-between" style="width: 100%">
            <el-col :span="6">
              <el-checkbox v-model="loginData.loginForm.rememberMe">
                {{ t('login.remember') }}
              </el-checkbox>
            </el-col>
            <el-col :span="12" :offset="6">
              <el-link type="primary" style="float: right">{{ t('login.forgetPassword') }}</el-link>
            </el-col>
          </el-row>
        </el-form-item>
      </el-col>
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item>
          <el-button :loading="loginLoading" type="primary" class="w-[100%]" @click="getCode()">
            {{ t('login.login') }}
          </el-button>
        </el-form-item>
      </el-col>
      <Verify
        ref="verify"
        mode="pop"
        :captchaType="captchaType"
        :imgSize="{ width: '400px', height: '200px' }"
        @success="handleLogin"
      />
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item>
          <el-row justify="space-between" style="width: 100%" :gutter="5">
            <el-col :span="8">
              <el-button class="w-[100%]" @click="setLoginState(LoginStateEnum.MOBILE)">
                {{ t('login.btnMobile') }}
              </el-button>
            </el-col>
            <el-col :span="8">
              <el-button class="w-[100%]" @click="setLoginState(LoginStateEnum.QR_CODE)">
                {{ t('login.btnQRCode') }}
              </el-button>
            </el-col>
            <el-col :span="8">
              <el-button class="w-[100%]" @click="setLoginState(LoginStateEnum.REGISTER)">
                {{ t('login.btnRegister') }}
              </el-button>
            </el-col>
          </el-row>
        </el-form-item>
      </el-col>
      <el-divider content-position="center">{{ t('login.otherLogin') }}</el-divider>
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item>
          <div class="flex justify-between w-[100%]">
            <Icon
              icon="ant-design:github-filled"
              :size="iconSize"
              class="cursor-pointer anticon"
              :color="iconColor"
              @click="doSocialLogin('github')"
            />
            <Icon
              icon="ant-design:wechat-filled"
              :size="iconSize"
              class="cursor-pointer anticon"
              :color="iconColor"
              @click="doSocialLogin('wechat')"
            />
            <Icon
              icon="ant-design:alipay-circle-filled"
              :size="iconSize"
              :color="iconColor"
              class="cursor-pointer anticon"
              @click="doSocialLogin('alipay')"
            />
            <Icon
              icon="ant-design:dingtalk-circle-filled"
              :size="iconSize"
              :color="iconColor"
              class="cursor-pointer anticon"
              @click="doSocialLogin('dingtalk')"
            />
          </div>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
<style lang="less" scoped>
:deep(.anticon) {
  &:hover {
    color: var(--el-color-primary) !important;
  }
}
.login-code {
  width: 100%;
  height: 38px;
  float: right;

  img {
    cursor: pointer;
    width: 100%;
    max-width: 100px;
    height: auto;
    vertical-align: middle;
  }
}
</style>
