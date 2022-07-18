<script setup lang="ts">
import { reactive, ref, unref, watch, onMounted } from 'vue'
import { Form } from '@/components/Form'
import { useI18n } from '@/hooks/web/useI18n'
import { ElCheckbox, ElLink } from 'element-plus'
import { required } from '@/utils/formRules'
import { useForm } from '@/hooks/web/useForm'
import { getTenantIdByNameApi, getCodeImgApi, loginApi, getAsyncRoutesApi } from '@/api/login'
import { useCache } from '@/hooks/web/useCache'
import { usePermissionStore } from '@/store/modules/permission'
import { useRouter } from 'vue-router'
import { setToken } from '@/utils/auth'
import { useUserStoreWithOut } from '@/store/modules/user'
import type { RouteLocationNormalizedLoaded, RouteRecordRaw } from 'vue-router'
import { UserLoginVO } from '@/api/login/types'

const { wsCache } = useCache()

const userStore = useUserStoreWithOut()

const permissionStore = usePermissionStore()

const { currentRoute, addRoute, push } = useRouter()

const { t } = useI18n()

const rules = {
  tenantName: [required],
  username: [required],
  password: [required],
  code: [required]
}
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
    code: '',
    uuid: ''
  }
})
const schema = reactive<FormSchema[]>([
  {
    field: 'title',
    colProps: {
      span: 24
    }
  },
  {
    field: 'tenantName',
    label: t('login.tenantname'),
    value: loginData.loginForm.tenantName,
    component: 'Input',
    colProps: {
      span: 24
    },
    componentProps: {
      placeholder: t('login.tenantNamePlaceholder')
    }
  },
  {
    field: 'username',
    label: t('login.username'),
    value: loginData.loginForm.username,
    component: 'Input',
    colProps: {
      span: 24
    },
    componentProps: {
      placeholder: t('login.usernamePlaceholder')
    }
  },
  {
    field: 'password',
    label: t('login.password'),
    value: loginData.loginForm.password,
    component: 'InputPassword',
    colProps: {
      span: 24
    },
    componentProps: {
      style: {
        width: '100%'
      },
      placeholder: t('login.passwordPlaceholder')
    }
  },
  {
    field: 'code',
    label: t('login.code'),
    value: loginData.loginForm.code,
    component: 'Input',
    colProps: {
      span: 12
    },
    componentProps: {
      style: {
        width: '100%'
      },
      placeholder: t('login.codePlaceholder')
    }
  },
  {
    field: 'codeImg',
    colProps: {
      span: 12
    }
  },
  {
    field: 'tool',
    colProps: {
      span: 24
    }
  },
  {
    field: 'login',
    colProps: {
      span: 24
    }
  },
  {
    field: 'other',
    component: 'Divider',
    label: t('login.otherLogin'),
    componentProps: {
      contentPosition: 'center'
    }
  },
  {
    field: 'otherIcon',
    colProps: {
      span: 24
    }
  }
])

const iconSize = 30
const remember = ref(false)
const { register, elFormRef, methods } = useForm()
const loading = ref(false)
const iconColor = '#999'
const redirect = ref<string>('')

watch(
  () => currentRoute.value,
  (route: RouteLocationNormalizedLoaded) => {
    redirect.value = route?.query?.redirect as string
  },
  {
    immediate: true
  }
)
// 获取验证码
const getCode = async () => {
  const res = await getCodeImgApi()
  loginData.codeImg = 'data:image/gif;base64,' + res.img
  loginData.loginForm.uuid = res.uuid
}
//获取租户ID
const getTenantId = async () => {
  const res = await getTenantIdByNameApi(loginData.loginForm.tenantName)
  wsCache.set('tenantId', res)
}
// 登录
const signIn = async () => {
  await getTenantId()
  const formRef = unref(elFormRef)
  await formRef?.validate(async (isValid) => {
    if (isValid) {
      loading.value = true
      const { getFormData } = methods
      const formData = await getFormData<UserLoginVO>()
      formData.uuid = loginData.loginForm.uuid
      await loginApi(formData)
        .then(async (res) => {
          setToken(res)
          getRoutes()
          await userStore.getUserInfoAction()
        })
        .catch(() => {
          getCode()
        })
        .finally(() => (loading.value = false))
    }
  })
}
// 获取路由
const getRoutes = async () => {
  // 后端过滤菜单
  const routers = await getAsyncRoutesApi()
  wsCache.set('roleRouters', routers)
  await permissionStore.generateRoutes(routers).catch(() => {})
  permissionStore.getAddRouters.forEach((route) => {
    addRoute(route as RouteRecordRaw) // 动态添加可访问路由表
  })
  permissionStore.setIsAddRouters(true)
  push({ path: redirect.value || permissionStore.addRouters[0].path })
}
onMounted(() => {
  getCode()
})
</script>

<template>
  <Form
    :schema="schema"
    :rules="rules"
    label-position="top"
    hide-required-asterisk
    size="large"
    @register="register"
    v-show="false"
  >
    <template #header>
      <h2 class="text-2xl font-bold text-center w-[100%]">{{ t('login.login') }}</h2>
    </template>
    <template #codeImg>
      <img :src="loginData.codeImg" @click="getCode" alt="" />
    </template>

    <template #tool>
      <div class="flex justify-between items-center w-[100%]">
        <ElCheckbox v-model="remember" :label="t('login.remember')" size="small" />
        <ElLink type="primary" :underline="false">{{ t('login.forgetPassword') }}</ElLink>
      </div>
    </template>

    <template #login>
      <ElButton :loading="loading" type="primary" class="w-[100%]" @click="signIn">
        {{ t('login.login') }}
      </ElButton>
    </template>

    <template #otherIcon>
      <div class="flex justify-between w-[100%]">
        <Icon
          icon="ant-design:github-filled"
          :size="iconSize"
          class="cursor-pointer anticon"
          :color="iconColor"
        />
        <Icon
          icon="ant-design:wechat-filled"
          :size="iconSize"
          class="cursor-pointer anticon"
          :color="iconColor"
        />
        <Icon
          icon="ant-design:alipay-circle-filled"
          :size="iconSize"
          :color="iconColor"
          class="cursor-pointer anticon"
        />
        <Icon
          icon="ant-design:weibo-circle-filled"
          :size="iconSize"
          :color="iconColor"
          class="cursor-pointer anticon"
        />
      </div>
    </template>
  </Form>
</template>

<style lang="less" scoped>
:deep(.anticon) {
  &:hover {
    color: var(--el-color-primary) !important;
  }
}
</style>
