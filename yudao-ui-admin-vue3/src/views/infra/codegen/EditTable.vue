<script setup lang="ts">
import { ref, unref, onMounted } from 'vue'
import { ContentDetailWrap } from '@/components/ContentDetailWrap'
import { BasicInfoForm, CloumInfoForm, GenInfoForm } from './components'
import { ElTabs, ElTabPane, ElButton, ElMessage } from 'element-plus'
import { getCodegenTableApi, updateCodegenTableApi } from '@/api/infra/codegen'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from '@/hooks/web/useI18n'
import { CodegenTableVO, CodegenColumnVO, CodegenUpdateReqVO } from '@/api/infra/codegen/types'
const { t } = useI18n()
const { push } = useRouter()
const { query } = useRoute()
const tableCurrentRow = ref<CodegenTableVO>()
const cloumCurrentRow = ref<CodegenColumnVO[]>()
const getList = async () => {
  const id = query.id as unknown as number
  if (id) {
    // 获取表详细信息
    const res = await getCodegenTableApi(id)
    tableCurrentRow.value = res.table
    cloumCurrentRow.value = res.columns
  }
}
const loading = ref(false)
const activeName = ref('cloum')
const basicInfoRef = ref<ComponentRef<typeof BasicInfoForm>>()
const genInfoRef = ref<ComponentRef<typeof GenInfoForm>>()
const cloumInfoRef = ref(null)
const submitForm = async () => {
  const basicInfo = unref(basicInfoRef)
  const genInfo = unref(genInfoRef)
  const basicForm = await basicInfo?.elFormRef?.validate()?.catch(() => {})
  const genForm = await genInfo?.elFormRef?.validate()?.catch(() => {})
  if (basicForm && genForm) {
    const basicInfoData = (await basicInfo?.getFormData()) as CodegenTableVO
    const genInfoData = (await genInfo?.getFormData()) as CodegenTableVO
    const genTable: CodegenUpdateReqVO = {
      table: Object.assign({}, basicInfoData, genInfoData),
      columns: cloumCurrentRow.value
    }
    await updateCodegenTableApi(genTable)
    ElMessage.success(t('common.updateSuccess'))
    push('/infra/codegen')
  }
}
onMounted(() => {
  getList()
})
</script>
<template>
  <ContentDetailWrap title="代码生成" @back="push('/infra/codegen')">
    <el-tabs v-model="activeName">
      <el-tab-pane label="基本信息" name="basicInfo">
        <BasicInfoForm ref="basicInfoRef" :basicInfo="tableCurrentRow" />
      </el-tab-pane>
      <el-tab-pane label="字段信息" name="cloum">
        <CloumInfoForm ref="cloumInfoRef" :info="cloumCurrentRow" />
      </el-tab-pane>
      <el-tab-pane label="生成信息" name="genInfo">
        <GenInfoForm ref="genInfoRef" :genInfo="tableCurrentRow" />
      </el-tab-pane>
    </el-tabs>
    <template #right>
      <el-button type="primary" :loading="loading" @click="submitForm">
        {{ t('action.save') }}
      </el-button>
    </template>
  </ContentDetailWrap>
</template>
