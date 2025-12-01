<script lang="ts" setup>
import type { MallCommentApi } from '#/api/mall/product/comment';
import type { MallSpuApi } from '#/api/mall/product/spu';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { Button, message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import { createComment, getComment } from '#/api/mall/product/comment';
import { getSpu } from '#/api/mall/product/spu';
import { $t } from '#/locales';
import {
  SkuTableSelect,
  SpuShowcase,
} from '#/views/mall/product/spu/components';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);

// 初始化 formData，确保始终有值
const formData = ref<Partial<MallCommentApi.Comment>>({
  descriptionScores: 5,
  benefitScores: 5,
});
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['虚拟评论'])
    : $t('ui.actionTitle.create', ['虚拟评论']);
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
  schema: useFormSchema(),
  showDefaultActions: false,
});

const skuTableSelectRef = ref<InstanceType<typeof SkuTableSelect>>();
const selectedSku = ref<MallSpuApi.Sku>();

/** 处理商品的选择变化 */
async function handleSpuChange(spu?: MallSpuApi.Spu | null) {
  // 处理商品选择：如果 spu 为 null 或 id 为 0，表示清空选择
  const spuId = spu?.id && spu.id ? spu.id : undefined;
  formData.value.spuId = spuId;
  await formApi.setFieldValue('spuId', spuId);
  // 清空已选规格
  selectedSku.value = undefined;
  formData.value.skuId = undefined;
  await formApi.setFieldValue('skuId', undefined);
}

/** 打开商品规格的选择弹框 */
async function openSkuSelect() {
  const currentValues =
    (await formApi.getValues()) as Partial<MallCommentApi.Comment>;
  const currentSpuId = currentValues.spuId ?? formData.value?.spuId;
  if (!currentSpuId) {
    message.warning('请先选择商品');
    return;
  }
  skuTableSelectRef.value?.open({ spuId: currentSpuId });
}

/** 处理商品规格的选择 */
async function handleSkuSelected(sku: MallSpuApi.Sku) {
  selectedSku.value = sku;
  formData.value.skuId = sku.id;
  await formApi.setFieldValue('skuId', sku.id);
}

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    // 提交表单
    const data = (await formApi.getValues()) as MallCommentApi.Comment;
    try {
      await createComment(data);
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
      // 重置表单数据
      selectedSku.value = undefined;
      return;
    }
    // 加载数据
    const data = modalApi.getData<MallCommentApi.Comment>();
    if (!data || !data.id) {
      // 新建模式：重置表单
      selectedSku.value = undefined;
      await formApi.setValues({ spuId: undefined, skuId: undefined });
      return;
    }
    // 编辑模式：加载数据
    modalApi.lock();
    try {
      formData.value = await getComment(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
      // 回显已选的商品规格
      if (formData.value?.spuId && formData.value?.skuId) {
        const spu = await getSpu(formData.value.spuId);
        const sku = spu.skus?.find((item) => item.id === formData.value!.skuId);
        if (sku) {
          selectedSku.value = sku;
        }
      } else {
        selectedSku.value = undefined;
      }
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-2/5" :title="getTitle">
    <Form class="mx-4">
      <template #spuId>
        <SpuShowcase
          v-model="(formData as any).spuId"
          :limit="1"
          @change="handleSpuChange"
        />
      </template>
      <template #skuId>
        <div class="flex items-center gap-2">
          <Button
            type="primary"
            :disabled="!formData?.spuId"
            @click="openSkuSelect"
          >
            选择规格
          </Button>
          <span
            v-if="
              selectedSku &&
              selectedSku.properties &&
              selectedSku.properties.length > 0
            "
          >
            已选：
            {{ selectedSku.properties.map((p: any) => p.valueName).join('/') }}
          </span>
          <span v-else-if="selectedSku">已选：{{ selectedSku.id }}</span>
        </div>
      </template>
    </Form>
    <SkuTableSelect ref="skuTableSelectRef" @change="handleSkuSelected" />
  </Modal>
</template>
