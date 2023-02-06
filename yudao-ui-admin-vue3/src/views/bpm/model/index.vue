<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #toolbar_buttons>
        <!-- 操作：新增 -->
        <XButton
          type="primary"
          preIcon="ep:zoom-in"
          title="新建流程"
          v-hasPermi="['bpm:model:create']"
          @click="handleCreate"
        />
        <!-- 操作：导入 -->
        <XButton
          type="warning"
          preIcon="ep:upload"
          :title="'导入流程'"
          @click="handleImport"
          style="margin-left: 10px"
        />
      </template>
      <!-- 流程名称 -->
      <template #name_default="{ row }">
        <XTextButton :title="row.name" @click="handleBpmnDetail(row.id)" />
      </template>
      <!-- 表单信息 -->
      <template #formId_default="{ row }">
        <XTextButton
          v-if="row.formType === 10"
          :title="forms.find((form) => form.id === row.formId)?.name || row.formId"
          @click="handleFormDetail(row)"
        />
        <XTextButton v-else :title="row.formCustomCreatePath" @click="handleFormDetail(row)" />
      </template>
      <!-- 流程版本 -->
      <template #version_default="{ row }">
        <el-tag v-if="row.processDefinition">v{{ row.processDefinition.version }}</el-tag>
        <el-tag type="warning" v-else>未部署</el-tag>
      </template>
      <!-- 激活状态 -->
      <template #status_default="{ row }">
        <el-switch
          v-if="row.processDefinition"
          v-model="row.processDefinition.suspensionState"
          :active-value="1"
          :inactive-value="2"
          @change="handleChangeState(row)"
        />
      </template>
      <!-- 操作 -->
      <template #actionbtns_default="{ row }">
        <XTextButton
          preIcon="ep:edit"
          title="修改流程"
          v-hasPermi="['bpm:model:update']"
          @click="handleUpdate(row.id)"
        />
        <XTextButton
          preIcon="ep:setting"
          title="设计流程"
          v-hasPermi="['bpm:model:update']"
          @click="handleDesign(row)"
        />
        <XTextButton
          preIcon="ep:user"
          title="分配规则"
          v-hasPermi="['bpm:task-assign-rule:query']"
          @click="handleAssignRule(row)"
        />
        <XTextButton
          preIcon="ep:position"
          title="发布流程"
          v-hasPermi="['bpm:model:deploy']"
          @click="handleDeploy(row)"
        />
        <XTextButton
          preIcon="ep:aim"
          title="流程定义"
          v-hasPermi="['bpm:process-definition:query']"
          @click="handleDefinitionList(row)"
        />
        <!-- 操作：删除 -->
        <XTextButton
          preIcon="ep:delete"
          :title="t('action.del')"
          v-hasPermi="['bpm:model:delete']"
          @click="handleDelete(row.id)"
        />
      </template>
    </XTable>

    <!-- 对话框(添加 / 修改流程) -->
    <XModal v-model="dialogVisible" :title="dialogTitle" width="600">
      <el-form
        :loading="dialogLoading"
        el-form
        ref="saveFormRef"
        :model="saveForm"
        :rules="rules"
        label-width="110px"
      >
        <el-form-item label="流程标识" prop="key">
          <el-input
            v-model="saveForm.key"
            placeholder="请输入流标标识"
            style="width: 330px"
            :disabled="!!saveForm.id"
          />
          <el-tooltip
            v-if="!saveForm.id"
            class="item"
            effect="light"
            content="新建后，流程标识不可修改！"
            placement="top"
          >
            <i style="padding-left: 5px" class="el-icon-question"></i>
          </el-tooltip>
          <el-tooltip
            v-else
            class="item"
            effect="light"
            content="流程标识不可修改！"
            placement="top"
          >
            <i style="padding-left: 5px" class="el-icon-question"></i>
          </el-tooltip>
        </el-form-item>
        <el-form-item label="流程名称" prop="name">
          <el-input
            v-model="saveForm.name"
            placeholder="请输入流程名称"
            :disabled="!!saveForm.id"
            clearable
          />
        </el-form-item>
        <el-form-item v-if="saveForm.id" label="流程分类" prop="category">
          <el-select
            v-model="saveForm.category"
            placeholder="请选择流程分类"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="dict in getDictOptions(DICT_TYPE.BPM_MODEL_CATEGORY)"
              :key="dict.value"
              :label="dict.label"
              :value="dict.value"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="流程描述" prop="description">
          <el-input type="textarea" v-model="saveForm.description" clearable />
        </el-form-item>
        <div v-if="saveForm.id">
          <el-form-item label="表单类型" prop="formType">
            <el-radio-group v-model="saveForm.formType">
              <el-radio
                v-for="dict in getDictOptions(DICT_TYPE.BPM_MODEL_FORM_TYPE)"
                :key="parseInt(dict.value)"
                :label="parseInt(dict.value)"
              >
                {{ dict.label }}
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item v-if="saveForm.formType === 10" label="流程表单" prop="formId">
            <el-select v-model="saveForm.formId" clearable style="width: 100%">
              <el-option v-for="form in forms" :key="form.id" :label="form.name" :value="form.id" />
            </el-select>
          </el-form-item>
          <el-form-item
            v-if="saveForm.formType === 20"
            label="表单提交路由"
            prop="formCustomCreatePath"
          >
            <el-input
              v-model="saveForm.formCustomCreatePath"
              placeholder="请输入表单提交路由"
              style="width: 330px"
            />
            <el-tooltip
              class="item"
              effect="light"
              content="自定义表单的提交路径，使用 Vue 的路由地址，例如说：bpm/oa/leave/create"
              placement="top"
            >
              <i style="padding-left: 5px" class="el-icon-question"></i>
            </el-tooltip>
          </el-form-item>
          <el-form-item
            v-if="saveForm.formType === 20"
            label="表单查看路由"
            prop="formCustomViewPath"
          >
            <el-input
              v-model="saveForm.formCustomViewPath"
              placeholder="请输入表单查看路由"
              style="width: 330px"
            />
            <el-tooltip
              class="item"
              effect="light"
              content="自定义表单的查看路径，使用 Vue 的路由地址，例如说：bpm/oa/leave/view"
              placement="top"
            >
              <i style="padding-left: 5px" class="el-icon-question"></i>
            </el-tooltip>
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <!-- 按钮：保存 -->
        <XButton
          type="primary"
          :loading="dialogLoading"
          @click="submitForm"
          :title="t('action.save')"
        />
        <!-- 按钮：关闭 -->
        <XButton
          :loading="dialogLoading"
          @click="dialogVisible = false"
          :title="t('dialog.close')"
        />
      </template>
    </XModal>

    <!-- 导入流程 -->
    <XModal v-model="importDialogVisible" width="400" title="导入流程">
      <div>
        <el-upload
          ref="uploadRef"
          :action="importUrl"
          :headers="uploadHeaders"
          :drag="true"
          :limit="1"
          :multiple="true"
          :show-file-list="true"
          :disabled="uploadDisabled"
          :on-exceed="handleExceed"
          :on-success="handleFileSuccess"
          :on-error="excelUploadError"
          :auto-upload="false"
          accept=".bpmn, .xml"
          name="bpmnFile"
          :data="importForm"
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text"> 将文件拖到此处，或 <em>点击上传</em> </div>
          <template #tip>
            <div class="el-upload__tip" style="color: red">
              提示：仅允许导入“bpm”或“xml”格式文件！
            </div>
            <div>
              <el-form
                ref="importFormRef"
                :model="importForm"
                :rules="rules"
                label-width="120px"
                status-icon
              >
                <el-form-item label="流程标识" prop="key">
                  <el-input
                    v-model="importForm.key"
                    placeholder="请输入流标标识"
                    style="width: 250px"
                  />
                </el-form-item>
                <el-form-item label="流程名称" prop="name">
                  <el-input v-model="importForm.name" placeholder="请输入流程名称" clearable />
                </el-form-item>
                <el-form-item label="流程描述" prop="description">
                  <el-input type="textarea" v-model="importForm.description" clearable />
                </el-form-item>
              </el-form>
            </div>
          </template>
        </el-upload>
      </div>
      <template #footer>
        <!-- 按钮：保存 -->
        <XButton
          type="warning"
          preIcon="ep:upload-filled"
          :title="t('action.save')"
          @click="submitFileForm"
        />
        <XButton title="取 消" @click="uploadClose" />
      </template>
    </XModal>

    <!-- 表单详情的弹窗 -->
    <XModal v-model="formDetailVisible" width="800" title="表单详情" :show-footer="false">
      <form-create
        :rule="formDetailPreview.rule"
        :option="formDetailPreview.option"
        v-if="formDetailVisible"
      />
    </XModal>

    <!-- 流程模型图的预览 -->
    <XModal title="流程图" v-model="showBpmnOpen" width="80%" height="90%">
      <my-process-viewer
        key="designer"
        v-model="bpmnXML"
        :value="bpmnXML"
        v-bind="bpmnControlForm"
        :prefix="bpmnControlForm.prefix"
      />
    </XModal>
  </ContentWrap>
</template>

<script setup lang="ts">
// 全局相关的 import
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import { FormInstance, UploadInstance } from 'element-plus'

// 业务相关的 import
import { getAccessToken, getTenantId } from '@/utils/auth'
import * as FormApi from '@/api/bpm/form'
import * as ModelApi from '@/api/bpm/model'
import { allSchemas, rules } from './model.data'
import { setConfAndFields2 } from '@/utils/formCreate'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const router = useRouter() // 路由

const showBpmnOpen = ref(false)
const bpmnXML = ref(null)
const bpmnControlForm = ref({
  prefix: 'flowable'
})
// ========== 列表相关 ==========
const [registerTable, { reload }] = useXTable({
  allSchemas: allSchemas,
  getListApi: ModelApi.getModelPageApi
})
const forms = ref() // 流程表单的下拉框的数据

// 设计流程
const handleDesign = (row) => {
  console.log(row, '设计流程')
  router.push({
    name: 'modelEditor',
    query: {
      modelId: row.id
    }
  })
}

// 跳转到指定流程定义列表
const handleDefinitionList = (row) => {
  router.push({
    name: 'BpmProcessDefinitionList',
    query: {
      key: row.key
    }
  })
}

// 流程表单的详情按钮操作
const formDetailVisible = ref(false)
const formDetailPreview = ref({
  rule: [],
  option: {}
})
const handleFormDetail = async (row) => {
  if (row.formType == 10) {
    // 设置表单
    const data = await FormApi.getFormApi(row.formId)
    setConfAndFields2(formDetailPreview, data.conf, data.fields)
    // 弹窗打开
    formDetailVisible.value = true
  } else {
    await router.push({
      path: row.formCustomCreatePath
    })
  }
}

// 流程图的详情按钮操作
const handleBpmnDetail = (row) => {
  // TODO 芋艿：流程组件开发中
  console.log(row)
  ModelApi.getModelApi(row).then((response) => {
    console.log(response, 'response')
    bpmnXML.value = response.bpmnXml
    // 弹窗打开
    showBpmnOpen.value = true
  })
  // message.success('流程组件开发中，预计 2 月底完成')
}

// 点击任务分配按钮
const handleAssignRule = (row) => {
  router.push({
    name: 'BpmTaskAssignRuleList',
    query: {
      modelId: row.id
    }
  })
}

// ========== 新建/修改流程 ==========
const dialogVisible = ref(false)
const dialogTitle = ref('新建模型')
const dialogLoading = ref(false)
const saveForm = ref()
const saveFormRef = ref<FormInstance>()

// 设置标题
const setDialogTile = async (type: string) => {
  dialogTitle.value = t('action.' + type)
  dialogVisible.value = true
}

// 新增操作
const handleCreate = async () => {
  resetForm()
  await setDialogTile('create')
}

// 修改操作
const handleUpdate = async (rowId: number) => {
  resetForm()
  await setDialogTile('edit')
  // 设置数据
  saveForm.value = await ModelApi.getModelApi(rowId)
}

// 提交按钮
const submitForm = async () => {
  // 参数校验
  const elForm = unref(saveFormRef)
  if (!elForm) return
  const valid = await elForm.validate()
  if (!valid) return

  // 提交请求
  dialogLoading.value = true
  try {
    const data = saveForm.value as ModelApi.ModelVO
    if (!data.id) {
      await ModelApi.createModelApi(data)
      message.success(t('common.createSuccess'))
    } else {
      await ModelApi.updateModelApi(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
  } finally {
    // 刷新列表
    await reload()
    dialogLoading.value = false
  }
}

// 重置表单
const resetForm = () => {
  saveForm.value = {
    formType: 10,
    name: '',
    courseSort: '',
    description: '',
    formId: '',
    formCustomCreatePath: '',
    formCustomViewPath: ''
  }
  saveFormRef.value?.resetFields()
}

// ========== 删除 / 更新状态 / 发布流程 ==========
// 删除流程
const handleDelete = (rowId) => {
  message.delConfirm('是否删除该流程！！').then(async () => {
    await ModelApi.deleteModelApi(rowId)
    message.success(t('common.delSuccess'))
    // 刷新列表
    reload()
  })
}

// 更新状态操作
const handleChangeState = (row) => {
  const id = row.id
  const state = row.processDefinition.suspensionState
  const statusState = state === 1 ? '激活' : '挂起'
  const content = '是否确认' + statusState + '流程名字为"' + row.name + '"的数据项?'
  message
    .confirm(content)
    .then(async () => {
      await ModelApi.updateModelStateApi(id, state)
      message.success(t('部署成功'))
      // 刷新列表
      reload()
    })
    .catch(() => {
      // 取消后，进行恢复按钮
      row.processDefinition.suspensionState = state === 1 ? 2 : 1
    })
}

// 发布流程
const handleDeploy = (row) => {
  message.confirm('是否部署该流程！！').then(async () => {
    await ModelApi.deployModelApi(row.id)
    message.success(t('部署成功'))
    // 刷新列表
    reload()
  })
}

// ========== 导入流程 ==========
const uploadRef = ref<UploadInstance>()
let importUrl = import.meta.env.VITE_BASE_URL + import.meta.env.VITE_API_URL + '/bpm/model/import'
const uploadHeaders = ref()
const importDialogVisible = ref(false)
const uploadDisabled = ref(false)
const importFormRef = ref<FormInstance>()
const importForm = ref({
  key: '',
  name: '',
  description: ''
})

// 导入流程弹窗显示
const handleImport = () => {
  importDialogVisible.value = true
}
// 文件数超出提示
const handleExceed = (): void => {
  message.error('最多只能上传一个文件！')
}
// 上传错误提示
const excelUploadError = (): void => {
  message.error('导入流程失败，请您重新上传！')
}

// 提交文件上传
const submitFileForm = () => {
  uploadHeaders.value = {
    Authorization: 'Bearer ' + getAccessToken(),
    'tenant-id': getTenantId()
  }
  uploadDisabled.value = true
  uploadRef.value!.submit()
}
// 文件上传成功
const handleFileSuccess = async (response: any): Promise<void> => {
  if (response.code !== 0) {
    message.error(response.msg)
    return
  }
  // 重置表单
  uploadClose()
  // 提示，并刷新
  message.success('导入流程成功！请点击【设计流程】按钮，进行编辑保存后，才可以进行【发布流程】')
  await reload()
}
// 关闭文件上传
const uploadClose = () => {
  // 关闭弹窗
  importDialogVisible.value = false
  // 重置上传状态和文件
  uploadDisabled.value = false
  uploadRef.value!.clearFiles()
  // 重置表单
  importForm.value = {
    key: '',
    name: '',
    description: ''
  }
  importFormRef.value?.resetFields()
}

// ========== 初始化 ==========
onMounted(() => {
  // 获得流程表单的下拉框的数据
  FormApi.getSimpleFormsApi().then((data) => {
    forms.value = data
  })
})
</script>
