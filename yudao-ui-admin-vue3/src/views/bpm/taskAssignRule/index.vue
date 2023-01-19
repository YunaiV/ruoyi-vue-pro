<template>
  <ContentWrap>
    <!-- 列表 -->
    <XTable @register="registerTable">
      <template #options_default="{ row }">
        <el-tag :key="option" v-for="option in row.options">
          {{ getAssignRuleOptionName(row.type, option) }}
        </el-tag>
      </template>
      <!-- 操作 -->
      <template #actionbtns_default="{ row }" v-if="modelId">
        <vxe-button
          type="text"
          status="primary"
          content="修改"
          icon="vxe-icon-edit"
          @click="handleUpdate(row)"
        />
      </template>
    </XTable>

    <!-- 添加/修改弹窗 -->
    <XModal v-model="openVisible" title="修改任务规则" width="800" height="35%" resize>
      <el-form
        ref="xForm"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="demo-ruleForm"
        size="default"
        status-icon
      >
        <el-form-item label="任务名称" prop="taskDefinitionName">
          <el-input -model="formData.taskDefinitionName" placeholder="请输入流标标识" disabled />
        </el-form-item>
        <el-form-item label="任务标识" prop="taskDefinitionKey">
          <el-input v-model="formData.taskDefinitionKey" placeholder="请输入任务标识" disabled />
        </el-form-item>
        <el-form-item label="规则类型" prop="type">
          <el-select v-model="formData.type" clearable style="width: 100%">
            <el-option
              v-for="dict in taskAssignRuleTypeDictDatas"
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
          <treeselect
            v-model="formData.deptIds"
            :options="deptTreeOptions"
            multiple
            flat
            :defaultExpandLevel="3"
            placeholder="请选择指定部门"
            :normalizer="normalizer"
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
      <template #footer>
        <XButton type="primary" title="保存" :loading="loading" @click="submitEvent(xForm)" />
        <XButton title="关闭" @click="resetEvent(xForm)" />
      </template>
    </XModal>
  </ContentWrap>
</template>
<script setup lang="ts" name="TaskAssignRule">
// 全局相关的 import
import { onMounted, reactive, ref } from 'vue'
import { ElInput, ElTag, ElOption, ElSelect, ElForm, ElFormItem } from 'element-plus'
import type { FormInstance } from 'element-plus'

// 业务相关的 import
import Treeselect from 'vue3-treeselect'
import 'vue3-treeselect/dist/vue3-treeselect.css'
import { useXTable } from '@/hooks/web/useXTable'
import { allSchemas } from './taskAssignRule.data'
import * as TaskAssignRuleApi from '@/api/bpm/taskAssignRule'
import { listSimpleRolesApi } from '@/api/system/role'
import { handleTree } from '@/utils/tree'
import { listSimplePostsApi } from '@/api/system/post'
import { getListSimpleUsersApi } from '@/api/system/user'
import { listSimpleUserGroupsApi } from '@/api/bpm/userGroup'
import { listSimpleDeptApi } from '@/api/system/dept'
import { DICT_TYPE, getDictOptions } from '@/utils/dict'
// import {useI18n} from "@/hooks/web/useI18n";
// import {useMessage} from "@/hooks/web/useMessage";
import { useRoute } from 'vue-router'

// const { t } = useI18n() // 国际化
// const message = useMessage() // 消息弹窗
// const router = useRouter() // 路由
const { query } = useRoute()

// ========== 列表相关 ==========

// 流程模型的编号。如果 modelId 非空，则用于流程模型的查看与配置
const modelId = query.modelId
// 流程定义的编号。如果 processDefinitionId 非空，则用于流程定义的查看，不支持配置
const processDefinitionId = query.processDefinitionId
// 查询参数
const queryParams = reactive({
  modelId: modelId,
  processDefinitionId: processDefinitionId
})
const [registerTable] = useXTable({
  allSchemas: allSchemas,
  params: queryParams,
  getListApi: TaskAssignRuleApi.getTaskAssignRuleList,
  isList: false // TODO 如果 isList 改成 true 时，进入页面不会加载数据
})
// 修改任务责任表单
const xForm = ref<FormInstance>()
const formData = ref()
const formRules = reactive({
  type: [{ required: true, message: '规则类型不能为空', trigger: 'change' }],
  roleIds: [{ required: true, message: '指定角色不能为空', trigger: 'change' }],
  deptIds: [{ required: true, message: '指定部门不能为空', trigger: 'change' }],
  postIds: [{ required: true, message: '指定岗位不能为空', trigger: 'change' }],
  userIds: [{ required: true, message: '指定用户不能为空', trigger: 'change' }],
  userGroupIds: [{ required: true, message: '指定用户组不能为空', trigger: 'change' }],
  scripts: [{ required: true, message: '指定脚本不能为空', trigger: 'change' }]
})
const loading = ref(false)

const roleOptions = ref() // 角色列表
const deptOptions = ref() // 部门列表
const deptTreeOptions = ref()
const postOptions = ref() // 岗位列表
const userOptions = ref() // 用户列表
const userGroupOptions = ref() // 用户组列表
const taskAssignScriptDictDatas = getDictOptions(DICT_TYPE.BPM_TASK_ASSIGN_SCRIPT)
const taskAssignRuleTypeDictDatas = getDictOptions(DICT_TYPE.BPM_TASK_ASSIGN_RULE_TYPE)

// 修改任务分配规则
const openVisible = ref(false)

// 添加修改弹窗提交修改
const submitEvent = async (formEl: FormInstance | undefined) => {
  console.log('确认')
  if (!formEl) return
  loading.value = true
  await formEl.validate((valid, fields) => {
    if (valid) {
      console.log('submit!')
      loading.value = false
      openVisible.value = false
    } else {
      console.log('error submit!', fields)
      loading.value = false
    }
  })
}

const resetEvent = (formEl: FormInstance | undefined) => {
  if (!formEl) return
  formEl.resetFields()
  openVisible.value = false
}

// 修改任务分配规则
const handleUpdate = (row) => {
  console.log(row, '修改i数据')
  openVisible.value = true
  formData.value = { ...row }
}
// 类型字典
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
// 格式化部门的下拉框
const normalizer = (node) => {
  return {
    id: node.id,
    label: node.name,
    children: node.children
  }
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
