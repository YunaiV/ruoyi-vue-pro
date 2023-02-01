<template>
  <el-form ref="formRef" :model="password" :rules="rules" label-width="80px">
    <el-form-item :label="t('profile.password.oldPassword')">
      <InputPassword v-model="password.oldPassword" />
    </el-form-item>
    <el-form-item :label="t('profile.password.newPassword')">
      <InputPassword v-model="password.newPassword" strength />
    </el-form-item>
    <el-form-item :label="t('profile.password.confirmPassword')">
      <InputPassword v-model="password.confirmPassword" strength />
    </el-form-item>
    <el-form-item>
      <XButton type="primary" @click="submit(formRef)" :title="t('common.save')" />
      <XButton type="danger" :title="t('common.reset')" @click="reset(formRef)" />
    </el-form-item>
  </el-form>
</template>
<script setup lang="ts">
import type { FormRules, FormInstance } from 'element-plus'

import { InputPassword } from '@/components/InputPassword'
import { updateUserPwdApi } from '@/api/system/user/profile'

const { t } = useI18n()
const message = useMessage()
const formRef = ref<FormInstance>()
const password = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单校验
const equalToPassword = (value, callback) => {
  if (password.newPassword !== value) {
    callback(new Error(t('profile.password.diffPwd')))
  } else {
    callback()
  }
}
const rules = reactive<FormRules>({
  oldPassword: [
    { required: true, message: t('profile.password.oldPwdMsg'), trigger: 'blur' },
    { min: 3, max: 5, message: t('profile.password.pwdRules'), trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: t('profile.password.newPwdMsg'), trigger: 'blur' },
    { min: 6, max: 20, message: t('profile.password.pwdRules'), trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: t('profile.password.cfPwdMsg'), trigger: 'blur' },
    { required: true, validator: equalToPassword, trigger: 'blur' }
  ]
})
const submit = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      await updateUserPwdApi(password.oldPassword, password.newPassword)
      message.success(t('common.updateSuccess'))
    }
  })
}
const reset = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
}
</script>
