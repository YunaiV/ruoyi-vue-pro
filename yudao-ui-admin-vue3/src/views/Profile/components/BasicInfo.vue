<template>
  <Form ref="formRef" :rules="rules" :schema="schema" :labelWidth="80">
    <template #sex="form">
      <el-radio-group v-model="form['sex']">
        <el-radio :label="1">{{ t('profile.user.man') }}</el-radio>
        <el-radio :label="2">{{ t('profile.user.woman') }}</el-radio>
      </el-radio-group>
    </template>
  </Form>
  <XButton :title="t('common.save')" @click="submit()" />
  <XButton type="danger" :title="t('common.reset')" @click="init()" />
</template>
<script setup lang="ts">
import type { FormRules } from 'element-plus'
import { ElMessage } from 'element-plus'

import { FormSchema } from '@/types/form'
import type { FormExpose } from '@/components/Form'
import {
  getUserProfileApi,
  updateUserProfileApi,
  UserProfileUpdateReqVO
} from '@/api/system/user/profile'

const { t } = useI18n()
// 表单校验
const rules = reactive<FormRules>({
  nickname: [{ required: true, message: t('profile.rules.nickname'), trigger: 'blur' }],
  email: [
    { required: true, message: t('profile.rules.mail'), trigger: 'blur' },
    {
      type: 'email',
      message: t('profile.rules.truemail'),
      trigger: ['blur', 'change']
    }
  ],
  mobile: [
    { required: true, message: t('profile.rules.phone'), trigger: 'blur' },
    {
      pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
      message: t('profile.rules.truephone'),
      trigger: 'blur'
    }
  ]
})
const schema = reactive<FormSchema[]>([
  {
    field: 'nickname',
    label: t('profile.user.nickname'),
    component: 'Input'
  },
  {
    field: 'mobile',
    label: t('profile.user.mobile'),
    component: 'Input'
  },
  {
    field: 'email',
    label: t('profile.user.email'),
    component: 'Input'
  },
  {
    field: 'sex',
    label: t('profile.user.sex'),
    component: 'InputNumber',
    value: 0
  }
])
const formRef = ref<FormExpose>() // 表单 Ref
const submit = () => {
  const elForm = unref(formRef)?.getElFormRef()
  if (!elForm) return
  elForm.validate(async (valid) => {
    if (valid) {
      const data = unref(formRef)?.formModel as UserProfileUpdateReqVO
      await updateUserProfileApi(data)
      ElMessage.success(t('common.updateSuccess'))
      await init()
    }
  })
}
const init = async () => {
  const res = await getUserProfileApi()
  unref(formRef)?.setValues(res)
}
onMounted(async () => {
  await init()
})
</script>
