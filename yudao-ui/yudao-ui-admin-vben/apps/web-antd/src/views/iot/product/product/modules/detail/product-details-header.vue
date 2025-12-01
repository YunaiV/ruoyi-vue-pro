<script setup lang="ts">
// TODO @haohao：放到 detail/modules 里。然后名字就是 header.vue
import type { IotProductApi } from '#/api/iot/product/product';

import { ref } from 'vue';
import { useRouter } from 'vue-router';

import { Button, Card, Descriptions, message } from 'ant-design-vue';

import { updateProductStatus } from '#/api/iot/product/product';

import ProductForm from '../product-form.vue';

interface Props {
  product: IotProductApi.Product;
  loading?: boolean;
}

withDefaults(defineProps<Props>(), {
  loading: false,
});

const emit = defineEmits<{
  refresh: [];
}>();

const router = useRouter();
const formRef = ref();

/** 复制到剪贴板 */
async function copyToClipboard(text: string) {
  try {
    await navigator.clipboard.writeText(text);
    message.success('复制成功');
  } catch {
    message.error('复制失败');
  }
}

/** 跳转到设备管理 */
function goToDeviceList(productId: number) {
  router.push({
    path: '/iot/device/device',
    query: { productId: String(productId) },
  });
}

/** 打开编辑表单 */
function openForm(type: string, id?: number) {
  formRef.value?.open(type, id);
}

/** 发布产品 */
async function confirmPublish(id: number) {
  // TODO @haohao：最好类似；async function handleDeleteBatch() { 的做法：1）有个 confirm；2）有个 loading
  try {
    await updateProductStatus(id, 1); // TODO @好好】：1 和 0，最好用枚举；
    message.success('发布成功');
    emit('refresh');
  } catch {
    message.error('发布失败');
  }
}

/** 撤销发布 */
async function confirmUnpublish(id: number) {
  // TODO @haohao：最好类似；async function handleDeleteBatch() { 的做法：1）有个 confirm；2）有个 loading
  try {
    await updateProductStatus(id, 0);
    message.success('撤销发布成功');
    emit('refresh');
  } catch {
    message.error('撤销发布失败');
  }
}
</script>

<template>
  <div class="mb-4">
    <div class="flex items-start justify-between">
      <div>
        <h2 class="text-xl font-bold">{{ product.name }}</h2>
      </div>
      <div class="space-x-2">
        <Button
          :disabled="product.status === 1"
          @click="openForm('update', product.id)"
        >
          编辑
        </Button>
        <Button
          v-if="product.status === 0"
          type="primary"
          @click="confirmPublish(product.id!)"
        >
          发布
        </Button>
        <Button
          v-if="product.status === 1"
          danger
          @click="confirmUnpublish(product.id!)"
        >
          撤销发布
        </Button>
      </div>
    </div>

    <Card class="mt-4">
      <Descriptions :column="1">
        <Descriptions.Item label="ProductKey">
          {{ product.productKey }}
          <Button
            size="small"
            class="ml-2"
            @click="copyToClipboard(product.productKey || '')"
          >
            复制
          </Button>
        </Descriptions.Item>
        <Descriptions.Item label="设备总数">
          <span class="ml-5 mr-2">
            {{ product.deviceCount ?? '加载中...' }}
          </span>
          <Button size="small" @click="goToDeviceList(product.id!)">
            前往管理
          </Button>
        </Descriptions.Item>
      </Descriptions>
    </Card>

    <!-- 表单弹窗 -->
    <!-- TODO @haohao：弹不出来；另外，应该用 index.vue 里，Form 的声明方式哈。 -->
    <ProductForm ref="formRef" @success="emit('refresh')" />
  </div>
</template>
