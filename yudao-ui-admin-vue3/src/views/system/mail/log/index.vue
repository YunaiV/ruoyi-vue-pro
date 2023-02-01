<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #accountId_search>
        <el-select v-model="queryParams.accountId">
          <el-option :key="undefined" label="全部" :value="undefined" />
          <el-option
            v-for="item in accountOptions"
            :key="item.id"
            :label="item.mail"
            :value="item.id"
          />
        </el-select>
      </template>
      <template #toMail_default="{ row }">
        <div>{{ row.toMail }}</div>
        <div v-if="row.userType && row.userId">
          <DictTag :type="DICT_TYPE.USER_TYPE" :value="row.userType" />{{ '(' + row.userId + ')' }}
        </div>
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['system:mail-log:query']"
          @click="handleDetail(row.id)"
        />
      </template>
    </XTable>
  </ContentWrap>
  <!-- 弹窗 -->
  <XModal id="mailLogModel" :loading="modelLoading" v-model="modelVisible" :title="modelTitle">
    <!-- 表单：详情 -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailData"
    />
    <template #footer>
      <!-- 按钮：关闭 -->
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="modelVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="MailLog">
// 业务相关的 import
import { DICT_TYPE } from '@/utils/dict'
import { allSchemas } from './log.data'
import * as MailLogApi from '@/api/system/mail/log'
import * as MailAccountApi from '@/api/system/mail/account'

const { t } = useI18n() // 国际化

// 列表相关的变量
const queryParams = reactive({
  accountId: null
})
const [registerTable] = useXTable({
  allSchemas: allSchemas,
  params: queryParams,
  getListApi: MailLogApi.getMailLogPageApi
})
const accountOptions = ref<any[]>([]) // 账号下拉选项

// 弹窗相关的变量
const modelVisible = ref(false) // 是否显示弹出层
const modelTitle = ref('edit') // 弹出层标题
const modelLoading = ref(false) // 弹出层loading
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 按钮 Loading
const detailData = ref() // 详情 Ref

// 设置标题
const setDialogTile = (type: string) => {
  modelLoading.value = true
  modelTitle.value = t('action.' + type)
  actionType.value = type
  modelVisible.value = true
}

// 详情操作
const handleDetail = async (rowId: number) => {
  setDialogTile('detail')
  const res = await MailLogApi.getMailLogApi(rowId)
  detailData.value = res
  modelLoading.value = false
}

// ========== 初始化 ==========
onMounted(() => {
  MailAccountApi.getSimpleMailAccounts().then((data) => {
    accountOptions.value = data
  })
})
</script>
