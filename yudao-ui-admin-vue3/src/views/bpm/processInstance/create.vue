<template>
  <div class="app-container">
    <!-- 第一步，通过流程定义的列表，选择对应的流程 -->
    <div v-if="!selectProcessInstance">
      <XTable @register="registerTable">
        <template #version_default="{ row }">
          <el-tag v-if="row">v{{ row.version }}</el-tag>
        </template>
        <template #actionbtns_default="{ row }">
          <el-button type="text" size="small" icon="el-icon-plus" @click="handleSelect(row)"
            >选择</el-button
          >
        </template>
      </XTable>
    </div>
    <!-- 第二步，填写表单，进行流程的提交 -->
    <div v-else>
      <el-card class="box-card">
        <div class="clearfix">
          <span class="el-icon-document">申请信息【{{ selectProcessInstance.name }}】</span>
          <el-button style="float: right" type="primary" @click="selectProcessInstance = undefined"
            >选择其它流程</el-button
          >
        </div>
        <el-col :span="16" :offset="6">
          <div>
            <!-- <parser :key="new Date().getTime()" :form-conf="detailForm" @submit="submitForm" /> -->
          </div>
        </el-col>
      </el-card>
      <el-card class="box-card">
        <div class="clearfix">
          <span class="el-icon-picture-outline">流程图</span>
        </div>
        <!-- <my-process-viewer key="designer" v-model="bpmnXML" v-bind="bpmnControlForm" /> -->
      </el-card>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useXTable } from '@/hooks/web/useXTable'
import * as definitionApi from '@/api/bpm/definition'
// import {DICT_TYPE, getDictDatas} from "@/utils/dict";
import { decodeFields } from '@/utils/formGenerator'
// import Parser from '@/components/parser/Parser'
// import * as processInstanceApi from "@/api/bpm/processInstance";
import { allSchemas } from './process.create'
import { useRouter } from 'vue-router'
const router = useRouter()

const queryParams = reactive({
  suspensionState: 1
})
const [registerTable] = useXTable({
  allSchemas: allSchemas,
  params: queryParams,
  getListApi: definitionApi.getProcessDefinitionListApi
})

// 流程表单详情
const detailForm = ref({
  fields: []
})

// // BPMN 数据
const bpmnXML = ref(null)
// const bpmnControlForm=ref( {
//   prefix: "flowable"
// })

// 流程表单
const selectProcessInstance = ref(undefined) // 选择的流程实例
/** 处理选择流程的按钮操作 **/
const handleSelect = (row) => {
  // 设置选择的流程
  selectProcessInstance.value = row

  // 流程表单
  if (row.formId) {
    // 设置对应的表单
    detailForm.value = {
      ...JSON.parse(row.formConf),
      fields: decodeFields([], row.formFields)
    }

    // 加载流程图
    definitionApi.getProcessDefinitionBpmnXMLApi(row.id).then((response) => {
      bpmnXML.value = response.data
    })
  } else if (row.formCustomCreatePath) {
    router.push({ path: row.formCustomCreatePath })
    // 这里暂时无需加载流程图，因为跳出到另外个 Tab；
  }
}
/** 提交按钮 */
//  const submitForm=(params)=> {
//     if (!params) {
//       return;
//     }
//     // 设置表单禁用
//     const conf = params.conf;
//     conf.disabled = true; // 表单禁用
//     conf.formBtns = false; // 按钮隐藏

//     // 提交表单，创建流程
//     const variables = params.values;
//   await  processInstanceApi.createProcessInstanceApi({
//       processDefinitionId: this.selectProcessInstance.id,
//       variables: variables
//     }).then(response => {
//       this.$modal.msgSuccess("发起流程成功");
//       // 关闭当前窗口
//       this.$tab.closeOpenPage();
//       this.$router.go(-1);
//     }).catch(() => {
//       conf.disabled = false; // 表单开启
//       conf.formBtns = true; // 按钮展示
//     })
//   }
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
