<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #toolbar_buttons>
        <!-- 操作：标记已读 -->
        <XButton type="primary" preIcon="ep:zoom-in" title="标记已读" @click="handleUpdateList" />
        <!-- 操作：全部已读 -->
        <XButton type="primary" preIcon="ep:zoom-in" title="全部已读" @click="handleUpdateAll" />
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：已读 -->
        <XTextButton
          preIcon="ep:view"
          title="已读"
          v-hasPermi="['system:notify-message:query']"
          v-if="!row.readStatus"
          @click="handleUpdate([row.id])"
        />
      </template>
    </XTable>
  </ContentWrap>
</template>
<script setup lang="ts" name="MyNotifyMessage">
// 业务相关的 import
import { allSchemas } from './my.data'
import * as NotifyMessageApi from '@/api/system/notify/message'

const message = useMessage() // 消息

// 列表相关的变量
const [registerTable, { reload, getCheckboxRecords }] = useXTable({
  allSchemas: allSchemas,
  getListApi: NotifyMessageApi.getMyNotifyMessagePage
})

const handleUpdateList = async () => {
  const list = getCheckboxRecords()
  if (list.length === 0) {
    return
  }
  await handleUpdate(list.map((v) => v.id))
}

// 标记指定 id 已读
const handleUpdate = async (ids) => {
  await NotifyMessageApi.updateNotifyMessageRead(ids)
  message.success('标记已读成功！')
  reload()
}

// 标记全部已读
const handleUpdateAll = async () => {
  await NotifyMessageApi.updateAllNotifyMessageRead()
  message.success('全部已读成功！')
  reload()
}
</script>
