<template>
  <ContentWrap>
    <ContentDetailWrap :title="title" @back="push('/infra/codegen')">
      <el-tabs v-model="activeName">
        <el-tab-pane label="基本信息" name="basicInfo">
          <BasicInfoForm ref="basicInfoRef" :basicInfo="tableCurrentRow" />
        </el-tab-pane>
        <el-tab-pane label="字段信息" name="cloum">
          <CloumInfoForm ref="cloumInfoRef" :info="cloumCurrentRow" />
        </el-tab-pane>
      </el-tabs>
      <template #right>
        <XButton
          type="primary"
          :title="t('action.save')"
          :loading="loading"
          @click="submitForm()"
        />
      </template>
    </ContentDetailWrap>
  </ContentWrap>
</template>
<script setup lang="ts">
import { BasicInfoForm, CloumInfoForm } from './components'
import { getCodegenTableApi, updateCodegenTableApi } from '@/api/infra/codegen'
import { CodegenTableVO, CodegenColumnVO, CodegenUpdateReqVO } from '@/api/infra/codegen/types'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const { push } = useRouter()
const { query } = useRoute()
const loading = ref(false)
const title = ref('代码生成')
const activeName = ref('basicInfo')
const cloumInfoRef = ref(null)
const tableCurrentRow = ref<CodegenTableVO>()
const cloumCurrentRow = ref<CodegenColumnVO[]>([])
const basicInfoRef = ref<ComponentRef<typeof BasicInfoForm>>()

const getList = async () => {
  const id = query.id as unknown as number
  if (id) {
    // 获取表详细信息
    const res = await getCodegenTableApi(id)
    title.value = '修改[ ' + res.table.tableName + ' ]生成配置'
    tableCurrentRow.value = res.table
    cloumCurrentRow.value = res.columns
  }
}
const submitForm = async () => {
  const basicInfo = unref(basicInfoRef)
  const basicForm = await basicInfo?.elFormRef?.validate()?.catch(() => {})
  if (basicForm) {
    const basicInfoData = (await basicInfo?.getFormData()) as CodegenTableVO
    const genTable: CodegenUpdateReqVO = {
      table: basicInfoData,
      columns: cloumCurrentRow.value
    }
    await updateCodegenTableApi(genTable)
    message.success(t('common.updateSuccess'))
    push('/infra/codegen')
  }
}
onMounted(() => {
  getList()
})
</script>
