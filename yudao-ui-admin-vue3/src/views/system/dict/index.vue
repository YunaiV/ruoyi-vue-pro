<template>
  <div class="flex">
    <!-- ====== 字典分类 ====== -->
    <el-card class="w-1/2 dict" :gutter="12" shadow="always">
      <template #header>
        <div class="card-header">
          <span>字典分类</span>
        </div>
      </template>
      <XTable @register="registerType" @cell-click="cellClickEvent">
        <!-- 操作：新增类型 -->
        <template #toolbar_buttons>
          <XButton
            type="primary"
            preIcon="ep:zoom-in"
            :title="t('action.add')"
            v-hasPermi="['system:dict:create']"
            @click="handleTypeCreate()"
          />
        </template>
        <template #actionbtns_default="{ row }">
          <!-- 操作：编辑类型 -->
          <XTextButton
            preIcon="ep:edit"
            :title="t('action.edit')"
            v-hasPermi="['system:dict:update']"
            @click="handleTypeUpdate(row.id)"
          />
          <!-- 操作：删除类型 -->
          <XTextButton
            preIcon="ep:delete"
            :title="t('action.del')"
            v-hasPermi="['system:dict:delete']"
            @click="typeDeleteData(row.id)"
          />
        </template>
      </XTable>
      <!-- @星语：分页和列表重叠在一起了 -->
    </el-card>
    <!-- ====== 字典数据 ====== -->
    <el-card class="w-1/2 dict ml-3" :gutter="12" shadow="hover">
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
        <!-- 列表 -->
        <XTable @register="registerData">
          <!-- 操作：新增数据 -->
          <template #toolbar_buttons>
            <XButton
              type="primary"
              preIcon="ep:zoom-in"
              :title="t('action.add')"
              v-hasPermi="['system:dict:create']"
              @click="handleDataCreate()"
            />
          </template>
          <template #actionbtns_default="{ row }">
            <!-- 操作：修改数据 -->
            <XTextButton
              v-hasPermi="['system:dict:update']"
              preIcon="ep:edit"
              :title="t('action.edit')"
              @click="handleDataUpdate(row.id)"
            />
            <!-- 操作：删除数据 -->
            <XTextButton
              v-hasPermi="['system:dict:delete']"
              preIcon="ep:delete"
              :title="t('action.del')"
              @click="dataDeleteData(row.id)"
            />
          </template>
        </XTable>
      </div>
    </el-card>
    <XModal id="dictModel" v-model="dialogVisible" :title="dialogTitle">
      <Form
        v-if="['typeCreate', 'typeUpdate'].includes(actionType)"
        :schema="DictTypeSchemas.allSchemas.formSchema"
        :rules="DictTypeSchemas.dictTypeRules"
        ref="typeFormRef"
      >
        <template #type>
          <template v-if="actionType == 'typeUpdate'">
            <el-tag>{{ dictTypeValue }}</el-tag>
          </template>
          <template v-else><el-input v-model="dictTypeValue" /> </template>
        </template>
      </Form>
      <Form
        v-if="['dataCreate', 'dataUpdate'].includes(actionType)"
        :schema="DictDataSchemas.allSchemas.formSchema"
        :rules="DictDataSchemas.dictDataRules"
        ref="dataFormRef"
      />
      <!-- 操作按钮 -->
      <template #footer>
        <XButton
          v-if="['typeCreate', 'typeUpdate'].includes(actionType)"
          type="primary"
          :title="t('action.save')"
          :loading="actionLoading"
          @click="submitTypeForm"
        />
        <XButton
          v-if="['dataCreate', 'dataUpdate'].includes(actionType)"
          type="primary"
          :title="t('action.save')"
          :loading="actionLoading"
          @click="submitDataForm"
        />
        <XButton :title="t('dialog.close')" @click="dialogVisible = false" />
      </template>
    </XModal>
  </div>
</template>
<script setup lang="ts" name="Dict">
import { VxeTableEvents } from 'vxe-table'
import type { FormExpose } from '@/components/Form'
import * as DictTypeSchemas from './dict.type'
import * as DictDataSchemas from './dict.data'
import * as DictTypeApi from '@/api/system/dict/dict.type'
import * as DictDataApi from '@/api/system/dict/dict.data'
import { DictDataVO, DictTypeVO } from '@/api/system/dict/types'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗

const [registerType, { reload: typeGetList, deleteData: typeDeleteData }] = useXTable({
  allSchemas: DictTypeSchemas.allSchemas,
  getListApi: DictTypeApi.getDictTypePageApi,
  deleteApi: DictTypeApi.deleteDictTypeApi
})

const queryParams = reactive({
  dictType: null
})
const [registerData, { reload: dataGetList, deleteData: dataDeleteData }] = useXTable({
  allSchemas: DictDataSchemas.allSchemas,
  params: queryParams,
  getListApi: DictDataApi.getDictDataPageApi,
  deleteApi: DictDataApi.deleteDictDataApi
})
// ========== 字典分类列表相关 ==========
const dictTypeValue = ref('')
// 字典分类修改操作
const handleTypeCreate = () => {
  dictTypeValue.value = ''
  setDialogTile('typeCreate')
}
const handleTypeUpdate = async (rowId: number) => {
  setDialogTile('typeUpdate')
  // 设置数据
  const res = await DictTypeApi.getDictTypeApi(rowId)
  dictTypeValue.value = res.type
  unref(typeFormRef)?.setValues(res)
}

// 字典数据修改操作
const handleDataCreate = () => {
  setDialogTile('dataCreate')
}
const handleDataUpdate = async (rowId: number) => {
  setDialogTile('dataUpdate')
  // 设置数据
  const res = await DictDataApi.getDictDataApi(rowId)
  unref(dataFormRef)?.setValues(res)
}
// 字典分类点击行事件
const parentType = ref('')
const tableTypeSelect = ref(false)
const cellClickEvent: VxeTableEvents.CellClick = async ({ row }) => {
  tableTypeSelect.value = true
  queryParams.dictType = row['type']
  await dataGetList()
  parentType.value = row['type']
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
    if (valid && dictTypeValue.value != '') {
      actionLoading.value = true
      // 提交请求
      try {
        const data = unref(typeFormRef)?.formModel as DictTypeVO
        if (actionType.value === 'typeCreate') {
          data.type = dictTypeValue.value
          await DictTypeApi.createDictTypeApi(data)
          message.success(t('common.createSuccess'))
        } else if (actionType.value === 'typeUpdate') {
          await DictTypeApi.updateDictTypeApi(data)
          message.success(t('common.updateSuccess'))
        }
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
        typeGetList()
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
          message.success(t('common.createSuccess'))
        } else if (actionType.value === 'dataUpdate') {
          await DictDataApi.updateDictDataApi(data)
          message.success(t('common.updateSuccess'))
        }
        dialogVisible.value = false
      } finally {
        actionLoading.value = false
        dataGetList()
      }
    }
  })
}
</script>
