<script setup lang="ts">
import { ref, unref, onMounted } from 'vue'
import { DICT_TYPE } from '@/utils/dict'
import { useI18n } from '@/hooks/web/useI18n'
import { FormExpose } from '@/components/Form'
import * as DictTypeSchemas from './dict.type'
import * as DictDataSchemas from './dict.data'
import { useTable } from '@/hooks/web/useTable'
import { ElCard, ElMessage } from 'element-plus'
import * as DictTypeApi from '@/api/system/dict/dict.type'
import * as DictDataApi from '@/api/system/dict/dict.data'
import { DictDataVO, DictTypeVO } from '@/api/system/dict/types'
const { t } = useI18n() // 国际化
// ========== 字典分类列表相关 ==========
const {
  register: typeRegister,
  tableObject: typeTableObject,
  methods: typeMethods
} = useTable<DictTypeVO>({
  getListApi: DictTypeApi.getDictTypePageApi,
  delListApi: DictTypeApi.deleteDictTypeApi
})
const {
  getList: getTypeList,
  setSearchParams: setTypeSearchParams,
  delList: delTypeList
} = typeMethods

// 字典分类修改操作
const handleTypeCreate = () => {
  setDialogTile('typeCreate')
  // 重置表单
  unref(typeFormRef)?.getElFormRef()?.resetFields()
}
const handleTypeUpdate = async (row: DictTypeVO) => {
  setDialogTile('typeUpdate')
  // 设置数据
  const res = await DictTypeApi.getDictTypeApi(row.id)
  unref(typeFormRef)?.setValues(res)
}

// ========== 字典数据列表相关 ==========
const tableTypeSelect = ref(false)
const {
  register: dataRegister,
  tableObject: dataTableObject,
  methods: dataMethods
} = useTable<DictDataVO>({
  getListApi: DictDataApi.getDictDataPageApi,
  delListApi: DictDataApi.deleteDictDataApi
})
const {
  getList: getDataList,
  setSearchParams: setDataSearchParams,
  delList: delDataList
} = dataMethods

// 字典数据修改操作
const handleDataCreate = () => {
  setDialogTile('dataCreate')
  // 重置表单
  unref(dataFormRef)?.getElFormRef()?.resetFields()
}
const handleDataUpdate = async (row: DictDataVO) => {
  setDialogTile('dataUpdate')
  // 设置数据
  const res = await DictDataApi.getDictDataApi(row.id)
  unref(dataFormRef)?.setValues(res)
}
// 字典分类点击行事件
const parentType = ref('')
const onClickType = async (data: { [key: string]: any }) => {
  tableTypeSelect.value = true
  dataTableObject.params = {
    dictType: data.type
  }
  getDataList()
  parentType.value = data.type
}
// 弹出框
const dialogVisible = ref(false) // 是否显示弹出层
const dialogTitle = ref('edit') // 弹出层标题
const actionLoading = ref(false) // 遮罩层
const typeFormRef = ref<FormExpose>() // 分类表单 Ref
const dataFormRef = ref<FormExpose>() // 数据表单 Ref
const actionType = ref('') // 操作按钮的类型
// 设置标题
const setDialogTile = (type: string) => {
  dialogTitle.value = t('action.' + type)
  actionType.value = type
  dialogVisible.value = true
}
// 提交按钮
const submitTypeForm = async () => {
  const elForm = unref(typeFormRef)?.getElFormRef()
  if (!elForm) return
  elForm.validate(async (valid) => {
    if (valid) {
      actionLoading.value = true
      // 提交请求
      try {
        const data = unref(typeFormRef)?.formModel as DictTypeVO
        if (actionType.value === 'typeCreate') {
          await DictTypeApi.createDictTypeApi(data)
          ElMessage.success(t('common.createSuccess'))
        } else if (actionType.value === 'typeUpdate') {
          await DictTypeApi.updateDictTypeApi(data)
          ElMessage.success(t('common.updateSuccess'))
        }
        // 操作成功，重新加载列表
        await getTypeList()
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
      }
    }
  })
}
const submitDataForm = async () => {
  const elForm = unref(dataFormRef)?.getElFormRef()
  if (!elForm) return
  elForm.validate(async (valid) => {
    if (valid) {
      actionLoading.value = true
      // 提交请求
      try {
        const data = unref(dataFormRef)?.formModel as DictDataVO
        if (actionType.value === 'dataCreate') {
          data.dictType = parentType.value
          await DictDataApi.createDictDataApi(data)
          ElMessage.success(t('common.createSuccess'))
        } else if (actionType.value === 'dataUpdate') {
          await DictDataApi.updateDictDataApi(data)
          ElMessage.success(t('common.updateSuccess'))
        }
        await getDataList()
        // 操作成功，重新加载列表
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
      }
    }
  })
}
// 初始化查询
onMounted(async () => {
  await getTypeList()
  typeTableObject.tableList[0] && onClickType(typeTableObject.tableList[0])
})
</script>

<template>
  <div class="flex">
    <el-card class="w-1/2 dict" :gutter="12" shadow="always">
      <template #header>
        <div class="card-header">
          <span>字典分类</span>
        </div>
      </template>
      <Search
        :schema="DictTypeSchemas.allSchemas.searchSchema"
        @search="setTypeSearchParams"
        @reset="setTypeSearchParams"
      />
      <!-- 操作工具栏 -->
      <div class="mb-10px">
        <el-button type="primary" v-hasPermi="['system:dict:create']" @click="handleTypeCreate">
          <Icon icon="ep:zoom-in" class="mr-5px" /> {{ t('action.add') }}
        </el-button>
      </div>
      <!-- 列表 -->
      <Table
        @row-click="onClickType"
        :columns="DictTypeSchemas.allSchemas.tableColumns"
        :selection="false"
        :data="typeTableObject.tableList"
        :loading="typeTableObject.loading"
        :pagination="{
          total: typeTableObject.total
        }"
        :highlight-current-row="true"
        v-model:pageSize="typeTableObject.pageSize"
        v-model:currentPage="typeTableObject.currentPage"
        @register="typeRegister"
      >
        <template #status="{ row }">
          <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
        </template>
        <template #action="{ row }">
          <el-button
            link
            type="primary"
            v-hasPermi="['system:dict:update']"
            @click="handleTypeUpdate(row)"
          >
            <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
          </el-button>
          <el-button
            link
            type="primary"
            v-hasPermi="['system:dict:delete']"
            @click="delTypeList(row.id, false)"
          >
            <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
          </el-button>
        </template>
      </Table>
    </el-card>
    <el-card class="w-1/2 dict" style="margin-left: 10px" :gutter="12" shadow="hover">
      <template #header>
        <div class="card-header">
          <span>字典数据</span>
        </div>
      </template>
      <!-- 列表 -->
      <div v-if="!tableTypeSelect">
        <span>请从左侧选择</span>
      </div>
      <div v-if="tableTypeSelect">
        <Search
          :schema="DictDataSchemas.allSchemas.searchSchema"
          @search="setDataSearchParams"
          @reset="setDataSearchParams"
        />
        <!-- 操作工具栏 -->
        <div class="mb-10px">
          <el-button type="primary" v-hasPermi="['system:dict:create']" @click="handleDataCreate">
            <Icon icon="ep:zoom-in" class="mr-1px" /> {{ t('action.add') }}
          </el-button>
        </div>
        <Table
          :columns="DictDataSchemas.allSchemas.tableColumns"
          :selection="false"
          :data="dataTableObject.tableList"
          :loading="dataTableObject.loading"
          :pagination="{
            total: dataTableObject.total
          }"
          v-model:pageSize="dataTableObject.pageSize"
          v-model:currentPage="dataTableObject.currentPage"
          @register="dataRegister"
        >
          <template #status="{ row }">
            <DictTag :type="DICT_TYPE.COMMON_STATUS" :value="row.status" />
          </template>
          <template #action="{ row }">
            <el-button
              link
              type="primary"
              v-hasPermi="['system:dict:update']"
              @click="handleDataUpdate(row)"
            >
              <Icon icon="ep:edit" class="mr-1px" /> {{ t('action.edit') }}
            </el-button>
            <el-button
              link
              type="primary"
              v-hasPermi="['system:dict:delete']"
              @click="delDataList(row.id, false)"
            >
              <Icon icon="ep:delete" class="mr-1px" /> {{ t('action.del') }}
            </el-button>
          </template>
        </Table>
      </div>
    </el-card>
    <Dialog v-model="dialogVisible" :title="dialogTitle">
      <Form
        v-if="['typeCreate', 'typeUpdate'].includes(actionType)"
        :schema="DictTypeSchemas.allSchemas.formSchema"
        :rules="DictTypeSchemas.dictTypeRules"
        ref="typeFormRef"
      />
      <Form
        v-if="['dataCreate', 'dataUpdate'].includes(actionType)"
        :schema="DictDataSchemas.allSchemas.formSchema"
        :rules="DictDataSchemas.dictDataRules"
        ref="dataFormRef"
      />
      <!-- 操作按钮 -->
      <template #footer>
        <el-button
          v-if="['typeCreate', 'typeUpdate'].includes(actionType)"
          type="primary"
          :loading="actionLoading"
          @click="submitTypeForm"
        >
          {{ t('action.save') }}
        </el-button>
        <el-button
          v-if="['dataCreate', 'dataUpdate'].includes(actionType)"
          type="primary"
          :loading="actionLoading"
          @click="submitDataForm"
        >
          {{ t('action.save') }}
        </el-button>
        <el-button @click="dialogVisible = false">{{ t('dialog.close') }}</el-button>
      </template>
    </Dialog>
  </div>
</template>
