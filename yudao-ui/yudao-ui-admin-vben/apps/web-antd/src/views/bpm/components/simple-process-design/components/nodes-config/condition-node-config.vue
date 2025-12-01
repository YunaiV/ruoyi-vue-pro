<script setup lang="ts">
import type { SimpleFlowNode } from '../../consts';

import { nextTick, ref, watch } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { IconifyIcon } from '@vben/icons';
import { cloneDeep } from '@vben/utils';

import { Input } from 'ant-design-vue';

import { ConditionType } from '../../consts';
import {
  getConditionShowText,
  getDefaultConditionNodeName,
  useFormFieldsAndStartUser,
} from '../../helpers';
import Condition from './modules/condition.vue';

defineOptions({
  name: 'ConditionNodeConfig',
});

const props = defineProps({
  conditionNode: {
    type: Object as () => SimpleFlowNode,
    required: true,
  },
  nodeIndex: {
    type: Number,
    required: true,
  },
});

const currentNode = ref<SimpleFlowNode>(props.conditionNode);
const condition = ref<any>({
  conditionType: ConditionType.RULE, // 设置默认值
  conditionExpression: '',
  conditionGroups: {
    and: true,
    conditions: [
      {
        and: true,
        rules: [
          {
            opCode: '==',
            leftSide: '',
            rightSide: '',
          },
        ],
      },
    ],
  },
});

const conditionRef = ref();
const fieldOptions = useFormFieldsAndStartUser(); // 流程表单字段和发起人字段

/** 保存配置 */
async function saveConfig() {
  if (!currentNode.value.conditionSetting?.defaultFlow) {
    // 校验表单
    const valid = await conditionRef.value.validate();
    if (!valid) return false;
    const showText = getConditionShowText(
      condition.value?.conditionType,
      condition.value?.conditionExpression,
      condition.value.conditionGroups,
      fieldOptions,
    );
    if (!showText) {
      return false;
    }
    currentNode.value.showText = showText;
    // 使用 cloneDeep 进行深拷贝
    currentNode.value.conditionSetting = cloneDeep({
      ...currentNode.value.conditionSetting,
      conditionType: condition.value?.conditionType,
      conditionExpression:
        condition.value?.conditionType === ConditionType.EXPRESSION
          ? condition.value?.conditionExpression
          : undefined,
      conditionGroups:
        condition.value?.conditionType === ConditionType.RULE
          ? condition.value?.conditionGroups
          : undefined,
    });
  }
  drawerApi.close();
  return true;
}

const [Drawer, drawerApi] = useVbenDrawer({
  title: currentNode.value.name,
  onConfirm: saveConfig,
});

function open() {
  // 使用三元表达式代替 if-else，解决 linter 警告
  condition.value = currentNode.value.conditionSetting
    ? cloneDeep(currentNode.value.conditionSetting)
    : {
        conditionType: ConditionType.RULE,
        conditionExpression: '',
        conditionGroups: {
          and: true,
          conditions: [
            {
              and: true,
              rules: [
                {
                  opCode: '==',
                  leftSide: '',
                  rightSide: '',
                },
              ],
            },
          ],
        },
      };

  drawerApi.open();
}

watch(
  () => props.conditionNode,
  (newValue) => {
    currentNode.value = newValue;
  },
);
// 显示名称输入框
const showInput = ref(false);
// 输入框的引用
const inputRef = ref<HTMLInputElement | null>(null);
// 监听 showInput 的变化，当变为 true 时自动聚焦
watch(showInput, (value) => {
  if (value) {
    nextTick(() => {
      inputRef.value?.focus();
    });
  }
});
function clickIcon() {
  showInput.value = true;
}

// 修改节点名称
function changeNodeName() {
  showInput.value = false;
  currentNode.value.name =
    currentNode.value.name ||
    getDefaultConditionNodeName(
      props.nodeIndex,
      currentNode.value?.conditionSetting?.defaultFlow,
    );
}

defineExpose({ open }); // 提供 open 方法，用于打开弹窗
</script>
<template>
  <Drawer class="w-1/3">
    <template #title>
      <div class="flex items-center">
        <Input
          ref="inputRef"
          v-if="showInput"
          type="text"
          class="mr-2 w-48"
          @blur="changeNodeName()"
          @press-enter="changeNodeName()"
          v-model:value="currentNode.name"
          :placeholder="currentNode.name"
        />
        <div
          v-else
          class="flex cursor-pointer items-center"
          @click="clickIcon()"
        >
          {{ currentNode.name }}
          <IconifyIcon class="ml-1" icon="lucide:edit-3" />
        </div>
      </div>
    </template>

    <div>
      <div
        class="mb-3 text-base"
        v-if="currentNode.conditionSetting?.defaultFlow"
      >
        未满足其它条件时，将进入此分支（该分支不可编辑和删除）
      </div>
      <div v-else>
        <Condition ref="conditionRef" v-model:model-value="condition" />
      </div>
    </div>
  </Drawer>
</template>
