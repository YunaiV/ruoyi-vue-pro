<script setup lang="ts">
import type { Ref } from 'vue';

import type { RouterSetting, SimpleFlowNode } from '../../consts';

import { inject, ref } from 'vue';

import { useVbenDrawer } from '@vben/common-ui';
import { BpmNodeTypeEnum } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';

import {
  ElButton,
  ElCard,
  ElCol,
  ElForm,
  ElFormItem,
  ElInput,
  ElMessage,
  ElOption,
  ElRow,
  ElSelect,
} from 'element-plus';

import { ConditionType } from '../../consts';
import { useNodeName, useWatchNode } from '../../helpers';
import Condition from './modules/condition.vue';

defineOptions({ name: 'RouterNodeConfig' });

const props = defineProps({
  flowNode: {
    type: Object as () => SimpleFlowNode,
    required: true,
  },
});

const processNodeTree = inject<Ref<SimpleFlowNode>>('processNodeTree');

/** 当前节点 */
const currentNode = useWatchNode(props);
/** 节点名称 */
const { nodeName, showInput, clickIcon, changeNodeName, inputRef } =
  useNodeName(BpmNodeTypeEnum.ROUTER_BRANCH_NODE);
const routerGroups = ref<RouterSetting[]>([]);
const nodeOptions = ref<any[]>([]);
const conditionRef = ref<any[]>([]);
const formRef = ref();

/** 校验节点配置 */
async function validateConfig() {
  // 校验路由分支选择
  const routeIdValid = await formRef.value.validate().catch(() => false);
  if (!routeIdValid) {
    ElMessage.warning('请配置路由目标节点');
    return false;
  }

  // 校验条件规则
  let valid = true;
  for (const item of conditionRef.value) {
    if (item && !(await item.validate())) {
      valid = false;
    }
  }
  if (!valid) return false;

  // 获取节点显示文本，如果为空，校验不通过
  const showText = getShowText();
  if (!showText) return false;

  return true;
}

/** 保存配置 */
async function saveConfig() {
  // 校验配置
  if (!(await validateConfig())) {
    return false;
  }
  // 保存配置
  currentNode.value.name = nodeName.value!;
  currentNode.value.showText = getShowText();
  currentNode.value.routerGroups = routerGroups.value;
  drawerApi.close();
  return true;
}

const [Drawer, drawerApi] = useVbenDrawer({
  title: nodeName.value,
  onConfirm: saveConfig,
});

/** 打开路由节点配置抽屉，由父组件调用 */
function openDrawer(node: SimpleFlowNode) {
  nodeOptions.value = [];
  getRouterNode(processNodeTree?.value);
  routerGroups.value = [];
  nodeName.value = node.name;
  if (node.routerGroups) {
    routerGroups.value = node.routerGroups;
  }
  drawerApi.open();
}

/** 获取显示文本 */
function getShowText() {
  if (
    !routerGroups.value ||
    !Array.isArray(routerGroups.value) ||
    routerGroups.value.length <= 0
  ) {
    ElMessage.warning('请配置路由！');
    return '';
  }
  for (const route of routerGroups.value) {
    if (!route.nodeId || !route.conditionType) {
      ElMessage.warning('请完善路由配置项！');
      return '';
    }
    if (
      route.conditionType === ConditionType.EXPRESSION &&
      !route.conditionExpression
    ) {
      ElMessage.warning('请完善路由配置项！');
      return '';
    }
    if (route.conditionType === ConditionType.RULE) {
      for (const condition of route.conditionGroups.conditions) {
        for (const rule of condition.rules) {
          if (!rule.leftSide || !rule.rightSide) {
            ElMessage.warning('请完善路由配置项！');
            return '';
          }
        }
      }
    }
  }
  return `${routerGroups.value.length}条路由分支`;
}

/** 添加路由分支 */
function addRouterGroup() {
  routerGroups.value.push({
    nodeId: undefined,
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
              leftSide: undefined,
              rightSide: '',
            },
          ],
        },
      ],
    },
  });
}

/** 删除路由分支 */
function deleteRouterGroup(index: number) {
  routerGroups.value.splice(index, 1);
}

/** 递归获取所有节点 */
function getRouterNode(node: any) {
  // TODO 最好还需要满足以下要求：
  // 并行分支、包容分支内部节点不能跳转到外部节点
  // 条件分支节点可以向上跳转到外部节点
  while (true) {
    if (!node) break;
    if (
      node.type !== BpmNodeTypeEnum.ROUTER_BRANCH_NODE &&
      node.type !== BpmNodeTypeEnum.CONDITION_NODE
    ) {
      nodeOptions.value.push({
        label: node.name,
        value: node.id,
      });
    }
    if (!node.childNode || node.type === BpmNodeTypeEnum.END_EVENT_NODE) {
      break;
    }
    if (node.conditionNodes && node.conditionNodes.length > 0) {
      node.conditionNodes.forEach((item: any) => {
        getRouterNode(item);
      });
    }
    node = node.childNode;
  }
}

defineExpose({ openDrawer }); // 暴露方法给父组件
</script>
<template>
  <Drawer class="w-2/5">
    <template #title>
      <div class="flex items-center">
        <ElInput
          ref="inputRef"
          v-if="showInput"
          type="text"
          class="mr-2 w-48"
          @blur="changeNodeName()"
          @keyup.enter="changeNodeName()"
          v-model="nodeName"
          :placeholder="nodeName"
        />
        <div
          v-else
          class="flex cursor-pointer items-center"
          @click="clickIcon()"
        >
          {{ nodeName }}
          <IconifyIcon class="ml-1" icon="lucide:edit-3" />
        </div>
      </div>
    </template>

    <ElForm ref="formRef" :model="{ routerGroups }">
      <ElCard
        :body-style="{ padding: '10px' }"
        class="mt-4"
        v-for="(item, index) in routerGroups"
        :key="index"
      >
        <template #header>
          <div class="flex w-full items-center justify-between py-2">
            <div class="flex items-center gap-4">
              <span class="font-medium">路由{{ index + 1 }}</span>
              <ElFormItem
                class="!mb-0 w-64"
                :prop="`routerGroups.${index}.nodeId`"
                :rules="{
                  required: true,
                  message: '路由目标节点不能为空',
                  trigger: 'change',
                }"
              >
                <ElSelect
                  v-model="item.nodeId"
                  placeholder="请选择路由目标节点"
                  clearable
                >
                  <ElOption
                    v-for="node in nodeOptions"
                    :key="node.value"
                    :label="node.label"
                    :value="node.value"
                  />
                </ElSelect>
              </ElFormItem>
            </div>
            <ElButton
              v-if="routerGroups.length > 1"
              circle
              size="small"
              @click="deleteRouterGroup(index)"
            >
              <template #icon>
                <IconifyIcon icon="lucide:x" />
              </template>
            </ElButton>
          </div>
        </template>
        <Condition
          :ref="(el) => (conditionRef[index] = el)"
          :model-value="routerGroups[index]"
          @update:model-value="(val) => (routerGroups[index] = val)"
        />
      </ElCard>
    </ElForm>

    <ElRow class="mt-4">
      <ElCol :span="24">
        <ElButton class="flex items-center p-0" link @click="addRouterGroup">
          <template #icon>
            <IconifyIcon icon="lucide:settings" />
          </template>
          新增路由分支
        </ElButton>
      </ElCol>
    </ElRow>
  </Drawer>
</template>
