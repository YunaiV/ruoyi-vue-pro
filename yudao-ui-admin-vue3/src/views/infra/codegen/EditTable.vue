<template>
  <ContentDetailWrap :title="title" @back="push('/infra/codegen')">
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
      <XButton type="primary" :title="t('action.save')" :loading="loading" @click="submitForm()" />
    </template>
  </ContentDetailWrap>
</template>
<script setup lang="ts">
import { ref, unref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElTabs, ElTabPane } from 'element-plus'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { ContentDetailWrap } from '@/components/ContentDetailWrap'
import { BasicInfoForm, CloumInfoForm, GenInfoForm } from './components'
import { getCodegenTableApi, updateCodegenTableApi } from '@/api/infra/codegen'
import { CodegenTableVO, CodegenColumnVO, CodegenUpdateReqVO } from '@/api/infra/codegen/types'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const { push } = useRouter()
const { query } = useRoute()
const loading = ref(false)
const title = ref('代码生成')
const activeName = ref('cloum')
const cloumInfoRef = ref(null)
const tableCurrentRow = ref<CodegenTableVO>()
const cloumCurrentRow = ref<CodegenColumnVO[]>([])
const basicInfoRef = ref<ComponentRef<typeof BasicInfoForm>>()
const genInfoRef = ref<ComponentRef<typeof GenInfoForm>>()

const getList = async () => {
  const id = query.id as unknown as number
  if (id) {
    // 获取表详细信息
    const res = await getCodegenTableApi(id)
    tableCurrentRow.value = res.table
    title.value = '修改[ ' + res.table.tableName + ' ]生成配置'
    cloumCurrentRow.value = res.columns
  }
}
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
    message.success(t('common.updateSuccess'))
    push('/infra/codegen')
  }
}
onMounted(() => {
  getList()
})
</script>
