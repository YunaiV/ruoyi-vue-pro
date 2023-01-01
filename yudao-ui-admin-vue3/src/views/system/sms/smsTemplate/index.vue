<template>
  <ContentWrap>
    <!-- 列表 -->
    <vxe-grid ref="xGrid" v-bind="gridOptions" class="xtable-scrollbar">
      <!-- 操作：新增 -->
      <template #toolbar_buttons>
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          :title="t('action.add')"
          v-hasPermi="['system:sms-channel:create']"
          @click="handleCreate()"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <XTextButton
          preIcon="ep:cpu"
          :title="t('action.test')"
          v-hasPermi="['system:sms-template:send-sms']"
          @click="handleSendSms(row)"
        />
        <!-- 操作：修改 -->
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['system:sms-template:update']"
          @click="handleUpdate(row.id)"
        />
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['system:sms-template:query']"
          @click="handleDetail(row.id)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['system:sms-template:delete']"
          @click="handleDelete(row.id)"
        />
      </template>
    </vxe-grid>
  </ContentWrap>
  <XModal id="smsTemplate" v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(添加 / 修改) -->
    <Form
      v-if="['create', 'update'].includes(actionType)"
      :schema="allSchemas.formSchema"
      :rules="rules"
      ref="formRef"
    />
    <!-- 对话框(详情) -->
    <Descriptions
      v-if="actionType === 'detail'"
      :schema="allSchemas.detailSchema"
      :data="detailData"
    />
    <!-- 操作按钮 -->
    <template #footer>
      <!-- 按钮：保存 -->
      <XButton
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :title="t('action.save')"
        :loading="actionLoading"
        @click="submitForm()"
      />
      <!-- 按钮：关闭 -->
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
  <XModal id="sendTest" v-model="sendVisible" title="测试">
    <el-form :model="sendSmsForm" :rules="sendSmsRules" label-width="200px" label-position="top">
      <el-form-item label="模板内容" prop="content">
        <el-input
          v-model="sendSmsForm.content"
          type="textarea"
          placeholder="请输入模板内容"
          readonly
        />
      </el-form-item>
      <el-form-item label="手机号" prop="mobile">
        <el-input v-model="sendSmsForm.mobile" placeholder="请输入手机号" />
      </el-form-item>
      <el-form-item
        v-for="param in sendSmsForm.params"
        :key="param"
        :label="'参数 {' + param + '}'"
        :prop="'templateParams.' + param"
      >
        <el-input
          v-model="sendSmsForm.templateParams[param]"
          :placeholder="'请输入 ' + param + ' 参数'"
        />
      </el-form-item>
    </el-form>
    <!-- 操作按钮 -->
    <template #footer>
      <XButton
        type="primary"
        :title="t('action.test')"
        :loading="actionLoading"
        @click="sendSmsTest()"
      />
      <XButton :title="t('dialog.close')" @click="sendVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="SmsTemplate">
// 全局相关的 import
import { ref, unref } from 'vue'
import { useI18n } from '@/hooks/web/useI18n'
import { useMessage } from '@/hooks/web/useMessage'
import { useVxeGrid } from '@/hooks/web/useVxeGrid'
import { VxeGridInstance } from 'vxe-table'
import { FormExpose } from '@/components/Form'
import { ElForm, ElFormItem, ElInput } from 'element-plus'
// 业务相关的 import
import * as SmsTemplateApi from '@/api/system/sms/smsTemplate'
import { rules, allSchemas } from './sms.template.data'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

// 列表相关的变量
const xGrid = ref<VxeGridInstance>() // 列表 Grid Ref
const { gridOptions, getList, deleteData } = useVxeGrid<SmsTemplateApi.SmsTemplateVO>({
  allSchemas: allSchemas,
  getListApi: SmsTemplateApi.getSmsTemplatePageApi,
  deleteApi: SmsTemplateApi.deleteSmsTemplateApi
})

// 弹窗相关的变量
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionType = ref('') // 操作按钮的类型
const actionLoading = ref(false) // 按钮 Loading
const formRef = ref<FormExpose>() // 表单 Ref
const detailData = ref() // 详情 Ref

// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = () => {
  setDialogTile('create')
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  setDialogTile('update')
  // 设置数据
  const res = await SmsTemplateApi.getSmsTemplateApi(rowId)
  unref(formRef)?.setValues(res)
}

// 详情操作
const handleDetail = async (rowId: number) => {
  setDialogTile('detail')
  // 设置数据
  const res = await SmsTemplateApi.getSmsTemplateApi(rowId)
  detailData.value = res
}

// 删除操作
const handleDelete = async (rowId: number) => {
  await deleteData(xGrid, rowId)
}

// 提交按钮
const submitForm = async () => {
  const elForm = unref(formRef)?.getElFormRef()
  if (!elForm) return
  elForm.validate(async (valid) => {
    if (valid) {
      actionLoading.value = true
      // 提交请求
      try {
        const data = unref(formRef)?.formModel as SmsTemplateApi.SmsTemplateVO
        if (actionType.value === 'create') {
          await SmsTemplateApi.createSmsTemplateApi(data)
          message.success(t('common.createSuccess'))
        } else {
          await SmsTemplateApi.updateSmsTemplateApi(data)
          message.success(t('common.updateSuccess'))
        }
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
        // 刷新列表
        await getList(xGrid)
      }
    }
  })
}

// ========== 测试相关 ==========
const sendSmsForm = ref({
  content: '',
  params: {},
  mobile: '',
  templateCode: '',
  templateParams: {}
})
const sendSmsRules = ref({
  mobile: [{ required: true, message: '手机不能为空', trigger: 'blur' }],
  templateCode: [{ required: true, message: '手机不能为空', trigger: 'blur' }],
  templateParams: {}
})
const sendVisible = ref(false)

const handleSendSms = (row: any) => {
  sendSmsForm.value.content = row.content
  sendSmsForm.value.params = row.params
  sendSmsForm.value.templateCode = row.code
  sendSmsForm.value.templateParams = row.params.reduce(function (obj, item) {
    obj[item] = undefined
    return obj
  }, {})
  sendSmsRules.value.templateParams = row.params.reduce(function (obj, item) {
    obj[item] = { required: true, message: '参数 ' + item + ' 不能为空', trigger: 'change' }
    return obj
  }, {})
  sendVisible.value = true
}

const sendSmsTest = async () => {
  const data: SmsTemplateApi.SendSmsReqVO = {
    mobile: sendSmsForm.value.mobile,
    templateCode: sendSmsForm.value.templateCode,
    templateParams: sendSmsForm.value.templateParams as unknown as Map<string, Object>
  }
  const res = await SmsTemplateApi.sendSmsApi(data)
  if (res) {
    message.success('发送成功')
  }
  sendVisible.value = false
}
</script>
