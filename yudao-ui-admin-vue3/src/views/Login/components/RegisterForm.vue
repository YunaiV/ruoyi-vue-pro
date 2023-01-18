<template>
  <Form
    :schema="schema"
    :rules="rules"
    label-position="top"
    hide-required-asterisk
    size="large"
    v-show="getShow"
    class="dark:(border-1 border-[var(--el-border-color)] border-solid)"
    @register="register"
  >
    <template #title>
      <LoginFormTitle style="width: 100%" />
    </template>

    <template #code="form">
      <div class="w-[100%] flex">
        <el-input v-model="form['code']" :placeholder="t('login.codePlaceholder')" />
      </div>
    </template>

    <template #register>
      <div class="w-[100%]">
        <XButton
          :loading="loading"
          type="primary"
          class="w-[100%]"
          :title="t('login.register')"
          @click="loginRegister()"
        />
      </div>
      <div class="w-[100%] mt-15px">
        <XButton class="w-[100%]" :title="t('login.hasUser')" @click="handleBackLogin()" />
      </div>
    </template>
  </Form>
</template>
<script setup lang="ts">
import type { FormRules } from 'element-plus'

import { useForm } from '@/hooks/web/useForm'
import { useValidator } from '@/hooks/web/useValidator'
import LoginFormTitle from './LoginFormTitle.vue'
import { useLoginState, LoginStateEnum } from './useLogin'
import { FormSchema } from '@/types/form'

const { t } = useI18n()
const { required } = useValidator()
const { register, elFormRef } = useForm()
const { handleBackLogin, getLoginState } = useLoginState()
const getShow = computed(() => unref(getLoginState) === LoginStateEnum.REGISTER)

const schema = reactive<FormSchema[]>([
  {
    field: 'title',
    colProps: {
      span: 24
    }
  },
  {
    field: 'username',
    label: t('login.username'),
    value: '',
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
    value: '',
    component: 'InputPassword',
    colProps: {
      span: 24
    },
    componentProps: {
      style: {
        width: '100%'
      },
      strength: true,
      placeholder: t('login.passwordPlaceholder')
    }
  },
  {
    field: 'check_password',
    label: t('login.checkPassword'),
    value: '',
    component: 'InputPassword',
    colProps: {
      span: 24
    },
    componentProps: {
      style: {
        width: '100%'
      },
      strength: true,
      placeholder: t('login.passwordPlaceholder')
    }
  },
  {
    field: 'code',
    label: t('login.code'),
    colProps: {
      span: 24
    }
  },
  {
    field: 'register',
    colProps: {
      span: 24
    }
  }
])

const rules: FormRules = {
  username: [required()],
  password: [required()],
  check_password: [required()],
  code: [required()]
}

const loading = ref(false)

const loginRegister = async () => {
  const formRef = unref(elFormRef)
  formRef?.validate(async (valid) => {
    if (valid) {
      try {
        loading.value = true
      } finally {
        loading.value = false
      }
    }
  })
}
</script>
