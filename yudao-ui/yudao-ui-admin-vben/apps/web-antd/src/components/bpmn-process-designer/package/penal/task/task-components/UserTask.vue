<script lang="ts" setup>
import type { BpmProcessExpressionApi } from '#/api/bpm/processExpression';
import type { BpmUserGroupApi } from '#/api/bpm/userGroup';
import type { SystemPostApi } from '#/api/system/post';
import type { SystemRoleApi } from '#/api/system/role';
import type { SystemUserApi } from '#/api/system/user';

import {
  computed,
  h,
  inject,
  nextTick,
  onBeforeUnmount,
  onMounted,
  ref,
  toRaw,
  watch,
} from 'vue';

import { SelectOutlined } from '@vben/icons';
import { handleTree } from '@vben/utils';

import {
  Button,
  Form,
  FormItem,
  Select,
  SelectOption,
  Textarea,
  TreeSelect,
} from 'ant-design-vue';

import { getUserGroupSimpleList } from '#/api/bpm/userGroup';
import { getSimpleDeptList } from '#/api/system/dept';
import { getSimplePostList } from '#/api/system/post';
import { getSimpleRoleList } from '#/api/system/role';
import { getSimpleUserList } from '#/api/system/user';
import {
  CANDIDATE_STRATEGY,
  CandidateStrategy,
  FieldPermissionType,
  MULTI_LEVEL_DEPT,
} from '#/components/simple-process-design/consts';
import { useFormFieldsPermission } from '#/components/simple-process-design/helpers';

import ProcessExpressionDialog from './ProcessExpressionDialog.vue';

defineOptions({ name: 'UserTask' });
const props = defineProps({
  id: {
    type: String,
    default: '',
  },
  type: {
    type: String,
    default: '',
  },
});
const prefix = inject('prefix');
const userTaskForm = ref({
  candidateStrategy: undefined, // 分配规则
  candidateParam: [], // 分配选项
  skipExpression: '', // 跳过表达式
});
const bpmnElement = ref<any>();
const bpmnInstances = () => (window as Record<string, any>)?.bpmnInstances;

const roleOptions = ref<SystemRoleApi.Role[]>([]); // 角色列表
const deptTreeOptions = ref<any>(); // 部门树
const postOptions = ref<SystemPostApi.Post[]>([]); // 岗位列表
const userOptions = ref<SystemUserApi.User[]>([]); // 用户列表
const userGroupOptions = ref<BpmUserGroupApi.UserGroup[]>([]); // 用户组列表
const treeRef = ref<any>();

const { formFieldOptions } = useFormFieldsPermission(FieldPermissionType.READ);

// 定义 TreeSelect 的默认属性映射
const defaultProps = {
  children: 'children',
  label: 'name',
  value: 'id',
};
// 表单内用户字段选项, 必须是必填和用户选择器
const userFieldOnFormOptions = computed(() => {
  return formFieldOptions.filter((item) => item.type === 'UserSelect');
});
// 表单内部门字段选项, 必须是必填和部门选择器
const deptFieldOnFormOptions = computed(() => {
  return formFieldOptions.filter((item) => item.type === 'DeptSelect');
});

const deptLevel = ref(1);
const deptLevelLabel = computed(() => {
  let label = '部门负责人来源';
  if (
    userTaskForm.value.candidateStrategy ===
    CandidateStrategy.MULTI_LEVEL_DEPT_LEADER
  ) {
    label = `${label}(指定部门向上)`;
  } else if (
    userTaskForm.value.candidateStrategy === CandidateStrategy.FORM_DEPT_LEADER
  ) {
    label = `${label}(表单内部门向上)`;
  } else {
    label = `${label}(发起人部门向上)`;
  }
  return label;
});

const otherExtensions = ref<any>();

const resetTaskForm = () => {
  const businessObject = bpmnElement.value.businessObject;
  if (!businessObject) {
    return;
  }

  const extensionElements =
    businessObject?.extensionElements ??
    bpmnInstances().moddle.create('bpmn:ExtensionElements', { values: [] });
  userTaskForm.value.candidateStrategy = extensionElements.values?.filter(
    (ex: any) => ex.$type === `${prefix}:CandidateStrategy`,
  )?.[0]?.value;
  const candidateParamStr = extensionElements.values?.filter(
    (ex: any) => ex.$type === `${prefix}:CandidateParam`,
  )?.[0]?.value;
  if (candidateParamStr && candidateParamStr.length > 0) {
    // eslint-disable-next-line unicorn/prefer-switch
    if (userTaskForm.value.candidateStrategy === CandidateStrategy.EXPRESSION) {
      // 特殊：流程表达式，只有一个 input 输入框
      // @ts-ignore
      userTaskForm.value.candidateParam = [candidateParamStr];
    } else if (
      userTaskForm.value.candidateStrategy ===
      CandidateStrategy.MULTI_LEVEL_DEPT_LEADER
    ) {
      // 特殊：多级不部门负责人，需要通过'|'分割
      userTaskForm.value.candidateParam = candidateParamStr
        .split('|')[0]
        .split(',')
        .map((item: any) => {
          // 如果数字超出了最大安全整数范围，则将其作为字符串处理
          const num = Number(item);
          return num > Number.MAX_SAFE_INTEGER || num < -Number.MAX_SAFE_INTEGER
            ? item
            : num;
        });
      deptLevel.value = +candidateParamStr.split('|')[1];
    } else if (
      userTaskForm.value.candidateStrategy ===
        CandidateStrategy.START_USER_DEPT_LEADER ||
      userTaskForm.value.candidateStrategy ===
        CandidateStrategy.START_USER_MULTI_LEVEL_DEPT_LEADER
    ) {
      // @ts-ignore
      userTaskForm.value.candidateParam = +candidateParamStr;
      deptLevel.value = +candidateParamStr;
    } else if (
      userTaskForm.value.candidateStrategy ===
      CandidateStrategy.FORM_DEPT_LEADER
    ) {
      userTaskForm.value.candidateParam = candidateParamStr.split('|')[0];
      deptLevel.value = +candidateParamStr.split('|')[1];
    } else {
      userTaskForm.value.candidateParam = candidateParamStr
        .split(',')
        .map((item: any) => {
          // 如果数字超出了最大安全整数范围，则将其作为字符串处理
          const num = Number(item);
          return num > Number.MAX_SAFE_INTEGER || num < -Number.MAX_SAFE_INTEGER
            ? item
            : num;
        });
    }
  } else {
    userTaskForm.value.candidateParam = [];
  }

  otherExtensions.value =
    extensionElements.values?.filter(
      (ex: any) =>
        ex.$type !== `${prefix}:CandidateStrategy` &&
        ex.$type !== `${prefix}:CandidateParam`,
    ) ?? [];

  // 跳过表达式
  userTaskForm.value.skipExpression =
    businessObject.skipExpression === undefined
      ? ''
      : businessObject.skipExpression;

  // 改用通过extensionElements来存储数据

  // if (businessObject.candidateStrategy != undefined) {
  //   userTaskForm.value.candidateStrategy = parseInt(
  //     businessObject.candidateStrategy,
  //   ) as any;
  // } else {
  //   userTaskForm.value.candidateStrategy = undefined;
  // }
  // if (
  //   businessObject.candidateParam &&
  //   businessObject.candidateParam.length > 0
  // ) {
  //   if (userTaskForm.value.candidateStrategy === 60) {
  //     // 特殊：流程表达式，只有一个 input 输入框
  //     userTaskForm.value.candidateParam = [businessObject.candidateParam];
  //   } else {
  //     userTaskForm.value.candidateParam = businessObject.candidateParam
  //       .split(',')
  //       .map((item) => item);
  //   }
  // } else {
  //   userTaskForm.value.candidateParam = [];
  // }
};

/** 更新 candidateStrategy 字段时，需要清空 candidateParam，并触发 bpmn 图更新 */
const changeCandidateStrategy = () => {
  userTaskForm.value.candidateParam = [];
  deptLevel.value = 1;
  // 注释 by 芋艿：这个交互很多用户反馈费解，https://t.zsxq.com/xNmas 所以暂时屏蔽
  // if (userTaskForm.value.candidateStrategy === CandidateStrategy.FORM_USER) {
  //   // 特殊处理表单内用户字段，当只有发起人选项时应选中发起人
  //   if (!userFieldOnFormOptions.value || userFieldOnFormOptions.value.length <= 1) {
  //     userTaskForm.value.candidateStrategy = CandidateStrategy.START_USER
  //   }
  // }
  updateElementTask();
};

/** 选中某个 options 时候，更新 bpmn 图  */
const updateElementTask = () => {
  let candidateParam = Array.isArray(userTaskForm.value.candidateParam)
    ? userTaskForm.value.candidateParam.join(',')
    : userTaskForm.value.candidateParam;

  // 特殊处理多级部门情况
  if (
    userTaskForm.value.candidateStrategy ===
      CandidateStrategy.MULTI_LEVEL_DEPT_LEADER ||
    userTaskForm.value.candidateStrategy === CandidateStrategy.FORM_DEPT_LEADER
  ) {
    candidateParam += `|${deptLevel.value}`;
  }
  // 特殊处理发起人部门负责人、发起人连续部门负责人
  if (
    userTaskForm.value.candidateStrategy ===
      CandidateStrategy.START_USER_DEPT_LEADER ||
    userTaskForm.value.candidateStrategy ===
      CandidateStrategy.START_USER_MULTI_LEVEL_DEPT_LEADER
  ) {
    candidateParam = `${deptLevel.value}`;
  }

  const extensions = bpmnInstances().moddle.create('bpmn:ExtensionElements', {
    values: [
      ...otherExtensions.value,
      bpmnInstances().moddle.create(`${prefix}:CandidateStrategy`, {
        value: userTaskForm.value.candidateStrategy,
      }),
      bpmnInstances().moddle.create(`${prefix}:CandidateParam`, {
        value: candidateParam,
      }),
    ],
  });
  bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), {
    extensionElements: extensions,
  });

  // 改用通过extensionElements来存储数据
  // return;
  // bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), {
  //   candidateStrategy: userTaskForm.value.candidateStrategy,
  //   candidateParam: userTaskForm.value.candidateParam.join(','),
  // });
};

const updateSkipExpression = () => {
  if (
    userTaskForm.value.skipExpression &&
    userTaskForm.value.skipExpression !== ''
  ) {
    bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), {
      skipExpression: userTaskForm.value.skipExpression,
    });
  } else {
    bpmnInstances().modeling.updateProperties(toRaw(bpmnElement.value), {
      skipExpression: null,
    });
  }
};

// 打开监听器弹窗
const processExpressionDialogRef = ref<any>();
const openProcessExpressionDialog = async () => {
  processExpressionDialogRef.value.open();
};
const selectProcessExpression = (
  expression: BpmProcessExpressionApi.ProcessExpression,
) => {
  // @ts-ignore
  userTaskForm.value.candidateParam = [expression.expression];
  updateElementTask();
};

const handleFormUserChange = (e: any) => {
  if (e === 'PROCESS_START_USER_ID') {
    userTaskForm.value.candidateParam = [];
    // @ts-ignore
    userTaskForm.value.candidateStrategy = CandidateStrategy.START_USER;
  }
  updateElementTask();
};

watch(
  () => props.id,
  () => {
    bpmnElement.value = bpmnInstances().bpmnElement;
    nextTick(() => {
      resetTaskForm();
    });
  },
  { immediate: true },
);

onMounted(async () => {
  // 获得角色列表
  roleOptions.value = await getSimpleRoleList();
  // 获得部门列表
  const deptOptions = await getSimpleDeptList();
  deptTreeOptions.value = handleTree(deptOptions, 'id');
  // 获得岗位列表
  postOptions.value = await getSimplePostList();
  // 获得用户列表
  userOptions.value = await getSimpleUserList();
  // 获得用户组列表
  userGroupOptions.value = await getUserGroupSimpleList();
});

onBeforeUnmount(() => {
  bpmnElement.value = null;
});
</script>

<template>
  <Form>
    <FormItem label="规则类型" name="candidateStrategy">
      <Select
        v-model:value="userTaskForm.candidateStrategy"
        allow-clear
        style="width: 100%"
        @change="changeCandidateStrategy"
      >
        <SelectOption
          v-for="(dict, index) in CANDIDATE_STRATEGY"
          :key="index"
          :value="dict.value"
        >
          {{ dict.label }}
        </SelectOption>
      </Select>
    </FormItem>
    <FormItem
      v-if="userTaskForm.candidateStrategy === CandidateStrategy.ROLE"
      label="指定角色"
      name="candidateParam"
    >
      <Select
        v-model:value="userTaskForm.candidateParam"
        allow-clear
        mode="multiple"
        style="width: 100%"
        @change="updateElementTask"
      >
        <SelectOption
          v-for="item in roleOptions"
          :key="item.id"
          :value="item.id"
        >
          {{ item.name }}
        </SelectOption>
      </Select>
    </FormItem>
    <FormItem
      v-if="
        userTaskForm.candidateStrategy === CandidateStrategy.DEPT_MEMBER ||
        userTaskForm.candidateStrategy === CandidateStrategy.DEPT_LEADER ||
        userTaskForm.candidateStrategy ===
          CandidateStrategy.MULTI_LEVEL_DEPT_LEADER
      "
      label="指定部门"
      name="candidateParam"
    >
      <TreeSelect
        ref="treeRef"
        v-model:value="userTaskForm.candidateParam"
        :tree-data="deptTreeOptions"
        :field-names="defaultProps"
        placeholder="加载中，请稍后"
        multiple
        tree-checkable
        @change="updateElementTask"
      />
    </FormItem>
    <FormItem
      v-if="userTaskForm.candidateStrategy === CandidateStrategy.POST"
      label="指定岗位"
      name="candidateParam"
    >
      <Select
        v-model:value="userTaskForm.candidateParam"
        allow-clear
        mode="multiple"
        style="width: 100%"
        @change="updateElementTask"
      >
        <SelectOption
          v-for="item in postOptions"
          :key="item.id"
          :value="item.id"
        >
          {{ item.name }}
        </SelectOption>
      </Select>
    </FormItem>
    <FormItem
      v-if="userTaskForm.candidateStrategy === CandidateStrategy.USER"
      label="指定用户"
      name="candidateParam"
    >
      <Select
        v-model:value="userTaskForm.candidateParam"
        allow-clear
        mode="multiple"
        style="width: 100%"
        @change="updateElementTask"
      >
        <SelectOption
          v-for="item in userOptions"
          :key="item.id"
          :value="item.id"
        >
          {{ item.nickname }}
        </SelectOption>
      </Select>
    </FormItem>
    <FormItem
      v-if="userTaskForm.candidateStrategy === CandidateStrategy.USER_GROUP"
      label="指定用户组"
      name="candidateParam"
    >
      <Select
        v-model:value="userTaskForm.candidateParam"
        allow-clear
        mode="multiple"
        style="width: 100%"
        @change="updateElementTask"
      >
        <SelectOption
          v-for="item in userGroupOptions"
          :key="item.id"
          :value="item.id"
        >
          {{ item.name }}
        </SelectOption>
      </Select>
    </FormItem>
    <FormItem
      v-if="userTaskForm.candidateStrategy === CandidateStrategy.FORM_USER"
      label="表单内用户字段"
      name="formUser"
    >
      <Select
        v-model:value="userTaskForm.candidateParam"
        allow-clear
        style="width: 100%"
        @change="handleFormUserChange"
      >
        <SelectOption
          v-for="(item, idx) in userFieldOnFormOptions"
          :key="idx"
          :value="item.field"
          :disabled="!item.required"
        >
          {{ item.title }}
        </SelectOption>
      </Select>
    </FormItem>
    <FormItem
      v-if="
        userTaskForm.candidateStrategy === CandidateStrategy.FORM_DEPT_LEADER
      "
      label="表单内部门字段"
      name="formDept"
    >
      <Select
        v-model:value="userTaskForm.candidateParam"
        allow-clear
        style="width: 100%"
        @change="updateElementTask"
      >
        <SelectOption
          v-for="(item, idx) in deptFieldOnFormOptions"
          :key="idx"
          :value="item.field"
          :disabled="!item.required"
        >
          {{ item.title }}
        </SelectOption>
      </Select>
    </FormItem>
    <FormItem
      v-if="
        userTaskForm.candidateStrategy ===
          CandidateStrategy.MULTI_LEVEL_DEPT_LEADER ||
        userTaskForm.candidateStrategy ===
          CandidateStrategy.START_USER_DEPT_LEADER ||
        userTaskForm.candidateStrategy ===
          CandidateStrategy.START_USER_MULTI_LEVEL_DEPT_LEADER ||
        userTaskForm.candidateStrategy === CandidateStrategy.FORM_DEPT_LEADER
      "
      :label="deptLevelLabel!"
      name="deptLevel"
    >
      <Select v-model:value="deptLevel" allow-clear @change="updateElementTask">
        <SelectOption
          v-for="(item, index) in MULTI_LEVEL_DEPT"
          :key="index"
          :value="item.value"
        >
          {{ item.label }}
        </SelectOption>
      </Select>
    </FormItem>
    <FormItem
      v-if="userTaskForm.candidateStrategy === CandidateStrategy.EXPRESSION"
      label="流程表达式"
      name="candidateParam"
    >
      <Textarea
        v-model:value="userTaskForm.candidateParam[0]"
        allow-clear
        style="width: 100%"
        @change="updateElementTask"
      />
      <Button
        class="!w-1/1 mt-5px"
        type="primary"
        :icon="h(SelectOutlined)"
        @click="openProcessExpressionDialog"
      >
        选择表达式
      </Button>
      <!-- 选择弹窗 -->
      <ProcessExpressionDialog
        ref="processExpressionDialogRef"
        @select="selectProcessExpression"
      />
    </FormItem>

    <FormItem label="跳过表达式" name="skipExpression">
      <Textarea
        v-model:value="userTaskForm.skipExpression"
        allow-clear
        style="width: 100%"
        @change="updateSkipExpression"
      />
    </FormItem>
  </Form>
</template>
