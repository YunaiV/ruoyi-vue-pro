<script setup lang="ts">
import type { Ref } from 'vue';

import type { SimpleFlowNode } from '../consts';

import type { BpmUserGroupApi } from '#/api/bpm/userGroup';
import type { SystemDeptApi } from '#/api/system/dept';
import type { SystemPostApi } from '#/api/system/post';
import type { SystemRoleApi } from '#/api/system/role';
import type { SystemUserApi } from '#/api/system/user';

import { inject, onMounted, provide, ref, watch } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { BpmModelFormType, BpmNodeTypeEnum } from '@vben/constants';
import { handleTree } from '@vben/utils';

import { Button } from 'ant-design-vue';

import { getForm } from '#/api/bpm/form';
import { getUserGroupSimpleList } from '#/api/bpm/userGroup';
import { getSimpleDeptList } from '#/api/system/dept';
import { getSimplePostList } from '#/api/system/post';
import { getSimpleRoleList } from '#/api/system/role';
import { getSimpleUserList } from '#/api/system/user';

import { NODE_DEFAULT_TEXT, NodeId } from '../consts';
import SimpleProcessModel from './simple-process-model.vue';

defineOptions({
  name: 'SimpleProcessDesigner',
});

const props = defineProps({
  modelName: {
    type: String,
    required: false,
    default: undefined,
  },
  // 流程表单 ID
  modelFormId: {
    type: Number,
    required: false,
    default: undefined,
  },
  // 表单类型
  modelFormType: {
    type: Number,
    required: false,
    default: BpmModelFormType.NORMAL,
  },
  // 可发起流程的人员编号
  startUserIds: {
    type: Array,
    required: false,
    default: undefined,
  },
  // 可发起流程的部门编号
  startDeptIds: {
    type: Array,
    required: false,
    default: undefined,
  },
});
// 保存成功事件
const emits = defineEmits(['success']);
const processData = inject('processData') as Ref;
const loading = ref(false);
const formFields = ref<string[]>([]);
const formType = ref(props.modelFormType);

// 监听 modelFormType 变化
watch(
  () => props.modelFormType,
  (newVal) => {
    formType.value = newVal;
  },
);

// 监听 modelFormId 变化
watch(
  () => props.modelFormId,
  async (newVal) => {
    if (newVal) {
      const form = await getForm(newVal);
      formFields.value = form?.fields;
    } else {
      // 如果 modelFormId 为空，清空表单字段
      formFields.value = [];
    }
  },
  { immediate: true },
);

const roleOptions = ref<SystemRoleApi.Role[]>([]); // 角色列表
const postOptions = ref<SystemPostApi.Post[]>([]); // 岗位列表
const userOptions = ref<SystemUserApi.User[]>([]); // 用户列表
const deptOptions = ref<SystemDeptApi.Dept[]>([]); // 部门列表
const deptTreeOptions = ref();
const userGroupOptions = ref<BpmUserGroupApi.UserGroup[]>([]); // 用户组列表

provide('formFields', formFields);
provide('formType', formType);
provide('roleList', roleOptions);
provide('postList', postOptions);
provide('userList', userOptions);
provide('deptList', deptOptions);
provide('userGroupList', userGroupOptions);
provide('deptTree', deptTreeOptions);
provide('startUserIds', props.startUserIds);
provide('startDeptIds', props.startDeptIds);
provide('tasks', []);
provide('processInstance', {});
const processNodeTree = ref<SimpleFlowNode | undefined>();
provide('processNodeTree', processNodeTree);

// 创建错误提示弹窗
const [ErrorModal, errorModalApi] = useVbenModal({
  fullscreenButton: false,
});

// 添加更新模型的方法
function updateModel() {
  if (!processNodeTree.value) {
    processNodeTree.value = {
      name: '发起人',
      type: BpmNodeTypeEnum.START_USER_NODE,
      id: NodeId.START_USER_NODE_ID,
      // 默认为空，需要进行配置
      showText: '',
      childNode: {
        id: NodeId.END_EVENT_NODE_ID,
        name: '结束',
        type: BpmNodeTypeEnum.END_EVENT_NODE,
      },
    };
    // 初始化时也触发一次保存
    saveSimpleFlowModel(processNodeTree.value);
  }
}

async function saveSimpleFlowModel(
  simpleModelNode: SimpleFlowNode | undefined,
) {
  if (!simpleModelNode) {
    return;
  }

  try {
    processData.value = simpleModelNode;
    emits('success', simpleModelNode);
  } catch (error) {
    console.error('保存失败:', error);
  }
}

/**
 * 校验节点设置。 暂时以 showText 为空作为节点错误配置的判断条件
 */
function validateNode(
  node: SimpleFlowNode | undefined,
  errorNodes: SimpleFlowNode[],
) {
  if (node) {
    const { type, showText, conditionNodes } = node;
    if (type === BpmNodeTypeEnum.END_EVENT_NODE) {
      return;
    }

    if (
      type === BpmNodeTypeEnum.CONDITION_BRANCH_NODE ||
      type === BpmNodeTypeEnum.PARALLEL_BRANCH_NODE ||
      type === BpmNodeTypeEnum.INCLUSIVE_BRANCH_NODE
    ) {
      // 1. 分支节点, 先校验各个分支
      conditionNodes?.forEach((item) => {
        validateNode(item, errorNodes);
      });
      // 2. 校验孩子节点
      validateNode(node.childNode, errorNodes);
    } else {
      if (!showText) {
        errorNodes.push(node);
      }
      validateNode(node.childNode, errorNodes);
    }
  }
}

onMounted(async () => {
  try {
    loading.value = true;
    // 获得角色列表
    roleOptions.value = await getSimpleRoleList();
    // 获得岗位列表
    postOptions.value = await getSimplePostList();
    // 获得用户列表
    userOptions.value = await getSimpleUserList();
    // 获得部门列表
    const deptList = await getSimpleDeptList();
    deptOptions.value = deptList;
    // 转换成树形结构
    deptTreeOptions.value = handleTree(deptList);
    // 获取用户组列表
    userGroupOptions.value = await getUserGroupSimpleList();
    // 加载流程数据
    if (processData.value) {
      processNodeTree.value = processData?.value;
    } else {
      updateModel();
    }
  } finally {
    loading.value = false;
  }
});

const validate = async () => {
  const errorNodes: SimpleFlowNode[] = [];
  validateNode(processNodeTree.value, errorNodes);
  if (errorNodes.length === 0) {
    return true;
  } else {
    // 设置错误节点数据并打开弹窗
    errorModalApi.setData(errorNodes);
    errorModalApi.open();
    return false;
  }
};
defineExpose({ validate });
</script>
<template>
  <div v-loading="loading">
    <SimpleProcessModel
      v-if="processNodeTree"
      :flow-node="processNodeTree"
      :readonly="false"
      @save="saveSimpleFlowModel"
    />
    <ErrorModal title="流程设计校验不通过" class="w-2/5">
      <div class="mb-2 text-base">以下节点配置不完善，请修改相关配置</div>
      <div
        class="mb-3 rounded-md p-2 text-sm"
        v-for="(item, index) in errorModalApi.getData()"
        :key="index"
      >
        {{ item.name }} : {{ NODE_DEFAULT_TEXT.get(item.type) }}
      </div>
      <template #footer>
        <Button type="primary" @click="errorModalApi.close()">知道了</Button>
      </template>
    </ErrorModal>
  </div>
</template>
