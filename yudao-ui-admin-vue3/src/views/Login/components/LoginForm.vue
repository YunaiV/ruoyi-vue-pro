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
        <el-form-item prop="tenantName" v-if="loginData.tenantEnable === 'true'">
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
          <XButton
            :loading="loginLoading"
            type="primary"
            class="w-[100%]"
            :title="t('login.login')"
            @click="getCode()"
          />
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
              <XButton
                class="w-[100%]"
                :title="t('login.btnMobile')"
                @click="setLoginState(LoginStateEnum.MOBILE)"
              />
            </el-col>
            <el-col :span="8">
              <XButton
                class="w-[100%]"
                :title="t('login.btnQRCode')"
                @click="setLoginState(LoginStateEnum.QR_CODE)"
              />
            </el-col>
            <el-col :span="8">
              <XButton
                class="w-[100%]"
                :title="t('login.btnRegister')"
                @click="setLoginState(LoginStateEnum.REGISTER)"
              />
            </el-col>
          </el-row>
        </el-form-item>
      </el-col>
      <el-divider content-position="center">{{ t('login.otherLogin') }}</el-divider>
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item>
          <div class="flex justify-between w-[100%]">
            <Icon
              v-for="(item, key) in socialList"
              :key="key"
              :icon="item.icon"
              :size="30"
              class="cursor-pointer anticon"
              color="#999"
              @click="doSocialLogin(item.type)"
            />
          </div>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
<script setup lang="ts">
import { ElLoading } from 'element-plus'
import LoginFormTitle from './LoginFormTitle.vue'
import type { RouteLocationNormalizedLoaded } from 'vue-router'

import { useIcon } from '@/hooks/web/useIcon'

import * as authUtil from '@/utils/auth'
import { usePermissionStore } from '@/store/modules/permission'
import * as LoginApi from '@/api/login'
import { LoginStateEnum, useLoginState, useFormValid } from './useLogin'

const { t } = useI18n()
const message = useMessage()
const iconHouse = useIcon({ icon: 'ep:house' })
const iconAvatar = useIcon({ icon: 'ep:avatar' })
const iconLock = useIcon({ icon: 'ep:lock' })
const formLogin = ref()
const { validForm } = useFormValid(formLogin)
const { setLoginState, getLoginState } = useLoginState()
const { currentRoute, push } = useRouter()
const permissionStore = usePermissionStore()
const redirect = ref<string>('')
const loginLoading = ref(false)
const verify = ref()
const captchaType = ref('blockPuzzle') // blockPuzzle 滑块 clickWord 点击文字

const getShow = computed(() => unref(getLoginState) === LoginStateEnum.LOGIN)

const LoginRules = {
  tenantName: [required],
  username: [required],
  password: [required]
}
const loginData = reactive({
  isShowPassword: false,
  captchaEnable: import.meta.env.VITE_APP_CAPTCHA_ENABLE,
  tenantEnable: import.meta.env.VITE_APP_TENANT_ENABLE,
  loginForm: {
    tenantName: '芋道源码',
    username: 'admin',
    password: 'admin123',
    captchaVerification: '',
    rememberMe: false
  }
})

const socialList = [
  { icon: 'ant-design:github-filled', type: 0 },
  { icon: 'ant-design:wechat-filled', type: 30 },
  { icon: 'ant-design:alipay-circle-filled', type: 0 },
  { icon: 'ant-design:dingtalk-circle-filled', type: 20 }
]

// 获取验证码
const getCode = async () => {
  // 情况一，未开启：则直接登录
  if (loginData.captchaEnable === 'false') {
    await handleLogin({})
  } else {
    // 情况二，已开启：则展示验证码；只有完成验证码的情况，才进行登录
    // 弹出验证码
    verify.value.show()
  }
}
//获取租户ID
const getTenantId = async () => {
  if (loginData.tenantEnable === 'true') {
    const res = await LoginApi.getTenantIdByNameApi(loginData.loginForm.tenantName)
    authUtil.setTenantId(res)
  }
}
// 记住我
const getCookie = () => {
  const loginForm = authUtil.getLoginForm()
  if (loginForm) {
    loginData.loginForm = {
      ...loginData.loginForm,
      username: loginForm.username ? loginForm.username : loginData.loginForm.username,
      password: loginForm.password ? loginForm.password : loginData.loginForm.password,
      rememberMe: loginForm.rememberMe ? true : false,
      tenantName: loginForm.tenantName ? loginForm.tenantName : loginData.loginForm.tenantName
    }
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
    ElLoading.service({
      lock: true,
      text: '正在加载系统中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    if (loginData.loginForm.rememberMe) {
      authUtil.setLoginForm(loginData.loginForm)
    } else {
      authUtil.removeLoginForm()
    }
    authUtil.setToken(res)
    if (!redirect.value) {
      redirect.value = '/'
    }
    push({ path: redirect.value || permissionStore.addRouters[0].path })
  } catch {
    loginLoading.value = false
  } finally {
    setTimeout(() => {
      const loadingInstance = ElLoading.service()
      loadingInstance.close()
    }, 400)
  }
}

// 社交登录
const doSocialLogin = async (type: number) => {
  if (type === 0) {
    message.error('此方式未配置')
  } else {
    loginLoading.value = true
    if (loginData.tenantEnable === 'true') {
      await message.prompt('请输入租户名称', t('common.reminder')).then(async ({ value }) => {
        const res = await LoginApi.getTenantIdByNameApi(value)
        authUtil.setTenantId(res)
      })
    }
    // 计算 redirectUri
    const redirectUri =
      location.origin + '/social-login?type=' + type + '&redirect=' + (redirect.value || '/')
    // 进行跳转
    const res = await LoginApi.socialAuthRedirectApi(type, encodeURIComponent(redirectUri))
    window.location.href = res
  }
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

<style lang="scss" scoped>
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
