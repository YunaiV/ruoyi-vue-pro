<script lang="ts" setup>
import type { IotProductCategoryApi } from '#/api/iot/product/category';

import { computed, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createProductCategory,
  getProductCategory,
  updateProductCategory,
} from '#/api/iot/product/category';
import { $t } from '#/locales';

import { useFormSchema } from '../data';

const emit = defineEmits(['success']);
const formData = ref<IotProductCategoryApi.ProductCategory>();
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['产品分类'])
    : $t('ui.actionTitle.create', ['产品分类']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: useFormSchema(),
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
    const data =
      (await formApi.getValues()) as IotProductCategoryApi.ProductCategory;
    try {
      await (formData.value?.id
        ? updateProductCategory(data)
        : createProductCategory(data));
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
    let data = modalApi.getData<
      IotProductCategoryApi.ProductCategory & { parentId?: number }
    >();
    if (!data) {
      // 新增模式：设置默认值
      await formApi.setValues({
        sort: 0,
        status: 1,
      });
      return;
    }
    modalApi.lock();
    try {
      if (data.id) {
        // 编辑模式：加载完整数据
        data = await getProductCategory(data.id);
      } else if (data.parentId) {
        // 新增下级分类：设置父级ID
        await formApi.setValues({
          parentId: data.parentId,
          sort: 0,
          status: 1,
        });
        return;
      }
      // 设置到 values
      formData.value = data;
      await formApi.setValues(data);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-2/5" :title="getTitle">
    <Form class="mx-4" />
  </Modal>
</template>
