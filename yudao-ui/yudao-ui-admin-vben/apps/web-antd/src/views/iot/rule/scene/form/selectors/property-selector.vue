<!-- 属性选择器组件 -->
<script setup lang="ts">
import type {
  ThingModelApi,
  ThingModelEvent,
  ThingModelProperty,
  ThingModelService,
} from '#/api/iot/thingmodel';

import { computed, ref, watch } from 'vue';

import { IconifyIcon } from '@vben/icons';

import { useVModel } from '@vueuse/core';
import { Button, Popover, Select, Tag } from 'ant-design-vue';

import { getThingModelListByProductId } from '#/api/iot/thingmodel';
import {
  getAccessModeLabel,
  getDataTypeName,
  getDataTypeTagType,
  getEventTypeLabel,
  getThingModelServiceCallTypeLabel,
  IotRuleSceneTriggerTypeEnum,
  IoTThingModelTypeEnum,
  THING_MODEL_GROUP_LABELS,
} from '#/views/iot/utils/constants';

/** 属性选择器组件 */
defineOptions({ name: 'PropertySelector' });

const props = defineProps<{
  deviceId?: number;
  modelValue?: string;
  productId?: number;
  triggerType: number;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'change', value: { config: any; type: string }): void;
}>();

// TODO 芋艿
/** 属性选择器内部使用的统一数据结构 */
interface PropertySelectorItem {
  identifier: string;
  name: string;
  description?: string;
  dataType: string;
  type: number; // IoTThingModelTypeEnum
  accessMode?: string;
  required?: boolean;
  unit?: string;
  range?: string;
  eventType?: string;
  callType?: string;
  inputParams?: ThingModelParam[];
  outputParams?: ThingModelParam[];
  property?: ThingModelProperty;
  event?: ThingModelEvent;
  service?: ThingModelService;
}

const localValue = useVModel(props, 'modelValue', emit);

const loading = ref(false); // 加载状态
const propertyList = ref<ThingModelApi.Property[]>([]); // 属性列表
const thingModelTSL = ref<null | ThingModelApi.ThingModel>(null); // 物模型TSL数据

// 计算属性：属性分组
const propertyGroups = computed(() => {
  const groups: { label: string; options: any[] }[] = [];

  if (props.triggerType === IotRuleSceneTriggerTypeEnum.DEVICE_PROPERTY_POST) {
    groups.push({
      label: THING_MODEL_GROUP_LABELS.PROPERTY,
      options: propertyList.value.filter(
        (p) => p.type === IoTThingModelTypeEnum.PROPERTY,
      ),
    });
  }

  if (props.triggerType === IotRuleSceneTriggerTypeEnum.DEVICE_EVENT_POST) {
    groups.push({
      label: THING_MODEL_GROUP_LABELS.EVENT,
      options: propertyList.value.filter(
        (p) => p.type === IoTThingModelTypeEnum.EVENT,
      ),
    });
  }

  if (props.triggerType === IotRuleSceneTriggerTypeEnum.DEVICE_SERVICE_INVOKE) {
    groups.push({
      label: THING_MODEL_GROUP_LABELS.SERVICE,
      options: propertyList.value.filter(
        (p) => p.type === IoTThingModelTypeEnum.SERVICE,
      ),
    });
  }

  return groups.filter((group) => group.options.length > 0);
});

// 计算属性：当前选中的属性
const selectedProperty = computed(() => {
  return propertyList.value.find((p) => p.identifier === localValue.value);
});

/**
 * 处理选择变化事件
 * @param value 选中的属性标识符
 */
function handleChange(value: any) {
  const property = propertyList.value.find((p) => p.identifier === value);
  if (property) {
    emit('change', {
      type: property.dataType,
      config: property,
    });
  }
}

/**
 * 获取物模型TSL数据
 */
async function getThingModelTSL() {
  if (!props.productId) {
    thingModelTSL.value = null;
    propertyList.value = [];
    return;
  }

  loading.value = true;
  try {
    const tslData = await getThingModelListByProductId(props.productId);

    if (tslData) {
      thingModelTSL.value = tslData;
      parseThingModelData();
    } else {
      console.error('获取物模型TSL失败: 返回数据为空');
      propertyList.value = [];
    }
  } catch (error) {
    console.error('获取物模型TSL失败:', error);
    propertyList.value = [];
  } finally {
    loading.value = false;
  }
}

/** 解析物模型 TSL 数据 */
function parseThingModelData() {
  const tsl = thingModelTSL.value;
  const properties: PropertySelectorItem[] = [];

  if (!tsl) {
    propertyList.value = properties;
    return;
  }
  // 解析属性
  if (tsl.properties && Array.isArray(tsl.properties)) {
    tsl.properties.forEach((prop) => {
      properties.push({
        identifier: prop.identifier,
        name: prop.name,
        description: prop.description,
        dataType: prop.dataType,
        type: IoTThingModelTypeEnum.PROPERTY,
        accessMode: prop.accessMode,
        required: prop.required,
        unit: getPropertyUnit(prop),
        range: getPropertyRange(prop),
        property: prop,
      });
    });
  }

  // 解析事件
  if (tsl.events && Array.isArray(tsl.events)) {
    tsl.events.forEach((event) => {
      properties.push({
        identifier: event.identifier,
        name: event.name,
        description: event.description,
        dataType: 'struct',
        type: IoTThingModelTypeEnum.EVENT,
        eventType: event.type,
        required: event.required,
        outputParams: event.outputParams,
        event,
      });
    });
  }

  // 解析服务
  if (tsl.services && Array.isArray(tsl.services)) {
    tsl.services.forEach((service) => {
      properties.push({
        identifier: service.identifier,
        name: service.name,
        description: service.description,
        dataType: 'struct',
        type: IoTThingModelTypeEnum.SERVICE,
        callType: service.callType,
        required: service.required,
        inputParams: service.inputParams,
        outputParams: service.outputParams,
        service,
      });
    });
  }
  propertyList.value = properties;
}

/**
 * 获取属性单位
 * @param property 属性对象
 * @returns 属性单位
 */
function getPropertyUnit(property: any) {
  if (!property) return undefined;

  // 数值型数据的单位
  if (property.dataSpecs && property.dataSpecs.unit) {
    return property.dataSpecs.unit;
  }

  return undefined;
}

/**
 * 获取属性范围描述
 * @param property 属性对象
 * @returns 属性范围描述
 */
function getPropertyRange(property: any) {
  if (!property) return undefined;

  // 数值型数据的范围
  if (property.dataSpecs) {
    const specs = property.dataSpecs;
    if (specs.min !== undefined && specs.max !== undefined) {
      return `${specs.min}~${specs.max}`;
    }
  }

  // 枚举型和布尔型数据的选项
  if (property.dataSpecsList && Array.isArray(property.dataSpecsList)) {
    return property.dataSpecsList
      .map((item: any) => `${item.name}(${item.value})`)
      .join(', ');
  }

  return undefined;
}

/** 监听产品变化 */
watch(
  () => props.productId,
  () => {
    getThingModelTSL();
  },
  { immediate: true },
);

/** 监听触发类型变化 */
watch(
  () => props.triggerType,
  () => {
    localValue.value = '';
  },
);
</script>

<template>
  <div class="gap-8px flex items-center">
    <Select
      v-model="localValue"
      placeholder="请选择监控项"
      filterable
      clearable
      @change="handleChange"
      class="!w-150px"
      :loading="loading"
    >
      <Select.OptionGroup
        v-for="group in propertyGroups"
        :key="group.label"
        :label="group.label"
      >
        <Select.Option
          v-for="property in group.options"
          :key="property.identifier"
          :label="property.name"
          :value="property.identifier"
        >
          <div class="py-2px flex w-full items-center justify-between">
            <span class="text-14px font-500 flex-1 truncate text-primary">
              {{ property.name }}
            </span>
            <Tag
              :type="getDataTypeTagType(property.dataType)"
              size="small"
              class="ml-8px flex-shrink-0"
            >
              {{ property.identifier }}
            </Tag>
          </div>
        </Select.Option>
      </Select.OptionGroup>
    </Select>

    <!-- 属性详情弹出层 -->
    <Popover
      v-if="selectedProperty"
      placement="rightTop"
      :width="350"
      trigger="click"
      :show-arrow="true"
      :offset="8"
      popper-class="property-detail-popover"
    >
      <template #reference>
        <Button
          type="primary"
          text
          circle
          size="small"
          class="flex-shrink-0"
          title="查看属性详情"
        >
          <IconifyIcon icon="ep:info-filled" />
        </Button>
      </template>

      <!-- 弹出层内容 -->
      <div class="property-detail-content">
        <div class="gap-8px mb-12px flex items-center">
          <IconifyIcon icon="ep:info-filled" class="text-16px text-info" />
          <span class="text-14px font-500 text-primary">
            {{ selectedProperty.name }}
          </span>
          <Tag
            :type="getDataTypeTagType(selectedProperty.dataType)"
            size="small"
          >
            {{ getDataTypeName(selectedProperty.dataType) }}
          </Tag>
        </div>

        <div class="space-y-8px ml-24px">
          <div class="gap-8px flex items-start">
            <span class="text-12px min-w-60px flex-shrink-0 text-secondary">
              标识符：
            </span>
            <span class="text-12px flex-1 text-primary">
              {{ selectedProperty.identifier }}
            </span>
          </div>

          <div
            v-if="selectedProperty.description"
            class="gap-8px flex items-start"
          >
            <span class="text-12px min-w-60px flex-shrink-0 text-secondary">
              描述：
            </span>
            <span class="text-12px flex-1 text-primary">
              {{ selectedProperty.description }}
            </span>
          </div>

          <div v-if="selectedProperty.unit" class="gap-8px flex items-start">
            <span class="text-12px min-w-60px flex-shrink-0 text-secondary">
              单位：
            </span>
            <span class="text-12px flex-1 text-primary">
              {{ selectedProperty.unit }}
            </span>
          </div>

          <div v-if="selectedProperty.range" class="gap-8px flex items-start">
            <span class="text-12px min-w-60px flex-shrink-0 text-secondary">
              取值范围：
            </span>
            <span class="text-12px flex-1 text-primary">
              {{ selectedProperty.range }}
            </span>
          </div>

          <!-- 根据属性类型显示额外信息 -->
          <div
            v-if="
              selectedProperty.type === IoTThingModelTypeEnum.PROPERTY &&
              selectedProperty.accessMode
            "
            class="gap-8px flex items-start"
          >
            <span class="text-12px min-w-60px flex-shrink-0 text-secondary">
              访问模式：
            </span>
            <span class="text-12px flex-1 text-primary">
              {{ getAccessModeLabel(selectedProperty.accessMode) }}
            </span>
          </div>

          <div
            v-if="
              selectedProperty.type === IoTThingModelTypeEnum.EVENT &&
              selectedProperty.eventType
            "
            class="gap-8px flex items-start"
          >
            <span class="text-12px min-w-60px flex-shrink-0 text-secondary">
              事件类型：
            </span>
            <span class="text-12px flex-1 text-primary">
              {{ getEventTypeLabel(selectedProperty.eventType) }}
            </span>
          </div>

          <div
            v-if="
              selectedProperty.type === IoTThingModelTypeEnum.SERVICE &&
              selectedProperty.callType
            "
            class="gap-8px flex items-start"
          >
            <span class="text-12px min-w-60px flex-shrink-0 text-secondary">
              调用类型：
            </span>
            <span class="text-12px flex-1 text-primary">
              {{ getThingModelServiceCallTypeLabel(selectedProperty.callType) }}
            </span>
          </div>
        </div>
      </div>
    </Popover>
  </div>
</template>

<style scoped>
/* 下拉选项样式 */
:deep(.el-select-dropdown__item) {
  height: auto;
  padding: 6px 20px;
}

/* 弹出层内容样式 */
.property-detail-content {
  padding: 4px 0;
}

/* 弹出层自定义样式 */
:global(.property-detail-popover) {
  /* 可以在这里添加全局弹出层样式 */
  max-width: 400px !important;
}

:global(.property-detail-popover .el-popover__content) {
  padding: 16px !important;
}
</style>
