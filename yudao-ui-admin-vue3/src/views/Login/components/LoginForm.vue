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
              :size="30"
              class="cursor-pointer anticon"
              color="#999"
              @click="doSocialLogin('github')"
            />
            <Icon
              icon="ant-design:wechat-filled"
              :size="30"
              class="cursor-pointer anticon"
              color="#999"
              @click="doSocialLogin('wechat')"
            />
            <Icon
              icon="ant-design:alipay-circle-filled"
              :size="30"
              color="#999"
              class="cursor-pointer anticon"
              @click="doSocialLogin('alipay')"
            />
            <Icon
              icon="ant-design:dingtalk-circle-filled"
              :size="30"
              color="#999"
              class="cursor-pointer anticon"
              @click="doSocialLogin('dingtalk')"
            />
          </div>
        </el-form-item>
      </el-col>
    </el-row>
  </el-form>
</template>
<script setup lang="ts">
import { reactive, ref, unref, onMounted, computed, watch } from 'vue'
import LoginFormTitle from './LoginFormTitle.vue'
import {
  ElForm,
  ElFormItem,
  ElInput,
  ElCheckbox,
  ElCol,
  ElLink,
  ElRow,
  ElDivider,
  ElLoading
} from 'element-plus'
import Cookies from 'js-cookie'
import { useRouter } from 'vue-router'
import type { RouteLocationNormalizedLoaded } from 'vue-router'
import { useI18n } from '@/hooks/web/useI18n'
import { useIcon } from '@/hooks/web/useIcon'
import { required } from '@/utils/formRules'
import { setToken, setTenantId } from '@/utils/auth'
import { decrypt, encrypt } from '@/utils/jsencrypt'
import { Icon } from '@/components/Icon'
import { Verify } from '@/components/Verifition'
import { usePermissionStore } from '@/store/modules/permission'
import * as LoginApi from '@/api/login'
import { LoginStateEnum, useLoginState, useFormValid } from './useLogin'

const { t } = useI18n()
const formLogin = ref()
const { validForm } = useFormValid(formLogin)
const { setLoginState, getLoginState } = useLoginState()
const { currentRoute, push } = useRouter()
const permissionStore = usePermissionStore()
const redirect = ref<string>('')
const loginLoading = ref(false)
const iconHouse = useIcon({ icon: 'ep:house' })
const iconAvatar = useIcon({ icon: 'ep:avatar' })
const iconLock = useIcon({ icon: 'ep:lock' })
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
  token: '',
  loading: {
    signIn: false
  },
  loginForm: {
    tenantName: Cookies.get('tenantName') ? Cookies.get('tenantName') : '芋道源码',
    username: Cookies.get('username') ? Cookies.get('username') : 'admin',
    password: Cookies.get('password')
      ? (decrypt(Cookies.get('password')) as unknown as string)
      : 'admin123',
    captchaVerification: '',
    rememberMe: false
  }
})

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
  const res = await LoginApi.getTenantIdByNameApi(loginData.loginForm.tenantName)
  setTenantId(res)
}
// 记住我
const getCookie = () => {
  const username = Cookies.get('username')
  const password = Cookies.get('password')
    ? (decrypt(Cookies.get('password')) as unknown as string)
    : undefined
  const rememberMe = Cookies.get('rememberMe')
  const tenantName = Cookies.get('tenantName')
  loginData.loginForm = {
    ...loginData.loginForm,
    username: username ? username : loginData.loginForm.username,
    password: password ? password : loginData.loginForm.password,
    rememberMe: rememberMe ? true : false,
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
    ElLoading.service({
      lock: true,
      text: '正在加载系统中...',
      background: 'rgba(0, 0, 0, 0.7)'
    })
    if (loginData.loginForm.rememberMe) {
      Cookies.set('username', loginData.loginForm.username, { expires: 30 })
      Cookies.set('password', encrypt(loginData.loginForm.password as unknown as string), {
        expires: 30
      })
      Cookies.set('rememberMe', loginData.loginForm.rememberMe, { expires: 30 })
      Cookies.set('tenantName', loginData.loginForm.tenantName, { expires: 30 })
    } else {
      Cookies.remove('username')
      Cookies.remove('password')
      Cookies.remove('rememberMe')
      Cookies.remove('tenantName')
    }
    setToken(res)
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
const doSocialLogin = async (type: string) => {
  loginLoading.value = true
  // 计算 redirectUri
  const redirectUri =
    location.origin + '/social-login?type=' + type + '&redirect=' + (redirect.value || '/')
  // 进行跳转
  const res = await LoginApi.socialAuthRedirectApi(type, encodeURIComponent(redirectUri))
  window.location.href = res
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
