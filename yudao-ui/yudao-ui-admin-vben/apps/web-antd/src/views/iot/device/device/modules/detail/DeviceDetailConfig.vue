<!-- 设备配置 -->
<script lang="ts" setup>
import type { IotDeviceApi } from '#/api/iot/device/device';

import { ref, watchEffect } from 'vue';

import { Alert, Button, message } from 'ant-design-vue';

import { sendDeviceMessage, updateDevice } from '#/api/iot/device/device';
import { IotDeviceMessageMethodEnum } from '#/views/iot/utils/constants';

defineOptions({ name: 'DeviceDetailConfig' });

const props = defineProps<{
  device: IotDeviceApi.Device;
}>();

const emit = defineEmits<{
  (e: 'success'): void; // 定义 success 事件，不需要参数
}>();

const loading = ref(false); // 加载中
const pushLoading = ref(false); // 推送加载中
const config = ref<any>({}); // 只存储 config 字段
const hasJsonError = ref(false); // 是否有 JSON 格式错误

/** 监听 props.device 的变化，只更新 config 字段 */
watchEffect(() => {
  try {
    config.value = props.device.config ? JSON.parse(props.device.config) : {};
  } catch {
    config.value = {};
  }
});

const isEditing = ref(false); // 编辑状态
/** 启用编辑模式的函数 */
function enableEdit() {
  isEditing.value = true;
  hasJsonError.value = false; // 重置错误状态
}

/** 取消编辑的函数 */
function cancelEdit() {
  try {
    config.value = props.device.config ? JSON.parse(props.device.config) : {};
  } catch {
    config.value = {};
  }
  isEditing.value = false;
  hasJsonError.value = false; // 重置错误状态
}

/** 保存配置的函数 */
async function saveConfig() {
  if (hasJsonError.value) {
    message.error({ content: 'JSON格式错误，请修正后再提交！' });
    return;
  }
  await updateDeviceConfig();
  isEditing.value = false;
}

/** 配置推送处理函数 */
async function handleConfigPush() {
  try {
    pushLoading.value = true;

    // 调用配置推送接口
    await sendDeviceMessage({
      deviceId: props.device.id!,
      method: IotDeviceMessageMethodEnum.CONFIG_PUSH.method,
      params: config.value,
    });

    message.success({ content: '配置推送成功！' });
  } catch (error) {
    if (error !== 'cancel') {
      message.error({ content: '配置推送失败！' });
      console.error('配置推送错误:', error);
    }
  } finally {
    pushLoading.value = false;
  }
}

/** 更新设备配置 */
async function updateDeviceConfig() {
  try {
    // 提交请求
    loading.value = true;
    await updateDevice({
      id: props.device.id,
      config: JSON.stringify(config.value),
    } as IotDeviceApi.Device);
    message.success({ content: '更新成功！' });
    // 触发 success 事件
    emit('success');
  } catch (error) {
    console.error(error);
  } finally {
    loading.value = false;
  }
}

/** 处理 JSON 编辑器错误的函数 */
function onError(errors: any) {
  if (!errors || (Array.isArray(errors) && errors.length === 0)) {
    hasJsonError.value = false;
    return;
  }
  hasJsonError.value = true;
}
</script>

<template>
  <div>
    <Alert
      message="支持远程更新设备的配置文件(JSON 格式)，可以在下方编辑配置模板，对设备的系统参数、网络参数等进行远程配置。配置完成后，需点击「下发」按钮，设备即可进行远程配置。"
      type="info"
      show-icon
      class="my-4"
      description="如需编辑文件，请点击下方编辑按钮"
    />
    <JsonEditor
      v-model="config"
      :mode="isEditing ? 'code' : 'view'"
      height="600px"
      @error="onError"
    />
    <div class="mt-5 text-center">
      <Button v-if="isEditing" @click="cancelEdit">取消</Button>
      <Button
        v-if="isEditing"
        type="primary"
        @click="saveConfig"
        :disabled="hasJsonError"
      >
        保存
      </Button>
      <Button v-else @click="enableEdit">编辑</Button>
      <Button
        v-if="!isEditing"
        type="primary"
        @click="handleConfigPush"
        :loading="pushLoading"
      >
        配置推送
      </Button>
    </div>
  </div>
</template>
