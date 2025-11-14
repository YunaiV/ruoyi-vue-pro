<!-- 设备信息 -->
<script setup lang="ts">
import type { IotDeviceApi } from '#/api/iot/device/device';
import type { IotProductApi } from '#/api/iot/product/product';

import { computed, ref } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { IconifyIcon } from '@vben/icons';
import { formatDate } from '@vben/utils';

import {
  Button,
  Card,
  Col,
  Descriptions,
  Form,
  Input,
  message,
  Modal,
  Row,
} from 'ant-design-vue';

import { getDeviceAuthInfo } from '#/api/iot/device/device';
import { DictTag } from '#/components/dict-tag';

// 消息提示

const { product, device } = defineProps<{
  device: IotDeviceApi.Device;
  product: IotProductApi.Product;
}>(); // 定义 Props
// const emit = defineEmits(['refresh']); // 定义 Emits

const authDialogVisible = ref(false); // 定义设备认证信息弹框的可见性
const authPasswordVisible = ref(false); // 定义密码可见性状态
const authInfo = ref<IotDeviceApi.DeviceAuthInfo>(
  {} as IotDeviceApi.DeviceAuthInfo,
); // 定义设备认证信息对象

/** 控制地图显示的标志 */
const showMap = computed(() => {
  return !!(device.longitude && device.latitude);
});

/** 复制到剪贴板方法 */
async function copyToClipboard(text: string) {
  try {
    await navigator.clipboard.writeText(text);
    message.success({ content: '复制成功' });
  } catch {
    message.error({ content: '复制失败' });
  }
}

/** 打开设备认证信息弹框的方法 */
async function handleAuthInfoDialogOpen() {
  if (!device.id) return;
  try {
    authInfo.value = await getDeviceAuthInfo(device.id);
    // 显示设备认证信息弹框
    authDialogVisible.value = true;
  } catch (error) {
    console.error('获取设备认证信息出错：', error);
    message.error({
      content: '获取设备认证信息失败，请检查网络连接或联系管理员',
    });
  }
}

/** 关闭设备认证信息弹框的方法 */
function handleAuthInfoDialogClose() {
  authDialogVisible.value = false;
}
</script>
<template>
  <div>
    <Row :gutter="16">
      <!-- 左侧设备信息 -->
      <Col :span="12">
        <Card class="h-full">
          <template #title>
            <div class="flex items-center">
              <IconifyIcon icon="ep:info-filled" class="text-primary mr-2" />
              <span>设备信息</span>
            </div>
          </template>
          <Descriptions :column="1" bordered>
            <Descriptions.Item label="产品名称">
              {{ product.name }}
            </Descriptions.Item>
            <Descriptions.Item label="ProductKey">
              {{ product.productKey }}
            </Descriptions.Item>
            <Descriptions.Item label="设备类型">
              <DictTag
                :type="DICT_TYPE.IOT_PRODUCT_DEVICE_TYPE"
                :value="product.deviceType"
              />
            </Descriptions.Item>
            <Descriptions.Item label="DeviceName">
              {{ device.deviceName }}
            </Descriptions.Item>
            <Descriptions.Item label="备注名称">
              {{ device.nickname || '--' }}
            </Descriptions.Item>
            <Descriptions.Item label="当前状态">
              <DictTag
                :type="DICT_TYPE.IOT_DEVICE_STATUS"
                :value="device.state"
              />
            </Descriptions.Item>
            <Descriptions.Item label="创建时间">
              {{ formatDate(device.createTime) }}
            </Descriptions.Item>
            <Descriptions.Item label="激活时间">
              {{ formatDate(device.activeTime) }}
            </Descriptions.Item>
            <Descriptions.Item label="最后上线时间">
              {{ formatDate(device.onlineTime) }}
            </Descriptions.Item>
            <Descriptions.Item label="最后离线时间">
              {{ formatDate(device.offlineTime) }}
            </Descriptions.Item>
            <Descriptions.Item label="MQTT 连接参数">
              <Button
                type="link"
                @click="handleAuthInfoDialogOpen"
                size="small"
              >
                查看
              </Button>
            </Descriptions.Item>
          </Descriptions>
        </Card>
      </Col>

      <!-- 右侧地图 -->
      <Col :span="12">
        <Card class="h-full">
          <template #title>
            <div class="flex items-center justify-between">
              <div class="flex items-center">
                <IconifyIcon icon="ep:location" class="text-primary mr-2" />
                <span>设备位置</span>
              </div>
            </div>
          </template>
          <div class="h-[500px] w-full">
            <div
              v-if="showMap"
              class="flex h-full w-full items-center justify-center rounded bg-gray-100"
            >
              <span class="text-gray-400">地图组件</span>
            </div>
            <div
              v-else
              class="flex h-full w-full items-center justify-center rounded bg-gray-50 text-gray-400"
            >
              <IconifyIcon icon="ep:warning" class="mr-2" />
              <span>暂无位置信息</span>
            </div>
          </div>
        </Card>
      </Col>
    </Row>

    <!-- 认证信息弹框 -->
    <Modal
      v-model:open="authDialogVisible"
      title="MQTT 连接参数"
      width="640px"
      :footer="null"
    >
      <Form :label-col="{ span: 6 }">
        <Form.Item label="clientId">
          <Input.Group compact>
            <Input
              v-model:value="authInfo.clientId"
              readonly
              style="width: calc(100% - 80px)"
            />
            <Button @click="copyToClipboard(authInfo.clientId)" type="primary">
              <IconifyIcon icon="ph:copy" />
            </Button>
          </Input.Group>
        </Form.Item>
        <Form.Item label="username">
          <Input.Group compact>
            <Input
              v-model:value="authInfo.username"
              readonly
              style="width: calc(100% - 80px)"
            />
            <Button @click="copyToClipboard(authInfo.username)" type="primary">
              <IconifyIcon icon="ph:copy" />
            </Button>
          </Input.Group>
        </Form.Item>
        <Form.Item label="password">
          <Input.Group compact>
            <Input
              v-model:value="authInfo.password"
              readonly
              :type="authPasswordVisible ? 'text' : 'password'"
              style="width: calc(100% - 160px)"
            />
            <Button
              @click="authPasswordVisible = !authPasswordVisible"
              type="primary"
            >
              <IconifyIcon
                :icon="authPasswordVisible ? 'ph:eye-slash' : 'ph:eye'"
              />
            </Button>
            <Button @click="copyToClipboard(authInfo.password)" type="primary">
              <IconifyIcon icon="ph:copy" />
            </Button>
          </Input.Group>
        </Form.Item>
      </Form>
      <div class="mt-4 text-right">
        <Button @click="handleAuthInfoDialogClose">关闭</Button>
      </div>
    </Modal>
  </div>
</template>
