<script setup lang="ts">
import { ElTable, ElTableColumn, ElInput, ElSelect, ElOption, ElCheckbox } from 'element-plus'
import { onMounted, PropType, ref } from 'vue'
import { CodegenColumnVO } from '@/api/infra/codegen/types'
import { listSimpleDictTypeApi } from '@/api/system/dict/dict.type'
import { DictTypeVO } from '@/api/system/dict/types'
const props = defineProps({
  info: {
    type: Array as unknown as PropType<CodegenColumnVO[]>,
    default: () => null
  }
})
/** 查询字典下拉列表 */
const dictOptions = ref<DictTypeVO[]>()
const getDictOptions = async () => {
  const res = await listSimpleDictTypeApi()
  dictOptions.value = res
}
const tableHeight = document.documentElement.scrollHeight - 245 + 'px'
onMounted(async () => {
  await getDictOptions()
})
defineExpose({
  info: props.info
})
</script>
<template>
  <el-table ref="dragTable" :data="info" row-key="columnId" :max-height="tableHeight">
    <el-table-column
      label="字段列名"
      prop="columnName"
      min-width="10%"
      :show-overflow-tooltip="true"
    />
    <el-table-column label="字段描述" min-width="10%" prop="columnComment">
      <template #default="{ row }">
        <el-input v-model="row.columnComment" />
      </template>
    </el-table-column>
    <el-table-column
      label="物理类型"
      prop="dataType"
      min-width="10%"
      :show-overflow-tooltip="true"
    />
    <el-table-column label="Java类型" min-width="11%" prop="javaType">
      <template #default="{ row }">
        <el-select v-model="row.javaType">
          <el-option label="Long" value="Long" />
          <el-option label="String" value="String" />
          <el-option label="Integer" value="Integer" />
          <el-option label="Double" value="Double" />
          <el-option label="BigDecimal" value="BigDecimal" />
          <el-option label="Date" value="Date" />
          <el-option label="Boolean" value="Boolean" />
        </el-select>
      </template>
    </el-table-column>
    <el-table-column label="java属性" min-width="10%" prop="javaField">
      <template #default="{ row }">
        <el-input v-model="row.javaField" />
      </template>
    </el-table-column>
    <el-table-column label="插入" min-width="4%" prop="createOperation">
      <template #default="{ row }">
        <el-checkbox true-label="true" false-label="false" v-model="row.createOperation" />
      </template>
    </el-table-column>
    <el-table-column label="编辑" min-width="4%" prop="updateOperation">
      <template #default="{ row }">
        <el-checkbox true-label="true" false-label="false" v-model="row.updateOperation" />
      </template>
    </el-table-column>
    <el-table-column label="列表" min-width="4%" prop="listOperationResult">
      <template #default="{ row }">
        <el-checkbox true-label="true" false-label="false" v-model="row.listOperationResult" />
      </template>
    </el-table-column>
    <el-table-column label="查询" min-width="4%" prop="listOperation">
      <template #default="{ row }">
        <el-checkbox true-label="true" false-label="false" v-model="row.listOperation" />
      </template>
    </el-table-column>
    <el-table-column label="查询方式" min-width="10%" prop="listOperationCondition">
      <template #default="{ row }">
        <el-select v-model="row.listOperationCondition">
          <el-option label="=" value="=" />
          <el-option label="!=" value="!=" />
          <el-option label=">" value=">" />
          <el-option label=">=" value=">=" />
          <el-option label="<" value="<>" />
          <el-option label="<=" value="<=" />
          <el-option label="LIKE" value="LIKE" />
          <el-option label="BETWEEN" value="BETWEEN" />
        </el-select>
      </template>
    </el-table-column>
    <el-table-column label="允许空" min-width="5%" prop="nullable">
      <template #default="{ row }">
        <el-checkbox true-label="true" false-label="false" v-model="row.nullable" />
      </template>
    </el-table-column>
    <el-table-column label="显示类型" min-width="12%" prop="htmlType">
      <template #default="{ row }">
        <el-select v-model="row.htmlType">
          <el-option label="文本框" value="input" />
          <el-option label="文本域" value="textarea" />
          <el-option label="下拉框" value="select" />
          <el-option label="单选框" value="radio" />
          <el-option label="复选框" value="checkbox" />
          <el-option label="日期控件" value="datetime" />
          <el-option label="图片上传" value="imageUpload" />
          <el-option label="文件上传" value="fileUpload" />
          <el-option label="富文本控件" value="editor" />
        </el-select>
      </template>
    </el-table-column>
    <el-table-column label="字典类型" min-width="12%" prop="dictType">
      <template #default="{ row }">
        <el-select v-model="row.dictType" clearable filterable placeholder="请选择">
          <el-option
            v-for="dict in dictOptions"
            :key="dict.id"
            :label="dict.name"
            :value="dict.type"
          />
        </el-select>
      </template>
    </el-table-column>
    <el-table-column label="示例" min-width="10%" prop="example">
      <template #default="{ row }">
        <el-input v-model="row.example" />
      </template>
    </el-table-column>
  </el-table>
</template>
