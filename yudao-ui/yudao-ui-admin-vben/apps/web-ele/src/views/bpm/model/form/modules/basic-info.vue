<script lang="ts" setup>
import type { FormItemRule } from 'element-plus';

import type { PropType } from 'vue';

import type { BpmCategoryApi } from '#/api/bpm/category';
import type { SystemDeptApi } from '#/api/system/dept';
import type { SystemUserApi } from '#/api/system/user';

import { ref, watch } from 'vue';

import { useVbenModal } from '@vben/common-ui';
import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';

import {
  ElAvatar,
  ElButton,
  ElForm,
  ElFormItem,
  ElInput,
  ElOption,
  ElRadio,
  ElRadioGroup,
  ElSelect,
  ElTooltip,
} from 'element-plus';

import { ImageUpload } from '#/components/upload';
import { DeptSelectModal } from '#/views/system/dept/components';
import { UserSelectModal } from '#/views/system/user/components';

const props = defineProps({
  categoryList: {
    type: Array as PropType<BpmCategoryApi.Category[]>,
    required: true,
  },
  userList: {
    type: Array as PropType<SystemUserApi.User[]>,
    required: true,
  },
  deptList: {
    type: Array as PropType<SystemDeptApi.Dept[]>,
    required: true,
  },
});

const [UserSelectModalComp, userSelectModalApi] = useVbenModal({
  connectedComponent: UserSelectModal,
  destroyOnClose: true,
});

const [DeptSelectModalComp, deptSelectModalApi] = useVbenModal({
  connectedComponent: DeptSelectModal,
  destroyOnClose: true,
});

const formRef = ref(); // 表单引用
const modelData = defineModel<any>(); // 创建本地数据副本

const selectedStartUsers = ref<SystemUserApi.User[]>([]); // 选中的发起人
const selectedStartDepts = ref<SystemDeptApi.Dept[]>([]); // 选中的发起部门

const selectedManagerUsers = ref<SystemUserApi.User[]>([]); // 选中的流程管理员
const currentSelectType = ref<'manager' | 'start'>('start');
const selectedUsers = ref<number[]>(); // 选中的用户
const rules: Record<string, FormItemRule[]> = {
  name: [{ required: true, message: '流程名称不能为空', trigger: 'blur' }],
  key: [
    { required: true, message: '流程标识不能为空', trigger: 'blur' },
    {
      validator: (_rule: any, value: string, callback: any) => {
        if (!value) {
          callback();
          return;
        }
        if (!/^[a-z_][-\w.$]*$/i.test(value)) {
          callback(
            new Error(
              '只能包含字母、数字、下划线、连字符和点号，且必须以字母或下划线开头',
            ),
          );
          return;
        }
        callback();
      },
      trigger: 'blur',
    },
  ],
  category: [{ required: true, message: '流程分类不能为空', trigger: 'blur' }],
  type: [{ required: true, message: '流程类型不能为空', trigger: 'blur' }],
  visible: [{ required: true, message: '是否可见不能为空', trigger: 'blur' }],
  managerUserIds: [
    { required: true, message: '流程管理员不能为空', trigger: 'blur' },
  ],
};

/** 初始化选中的用户 */
watch(
  () => modelData.value,
  (newVal) => {
    selectedStartUsers.value = newVal.startUserIds?.length
      ? (props.userList.filter((user: SystemUserApi.User) =>
          newVal.startUserIds.includes(user.id),
        ) as SystemUserApi.User[])
      : [];
    selectedStartDepts.value = newVal.startDeptIds?.length
      ? (props.deptList.filter((dept: SystemDeptApi.Dept) =>
          newVal.startDeptIds.includes(dept.id),
        ) as SystemDeptApi.Dept[])
      : [];
    selectedManagerUsers.value = newVal.managerUserIds?.length
      ? (props.userList.filter((user: SystemUserApi.User) =>
          newVal.managerUserIds.includes(user.id),
        ) as SystemUserApi.User[])
      : [];
  },
  {
    immediate: true,
  },
);

/** 打开发起人选择 */
function openStartUserSelect() {
  currentSelectType.value = 'start';
  selectedUsers.value = selectedStartUsers.value.map(
    (user) => user.id,
  ) as number[];
  userSelectModalApi.setData({ userIds: selectedUsers.value }).open();
}

/** 打开部门选择 */
function openStartDeptSelect() {
  deptSelectModalApi.setData({ selectedList: selectedStartDepts.value }).open();
}

/** 处理部门选择确认 */
function handleDeptSelectConfirm(depts: SystemDeptApi.Dept[]) {
  selectedStartDepts.value = depts;
  modelData.value = {
    ...modelData.value,
    startDeptIds: depts.map((d) => d.id),
  };
}

/** 打开管理员选择 */
function openManagerUserSelect() {
  currentSelectType.value = 'manager';
  selectedUsers.value = selectedManagerUsers.value.map(
    (user) => user.id,
  ) as number[];
  userSelectModalApi.setData({ userIds: selectedUsers.value }).open();
}

/** 处理用户选择确认 */
function handleUserSelectConfirm(userList: SystemUserApi.User[]) {
  modelData.value =
    currentSelectType.value === 'start'
      ? {
          ...modelData.value,
          startUserIds: userList.map((u) => u.id),
        }
      : {
          ...modelData.value,
          managerUserIds: userList.map((u) => u.id),
        };
}

/** 用户选择弹窗关闭 */
function handleUserSelectClosed() {
  selectedUsers.value = [];
}

/** 用户选择弹窗取消 */
function handleUserSelectCancel() {
  selectedUsers.value = [];
}

/** 处理发起人类型变化 */
function handleStartUserTypeChange(value: number) {
  switch (value) {
    case 0: {
      modelData.value = {
        ...modelData.value,
        startUserIds: [],
        startDeptIds: [],
      };
      break;
    }
    case 1: {
      modelData.value = {
        ...modelData.value,
        startDeptIds: [],
      };
      break;
    }
    case 2: {
      modelData.value = {
        ...modelData.value,
        startUserIds: [],
      };
      break;
    }
  }
}

/** 移除发起人 */
function handleRemoveStartUser(user: SystemUserApi.User) {
  modelData.value = {
    ...modelData.value,
    startUserIds: modelData.value.startUserIds.filter(
      (id: number) => id !== user.id,
    ),
  };
}

/** 移除部门 */
function handleRemoveStartDept(dept: SystemDeptApi.Dept) {
  modelData.value = {
    ...modelData.value,
    startDeptIds: modelData.value.startDeptIds.filter(
      (id: number) => id !== dept.id,
    ),
  };
}

/** 移除管理员 */
function handleRemoveManagerUser(user: SystemUserApi.User) {
  modelData.value = {
    ...modelData.value,
    managerUserIds: modelData.value.managerUserIds.filter(
      (id: number) => id !== user.id,
    ),
  };
}

/** 表单校验 */
async function validate() {
  await formRef.value?.validate();
}

defineExpose({ validate });
</script>

<template>
  <div>
    <ElForm ref="formRef" :model="modelData" :rules="rules" label-width="100px">
      <ElFormItem label="流程标识" prop="key">
        <div class="flex w-full items-center">
          <ElInput
            v-model="modelData.key"
            :disabled="!!modelData.id"
            placeholder="请输入流程标识，以字母或下划线开头"
            class="flex-1"
          />
          <ElTooltip
            :content="
              modelData.id ? '流程标识不可修改！' : '新建后，流程标识不可修改！'
            "
            placement="top"
          >
            <IconifyIcon icon="lucide:circle-help" class="ml-1 size-5" />
          </ElTooltip>
        </div>
      </ElFormItem>
      <ElFormItem label="流程名称" prop="name">
        <ElInput
          v-model="modelData.name"
          :disabled="!!modelData.id"
          clearable
          placeholder="请输入流程名称"
        />
      </ElFormItem>
      <ElFormItem label="流程分类" prop="category">
        <ElSelect
          v-model="modelData.category"
          clearable
          placeholder="请选择流程分类"
          class="w-full"
        >
          <ElOption
            v-for="category in categoryList"
            :key="category.code"
            :value="category.code"
            :label="category.name"
          />
        </ElSelect>
      </ElFormItem>
      <ElFormItem label="流程图标">
        <ImageUpload
          v-model:value="modelData.icon"
          :show-description="false"
          width="120px"
          height="120px"
        />
      </ElFormItem>
      <ElFormItem label="流程描述" prop="description">
        <ElInput v-model="modelData.description" type="textarea" clearable />
      </ElFormItem>
      <ElFormItem label="流程类型" prop="type">
        <ElRadioGroup v-model="modelData.type">
          <ElRadio
            v-for="dict in getDictOptions(DICT_TYPE.BPM_MODEL_TYPE, 'number')"
            :key="dict.value as number"
            :value="dict.value"
          >
            {{ dict.label }}
          </ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="是否可见" prop="visible">
        <ElRadioGroup v-model="modelData.visible">
          <ElRadio
            v-for="dict in getDictOptions(
              DICT_TYPE.INFRA_BOOLEAN_STRING,
              'boolean',
            )"
            :key="dict.label"
            :value="dict.value"
          >
            {{ dict.label }}
          </ElRadio>
        </ElRadioGroup>
      </ElFormItem>
      <ElFormItem label="谁可以发起" prop="startUserType">
        <ElSelect
          v-model="modelData.startUserType"
          placeholder="请选择谁可以发起"
          @change="handleStartUserTypeChange"
        >
          <ElOption :value="0" label="全员" />
          <ElOption :value="1" label="指定人员" />
          <ElOption :value="2" label="指定部门" />
        </ElSelect>
        <div
          v-if="modelData.startUserType === 1"
          class="mt-2 flex flex-wrap gap-1"
        >
          <div
            v-for="user in selectedStartUsers"
            :key="user.id"
            class="relative flex h-8 items-center rounded-lg bg-gray-100 pr-2 hover:bg-gray-200 dark:border dark:border-gray-500 dark:bg-gray-700 dark:hover:bg-gray-600"
          >
            <ElAvatar
              class="m-1 size-7"
              v-if="user.avatar"
              :src="user.avatar"
            />
            <ElAvatar class="m-1 size-7" v-else>
              {{ user.nickname?.substring(0, 1) }}
            </ElAvatar>
            <span class="text-gray-700 dark:text-gray-200">
              {{ user.nickname }}
            </span>
            <IconifyIcon
              icon="lucide:x"
              class="ml-2 size-4 cursor-pointer text-gray-400 hover:text-red-500 dark:text-gray-200"
              @click="handleRemoveStartUser(user)"
            />
          </div>
          <ElButton link @click="openStartUserSelect" class="flex items-center">
            <template #icon>
              <IconifyIcon icon="lucide:user-plus" class="size-4" />
            </template>
            选择人员
          </ElButton>
        </div>
        <div
          v-if="modelData.startUserType === 2"
          class="mt-2 flex flex-wrap gap-1"
        >
          <div
            v-for="dept in selectedStartDepts"
            :key="dept.id"
            class="relative flex h-8 items-center rounded-lg bg-gray-100 pr-2 shadow-sm hover:bg-gray-200 dark:border dark:border-gray-500 dark:bg-gray-700 dark:hover:bg-gray-600"
          >
            <IconifyIcon icon="lucide:building" class="size-6 px-1" />
            <span class="text-gray-700 dark:text-gray-200">
              {{ dept.name }}
            </span>
            <IconifyIcon
              icon="lucide:x"
              class="ml-2 size-4 cursor-pointer text-gray-400 hover:text-red-500"
              @click="handleRemoveStartDept(dept)"
            />
          </div>
          <ElButton link @click="openStartDeptSelect" class="flex items-center">
            <template #icon>
              <IconifyIcon icon="lucide:user-plus" class="size-4" />
            </template>
            选择部门
          </ElButton>
        </div>
      </ElFormItem>
      <ElFormItem label="流程管理员" prop="managerUserIds">
        <div class="flex flex-wrap gap-1">
          <div
            v-for="user in selectedManagerUsers"
            :key="user.id"
            class="relative flex h-9 items-center rounded-full bg-gray-100 pr-2 hover:bg-gray-200 dark:border dark:border-gray-500 dark:bg-gray-700 dark:hover:bg-gray-600"
          >
            <ElAvatar
              class="m-1 size-7"
              v-if="user.avatar"
              :src="user.avatar"
            />
            <ElAvatar class="m-1 size-7" v-else>
              {{ user.nickname?.substring(0, 1) }}
            </ElAvatar>
            <span class="text-gray-700 dark:text-gray-200">
              {{ user.nickname }}
            </span>
            <IconifyIcon
              icon="lucide:x"
              class="ml-2 size-4 cursor-pointer text-gray-400 hover:text-red-500"
              @click="handleRemoveManagerUser(user)"
            />
          </div>
          <ElButton
            link
            @click="openManagerUserSelect"
            class="flex items-center"
          >
            <template #icon>
              <IconifyIcon icon="lucide:user-plus" class="size-4" />
            </template>
            选择人员
          </ElButton>
        </div>
      </ElFormItem>
    </ElForm>

    <!-- 用户选择弹窗 -->
    <UserSelectModalComp
      class="w-3/5"
      v-model:value="selectedUsers"
      :multiple="true"
      @confirm="handleUserSelectConfirm"
      @closed="handleUserSelectClosed"
      @cancel="handleUserSelectCancel"
    />
    <!-- 部门选择对话框 -->
    <DeptSelectModalComp
      class="w-3/5"
      :check-strictly="true"
      @confirm="handleDeptSelectConfirm"
    />
  </div>
</template>
