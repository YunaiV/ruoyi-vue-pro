<!-- 设备信息（头部） -->
<script setup lang="ts">
import type { IotDeviceApi } from '#/api/iot/device/device';
import type { IotProductApi } from '#/api/iot/product/product';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { Button, Card, Descriptions, message } from 'ant-design-vue';

import DeviceForm from '../device-form.vue';

interface Props {
  product: IotProductApi.Product;
  device: IotDeviceApi.Device;
  loading?: boolean;
}

withDefaults(defineProps<Props>(), {
  loading: false,
});

const emit = defineEmits<{
  refresh: [];
}>();

const router = useRouter();

/** 操作修改 */
const formRef = ref();
function openForm(type: string, id?: number) {
  formRef.value.open(type, id);
}

/** 复制到剪贴板方法 */
async function copyToClipboard(text: string | undefined) {
  if (!text) return;
  try {
    await navigator.clipboard.writeText(text);
    message.success({ content: '复制成功' });
  } catch {
    message.error({ content: '复制失败' });
  }
}

/** 跳转到产品详情页面 */
function goToProductDetail(productId: number | undefined) {
  if (productId) {
    router.push({ name: 'IoTProductDetail', params: { id: productId } });
  }
}
</script>
<template>
  <div class="mb-4">
    <div class="flex items-start justify-between">
      <div>
        <h2 class="text-xl font-bold">{{ device.deviceName }}</h2>
      </div>
      <div class="space-x-2">
        <!-- 右上：按钮 -->
        <Button
          v-if="product.status === 0"
          v-access:code="['iot:device:update']"
          @click="openForm('update', device.id)"
        >
          编辑
        </Button>
      </div>
    </div>

    <Card class="mt-4">
      <Descriptions :column="1">
        <Descriptions.Item label="产品">
          <a
            @click="goToProductDetail(product.id)"
            class="cursor-pointer text-blue-600"
          >
            {{ product.name }}
          </a>
        </Descriptions.Item>
        <Descriptions.Item label="ProductKey">
          {{ product.productKey }}
          <Button
            size="small"
            class="ml-2"
            @click="copyToClipboard(product.productKey)"
          >
            复制
          </Button>
        </Descriptions.Item>
      </Descriptions>
    </Card>

    <!-- 表单弹窗：添加/修改 -->
    <DeviceForm ref="formRef" @success="emit('refresh')" />
  </div>
</template>
