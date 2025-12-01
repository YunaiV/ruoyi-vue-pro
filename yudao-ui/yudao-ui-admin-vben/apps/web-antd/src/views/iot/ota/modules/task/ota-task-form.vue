<script setup lang="ts">
import type { IotDeviceApi } from '#/api/iot/device/device';
import type { OtaTask } from '#/api/iot/ota/task';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Form, Input, message, Select, Spin } from 'ant-design-vue';

import { getDeviceListByProductId } from '#/api/iot/device/device';
import { createOtaTask } from '#/api/iot/ota/task';
import { IoTOtaTaskDeviceScopeEnum } from '#/views/iot/utils/constants';

/** IoT OTA 升级任务表单 */
defineOptions({ name: 'OtaTaskForm' });

const props = defineProps<{
  firmwareId: number;
  productId: number;
}>();

const emit = defineEmits(['success']);

const formLoading = ref(false);
const formData = ref<OtaTask>({
  name: '',
  deviceScope: IoTOtaTaskDeviceScopeEnum.ALL.value,
  firmwareId: props.firmwareId,
  description: '',
  deviceIds: [],
});
const formRef = ref();
const formRules = {
  name: [
    {
      required: true,
      message: '请输入任务名称',
      trigger: 'blur' as const,
      type: 'string' as const,
    },
  ],
  deviceScope: [
    {
      required: true,
      message: '请选择升级范围',
      trigger: 'change' as const,
      type: 'number' as const,
    },
  ],
  deviceIds: [
    {
      required: true,
      message: '请至少选择一个设备',
      trigger: 'change' as const,
      type: 'array' as const,
    },
  ],
};
const devices = ref<IotDeviceApi.Device[]>([]);

/** 设备选项 */
const deviceOptions = computed(() => {
  return devices.value.map((device) => ({
    label: device.nickname
      ? `${device.deviceName} (${device.nickname})`
      : device.deviceName,
    value: device.id,
  }));
});

/** 升级范围选项 */
const deviceScopeOptions = computed(() => {
  return Object.values(IoTOtaTaskDeviceScopeEnum).map((item) => ({
    label: item.label,
    value: item.value,
  }));
});

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    try {
      await formRef.value.validate();
      modalApi.lock();
      await createOtaTask(formData.value);
      message.success('创建成功');
      await modalApi.close();
      emit('success');
    } finally {
      modalApi.unlock();
    }
  },
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      resetForm();
      return;
    }
    // 加载设备列表
    formLoading.value = true;
    try {
      devices.value = (await getDeviceListByProductId(props.productId)) || [];
    } finally {
      formLoading.value = false;
    }
  },
});

/** 重置表单 */
function resetForm() {
  formData.value = {
    name: '',
    deviceScope: IoTOtaTaskDeviceScopeEnum.ALL.value,
    firmwareId: props.firmwareId,
    description: '',
    deviceIds: [],
  };
  formRef.value?.resetFields();
}

/** 打开弹窗 */
async function open() {
  await modalApi.open();
}

defineExpose({ open });
</script>

<template>
  <Modal title="新增升级任务" class="w-3/5">
    <Spin :spinning="formLoading">
      <Form
        ref="formRef"
        :model="formData"
        :rules="formRules"
        :label-col="{ span: 4 }"
        :wrapper-col="{ span: 20 }"
        class="mx-4"
      >
        <Form.Item label="任务名称" name="name">
          <Input v-model:value="formData.name" placeholder="请输入任务名称" />
        </Form.Item>
        <Form.Item label="任务描述" name="description">
          <Input.TextArea
            v-model:value="formData.description"
            :rows="3"
            placeholder="请输入任务描述"
          />
        </Form.Item>
        <Form.Item label="升级范围" name="deviceScope">
          <Select
            v-model:value="formData.deviceScope"
            placeholder="请选择升级范围"
            :options="deviceScopeOptions"
          />
        </Form.Item>
        <Form.Item
          v-if="formData.deviceScope === IoTOtaTaskDeviceScopeEnum.SELECT.value"
          label="选择设备"
          name="deviceIds"
        >
          <Select
            v-model:value="formData.deviceIds"
            mode="multiple"
            placeholder="请选择设备"
            :options="deviceOptions"
            :filter-option="true"
            show-search
          />
        </Form.Item>
      </Form>
    </Spin>
  </Modal>
</template>
