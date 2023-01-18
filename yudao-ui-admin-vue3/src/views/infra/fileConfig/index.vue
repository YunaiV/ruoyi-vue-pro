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
          v-hasPermi="['infra:file-config:create']"
          @click="handleCreate(formRef)"
        />
      </template>
      <template #actionbtns_default="{ row }">
        <!-- 操作：编辑 -->
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['infra:file-config:update']"
          @click="handleUpdate(row.id)"
        />
        <!-- 操作：详情 -->
        <XTextButton
          preIcon="ep:view"
          :title="t('action.detail')"
          v-hasPermi="['infra:file-config:query']"
          @click="handleDetail(row.id)"
        />
        <!-- 操作：主配置 -->
        <XTextButton
          preIcon="ep:flag"
          title="主配置"
          v-hasPermi="['infra:file-config:update']"
          @click="handleMaster(row)"
        />
        <!-- 操作：测试 -->
        <XTextButton preIcon="ep:share" :title="t('action.test')" @click="handleTest(row.id)" />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['infra:file-config:delete']"
          @click="deleteData(row.id)"
        />
      </template>
    </XTable>
  </ContentWrap>
  <XModal v-model="dialogVisible" :title="dialogTitle">
    <!-- 对话框(添加 / 修改) -->
    <el-form
      ref="formRef"
      v-if="['create', 'update'].includes(actionType)"
      :model="form"
      :rules="rules"
      label-width="120px"
    >
      <el-form-item label="配置名" prop="name">
        <el-input v-model="form.name" placeholder="请输入配置名" />
      </el-form-item>
      <el-form-item label="备注" prop="remark">
        <el-input v-model="form.remark" placeholder="请输入备注" />
      </el-form-item>
      <el-form-item label="存储器" prop="storage">
        <el-select v-model="form.storage" placeholder="请选择存储器" :disabled="form.id !== 0">
          <el-option
            v-for="(dict, index) in getIntDictOptions(DICT_TYPE.INFRA_FILE_STORAGE)"
            :key="index"
            :label="dict.label"
            :value="dict.value"
          />
        </el-select>
      </el-form-item>
      <!-- DB -->
      <!-- Local / FTP / SFTP -->
      <el-form-item
        v-if="form.storage >= 10 && form.storage <= 12"
        label="基础路径"
        prop="config.basePath"
      >
        <el-input v-model="form.config.basePath" placeholder="请输入基础路径" />
      </el-form-item>
      <el-form-item
        v-if="form.storage >= 11 && form.storage <= 12"
        label="主机地址"
        prop="config.host"
      >
        <el-input v-model="form.config.host" placeholder="请输入主机地址" />
      </el-form-item>
      <el-form-item
        v-if="form.storage >= 11 && form.storage <= 12"
        label="主机端口"
        prop="config.port"
      >
        <el-input-number :min="0" v-model="form.config.port" placeholder="请输入主机端口" />
      </el-form-item>
      <el-form-item
        v-if="form.storage >= 11 && form.storage <= 12"
        label="用户名"
        prop="config.username"
      >
        <el-input v-model="form.config.username" placeholder="请输入密码" />
      </el-form-item>
      <el-form-item
        v-if="form.storage >= 11 && form.storage <= 12"
        label="密码"
        prop="config.password"
      >
        <el-input v-model="form.config.password" placeholder="请输入密码" />
      </el-form-item>
      <el-form-item v-if="form.storage === 11" label="连接模式" prop="config.mode">
        <el-radio-group v-model="form.config.mode">
          <el-radio key="Active" label="Active">主动模式</el-radio>
          <el-radio key="Passive" label="Passive">主动模式</el-radio>
        </el-radio-group>
      </el-form-item>
      <!-- S3 -->
      <el-form-item v-if="form.storage === 20" label="节点地址" prop="config.endpoint">
        <el-input v-model="form.config.endpoint" placeholder="请输入节点地址" />
      </el-form-item>
      <el-form-item v-if="form.storage === 20" label="存储 bucket" prop="config.bucket">
        <el-input v-model="form.config.bucket" placeholder="请输入 bucket" />
      </el-form-item>
      <el-form-item v-if="form.storage === 20" label="accessKey" prop="config.accessKey">
        <el-input v-model="form.config.accessKey" placeholder="请输入 accessKey" />
      </el-form-item>
      <el-form-item v-if="form.storage === 20" label="accessSecret" prop="config.accessSecret">
        <el-input v-model="form.config.accessSecret" placeholder="请输入 accessSecret" />
      </el-form-item>
      <!-- 通用 -->
      <el-form-item v-if="form.storage === 20" label="自定义域名">
        <!-- 无需参数校验，所以去掉 prop -->
        <el-input v-model="form.config.domain" placeholder="请输入自定义域名" />
      </el-form-item>
      <el-form-item v-else-if="form.storage" label="自定义域名" prop="config.domain">
        <el-input v-model="form.config.domain" placeholder="请输入自定义域名" />
      </el-form-item>
    </el-form>
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
        @click="submitForm(formRef)"
      />
      <!-- 按钮：关闭 -->
      <XButton :loading="actionLoading" :title="t('dialog.close')" @click="dialogVisible = false" />
    </template>
  </XModal>
</template>
<script setup lang="ts" name="FileConfig">
import type { FormInstance } from 'element-plus'
// 业务相关的 import
import * as FileConfigApi from '@/api/infra/fileConfig'
import { rules, allSchemas } from './fileConfig.data'
import { getIntDictOptions, DICT_TYPE } from '@/utils/dict'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
// 列表相关的变量
const [registerTable, { reload, deleteData }] = useXTable({
  allSchemas: allSchemas,
  getListApi: FileConfigApi.getFileConfigPageApi,
  deleteApi: FileConfigApi.deleteFileConfigApi
})

// ========== CRUD 相关 ==========
const actionLoading = ref(false) // 遮罩层
const actionType = ref('') // 操作按钮的类型
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const formRef = ref<FormInstance>() // 表单 Ref
const detailData = ref() // 详情 Ref
const form = ref<FileConfigApi.FileConfigVO>({
  id: 0,
  name: '',
  storage: 0,
  master: false,
  visible: false,
  config: {
    basePath: '',
    host: '',
    port: 0,
    username: '',
    password: '',
    mode: '',
    endpoint: '',
    bucket: '',
    accessKey: '',
    accessSecret: '',
    domain: ''
  },
  remark: '',
  createTime: new Date()
})
// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}

// 新增操作
const handleCreate = (formEl: FormInstance | undefined) => {
  setDialogTile('create')
  formEl?.resetFields()
  form.value = {
    id: 0,
    name: '',
    storage: 0,
    master: false,
    visible: false,
    config: {
      basePath: '',
      host: '',
      port: 0,
      username: '',
      password: '',
      mode: '',
      endpoint: '',
      bucket: '',
      accessKey: '',
      accessSecret: '',
      domain: ''
    },
    remark: '',
    createTime: new Date()
  }
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  // 设置数据
  const res = await FileConfigApi.getFileConfigApi(rowId)
  form.value = res
  setDialogTile('update')
}

// 详情操作
const handleDetail = async (rowId: number) => {
  setDialogTile('detail')
  // 设置数据
  const res = await FileConfigApi.getFileConfigApi(rowId)
  detailData.value = res
}

// 主配置操作
const handleMaster = (row: FileConfigApi.FileConfigVO) => {
  message
    .confirm('是否确认修改配置【 ' + row.name + ' 】为主配置?', t('common.reminder'))
    .then(async () => {
      await FileConfigApi.updateFileConfigMasterApi(row.id)
      await reload()
    })
}

const handleTest = async (rowId: number) => {
  const res = await FileConfigApi.testFileConfigApi(rowId)
  message.alert('测试通过，上传文件成功！访问地址：' + res)
}

// 提交按钮
const submitForm = async (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.validate(async (valid) => {
    if (valid) {
      actionLoading.value = true
      // 提交请求
      try {
        if (actionType.value === 'create') {
          await FileConfigApi.createFileConfigApi(form.value)
          message.success(t('common.createSuccess'))
        } else {
          await FileConfigApi.updateFileConfigApi(form.value)
          message.success(t('common.updateSuccess'))
        }
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
        await reload()
      }
    }
  })
}
</script>
