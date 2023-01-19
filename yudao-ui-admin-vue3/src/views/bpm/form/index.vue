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
      <!-- TODO 芋艿：待完善 -->
      <XModal v-model="detailOpen" width="400" title="表单详情">
        <!-- <div class="test-form">
          <parser :key="new Date().getTime()" :form-conf="detailForm" />
        </div> -->
        表单详情
      </XModal>
    </div>
  </ContentWrap>
</template>

<script setup lang="ts" name="BpmForm">
// 全局相关的 import
// import { ref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { useXTable } from '@/hooks/web/useXTable'
// import { FormExpose } from '@/components/Form'
// 业务相关的 import
import * as FormApi from '@/api/bpm/form'
import { allSchemas } from './form.data'
import { useRouter } from 'vue-router'
// import { decodeFields } from '@/utils/formGenerator' // TODO 芋艿：可能要清理下

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const router = useRouter() // 路由

// 列表相关的变量
const [registerTable, { deleteData }] = useXTable({
  allSchemas: allSchemas,
  getListApi: FormApi.getFormPageApi,
  deleteApi: FormApi.deleteFormApi
})

// 弹窗相关的变量
// const modelVisible = ref(false) // 是否显示弹出层
// const modelTitle = ref('edit') // 弹出层标题
// const modelLoading = ref(false) // 弹出层loading
// const actionType = ref('') // 操作按钮的类型
// const actionLoading = ref(false) // 按钮 Loading
// const formRef = ref<FormExpose>() // 表单 Ref
// const detailData = ref() // 详情 Ref

// 设置标题
// const setDialogTile = (type: string) => {
//   modelLoading.value = true
//   modelTitle.value = t('action.' + type)
//   actionType.value = type
//   modelVisible.value = true
// }

// 新增操作
const handleCreate = () => {
  router.push({
    name: 'bpmFormEditor'
  })
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  console.log(rowId, '修改')
  if (true) {
    message.success('动态表单开发中，预计 2 月底完成')
    return
  }
  await router.push({
    path: '/bpm/manager/form/edit',
    query: {
      formId: rowId
    }
  })
}

// 详情操作 // TODO 芋艿：详情的实现
// const detailForm = ref({
//   fields: []
// })
// 表单详情弹出窗显示隐藏
// const detailOpen = ref(false)

const handleDetail = async (rowId: number) => {
  console.log(rowId, '详情')
  message.success('动态表单开发中，预计 2 月底完成')
  // FormApi.getFormApi(row.id).then((response) => {
  //   // 设置值
  //   const data = response.data
  //   detailForm.value = {
  //     ...JSON.parse(data.conf),
  //     fields: decodeFields([], data.fields)
  //   }
  //   // 弹窗打开
  //   detailOpen.value = true
  // })
}
</script>
