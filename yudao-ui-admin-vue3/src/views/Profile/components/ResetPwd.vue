<script setup lang="ts">
import { InputPassword } from '@/components/InputPassword'
import { ElForm, ElFormItem, ElMessage } from 'element-plus'
import type { FormRules, FormInstance } from 'element-plus'
import { updateUserPwdApi } from '@/api/system/user/profile'
import { ref, reactive } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
const { t } = useI18n()
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
      ElMessage.success(t('common.updateSuccess'))
    }
  })
}
const reset = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
}
</script>
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
      <el-button type="primary" @click="submit(formRef)">{{ t('common.save') }}</el-button>
      <el-button type="danger" @click="reset(formRef)">{{ t('common.reset') }}</el-button>
    </el-form-item>
  </el-form>
</template>
