<template>
  <ContentWrap>
    <!-- 表单设计器 -->
    <fc-designer ref="designer" height="780px">
      <template #handle>
        <XButton type="primary" :title="t('action.save')" @click="handleSave" />
      </template>
    </fc-designer>
    <!-- 表单保存的弹窗 -->
    <XModal v-model="dialogVisible" title="保存表单">
      <el-form ref="formRef" :model="formValues" :rules="formRules" label-width="80px">
        <el-form-item label="表单名" prop="name">
          <el-input v-model="formValues.name" placeholder="请输入表单名" />
        </el-form-item>
        <el-form-item label="开启状态" prop="status">
          <el-radio-group v-model="formValues.status">
            <el-radio
              v-for="dict in getIntDictOptions(DICT_TYPE.COMMON_STATUS)"
              :key="dict.value"
              :label="dict.value"
            >
              {{ dict.label }}
            </el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="formValues.remark" type="textarea" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <!-- 操作按钮 -->
      <template #footer>
        <!-- 按钮：保存 -->
        <XButton
          type="primary"
          :title="t('action.save')"
          :loading="dialogLoading"
          @click="submitForm"
        />
        <!-- 按钮：关闭 -->
        <XButton :title="t('dialog.close')" @click="dialogVisible = false" />
      </template>
    </XModal>
  </ContentWrap>
</template>
<script setup lang="ts" name="BpmFormEditor">
import { FormInstance } from 'element-plus'
import { DICT_TYPE, getIntDictOptions } from '@/utils/dict'
import { CommonStatusEnum } from '@/utils/constants'
import * as FormApi from '@/api/bpm/form'
import { encodeConf, encodeFields, setConfAndFields } from '@/utils/formCreate'
const { t } = useI18n() // 国际化
const message = useMessage() // 消息
const { query } = useRoute() // 路由

const designer = ref() // 表单设计器

const dialogVisible = ref(false) // 弹窗是否展示
const dialogLoading = ref(false) // 弹窗的加载中
const formRef = ref<FormInstance>()
const formRules = reactive({
  name: [{ required: true, message: '表单名不能为空', trigger: 'blur' }],
  status: [{ required: true, message: '开启状态不能为空', trigger: 'blur' }]
})
const formValues = ref({
  name: '',
  status: CommonStatusEnum.ENABLE,
  remark: ''
})

// 处理保存按钮
const handleSave = () => {
  dialogVisible.value = true
}

// 提交保存表单
const submitForm = async () => {
  // 参数校验
  const elForm = unref(formRef)
  if (!elForm) return
  const valid = await elForm.validate()
  if (!valid) return

  // 提交请求
  dialogLoading.value = true
  try {
    const data = formValues.value as FormApi.FormVO
    data.conf = encodeConf(designer) // 表单配置
    data.fields = encodeFields(designer) // 表单字段
    if (!data.id) {
      await FormApi.createFormApi(data)
      message.success(t('common.createSuccess'))
    } else {
      await FormApi.updateFormApi(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
  } finally {
    dialogLoading.value = false
  }
}

// ========== 初始化 ==========
onMounted(() => {
  // 场景一：新增表单
  const id = query.id as unknown as number
  if (!id) {
    return
  }
  // 场景二：修改表单
  FormApi.getFormApi(id).then((data) => {
    formValues.value = data
    setConfAndFields(designer, data.conf, data.fields)
  })
})
</script>
