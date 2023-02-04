<template>
  <vxe-table
    ref="dragTable"
    border
    :data="info"
    max-height="600"
    stripe
    class="xtable-scrollbar"
    :column-config="{ resizable: true }"
  >
    <vxe-column title="字段列名" field="columnName" fixed="left" width="10%" />
    <vxe-colgroup title="基础属性">
      <vxe-column title="字段描述" field="columnComment" width="10%">
        <template #default="{ row }">
          <vxe-input v-model="row.columnComment" placeholder="请输入字段描述" />
        </template>
      </vxe-column>
      <vxe-column title="物理类型" field="dataType" width="10%" />
      <vxe-column title="Java类型" width="10%" field="javaType">
        <template #default="{ row }">
          <vxe-select v-model="row.javaType" placeholder="请选择Java类型">
            <vxe-option label="Long" value="Long" />
            <vxe-option label="String" value="String" />
            <vxe-option label="Integer" value="Integer" />
            <vxe-option label="Double" value="Double" />
            <vxe-option label="BigDecimal" value="BigDecimal" />
            <vxe-option label="LocalDateTime" value="LocalDateTime" />
            <vxe-option label="Boolean" value="Boolean" />
          </vxe-select>
        </template>
      </vxe-column>
      <vxe-column title="java属性" width="8%" field="javaField">
        <template #default="{ row }">
          <vxe-input v-model="row.javaField" placeholder="请输入java属性" />
        </template>
      </vxe-column>
    </vxe-colgroup>
    <vxe-colgroup title="增删改查">
      <vxe-column title="插入" width="40px" field="createOperation">
        <template #default="{ row }">
          <vxe-checkbox true-label="true" false-label="false" v-model="row.createOperation" />
        </template>
      </vxe-column>
      <vxe-column title="编辑" width="40px" field="updateOperation">
        <template #default="{ row }">
          <vxe-checkbox true-label="true" false-label="false" v-model="row.updateOperation" />
        </template>
      </vxe-column>
      <vxe-column title="列表" width="40px" field="listOperationResult">
        <template #default="{ row }">
          <vxe-checkbox true-label="true" false-label="false" v-model="row.listOperationResult" />
        </template>
      </vxe-column>
      <vxe-column title="查询" width="40px" field="listOperation">
        <template #default="{ row }">
          <vxe-checkbox true-label="true" false-label="false" v-model="row.listOperation" />
        </template>
      </vxe-column>
      <vxe-column title="允许空" width="40px" field="nullable">
        <template #default="{ row }">
          <vxe-checkbox true-label="true" false-label="false" v-model="row.nullable" />
        </template>
      </vxe-column>
      <vxe-column title="查询方式" width="60px" field="listOperationCondition">
        <template #default="{ row }">
          <vxe-select v-model="row.listOperationCondition" placeholder="请选择查询方式">
            <vxe-option label="=" value="=" />
            <vxe-option label="!=" value="!=" />
            <vxe-option label=">" value=">" />
            <vxe-option label=">=" value=">=" />
            <vxe-option label="<" value="<>" />
            <vxe-option label="<=" value="<=" />
            <vxe-option label="LIKE" value="LIKE" />
            <vxe-option label="BETWEEN" value="BETWEEN" />
          </vxe-select>
        </template>
      </vxe-column>
    </vxe-colgroup>
    <vxe-column title="显示类型" width="10%" field="htmlType">
      <template #default="{ row }">
        <vxe-select v-model="row.htmlType" placeholder="请选择显示类型">
          <vxe-option label="文本框" value="input" />
          <vxe-option label="文本域" value="textarea" />
          <vxe-option label="下拉框" value="select" />
          <vxe-option label="单选框" value="radio" />
          <vxe-option label="复选框" value="checkbox" />
          <vxe-option label="日期控件" value="datetime" />
          <vxe-option label="图片上传" value="imageUpload" />
          <vxe-option label="文件上传" value="fileUpload" />
          <vxe-option label="富文本控件" value="editor" />
        </vxe-select>
      </template>
    </vxe-column>
    <vxe-column title="字典类型" width="10%" field="dictType">
      <template #default="{ row }">
        <vxe-select v-model="row.dictType" clearable filterable placeholder="请选择字典类型">
          <vxe-option
            v-for="dict in dictOptions"
            :key="dict.id"
            :label="dict.name"
            :value="dict.type"
          />
        </vxe-select>
      </template>
    </vxe-column>
    <vxe-column title="示例" field="example">
      <template #default="{ row }">
        <vxe-input v-model="row.example" placeholder="请输入示例" />
      </template>
    </vxe-column>
  </vxe-table>
</template>
<script setup lang="ts">
import { PropType } from 'vue'
import { DictTypeVO } from '@/api/system/dict/types'
import { CodegenColumnVO } from '@/api/infra/codegen/types'
import { listSimpleDictTypeApi } from '@/api/system/dict/dict.type'

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
onMounted(async () => {
  await getDictOptions()
})
defineExpose({
  info: props.info
})
</script>
