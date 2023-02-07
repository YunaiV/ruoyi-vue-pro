<template>
  <div class="panel-tab__content">
    <div class="panel-tab__content--title">
      <span><Icon icon="ep:menu" style="margin-right: 8px; color: #555555" />消息列表</span>
      <XButton type="primary" title="创建新消息" preIcon="ep:plus" @click="openModel('message')" />
    </div>
    <el-table :data="messageList" border>
      <el-table-column type="index" label="序号" width="60px" />
      <el-table-column label="消息ID" prop="id" max-width="300px" show-overflow-tooltip />
      <el-table-column label="消息名称" prop="name" max-width="300px" show-overflow-tooltip />
    </el-table>
    <div
      class="panel-tab__content--title"
      style="padding-top: 8px; margin-top: 8px; border-top: 1px solid #eeeeee"
    >
      <span><Icon icon="ep:menu" style="margin-right: 8px; color: #555555" />信号列表</span>
      <XButton type="primary" title="创建新信号" preIcon="ep:plus" @click="openModel('signal')" />
    </div>
    <el-table :data="signalList" border>
      <el-table-column type="index" label="序号" width="60px" />
      <el-table-column label="信号ID" prop="id" max-width="300px" show-overflow-tooltip />
      <el-table-column label="信号名称" prop="name" max-width="300px" show-overflow-tooltip />
    </el-table>

    <el-dialog
      v-model="modelVisible"
      :title="modelConfig.title"
      :close-on-click-modal="false"
      width="400px"
      append-to-body
      destroy-on-close
    >
      <el-form :model="modelObjectForm" label-width="90px">
        <el-form-item :label="modelConfig.idLabel">
          <el-input v-model="modelObjectForm.id" clearable />
        </el-form-item>
        <el-form-item :label="modelConfig.nameLabel">
          <el-input v-model="modelObjectForm.name" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="modelVisible = false">取 消</el-button>
        <el-button type="primary" @click="addNewObject">保 存</el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts" name="SignalAndMassage">
import {
  ElMessage,
  ElDialog,
  ElForm,
  ElFormItem,
  ElTable,
  ElTableColumn,
  ElButton,
  ElInput
} from 'element-plus'
import { ref, computed, onMounted } from 'vue'
const signalList = ref<any[]>([])
const messageList = ref<any[]>([])
const modelVisible = ref(false)
const modelType = ref('')
const modelObjectForm = ref<any>({})
const rootElements = ref()
const messageIdMap = ref()
const signalIdMap = ref()
const modelConfig = computed(() => {
  if (modelType.value === 'message') {
    return { title: '创建消息', idLabel: '消息ID', nameLabel: '消息名称' }
  } else {
    return { title: '创建信号', idLabel: '信号ID', nameLabel: '信号名称' }
  }
})

const initDataList = () => {
  console.log(window, 'window')
  rootElements.value = window.bpmnInstances.modeler.getDefinitions().rootElements
  messageIdMap.value = {}
  signalIdMap.value = {}
  messageList.value = []
  signalList.value = []
  rootElements.value.forEach((el) => {
    if (el.$type === 'bpmn:Message') {
      messageIdMap.value[el.id] = true
      messageList.value.push({ ...el })
    }
    if (el.$type === 'bpmn:Signal') {
      signalIdMap.value[el.id] = true
      signalList.value.push({ ...el })
    }
  })
}
const openModel = (type) => {
  modelType.value = type
  modelObjectForm.value = {}
  modelVisible.value = true
}
const addNewObject = () => {
  if (modelType.value === 'message') {
    if (messageIdMap.value[modelObjectForm.value.id]) {
      ElMessage.error('该消息已存在，请修改id后重新保存')
    }
    const messageRef = window.bpmnInstances.moddle.create('bpmn:Message', modelObjectForm.value)
    rootElements.value.push(messageRef)
  } else {
    if (signalIdMap.value[modelObjectForm.value.id]) {
      ElMessage.error('该信号已存在，请修改id后重新保存')
    }
    const signalRef = window.bpmnInstances.moddle.create('bpmn:Signal', modelObjectForm.value)
    rootElements.value.push(signalRef)
  }
  modelVisible.value = false
  initDataList()
}

onMounted(() => {
  initDataList()
})
</script>
