<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import type { FormRules, FormInstance } from 'element-plus'
import { ElForm, ElFormItem, ElInput, ElRadioGroup, ElRadio, ElMessage } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { getUserProfileApi, updateUserProfileApi } from '@/api/system/user/profile'
const { t } = useI18n()
const formRef = ref<FormInstance>()
interface BasicUserInfoVO {
  id: number
  nickname: string
  email: string
  mobile: string
  sex: number
}
interface userInfoType {
  basicUserInfo: BasicUserInfoVO
}
const user = reactive<userInfoType>({
  basicUserInfo: {
    id: 0,
    nickname: '',
    mobile: '',
    email: '',
    sex: 0
  }
})
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
const submit = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      await updateUserProfileApi({ params: user.basicUserInfo })
      ElMessage.success(t('common.updateSuccess'))
    }
  })
}
const reset = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  await getUserInfo()
}
const getUserInfo = async () => {
  const users = await getUserProfileApi()
  user.basicUserInfo = users
}
onMounted(async () => {
  await getUserInfo()
})
</script>
<template>
  <el-form ref="form" :model="user.basicUserInfo" :rules="rules" label-width="80px">
    <el-form-item :label="t('profile.user.nickname')" prop="nickname">
      <el-input v-model="user.basicUserInfo.nickname" />
    </el-form-item>
    <el-form-item :label="t('profile.user.mobile')" prop="mobile">
      <el-input v-model="user.basicUserInfo.mobile" maxlength="11" />
    </el-form-item>
    <el-form-item :label="t('profile.user.email')" prop="email">
      <el-input v-model="user.basicUserInfo.email" maxlength="50" />
    </el-form-item>
    <el-form-item :label="t('profile.user.sex')" prop="sex">
      <el-radio-group v-model="user.basicUserInfo.sex">
        <el-radio :label="1">{{ t('profile.user.man') }}</el-radio>
        <el-radio :label="2">{{ t('profile.user.woman') }}</el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="submit(formRef)">{{ t('common.save') }}</el-button>
      <el-button type="danger" @click="reset(formRef)">{{ t('common.reset') }}</el-button>
    </el-form-item>
  </el-form>
</template>
