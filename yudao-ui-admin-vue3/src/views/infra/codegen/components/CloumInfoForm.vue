<template>
  <vxe-table
    ref="dragTable"
    :data="info"
    max-height="600"
    stripe
    :column-config="{ resizable: true }"
  >
    <vxe-column title="字段列名" field="columnName" fixed="left" width="80" />
    <vxe-column title="字段描述" field="columnComment">
      <template #default="{ row }">
        <el-input v-model="row.columnComment" />
      </template>
    </vxe-column>
    <vxe-column title="物理类型" field="dataType" width="10%" />
    <vxe-column title="Java类型" width="10%" field="javaType">
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
    </vxe-column>
    <vxe-column title="java属性" width="10%" field="javaField">
      <template #default="{ row }">
        <el-input v-model="row.javaField" />
      </template>
    </vxe-column>
    <vxe-column title="插入" width="4%" field="createOperation">
      <template #default="{ row }">
        <vxe-checkbox true-label="true" false-label="false" v-model="row.createOperation" />
      </template>
    </vxe-column>
    <vxe-column title="编辑" width="4%" field="updateOperation">
      <template #default="{ row }">
        <vxe-checkbox true-label="true" false-label="false" v-model="row.updateOperation" />
      </template>
    </vxe-column>
    <vxe-column title="列表" width="4%" field="listOperationResult">
      <template #default="{ row }">
        <vxe-checkbox true-label="true" false-label="false" v-model="row.listOperationResult" />
      </template>
    </vxe-column>
    <vxe-column title="查询" width="4%" field="listOperation">
      <template #default="{ row }">
        <vxe-checkbox true-label="true" false-label="false" v-model="row.listOperation" />
      </template>
    </vxe-column>
    <vxe-column title="查询方式" width="8%" field="listOperationCondition">
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
    </vxe-column>
    <vxe-column title="允许空" width="4%" field="nullable">
      <template #default="{ row }">
        <vxe-checkbox true-label="true" false-label="false" v-model="row.nullable" />
      </template>
    </vxe-column>
    <vxe-column title="显示类型" width="10%" field="htmlType">
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
    </vxe-column>
    <vxe-column title="字典类型" width="10%" field="dictType">
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
    </vxe-column>
    <vxe-column title="示例" field="example">
      <template #default="{ row }">
        <el-input v-model="row.example" />
      </template>
    </vxe-column>
  </vxe-table>
</template>
<script setup lang="ts">
import { onMounted, PropType, ref } from 'vue'
import { ElInput, ElSelect, ElOption } from 'element-plus'
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
