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
import { useUserStoreWithOut } from '@/store/modules/user'
import { useCache } from '@/hooks/web/useCache'
import { usePermissionStore } from '@/store/modules/permission'
import { useRouter } from 'vue-router'
import { useI18n } from '@/hooks/web/useI18n'
import { required } from '@/utils/formRules'
import { Icon } from '@/components/Icon'
import { LoginStateEnum, useLoginState, useFormValid } from './useLogin'
import type { RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router'

const { currentRoute, addRoute, push } = useRouter()
const permissionStore = usePermissionStore()
const userStore = useUserStoreWithOut()
const formLogin = ref()
const { validForm } = useFormValid(formLogin)
const { wsCache } = useCache()
const { setLoginState, getLoginState } = useLoginState()
const getShow = computed(() => unref(getLoginState) === LoginStateEnum.LOGIN)
const iconSize = 30
const iconColor = '#999'
const redirect = ref<string>('')
const { t } = useI18n()
const iconHouse = useIcon({ icon: 'ep:house' })
const iconAvatar = useIcon({ icon: 'ep:avatar' })
const iconLock = useIcon({ icon: 'ep:lock' })
const iconCircleCheck = useIcon({ icon: 'ep:circle-check' })
const LoginCaptchaRules = {
  tenantName: [required],
  username: [required],
  password: [required],
  code: [required]
}
const LoginRules = {
  tenantName: [required],
  username: [required],
  password: [required]
}
const loginLoading = ref(false)
const loginData = reactive({
  codeImg: '',
  isShowPassword: false,
  captchaEnable: true,
  tenantEnable: true,
  token: '',
  loading: {
    signIn: false
  },
  loginForm: {
    tenantName: '芋道源码',
    username: 'admin',
    password: 'admin123',
    rememberMe: false,
    code: '',
    uuid: ''
  }
})

// 获取验证码
const getCode = async () => {
  const res = await LoginApi.getCodeImgApi()
  loginData.captchaEnable = res.enable
  if (res.enable) {
    loginData.codeImg = 'data:image/gif;base64,' + res.img
    loginData.loginForm.uuid = res.uuid
  }
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
const handleLogin = async () => {
  await getTenantId()
  const data = await validForm()
  if (!data) return
  loginLoading.value = true
  await LoginApi.loginApi(loginData.loginForm)
    .then(async (res) => {
      setToken(res)
      const userInfo = await LoginApi.getInfoApi()
      await userStore.getUserInfoAction(userInfo)
      await getRoutes()
    })
    .catch(() => {
      getCode()
    })
    .finally(() => {
      loginLoading.value = false
    })
}

// 获取路由
const getRoutes = async () => {
  // 后端过滤菜单
  const res = await LoginApi.getAsyncRoutesApi()
  wsCache.set('roleRouters', res)
  await permissionStore.generateRoutes(res)
  permissionStore.getAddRouters.forEach((route) => {
    addRoute(route as RouteRecordRaw) // 动态添加可访问路由表
  })
  permissionStore.setIsAddRouters(true)
  push({ path: redirect.value || permissionStore.addRouters[0].path })
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
onMounted(async () => {
  await getCode()
  getCookie()
})
</script>
<template>
  <el-form
    :model="loginData.loginForm"
    :rules="loginData.captchaEnable ? LoginCaptchaRules : LoginRules"
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
            @keyup.enter="handleLogin"
            :prefix-icon="iconLock"
          />
        </el-form-item>
      </el-col>
      <el-col :span="24" style="padding-left: 10px; padding-right: 10px">
        <el-form-item prop="code" v-if="loginData.captchaEnable">
          <el-row justify="space-between" style="width: 100%">
            <el-col :span="14">
              <el-input
                v-model="loginData.loginForm.code"
                :placeholder="t('login.codePlaceholder')"
                @keyup.enter="handleLogin"
                :prefix-icon="iconCircleCheck"
                style="width: 90%"
              />
            </el-col>
            <el-col :span="10">
              <div class="login-code">
                <img :src="loginData.codeImg" @click="getCode" class="login-code-img" alt="" />
              </div>
            </el-col>
          </el-row>
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
          <el-button :loading="loginLoading" type="primary" class="w-[100%]" @click="handleLogin">
            {{ t('login.login') }}
          </el-button>
        </el-form-item>
      </el-col>
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
