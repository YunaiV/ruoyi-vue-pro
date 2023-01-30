<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #options_default="{ row }">
        <span :key="option" v-for="option in row.options">
          <el-tag>
            {{ getAssignRuleOptionName(row.type, option) }}
          </el-tag>
          &nbsp;
        </span>
      </template>
      <!-- 操作 -->
      <template #actionbtns_default="{ row }" v-if="modelId">
        <!-- 操作：修改 -->
        <XTextButton
          preIcon="ep:edit"
          :title="t('action.edit')"
          v-hasPermi="['bpm:task-assign-rule:update']"
          @click="handleUpdate(row)"
        />
      </template>
    </XTable>

    <!-- 添加/修改弹窗 -->
    <XModal v-model="dialogVisible" title="修改任务规则" width="800" height="35%">
      <el-form ref="formRef" :model="formData" :rules="rules" label-width="80px">
        <el-form-item label="任务名称" prop="taskDefinitionName">
          <el-input v-model="formData.taskDefinitionName" placeholder="请输入流标标识" disabled />
        </el-form-item>
        <el-form-item label="任务标识" prop="taskDefinitionKey">
          <el-input v-model="formData.taskDefinitionKey" placeholder="请输入任务标识" disabled />
        </el-form-item>
        <el-form-item label="规则类型" prop="type">
          <el-select v-model="formData.type" clearable style="width: 100%">
            <el-option
              v-for="dict in getDictOptions(DICT_TYPE.BPM_TASK_ASSIGN_RULE_TYPE)"
              :key="parseInt(dict.value)"
              :label="dict.label"
              :value="parseInt(dict.value)"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formData.type === 10" label="指定角色" prop="roleIds">
          <el-select v-model="formData.roleIds" multiple clearable style="width: 100%">
            <el-option
              v-for="item in roleOptions"
              :key="parseInt(item.id)"
              :label="item.name"
              :value="parseInt(item.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="指定部门"
          prop="deptIds"
          span="24"
          v-if="formData.type === 20 || formData.type === 21"
        >
          <el-tree-select
            ref="treeRef"
            v-model="formData.deptIds"
            node-key="id"
            show-checkbox
            :props="defaultProps"
            :data="deptTreeOptions"
            empty-text="加载中，请稍后"
            multiple
          />
        </el-form-item>
        <el-form-item label="指定岗位" prop="postIds" span="24" v-if="formData.type === 22">
          <el-select v-model="formData.postIds" multiple clearable style="width: 100%">
            <el-option
              v-for="item in postOptions"
              :key="parseInt(item.id)"
              :label="item.name"
              :value="parseInt(item.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          label="指定用户"
          prop="userIds"
          span="24"
          v-if="formData.type === 30 || formData.type === 31 || formData.type === 32"
        >
          <el-select v-model="formData.userIds" multiple clearable style="width: 100%">
            <el-option
              v-for="item in userOptions"
              :key="parseInt(item.id)"
              :label="item.nickname"
              :value="parseInt(item.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="指定用户组" prop="userGroupIds" v-if="formData.type === 40">
          <el-select v-model="formData.userGroupIds" multiple clearable style="width: 100%">
            <el-option
              v-for="item in userGroupOptions"
              :key="parseInt(item.id)"
              :label="item.name"
              :value="parseInt(item.id)"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="指定脚本" prop="scripts" v-if="formData.type === 50">
          <el-select v-model="formData.scripts" multiple clearable style="width: 100%">
            <el-option
              v-for="dict in taskAssignScriptDictDatas"
              :key="parseInt(dict.value)"
              :label="dict.label"
              :value="parseInt(dict.value)"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <!-- 操作按钮 -->
      <template #footer>
        <!-- 按钮：保存 -->
        <XButton
          type="primary"
          :title="t('action.save')"
          :loading="actionLoading"
          @click="submitForm"
        />
        <!-- 按钮：关闭 -->
        <XButton
          :loading="actionLoading"
          :title="t('dialog.close')"
          @click="dialogVisible = false"
        />
      </template>
    </XModal>
  </ContentWrap>
</template>
<script setup lang="ts" name="TaskAssignRule">
// 全局相关的 import
import { FormInstance } from 'element-plus'
// 业务相关的 import
import * as TaskAssignRuleApi from '@/api/bpm/taskAssignRule'
import { listSimpleRolesApi } from '@/api/system/role'
import { listSimplePostsApi } from '@/api/system/post'
import { getListSimpleUsersApi } from '@/api/system/user'
import { listSimpleUserGroupsApi } from '@/api/bpm/userGroup'
import { listSimpleDeptApi } from '@/api/system/dept'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
import { handleTree, defaultProps } from '@/utils/tree'
import { allSchemas, rules } from './taskAssignRule.data'

const { t } = useI18n() // 国际化
const message = useMessage() // 消息弹窗
const { query } = useRoute()

// ========== 列表相关 ==========

const roleOptions = ref() // 角色列表
const deptOptions = ref() // 部门列表
const deptTreeOptions = ref()
const postOptions = ref() // 岗位列表
const userOptions = ref() // 用户列表
const userGroupOptions = ref() // 用户组列表
const taskAssignScriptDictDatas = getDictOptions(DICT_TYPE.BPM_TASK_ASSIGN_SCRIPT)

// 流程模型的编号。如果 modelId 非空，则用于流程模型的查看与配置
const modelId = query.modelId
// 流程定义的编号。如果 processDefinitionId 非空，则用于流程定义的查看，不支持配置
const processDefinitionId = query.processDefinitionId
// 查询参数
const queryParams = reactive({
  modelId: modelId,
  processDefinitionId: processDefinitionId
})
const [registerTable, { reload }] = useXTable({
  allSchemas: allSchemas,
  params: queryParams,
  getListApi: TaskAssignRuleApi.getTaskAssignRuleList,
  isList: true
})

// 翻译规则范围
const getAssignRuleOptionName = (type, option) => {
  if (type === 10) {
    for (const roleOption of roleOptions.value) {
      if (roleOption.id === option) {
        return roleOption.name
      }
    }
  } else if (type === 20 || type === 21) {
    for (const deptOption of deptOptions.value) {
      if (deptOption.id === option) {
        return deptOption.name
      }
    }
  } else if (type === 22) {
    for (const postOption of postOptions.value) {
      if (postOption.id === option) {
        return postOption.name
      }
    }
  } else if (type === 30 || type === 31 || type === 32) {
    for (const userOption of userOptions.value) {
      if (userOption.id === option) {
        return userOption.nickname
      }
    }
  } else if (type === 40) {
    for (const userGroupOption of userGroupOptions.value) {
      if (userGroupOption.id === option) {
        return userGroupOption.name
      }
    }
  } else if (type === 50) {
    option = option + '' // 转换成 string
    for (const dictData of taskAssignScriptDictDatas) {
      if (dictData.value === option) {
        return dictData.label
      }
    }
  }
  return '未知(' + option + ')'
}

// ========== 修改相关 ==========

// 修改任务责任表单
const actionLoading = ref(false) // 遮罩层
const dialogVisible = ref(false) // 是否显示弹出层
const formRef = ref<FormInstance>()
const formData = ref() // 表单数据

// 提交按钮
const submitForm = async () => {
  // 参数校验
  const elForm = unref(formRef)
  if (!elForm) return
  const valid = await elForm.validate()
  if (!valid) return
  // 构建表单
  let form = {
    ...formData.value,
    taskDefinitionName: undefined
  }
  // 将 roleIds 等选项赋值到 options 中
  if (form.type === 10) {
    form.options = form.roleIds
  } else if (form.type === 20 || form.type === 21) {
    form.options = form.deptIds
  } else if (form.type === 22) {
    form.options = form.postIds
  } else if (form.type === 30 || form.type === 31 || form.type === 32) {
    form.options = form.userIds
  } else if (form.type === 40) {
    form.options = form.userGroupIds
  } else if (form.type === 50) {
    form.options = form.scripts
  }
  form.roleIds = undefined
  form.deptIds = undefined
  form.postIds = undefined
  form.userIds = undefined
  form.userGroupIds = undefined
  form.scripts = undefined
  // 设置提交中
  actionLoading.value = true
  // 提交请求
  try {
    const data = form as TaskAssignRuleApi.TaskAssignVO
    // 新增
    if (!data.id) {
      await TaskAssignRuleApi.createTaskAssignRule(data)
      message.success(t('common.createSuccess'))
      // 修改
    } else {
      await TaskAssignRuleApi.updateTaskAssignRule(data)
      message.success(t('common.updateSuccess'))
    }
    dialogVisible.value = false
  } finally {
    actionLoading.value = false
    // 刷新列表
    await reload()
  }
}

// 修改任务分配规则
const handleUpdate = (row) => {
  // 1. 先重置表单
  formData.value = {}
  // 2. 再设置表单
  formData.value = {
    ...row,
    modelId: modelId,
    options: [],
    roleIds: [],
    deptIds: [],
    postIds: [],
    userIds: [],
    userGroupIds: [],
    scripts: []
  }
  // 将 options 赋值到对应的 roleIds 等选项
  if (row.type === 10) {
    formData.value.roleIds.push(...row.options)
  } else if (row.type === 20 || row.type === 21) {
    formData.value.deptIds.push(...row.options)
  } else if (row.type === 22) {
    formData.value.postIds.push(...row.options)
  } else if (row.type === 30 || row.type === 31 || row.type === 32) {
    formData.value.userIds.push(...row.options)
  } else if (row.type === 40) {
    formData.value.userGroupIds.push(...row.options)
  } else if (row.type === 50) {
    formData.value.scripts.push(...row.options)
  }
  // 打开弹窗
  dialogVisible.value = true
  actionLoading.value = false
}

// ========== 初始化 ==========
onMounted(() => {
  // 获得角色列表
  roleOptions.value = []
  listSimpleRolesApi().then((data) => {
    roleOptions.value.push(...data)
  })
  // 获得部门列表
  deptOptions.value = []
  deptTreeOptions.value = []
  listSimpleDeptApi().then((data) => {
    deptOptions.value.push(...data)
    deptTreeOptions.value.push(...handleTree(data, 'id'))
  })
  // 获得岗位列表
  postOptions.value = []
  listSimplePostsApi().then((data) => {
    postOptions.value.push(...data)
  })
  // 获得用户列表
  userOptions.value = []
  getListSimpleUsersApi().then((data) => {
    userOptions.value.push(...data)
  })
  // 获得用户组列表
  userGroupOptions.value = []
  listSimpleUserGroupsApi().then((data) => {
    userGroupOptions.value.push(...data)
  })
})
</script>
