<script setup lang="ts">
import { Form } from '@/components/Form'
import { computed, reactive, ref, unref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useForm } from '@/hooks/web/useForm'
import { ElInput, FormRules } from 'element-plus'
import { useValidator } from '@/hooks/web/useValidator'
import { useLoginState, LoginStateEnum } from './useLogin'
import LoginFormTitle from './LoginFormTitle.vue'
import { FormSchema } from '@/types/form'

const { register, elFormRef } = useForm()
const { handleBackLogin, getLoginState } = useLoginState()
const getShow = computed(() => unref(getLoginState) === LoginStateEnum.REGISTER)
const { t } = useI18n()

const { required } = useValidator()

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
        <el-button type="primary" class="w-[100%]" :loading="loading" @click="loginRegister">
          {{ t('login.register') }}
        </el-button>
      </div>
      <div class="w-[100%] mt-15px">
        <el-button class="w-[100%]" @click="handleBackLogin">
          {{ t('login.hasUser') }}
        </el-button>
      </div>
    </template>
  </Form>
</template>
