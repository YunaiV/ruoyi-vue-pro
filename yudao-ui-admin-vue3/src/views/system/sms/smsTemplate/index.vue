<script setup lang="ts">
import { ref, unref } from 'vue'
import dayjs from 'dayjs'
import { ElMessage, ElForm, ElFormItem, ElInput } from 'element-plus'
import { DICT_TYPE } from '@/utils/dict'
import { useTable } from '@/hooks/web/useTable'
import { useI18n } from '@/hooks/web/useI18n'
import { FormExpose } from '@/components/Form'
import type { SmsTemplateVO } from '@/api/system/sms/smsTemplate/types'
import { rules, allSchemas } from './sms.template.data'
import * as SmsTemplateApi from '@/api/system/sms/smsTemplate'
const { t } = useI18n() // 国际化

// ========== 列表相关 ==========
const { register, tableObject, methods } = useTable<SmsTemplateVO>({
  getListApi: SmsTemplateApi.getSmsTemplatePageApi,
  delListApi: SmsTemplateApi.deleteSmsTemplateApi
})
const { getList, setSearchParams, delList } = methods

// ========== CRUD 相关 ==========
const loading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const formRef = ref<FormExpose>() // 表单 Ref

// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = () => {
  setDialogTile('create')
  // 重置表单
  unref(formRef)?.getElFormRef()?.resetFields()
}

// 修改操作
const handleUpdate = async (row: SmsTemplateVO) => {
  setDialogTile('update')
  // 设置数据
  const res = await SmsTemplateApi.getSmsTemplateApi(row.id)
  unref(formRef)?.setValues(res)
}

// 提交按钮
const submitForm = async () => {
  const elForm = unref(formRef)?.getElFormRef()
  if (!elForm) return
  elForm.validate(async (valid) => {
    if (valid) {
      loading.value = true
      // 提交请求
      try {
        const data = unref(formRef)?.formModel as SmsTemplateVO
        if (actionType.value === 'create') {
          await SmsTemplateApi.createSmsTemplateApi(data)
          ElMessage.success(t('common.createSuccess'))
        } else {
          await SmsTemplateApi.updateSmsTemplateApi(data)
          ElMessage.success(t('common.updateSuccess'))
        }
        // 操作成功，重新加载列表
        dialogVisible.value = false
        await getList()
      } finally {
        loading.value = false
      }
    }
  })
}

// ========== 详情相关 ==========
const detailRef = ref() // 详情 Ref

// 详情操作
const handleDetail = async (row: SmsTemplateVO) => {
  // 设置数据
  detailRef.value = row
  setDialogTile('detail')
}
// ========== 测试相关 ==========
const sendSmsForm = ref({
  content: '',
  params: {},
  mobile: '141',
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

const sendSmsTest = () => {
  const data = {
    mobile: sendSmsForm.value.mobile,
    templateCode: sendSmsForm.value.templateCode,
    templateParams: sendSmsForm.value.templateParams
  }
  SmsTemplateApi.sendSmsApi(data)
  ElMessage.info('发送成功')
  sendVisible.value = false
}

// ========== 初始化 ==========
getList()
</script>

<template>
  <!-- 搜索工作区 -->
  <ContentWrap>
    <Search :schema="allSchemas.searchSchema" @search="setSearchParams" @reset="setSearchParams" />
  </ContentWrap>
  <ContentWrap>
    <!-- 操作工具栏 -->
    <div class="mb-10px">
      <el-button type="primary" v-hasPermi="['system:sms-channel:create']" @click="handleCreate">
        <Icon icon="ep:zoom-in" class="mr-5px" /> {{ t('action.add') }}
      </el-button>
    </div>
    <!-- 列表 -->
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
      <template #type="{ row }">
        <DictTag :type="DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE" :value="row.type" />
      </template>
      <template #status="{ row }">
        <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
      <template #action="{ row }">
        <el-button
          link
          type="primary"
          v-hasPermi="['system:sms-template:send-sms']"
          @click="handleSendSms(row)"
        >
          <Icon icon="ep:cpu" class="mr-1px" /> {{ t('action.test') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['system:sms-template:update']"
          @click="handleUpdate(row)"
        >
          <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['system:sms-template:update']"
          @click="handleDetail(row)"
        >
          <Icon icon="ep:view" class="mr-1px" /> {{ t('action.detail') }}
        </el-button>
        <el-button
          link
          type="primary"
          v-hasPermi="['system:sms-template:delete']"
          @click="delList(row.id, false)"
        >
          <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
        </el-button>
      </template>
    </Table>
  </ContentWrap>

  <Dialog v-model="dialogVisible" :title="dialogTitle">
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
      :data="detailRef"
    >
      <template #type="{ row }">
        <DictTag :type="DICT_TYPE.SYSTEM_SMS_TEMPLATE_TYPE" :value="row.code" />
      </template>
      <template #status="{ row }">
        <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
      </template>
      <template #createTime="{ row }">
        <span>{{ dayjs(row.createTime).format('YYYY-MM-DD HH:mm:ss') }}</span>
      </template>
    </Descriptions>
    <!-- 操作按钮 -->
    <template #footer>
      <el-button
        v-if="['create', 'update'].includes(actionType)"
        type="primary"
        :loading="loading"
        @click="submitForm"
      >
        {{ t('action.save') }}
      </el-button>
      <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
  <Dialog v-model="sendVisible" title="测试">
    <el-form :model="sendSmsForm" :rules="sendSmsRules" label-width="140px">
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
      <el-button type="primary" :loading="loading" @click="sendSmsTest">
        {{ t('action.test') }}
      </el-button>
      <el-button @click="sendVisible = false">{{ t('dialog.close') }}</el-button>
    </template>
  </Dialog>
</template>
