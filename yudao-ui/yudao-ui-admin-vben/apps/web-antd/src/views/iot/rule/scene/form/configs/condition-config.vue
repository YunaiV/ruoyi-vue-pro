<!-- 单个条件配置组件 -->
<script setup lang="ts">
import type { TriggerCondition } from '#/api/iot/rule/scene';

import { computed, ref } from 'vue';

import { useVModel } from '@vueuse/core';
import { Col, Form, Row, Select } from 'ant-design-vue';

import {
  getConditionTypeOptions,
  IoTDeviceStatusEnum,
  IotRuleSceneTriggerConditionParameterOperatorEnum,
  IotRuleSceneTriggerConditionTypeEnum,
} from '#/views/iot/utils/constants';

import ValueInput from '../inputs/value-input.vue';
import DeviceSelector from '../selectors/device-selector.vue';
import OperatorSelector from '../selectors/operator-selector.vue';
import ProductSelector from '../selectors/product-selector.vue';
import PropertySelector from '../selectors/property-selector.vue';
import CurrentTimeConditionConfig from './current-time-condition-config.vue';

/** 单个条件配置组件 */
defineOptions({ name: 'ConditionConfig' });

const props = defineProps<{
  modelValue: TriggerCondition;
  triggerType: number;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: TriggerCondition): void;
}>();

/** 获取设备状态选项 */
const deviceStatusOptions = [
  {
    value: IoTDeviceStatusEnum.ONLINE.value,
    label: IoTDeviceStatusEnum.ONLINE.label,
  },
  {
    value: IoTDeviceStatusEnum.OFFLINE.value,
    label: IoTDeviceStatusEnum.OFFLINE.label,
  },
];

/** 获取状态操作符选项 */
const statusOperatorOptions = [
  {
    value: IotRuleSceneTriggerConditionParameterOperatorEnum.EQUALS.value,
    label: IotRuleSceneTriggerConditionParameterOperatorEnum.EQUALS.name,
  },
  {
    value: IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_EQUALS.value,
    label: IotRuleSceneTriggerConditionParameterOperatorEnum.NOT_EQUALS.name,
  },
];

const condition = useVModel(props, 'modelValue', emit);

const propertyType = ref<string>('string'); // 属性类型
const propertyConfig = ref<any>(null); // 属性配置
const isDeviceCondition = computed(() => {
  return (
    condition.value.type ===
      IotRuleSceneTriggerConditionTypeEnum.DEVICE_STATUS.toString() ||
    condition.value.type ===
      IotRuleSceneTriggerConditionTypeEnum.DEVICE_PROPERTY.toString()
  );
}); // 计算属性：判断是否为设备相关条件

/**
 * 更新条件字段
 * @param field 字段名
 * @param value 字段值
 */
function updateConditionField(field: any, value: any) {
  (condition.value as any)[field] = value;
  emit('update:modelValue', condition.value);
}

/**
 * 更新整个条件对象
 * @param newCondition 新的条件对象
 */
function updateCondition(newCondition: TriggerCondition) {
  condition.value = newCondition;
  emit('update:modelValue', condition.value);
}

/**
 * 处理条件类型变化事件
 * @param type 条件类型
 */
function handleConditionTypeChange(type: any) {
  // 根据条件类型清理字段
  const isCurrentTime =
    type === IotRuleSceneTriggerConditionTypeEnum.CURRENT_TIME;
  const isDeviceStatus =
    type === IotRuleSceneTriggerConditionTypeEnum.DEVICE_STATUS;

  // 清理标识符字段（时间条件和设备状态条件都不需要）
  if (isCurrentTime || isDeviceStatus) {
    condition.value.identifier = undefined;
  }

  // 清理设备相关字段（仅时间条件需要）
  if (isCurrentTime) {
    condition.value.productId = undefined;
    condition.value.deviceId = undefined;
  }

  // 设置默认操作符
  condition.value.operator = isCurrentTime
    ? 'at_time'
    : IotRuleSceneTriggerConditionParameterOperatorEnum.EQUALS.value;

  // 清空参数值
  condition.value.param = '';
}

/** 处理产品变化事件 */
function handleProductChange(_: any) {
  // 产品变化时清空设备和属性
  condition.value.deviceId = undefined;
  condition.value.identifier = '';
}

/** 处理设备变化事件 */
function handleDeviceChange(_: any) {
  // 设备变化时清空属性
  condition.value.identifier = '';
}

/**
 * 处理属性变化事件
 * @param propertyInfo.config - 属性配置
 * @param propertyInfo.type - 属性类型
 */
function handlePropertyChange(propertyInfo: { config: any; type: string }) {
  propertyType.value = propertyInfo.type;
  propertyConfig.value = propertyInfo.config;

  // 重置操作符和值
  condition.value.operator =
    IotRuleSceneTriggerConditionParameterOperatorEnum.EQUALS.value;
  condition.value.param = '';
}

/** 处理操作符变化事件 */
function handleOperatorChange() {
  // 重置值
  condition.value.param = '';
}
</script>

<template>
  <div class="gap-16px flex flex-col">
    <!-- 条件类型选择 -->
    <Row :gutter="16">
      <Col :span="8">
        <Form.Item label="条件类型" required>
          <Select
            :model-value="condition.type"
            @update:model-value="
              (value: any) => updateConditionField('type', value)
            "
            @change="handleConditionTypeChange"
            placeholder="请选择条件类型"
            class="w-full"
          >
            <Select.Option
              v-for="option in getConditionTypeOptions()"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </Select>
        </Form.Item>
      </Col>
    </Row>

    <!-- 产品设备选择 - 设备相关条件的公共部分 -->
    <Row v-if="isDeviceCondition" :gutter="16">
      <Col :span="12">
        <Form.Item label="产品" required>
          <ProductSelector
            :model-value="condition.productId"
            @update:model-value="
              (value) => updateConditionField('productId', value)
            "
            @change="handleProductChange"
          />
        </Form.Item>
      </Col>
      <Col :span="12">
        <Form.Item label="设备" required>
          <DeviceSelector
            :model-value="condition.deviceId"
            @update:model-value="
              (value) => updateConditionField('deviceId', value)
            "
            :product-id="condition.productId"
            @change="handleDeviceChange"
          />
        </Form.Item>
      </Col>
    </Row>

    <!-- 设备状态条件配置 -->
    <div
      v-if="
        condition.type ===
        IotRuleSceneTriggerConditionTypeEnum.DEVICE_STATUS.toString()
      "
      class="gap-16px flex flex-col"
    >
      <!-- 状态和操作符选择 -->
      <Row :gutter="16">
        <!-- 操作符选择 -->
        <Col :span="12">
          <Form.Item label="操作符" required>
            <Select
              :model-value="condition.operator"
              @update:model-value="
                (value: any) => updateConditionField('operator', value)
              "
              placeholder="请选择操作符"
              class="w-full"
            >
              <Select.Option
                v-for="option in statusOperatorOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </Select>
          </Form.Item>
        </Col>

        <!-- 状态选择 -->
        <Col :span="12">
          <Form.Item label="设备状态" required>
            <Select
              :model-value="condition.param"
              @update:model-value="
                (value: any) => updateConditionField('param', value)
              "
              placeholder="请选择设备状态"
              class="w-full"
            >
              <Select.Option
                v-for="option in deviceStatusOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </Select>
          </Form.Item>
        </Col>
      </Row>
    </div>

    <!-- 设备属性条件配置 -->
    <div
      v-else-if="
        condition.type ===
        IotRuleSceneTriggerConditionTypeEnum.DEVICE_PROPERTY.toString()
      "
      class="space-y-16px"
    >
      <!-- 属性配置 -->
      <Row :gutter="16">
        <!-- 属性/事件/服务选择 -->
        <Col :span="6">
          <Form.Item label="监控项" required>
            <PropertySelector
              :model-value="condition.identifier"
              @update:model-value="
                (value) => updateConditionField('identifier', value)
              "
              :trigger-type="triggerType"
              :product-id="condition.productId"
              :device-id="condition.deviceId"
              @change="handlePropertyChange"
            />
          </Form.Item>
        </Col>

        <!-- 操作符选择 -->
        <Col :span="6">
          <Form.Item label="操作符" required>
            <OperatorSelector
              :model-value="condition.operator"
              @update:model-value="
                (value) => updateConditionField('operator', value)
              "
              :property-type="propertyType"
              @change="handleOperatorChange"
            />
          </Form.Item>
        </Col>

        <!-- 值输入 -->
        <Col :span="12">
          <Form.Item label="比较值" required>
            <ValueInput
              :model-value="condition.param"
              @update:model-value="
                (value) => updateConditionField('param', value)
              "
              :property-type="propertyType"
              :operator="condition.operator"
              :property-config="propertyConfig"
            />
          </Form.Item>
        </Col>
      </Row>
    </div>

    <!-- 当前时间条件配置 -->
    <CurrentTimeConditionConfig
      v-else-if="
        condition.type ===
        IotRuleSceneTriggerConditionTypeEnum.CURRENT_TIME.toString()
      "
      :model-value="condition"
      @update:model-value="updateCondition"
    />
  </div>
</template>

<style scoped>
:deep(.ant-form-item) {
  margin-bottom: 0;
}
</style>
