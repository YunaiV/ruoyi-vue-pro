<script setup lang="ts">
import { ref, unref, onMounted } from 'vue'
import { ContentDetailWrap } from '@/components/ContentDetailWrap'
import BasicInfoForm from './BasicInfoForm.vue'
import CloumInfoFormVue from './CloumInfoForm.vue'
import GenInfoFormVue from './GenInfoForm.vue'
import { ElTabs, ElTabPane, ElButton } from 'element-plus'
import { getCodegenTableApi } from '@/api/infra/codegen'
import { useRouter, useRoute } from 'vue-router'
import { useI18n } from '@/hooks/web/useI18n'
import { CodegenColumnVO, CodegenTableVO } from '@/api/infra/codegen/types'
const { t } = useI18n()
const { push } = useRouter()
const { query } = useRoute()
const tableCurrentRow = ref<Nullable<CodegenTableVO>>(null)
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
const genInfoRef = ref<ComponentRef<typeof GenInfoFormVue>>()
const submitForm = async () => {
  const basicInfo = unref(basicInfoRef)
  const genInfo = unref(genInfoRef)
  const basicValidate = await basicInfo?.elFormRef?.validate()?.catch(() => {})
  const genValidate = await genInfo?.elFormRef?.validate()?.catch(() => {})
  if (basicValidate && genValidate) {
    const basicInfoData = (await basicInfo?.getFormData()) as CodegenTableVO
    const genInfoData = (await genInfo?.getFormData()) as CodegenTableVO
    console.info(basicInfoData)
    console.info(genInfoData)
  }
}
onMounted(() => {
  getList()
})
</script>
<template>
  <ContentDetailWrap title="代码生成" @back="push('/infra/codegen')">
    <el-tabs v-model="activeName">
      <el-tab-pane label="基本信息" name="basic">
        <BasicInfoForm ref="basicInfoRef" :current-row="tableCurrentRow" />
      </el-tab-pane>
      <el-tab-pane label="字段信息" name="cloum">
        <CloumInfoFormVue ref="cloumInfoRef" :current-row="cloumCurrentRow" />
      </el-tab-pane>
      <el-tab-pane label="生成信息" name="genInfo">
        <GenInfoFormVue ref="basicInfoRef" :current-row="tableCurrentRow" />
      </el-tab-pane>
    </el-tabs>
    <template #right>
      <el-button type="primary" :loading="loading" @click="submitForm">
        {{ t('action.save') }}
      </el-button>
    </template>
  </ContentDetailWrap>
</template>
