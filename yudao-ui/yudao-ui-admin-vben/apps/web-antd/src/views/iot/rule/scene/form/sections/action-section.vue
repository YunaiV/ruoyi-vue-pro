<!-- 执行器配置组件 -->
<script setup lang="ts">
import type { Action } from '#/api/iot/rule/scene';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import { Button, Card, Empty, Form, Select, Tag } from 'ant-design-vue';

import {
  getActionTypeLabel,
  getActionTypeOptions,
  IotRuleSceneActionTypeEnum,
} from '#/views/iot/utils/constants';

import AlertConfig from '../configs/alert-config.vue';
import DeviceControlConfig from '../configs/device-control-config.vue';

/** 执行器配置组件 */
defineOptions({ name: 'ActionSection' });

const props = defineProps<{
  actions: Action[];
}>();

const emit = defineEmits<{
  (e: 'update:actions', value: Action[]): void;
}>();

const actions = useVModel(props, 'actions', emit);

/** 获取执行器标签类型（用于 el-tag 的 type 属性） */
function getActionTypeTag(
  type: number,
): 'danger' | 'info' | 'primary' | 'success' | 'warning' {
  const actionTypeTags: Record<
    number,
    'danger' | 'info' | 'primary' | 'success' | 'warning'
  > = {
    [IotRuleSceneActionTypeEnum.DEVICE_PROPERTY_SET]: 'primary',
    [IotRuleSceneActionTypeEnum.DEVICE_SERVICE_INVOKE]: 'success',
    [IotRuleSceneActionTypeEnum.ALERT_TRIGGER]: 'danger',
    [IotRuleSceneActionTypeEnum.ALERT_RECOVER]: 'warning',
  } as const;
  return actionTypeTags[type] || 'info';
}

/** 判断是否为设备执行器类型 */
function isDeviceAction(type: number): boolean {
  const deviceActionTypes = [
    IotRuleSceneActionTypeEnum.DEVICE_PROPERTY_SET,
    IotRuleSceneActionTypeEnum.DEVICE_SERVICE_INVOKE,
  ] as number[];
  return deviceActionTypes.includes(type);
}

/** 判断是否为告警执行器类型 */
function isAlertAction(type: number): boolean {
  const alertActionTypes = [
    IotRuleSceneActionTypeEnum.ALERT_TRIGGER,
    IotRuleSceneActionTypeEnum.ALERT_RECOVER,
  ] as number[];
  return alertActionTypes.includes(type);
}

/**
 * 创建默认的执行器数据
 * @returns 默认执行器对象
 */
function createDefaultActionData(): Action {
  return {
    type: IotRuleSceneActionTypeEnum.DEVICE_PROPERTY_SET.toString(), // 默认为设备属性设置
    productId: undefined,
    deviceId: undefined,
    identifier: undefined, // 物模型标识符（服务调用时使用）
    params: undefined,
    alertConfigId: undefined,
  };
}

/**
 * 添加执行器
 */
function addAction() {
  const newAction = createDefaultActionData();
  actions.value.push(newAction);
}

/**
 * 删除执行器
 * @param index 执行器索引
 */
function removeAction(index: number) {
  actions.value.splice(index, 1);
}

/**
 * 更新执行器类型
 * @param index 执行器索引
 * @param type 执行器类型
 */
function updateActionType(index: number, type: number) {
  actions.value[index]!.type = type.toString();
  onActionTypeChange(actions.value[index] as Action, type);
}

/**
 * 更新执行器
 * @param index 执行器索引
 * @param action 执行器对象
 */
function updateAction(index: number, action: Action) {
  actions.value[index] = action;
}

/**
 * 更新告警配置
 * @param index 执行器索引
 * @param alertConfigId 告警配置ID
 */
function updateActionAlertConfig(index: number, alertConfigId?: number) {
  actions.value[index]!.alertConfigId = alertConfigId;
  if (actions.value[index]) {
    actions.value[index].alertConfigId = alertConfigId;
  }
}

/**
 * 监听执行器类型变化
 * @param action 执行器对象
 * @param type 执行器类型
 */
function onActionTypeChange(action: Action, type: any) {
  // 清理不相关的配置，确保数据结构干净
  if (isDeviceAction(type)) {
    // 设备控制类型：清理告警配置，确保设备参数存在
    action.alertConfigId = undefined;
    if (!(action as any).params) {
      (action as any).params = '';
    }
    // 如果从其他类型切换到设备控制类型，清空identifier（让用户重新选择）
    if (action.identifier && type !== (action as any).type) {
      action.identifier = undefined;
    }
  } else if (isAlertAction(type)) {
    action.productId = undefined;
    action.deviceId = undefined;
    action.identifier = undefined; // 清理服务标识符
    action.params = undefined;
    action.alertConfigId = undefined;
  }
}
</script>

<template>
  <Card class="rounded-lg border border-primary" shadow="never">
    <template #title>
      <div class="flex items-center justify-between">
        <div class="gap-8px flex items-center">
          <IconifyIcon icon="ep:setting" class="text-18px text-primary" />
          <span class="text-16px font-600 text-primary"> 执行器配置 </span>
          <Tag size="small" type="info"> {{ actions.length }} 个执行器 </Tag>
        </div>
        <div class="gap-8px flex items-center">
          <Button type="primary" size="small" @click="addAction">
            <IconifyIcon icon="ep:plus" />
            添加执行器
          </Button>
        </div>
      </div>
    </template>

    <div class="p-0">
      <!-- 空状态 -->
      <div v-if="actions.length === 0">
        <Empty description="暂无执行器配置">
          <Button type="primary" @click="addAction">
            <IconifyIcon icon="ep:plus" />
            添加第一个执行器
          </Button>
        </Empty>
      </div>

      <!-- 执行器列表 -->
      <div v-else class="space-y-24px">
        <div
          v-for="(action, index) in actions"
          :key="`action-${index}`"
          class="rounded-lg border-2 border-blue-200 bg-blue-50 shadow-sm transition-shadow hover:shadow-md"
        >
          <!-- 执行器头部 - 蓝色主题 -->
          <div
            class="flex items-center justify-between rounded-t-lg border-b border-blue-200 bg-gradient-to-r from-blue-50 to-sky-50 p-4"
          >
            <div class="gap-12px flex items-center">
              <div
                class="font-600 flex items-center gap-2 text-base text-blue-700"
              >
                <div
                  class="flex size-6 items-center justify-center rounded-full bg-blue-500 text-xs font-bold text-white"
                >
                  {{ index + 1 }}
                </div>
                <span>执行器 {{ index + 1 }}</span>
              </div>
              <Tag
                :type="getActionTypeTag(action.type as any)"
                size="small"
                class="font-500"
              >
                {{ getActionTypeLabel(action.type as any) }}
              </Tag>
            </div>
            <div class="gap-8px flex items-center">
              <Button
                v-if="actions.length > 1"
                danger
                size="small"
                text
                @click="removeAction(index)"
                class="hover:bg-red-50"
              >
                <IconifyIcon icon="lucide:trash-2" />
                删除
              </Button>
            </div>
          </div>

          <!-- 执行器内容区域 -->
          <div class="p-16px space-y-16px">
            <!-- 执行类型选择 -->
            <div class="w-full">
              <Form.Item label="执行类型" required>
                <Select
                  :model-value="action.type"
                  @update:model-value="
                    (value: number) => updateActionType(index, value)
                  "
                  @change="(value) => onActionTypeChange(action, value)"
                  placeholder="请选择执行类型"
                  class="w-full"
                >
                  <Select.Option
                    v-for="option in getActionTypeOptions()"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </Select>
              </Form.Item>
            </div>

            <!-- 设备控制配置 -->
            <DeviceControlConfig
              v-if="isDeviceAction(action.type as any)"
              :model-value="action"
              @update:model-value="(value) => updateAction(index, value)"
            />

            <!-- 告警配置 - 只有恢复告警时才显示 -->
            <AlertConfig
              v-if="
                action.type ===
                IotRuleSceneActionTypeEnum.ALERT_RECOVER.toString()
              "
              :model-value="action.alertConfigId"
              @update:model-value="
                (value) => updateActionAlertConfig(index, value)
              "
            />

            <!-- 触发告警提示 - 触发告警时显示 -->
            <div
              v-if="
                action.type ===
                IotRuleSceneActionTypeEnum.ALERT_TRIGGER.toString()
              "
              class="bg-fill-color-blank rounded-lg border border-border p-4"
            >
              <div class="mb-2 flex items-center gap-2">
                <IconifyIcon icon="ep:warning" class="text-base text-warning" />
                <span class="font-600 text-sm text-primary">触发告警</span>
                <Tag size="small" type="warning">自动执行</Tag>
              </div>
              <div class="text-xs leading-relaxed text-secondary">
                当触发条件满足时，系统将自动发送告警通知，可在菜单 [告警中心 ->
                告警配置] 管理。
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 添加提示 -->
      <div v-if="actions.length > 0" class="py-16px text-center">
        <Button type="primary" plain @click="addAction">
          <IconifyIcon icon="ep:plus" />
          继续添加执行器
        </Button>
      </div>
    </div>
  </Card>
</template>
