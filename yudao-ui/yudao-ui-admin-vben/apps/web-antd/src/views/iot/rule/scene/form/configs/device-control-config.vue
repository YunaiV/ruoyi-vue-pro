<!-- 设备控制配置组件 -->
<script setup lang="ts">
import type { Action } from '#/api/iot/rule/scene';
import type {
  ThingModelProperty,
  ThingModelService,
} from '#/api/iot/thingmodel';

import { computed, onMounted, ref, watch } from 'vue';

import { isObject } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { Col, Form, Row, Select, Tag } from 'ant-design-vue';

import { getThingModelListByProductId } from '#/api/iot/thingmodel';
import {
  IoTDataSpecsDataTypeEnum,
  IotRuleSceneActionTypeEnum,
  IoTThingModelAccessModeEnum,
} from '#/views/iot/utils/constants';

import JsonParamsInput from '../inputs/json-params-input.vue';
import DeviceSelector from '../selectors/device-selector.vue';
import ProductSelector from '../selectors/product-selector.vue';

/** 设备控制配置组件 */
defineOptions({ name: 'DeviceControlConfig' });

const props = defineProps<{
  modelValue: Action;
}>();

const emit = defineEmits<{
  (e: 'update:modelValue', value: Action): void;
}>();

const action = useVModel(props, 'modelValue', emit);

const thingModelProperties = ref<ThingModelProperty[]>([]); // 物模型属性列表
const loadingThingModel = ref(false); // 物模型加载状态
const selectedService = ref<null | ThingModelService>(null); // 选中的服务对象
const serviceList = ref<ThingModelService[]>([]); // 服务列表
const loadingServices = ref(false); // 服务加载状态

// 参数值的计算属性，用于双向绑定
const paramsValue = computed({
  get: () => {
    // 如果 params 是对象，转换为 JSON 字符串（兼容旧数据）
    if (action.value.params && isObject(action.value.params)) {
      return JSON.stringify(action.value.params, null, 2);
    }
    // 如果 params 已经是字符串，直接返回
    return action.value.params || '';
  },
  set: (value: string) => {
    // 直接保存为 JSON 字符串，不进行解析转换
    action.value.params = value.trim() || '';
  },
});

// 计算属性：是否为属性设置类型
const isPropertySetAction = computed(() => {
  return (
    action.value.type ===
    IotRuleSceneActionTypeEnum.DEVICE_PROPERTY_SET.toString()
  );
});

// 计算属性：是否为服务调用类型
const isServiceInvokeAction = computed(() => {
  return (
    action.value.type ===
    IotRuleSceneActionTypeEnum.DEVICE_SERVICE_INVOKE.toString()
  );
});

/**
 * 处理产品变化事件
 * @param productId 产品 ID
 */
function handleProductChange(productId?: number) {
  // 当产品变化时，清空设备选择和参数配置
  if (action.value.productId !== productId) {
    action.value.deviceId = undefined;
    action.value.identifier = undefined; // 清空服务标识符
    action.value.params = ''; // 清空参数，保存为空字符串
    selectedService.value = null; // 清空选中的服务
    serviceList.value = []; // 清空服务列表
  }

  // 加载新产品的物模型属性或服务列表
  if (productId) {
    if (isPropertySetAction.value) {
      loadThingModelProperties(productId);
    } else if (isServiceInvokeAction.value) {
      loadServiceList(productId);
    }
  }
}

/**
 * 处理设备变化事件
 * @param deviceId 设备 ID
 */
function handleDeviceChange(deviceId?: number) {
  // 当设备变化时，清空参数配置
  if (action.value.deviceId !== deviceId) {
    action.value.params = ''; // 清空参数，保存为空字符串
  }
}

/**
 * 处理服务变化事件
 * @param serviceIdentifier 服务标识符
 */
function handleServiceChange(serviceIdentifier?: any) {
  // 根据服务标识符找到对应的服务对象
  const service =
    serviceList.value.find((s) => s.identifier === serviceIdentifier) || null;
  selectedService.value = service;

  // 当服务变化时，清空参数配置
  action.value.params = '';

  // 如果选择了服务且有输入参数，生成默认参数结构
  if (service && service.inputParams && service.inputParams.length > 0) {
    const defaultParams = {};
    service.inputParams.forEach((param) => {
      defaultParams[param.identifier] = getDefaultValueForParam(param);
    });
    // 将默认参数转换为 JSON 字符串保存
    action.value.params = JSON.stringify(defaultParams, null, 2);
  }
}

/**
 * 获取物模型TSL数据
 * @param productId 产品ID
 * @returns 物模型TSL数据
 */
async function getThingModelTSL(productId: number) {
  if (!productId) return null;

  try {
    return await getThingModelListByProductId(productId);
  } catch (error) {
    console.error('获取物模型TSL数据失败:', error);
    return null;
  }
}

/**
 * 加载物模型属性（可写属性）
 * @param productId 产品ID
 */
async function loadThingModelProperties(productId: number) {
  if (!productId) {
    thingModelProperties.value = [];
    return;
  }

  try {
    loadingThingModel.value = true;
    const tslData = await getThingModelTSL(productId);

    if (!tslData?.properties) {
      thingModelProperties.value = [];
      return;
    }

    // 过滤出可写的属性（accessMode 包含 'w'）
    thingModelProperties.value = tslData.properties.filter(
      (property: ThingModelProperty) =>
        property.accessMode &&
        (property.accessMode === IoTThingModelAccessModeEnum.READ_WRITE.value ||
          property.accessMode === IoTThingModelAccessModeEnum.WRITE_ONLY.value),
    );
  } catch (error) {
    console.error('加载物模型属性失败:', error);
    thingModelProperties.value = [];
  } finally {
    loadingThingModel.value = false;
  }
}

/**
 * 加载服务列表
 * @param productId 产品ID
 */
async function loadServiceList(productId: number) {
  if (!productId) {
    serviceList.value = [];
    return;
  }

  try {
    loadingServices.value = true;
    const tslData = await getThingModelTSL(productId);

    if (!tslData?.services) {
      serviceList.value = [];
      return;
    }

    serviceList.value = tslData.services;
  } catch (error) {
    console.error('加载服务列表失败:', error);
    serviceList.value = [];
  } finally {
    loadingServices.value = false;
  }
}

/**
 * 从TSL加载服务信息（用于编辑模式回显）
 * @param productId 产品ID
 * @param serviceIdentifier 服务标识符
 */
async function loadServiceFromTSL(
  productId: number,
  serviceIdentifier: string,
) {
  // 先加载服务列表
  await loadServiceList(productId);

  // 然后设置选中的服务
  const service = serviceList.value.find(
    (s: any) => s.identifier === serviceIdentifier,
  );
  if (service) {
    selectedService.value = service;
  }
}

/**
 * 根据参数类型获取默认值
 * @param param 参数对象
 * @returns 默认值
 */
function getDefaultValueForParam(param: any) {
  switch (param.dataType) {
    case IoTDataSpecsDataTypeEnum.BOOL: {
      return false;
    }
    case IoTDataSpecsDataTypeEnum.DOUBLE:
    case IoTDataSpecsDataTypeEnum.FLOAT: {
      return 0;
    }
    case IoTDataSpecsDataTypeEnum.ENUM: {
      // 如果有枚举值，使用第一个
      if (
        param.dataSpecs?.dataSpecsList &&
        param.dataSpecs.dataSpecsList.length > 0
      ) {
        return param.dataSpecs.dataSpecsList[0].value;
      }
      return '';
    }
    case IoTDataSpecsDataTypeEnum.INT: {
      return 0;
    }
    case IoTDataSpecsDataTypeEnum.TEXT: {
      return '';
    }
    default: {
      return '';
    }
  }
}

const isInitialized = ref(false); // 防止重复初始化的标志

/**
 * 初始化组件数据
 */
async function initializeComponent() {
  if (isInitialized.value) return;

  const currentAction = action.value;
  if (!currentAction) return;

  // 如果已经选择了产品且是属性设置类型，加载物模型
  if (currentAction.productId && isPropertySetAction.value) {
    await loadThingModelProperties(currentAction.productId);
  }

  // 如果是服务调用类型且已有标识符，初始化服务选择
  if (
    currentAction.productId &&
    isServiceInvokeAction.value &&
    currentAction.identifier
  ) {
    // 加载物模型TSL以获取服务信息
    await loadServiceFromTSL(currentAction.productId, currentAction.identifier);
  }

  isInitialized.value = true;
}

/** 组件初始化 */
onMounted(() => {
  initializeComponent();
});

/** 监听关键字段的变化，避免深度监听导致的性能问题 */
watch(
  () => [action.value.productId, action.value.type, action.value.identifier],
  async ([newProductId, , newIdentifier], [oldProductId, , oldIdentifier]) => {
    // 避免初始化时的重复调用
    if (!isInitialized.value) return;

    // 产品变化时重新加载数据
    if (newProductId !== oldProductId) {
      if (newProductId && isPropertySetAction.value) {
        await loadThingModelProperties(newProductId as number);
      } else if (newProductId && isServiceInvokeAction.value) {
        await loadServiceList(newProductId as number);
      }
    }

    // 服务标识符变化时更新选中的服务
    if (
      newIdentifier !== oldIdentifier &&
      newProductId &&
      isServiceInvokeAction.value &&
      newIdentifier
    ) {
      const service = serviceList.value.find(
        (s: any) => s.identifier === newIdentifier,
      );
      if (service) {
        selectedService.value = service;
      }
    }
  },
);
</script>

<template>
  <div class="gap-16px flex flex-col">
    <!-- 产品和设备选择 - 与触发器保持一致的分离式选择器 -->
    <Row :gutter="16">
      <Col :span="12">
        <Form.Item label="产品" required>
          <ProductSelector
            v-model="action.productId"
            @change="handleProductChange"
          />
        </Form.Item>
      </Col>
      <Col :span="12">
        <Form.Item label="设备" required>
          <DeviceSelector
            v-model="action.deviceId"
            :product-id="action.productId"
            @change="handleDeviceChange"
          />
        </Form.Item>
      </Col>
    </Row>

    <!-- 服务选择 - 服务调用类型时显示 -->
    <div v-if="action.productId && isServiceInvokeAction" class="space-y-16px">
      <Form.Item label="服务" required>
        <Select
          v-model="action.identifier"
          placeholder="请选择服务"
          filterable
          clearable
          class="w-full"
          :loading="loadingServices"
          @change="handleServiceChange"
        >
          <Select.Option
            v-for="service in serviceList"
            :key="service.identifier"
            :label="service.name"
            :value="service.identifier"
          >
            <div class="flex items-center justify-between">
              <span>{{ service.name }}</span>
              <Tag
                :type="service.callType === 'sync' ? 'primary' : 'success'"
                size="small"
              >
                {{ service.callType === 'sync' ? '同步' : '异步' }}
              </Tag>
            </div>
          </Select.Option>
        </Select>
      </Form.Item>

      <!-- 服务参数配置 -->
      <div v-if="action.identifier" class="space-y-16px">
        <Form.Item label="服务参数" required>
          <JsonParamsInput
            v-model="paramsValue"
            type="service"
            :config="{ service: selectedService } as any"
            placeholder="请输入 JSON 格式的服务参数"
          />
        </Form.Item>
      </div>
    </div>

    <!-- 控制参数配置 - 属性设置类型时显示 -->
    <div v-if="action.productId && isPropertySetAction" class="space-y-16px">
      <!-- 参数配置 -->
      <Form.Item label="参数" required>
        <JsonParamsInput
          v-model="paramsValue"
          type="property"
          :config="{ properties: thingModelProperties }"
          placeholder="请输入 JSON 格式的控制参数"
        />
      </Form.Item>
    </div>
  </div>
</template>
