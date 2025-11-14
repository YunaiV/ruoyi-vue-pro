<script lang="ts" setup>
import { computed, ref, watch } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createDataSink,
  getDataSink,
  updateDataSink,
} from '#/api/iot/rule/data/sink';
import { $t } from '#/locales';

import {
  HttpConfigForm,
  KafkaMQConfigForm,
  MqttConfigForm,
  RabbitMQConfigForm,
  RedisStreamConfigForm,
  RocketMQConfigForm,
} from './config';
import { useSinkFormSchema } from './data';

const emit = defineEmits(['success']);

const IotDataSinkTypeEnum = {
  HTTP: 1,
  MQTT: 2,
  ROCKETMQ: 3,
  KAFKA: 4,
  RABBITMQ: 5,
  REDIS_STREAM: 6,
} as const;

const formData = ref<any>();

const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['数据目的'])
    : $t('ui.actionTitle.create', ['数据目的']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 120,
  },
  layout: 'horizontal',
  schema: useSinkFormSchema(),
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const data = (await formApi.getValues()) as any;
    data.config = formData.value.config;

    try {
      await (formData.value?.id ? updateDataSink(data) : createDataSink(data));
      // 关闭并提示
      await modalApi.close();
      emit('success');
      message.success($t('ui.actionMessage.operationSuccess'));
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      formData.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData<any>();

    if (!data || !data.id) {
      formData.value = {
        type: IotDataSinkTypeEnum.HTTP,
        status: 0,
        config: {},
      };
      await formApi.setValues(formData.value);
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getDataSink(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
    } finally {
      modalApi.unlock();
    }
  },
});

// 监听类型变化，重置配置
watch(
  () => formApi.getValues().then((values) => values.type),
  (newType) => {
    if (formData.value && newType !== formData.value.type) {
      formData.value.config = {};
    }
  },
);
</script>

<template>
  <Modal class="w-3/5" :title="getTitle">
    <Form class="mx-4" />
    <div v-if="formData" class="mx-4 mt-4">
      <div class="mb-2 text-sm font-medium">配置信息</div>
      <HttpConfigForm
        v-if="IotDataSinkTypeEnum.HTTP === formData.type"
        v-model="formData.config"
      />
      <MqttConfigForm
        v-if="IotDataSinkTypeEnum.MQTT === formData.type"
        v-model="formData.config"
      />
      <RocketMQConfigForm
        v-if="IotDataSinkTypeEnum.ROCKETMQ === formData.type"
        v-model="formData.config"
      />
      <KafkaMQConfigForm
        v-if="IotDataSinkTypeEnum.KAFKA === formData.type"
        v-model="formData.config"
      />
      <RabbitMQConfigForm
        v-if="IotDataSinkTypeEnum.RABBITMQ === formData.type"
        v-model="formData.config"
      />
      <RedisStreamConfigForm
        v-if="IotDataSinkTypeEnum.REDIS_STREAM === formData.type"
        v-model="formData.config"
      />
    </div>
  </Modal>
</template>
