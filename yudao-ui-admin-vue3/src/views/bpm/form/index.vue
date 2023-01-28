<template>
  <ContentWrap>
    <!-- 列表 -->
    <div>
      <XTable @register="registerTable">
        <!-- 操作：新增 -->
        <template #toolbar_buttons>
          <XButton
            type="primary"
            preIcon="ep:zoom-in"
            :title="t('action.add')"
            v-hasPermi="['system:post:create']"
            @click="handleCreate()"
          />
        </template>
        <template #actionbtns_default="{ row }">
          <!-- 操作：修改 -->
          <XTextButton
            preIcon="ep:edit"
            :title="t('action.edit')"
            v-hasPermi="['bpm:form:update']"
            @click="handleUpdate(row.id)"
          />
          <!-- 操作：详情 -->
          <XTextButton
            preIcon="ep:view"
            :title="t('action.detail')"
            v-hasPermi="['bpm:form:query']"
            @click="handleDetail(row.id)"
          />
          <!-- 操作：删除 -->
          <XTextButton
            preIcon="ep:delete"
            :title="t('action.del')"
            v-hasPermi="['bpm:form:delete']"
            @click="deleteData(row.id)"
          />
        </template>
      </XTable>
      <!-- 表单详情的弹窗 -->
      <XModal v-model="detailOpen" width="800" title="表单详情">
        <form-create :rule="detailPreview.rule" :option="detailPreview.option" v-if="detailOpen" />
      </XModal>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts" name="BpmForm">
// 全局相关的 import
// 业务相关的 import
import * as FormApi from '@/api/bpm/form'
import { allSchemas } from './form.data'
// 表单详情相关的变量和 import
import { setConfAndFields2 } from '@/utils/formCreate'

const { t } = useI18n() // 国际化
const router = useRouter() // 路由

// 列表相关的变量
const [registerTable, { deleteData }] = useXTable({
  allSchemas: allSchemas,
  getListApi: FormApi.getFormPageApi,
  deleteApi: FormApi.deleteFormApi
})

// 新增操作
const handleCreate = () => {
  router.push({
    name: 'bpmFormEditor'
  })
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  await router.push({
    name: 'bpmFormEditor',
    query: {
      id: rowId
    }
  })
}

// 详情操作
const detailOpen = ref(false)
const detailPreview = ref({
  rule: [],
  option: {}
})
const handleDetail = async (rowId: number) => {
  // 设置表单
  const data = await FormApi.getFormApi(rowId)
  setConfAndFields2(detailPreview, data.conf, data.fields)
  // 弹窗打开
  detailOpen.value = true
}
</script>
