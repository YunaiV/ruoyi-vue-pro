<script setup lang="ts">
import { ref } from 'vue'
import dayjs from 'dayjs'
import { useTable } from '@/hooks/web/useTable'
import { allSchemas } from './loginLog.data'
import { DICT_TYPE } from '@/utils/dict'
import type { LoginLogVO } from '@/api/system/loginLog/types'
import { getLoginLogPageApi, exportLoginLogApi } from '@/api/system/loginLog'
import { useI18n } from '@/hooks/web/useI18n'

const { t } = useI18n() // 国际化
// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<LoginLogVO>({
  getListApi: getLoginLogPageApi,
  exportListApi: exportLoginLogApi
})
const { getList, setSearchParams } = methods
// 详情操作
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref(t('action.detail')) // 弹出层标题
const detailRef = ref() // 详情 Ref
const handleDetail = async (row: LoginLogVO) => {
  // 设置数据
  detailRef.value = row
  dialogVisible.value = true
}
getList()
</script>
<template>
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @search="setSearchParams" @reset="setSearchParams" />
  </ContentWrap>
  <ContentWrap>
    <Table
      :columns="allSchemas.tableColumns"
      :selection="false"
      :data="tableObject.tableList"
      :loading="tableObject.loading"
      :pagination="{
        total: tableObject.total
      }"
      v-model:pageSize="tableObject.pageSize"
      v-model:currentPage="tableObject.currentPage"
      @register="register"
    >
      <template #logType="{ row }">
        <DictTag :type="DICT_TYPE.SYSTEM_LOGIN_TYPE" :value="row.logType" />
      </template>
      <template #result="{ row }">
        <DictTag :type="DICT_TYPE.SYSTEM_LOGIN_RESULT" :value="row.result" />
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #action="{ row }">
        <el-button link type="primary" @click="handleDetail(row)">
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
      </template>
    </Table>
  </ContentWrap>
  <Dialog v-model="dialogVisible" :title="dialogTitle" maxHeight="500px" width="50%">
    <!-- 对话框(详情) -->
    <Descriptions :schema="allSchemas.detailSchema" :data="detailRef">
      <template #logType="{ row }">
        <DictTag :type="DICT_TYPE.SYSTEM_LOGIN_TYPE" :value="row.logType" />
      </template>
      <template #result="{ row }">
        <DictTag :type="DICT_TYPE.SYSTEM_LOGIN_RESULT" :value="row.result" />
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
</template>
