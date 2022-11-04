<template>
  <ContentWrap>
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <template #toolbar_buttons>
        <XButton
          preIcon="ep:zoom-in"
          iTitle="action.add"
          v-hasPermi="['system:post:create']"
          @click="handleCreate()"
        />
      </template>
      <template #status_default="{ row }">
        <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
      </template>
      <template #action_default="{ row }">
        <XButton
          link
          preIcon="ep:edit"
          iTitle="action.edit"
          v-hasPermi="['system:post:update']"
          @click="handleUpdate(row.id)"
        />
        <XButton
          link
          preIcon="ep:view"
          iTitle="action.detail"
          v-hasPermi="['system:post:update']"
          @click="handleDetail(row)"
        />
        <XButton
          link
          preIcon="ep:delete"
          iTitle="action.del"
          v-hasPermi="['system:post:delete']"
          @click="handleDelete(row.id)"
        />
      </template>
    </vxe-grid>
  </ContentWrap>
  <XModal id="postModel" v-model="dialogVisible" :title="dialogTitle">
    <template #default>
      <!-- 对话框(添加 / 修改) -->
      <vxe-form
        ref="xForm"
        v-if="['create', 'update'].includes(actionType)"
        :data="formData"
        :items="formItems"
        :rules="rules"
      />
      <Descriptions
        v-if="actionType === 'detail'"
        :schema="allSchemas.detailSchema"
        :data="detailRef"
      >
        <template #status="{ row }">
          <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
        </template>
        <template #createTime="{ row }">
          <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
        </template>
      </Descriptions>
    </template>
    <template #footer>
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        :loading="actionLoading"
        iTitle="action.save"
        @click="submitForm"
      />
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        :loading="actionLoading"
        iTitle="dialog.close"
        @click="dialogVisible = false"
      />
    </template>
  </XModal>
</template>
<script setup lang="ts">
import { ref } from 'vue'
import dayjs from 'dayjs'
import { useI18n } from '@/hooks/web/useI18n'
import { VxeFormEvents, VxeFormInstance, VxeFormItemProps, VxeGridInstance } from 'vxe-table'
import * as PostApi from '@/api/system/post'
import { DICT_TYPE } from '@/utils/dict'
import { ContentWrap } from '@/components/ContentWrap'
import { PostVO } from '@/api/system/post/types'
import { rules, allSchemas } from './post.data'
import { useMessage } from '@/hooks/web/useMessage'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'

const { t } = useI18n() // 国际化
const message = useMessage()
const xGrid = ref<VxeGridInstance>()
const xForm = ref<VxeFormInstance>()
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 遮罩层

const gridOptions = useVxeGrid(allSchemas, PostApi.getPostPageApi)
const formData = ref<PostVO>()
const formItems = ref<VxeFormItemProps[]>(allSchemas.formSchema)
// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}
// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref
// 详情操作
const handleDetail = (row: PostVO) => {
  setDialogTile('detail')
  detailRef.value = row
}
// 新增操作
const handleCreate = () => {
  setDialogTile('create')
  xForm.value?.reset()
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await PostApi.getPostApi(rowId)
  formData.value = res
}
// 删除操作
const handleDelete = (rowId: number) => {
  message
    .confirm(t('common.delMessage'), t('common.confirmTitle'))
    .then(async () => {
      await PostApi.deletePostApi(rowId)
      message.success(t('common.delSuccess'))
    })
    .finally(() => {
      xGrid.value?.commitProxy('query')
    })
}
// 提交按钮
const submitForm: VxeFormEvents.Submit = async () => {
  actionLoading.value = true
  // 提交请求
  try {
    const data = formData.value as PostVO
    if (actionType.value === 'create') {
      await PostApi.createPostApi(data)
      message.success(t('common.createSuccess'))
    } else {
      await PostApi.updatePostApi(data)
      message.success(t('common.updateSuccess'))
    }
    // 操作成功，重新加载列表
    dialogVisible.value = false
  } finally {
    actionLoading.value = false
    xGrid.value?.commitProxy('query')
  }
}
</script>
