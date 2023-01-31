<template>
  <ContentWrap>
    <!-- 第一步，通过流程定义的列表，选择对应的流程 -->
    <div v-if="!selectProcessInstance">
      <XTable @register="registerTable">
        <template #version_default="{ row }">
          <el-tag v-if="row">v{{ row.version }}</el-tag>
        </template>
        <template #actionbtns_default="{ row }">
          <XTextButton preIcon="ep:plus" title="选择" @click="handleSelect(row)" />
        </template>
      </XTable>
    </div>
    <!-- 第二步，填写表单，进行流程的提交 -->
    <div v-else>
      <el-card class="box-card">
        <div class="clearfix">
          <span class="el-icon-document">申请信息【{{ selectProcessInstance.name }}】</span>
          <XButton
            style="float: right"
            type="primary"
            preIcon="ep:delete"
            title="选择其它流程"
            @click="selectProcessInstance = undefined"
          />
        </div>
        <el-col :span="16" :offset="6" style="margin-top: 20px">
          <form-create
            :rule="detailForm.rule"
            v-model:api="fApi"
            :option="detailForm.option"
            @submit="submitForm"
          />
        </el-col>
      </el-card>
      <el-card class="box-card">
        <div class="clearfix">
          <span class="el-icon-picture-outline">流程图</span>
        </div>
        <!-- TODO 芋艿：待完成？？？ -->
        <my-process-viewer
          key="designer"
          v-model="bpmnXML"
          :value="bpmnXML"
          v-bind="bpmnControlForm"
          :prefix="bpmnControlForm.prefix"
        />
      </el-card>
    </div>
  </ContentWrap>
</template>
<script setup lang="ts">
// 业务相关的 import
import { allSchemas } from './process.create'
import * as DefinitionApi from '@/api/bpm/definition'
import * as ProcessInstanceApi from '@/api/bpm/processInstance'
import { setConfAndFields2 } from '@/utils/formCreate'
import { ApiAttrs } from '@form-create/element-ui/types/config'

const router = useRouter() // 路由
const message = useMessage() // 消息

// ========== 列表相关 ==========

const [registerTable] = useXTable({
  allSchemas: allSchemas,
  params: {
    suspensionState: 1
  },
  getListApi: DefinitionApi.getProcessDefinitionListApi,
  isList: true
})

// ========== 表单相关 ==========

const fApi = ref<ApiAttrs>()

// 流程表单详情
const detailForm = ref({
  rule: [],
  option: {}
})

// 流程表单
const selectProcessInstance = ref() // 选择的流程实例
/** 处理选择流程的按钮操作 **/
const handleSelect = async (row) => {
  // 设置选择的流程
  selectProcessInstance.value = row

  // 情况一：流程表单
  if (row.formType == 10) {
    // 设置表单
    setConfAndFields2(detailForm, row.formConf, row.formFields)

    // 加载流程图
    DefinitionApi.getProcessDefinitionBpmnXMLApi(row.id).then((response) => {
      bpmnXML.value = response
    })
    // 情况二：业务表单
  } else if (row.formCustomCreatePath) {
    await router.push({
      path: row.formCustomCreatePath
    })
    // 这里暂时无需加载流程图，因为跳出到另外个 Tab；
  }
}

/** 提交按钮 */
const submitForm = async (formData) => {
  if (!fApi.value || !selectProcessInstance.value) {
    return
  }

  // 提交请求
  fApi.value.btn.loading(true)
  try {
    await ProcessInstanceApi.createProcessInstanceApi({
      processDefinitionId: selectProcessInstance.value.id,
      variables: formData
    })
    // 提示
    message.success('发起流程成功')
    // this.$tab.closeOpenPage();
    router.go(-1)
  } finally {
    fApi.value.btn.loading(false)
  }
}

// ========== 流程图相关 ==========

// // BPMN 数据
const bpmnXML = ref(null)
const bpmnControlForm = ref({
  prefix: 'flowable'
})
</script>

<style lang="scss">
.my-process-designer {
  height: calc(100vh - 200px);
}

.box-card {
  width: 100%;
  margin-bottom: 20px;
}
</style>
