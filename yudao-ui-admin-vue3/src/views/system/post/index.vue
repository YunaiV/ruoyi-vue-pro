<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #toolbar_buttons>
        <!-- 操作：新增 -->
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['system:post:create']"
          @click="openModal('create')"
        />
        <!-- 操作：导出 -->
        <XButton
          type="warning"
          preIcon="ep:download"
          :title="t('action.export')"
          v-hasPermi="['system:post:export']"
          @click="exportList('岗位列表.xls')"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：修改 -->
        <XTextButton
          preIcon="ep:edit"
          v-hasPermi="['system:post:update']"
          @click="openModal('update', row.id)"
        />
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          v-hasPermi="['system:post:query']"
          @click="openModal('detail', row.id)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          v-hasPermi="['system:post:delete']"
          @click="deleteData(row.id)"
        />
      </template>
    </XTable>
  </ContentWrap>
  <PostForm ref="modalRef" @success="reload()" />
</template>
<script setup lang="ts" name="Post">
// 业务相关的 import
import * as PostApi from '@/api/system/post'
import { allSchemas } from './post.data'
import PostForm from './PostForm.vue'

const { t } = useI18n() // 国际化
const modalRef = ref()
// 列表相关的变量
const [registerTable, { reload, deleteData, exportList }] = useXTable({
  allSchemas: allSchemas,
  getListApi: PostApi.getPostPageApi,
  deleteApi: PostApi.deletePostApi,
  exportListApi: PostApi.exportPostApi
})

const openModal = (type: string, rowId?: number) => {
  modalRef.value.openModal(type, rowId)
}
</script>
