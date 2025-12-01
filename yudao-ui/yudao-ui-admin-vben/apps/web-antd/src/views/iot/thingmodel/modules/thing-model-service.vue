<!-- 产品的物模型表单（service 项） -->
<script lang="ts" setup>
import type { Ref } from 'vue';

import { watch } from 'vue';

import { isEmpty } from '@vben/utils';

import { useVModel } from '@vueuse/core';
import { Form, Radio } from 'ant-design-vue';

import {
  IoTThingModelParamDirectionEnum,
  IoTThingModelServiceCallTypeEnum,
} from '#/views/iot/utils/constants';

import ThingModelInputOutputParam from './thing-model-input-output-param.vue';

/** IoT 物模型服务 */
defineOptions({ name: 'ThingModelService' });

const props = defineProps<{ isStructDataSpecs?: boolean; modelValue: any }>();
const emits = defineEmits(['update:modelValue']);
const service = useVModel(props, 'modelValue', emits) as Ref<any>;

/** 默认选中，ASYNC 异步 */
watch(
  () => service.value.callType,
  (val: string | undefined) =>
    isEmpty(val) &&
    (service.value.callType = IoTThingModelServiceCallTypeEnum.ASYNC.value),
  { immediate: true },
);
</script>

<template>
  <Form.Item
    :rules="[{ required: true, message: '请选择调用方式', trigger: 'change' }]"
    label="调用方式"
    name="service.callType"
  >
    <Radio.Group v-model:value="service.callType">
      <Radio
        v-for="callType in Object.values(IoTThingModelServiceCallTypeEnum)"
        :key="callType.value"
        :value="callType.value"
      >
        {{ callType.label }}
      </Radio>
    </Radio.Group>
  </Form.Item>
  <Form.Item label="输入参数">
    <ThingModelInputOutputParam
      v-model="service.inputParams"
      :direction="IoTThingModelParamDirectionEnum.INPUT"
    />
  </Form.Item>
  <Form.Item label="输出参数">
    <ThingModelInputOutputParam
      v-model="service.outputParams"
      :direction="IoTThingModelParamDirectionEnum.OUTPUT"
    />
  </Form.Item>
</template>

<style lang="scss" scoped>
:deep(.ant-form-item) {
  .ant-form-item {
    margin-bottom: 0;
  }
}
</style>
