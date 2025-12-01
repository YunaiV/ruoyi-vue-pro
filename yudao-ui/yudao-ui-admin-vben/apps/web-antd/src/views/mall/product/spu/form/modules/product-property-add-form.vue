<!-- 商品发布 - 库存价格 - 添加属性 -->
<script lang="ts" setup>
import type { VbenFormSchema } from '#/adapter/form';
import type { MallPropertyApi } from '#/api/mall/product/property';

import { ref, watch } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createProperty,
  getPropertySimpleList,
} from '#/api/mall/product/property';
import { $t } from '#/locales';

defineOptions({ name: 'ProductPropertyAddForm' });

const props = defineProps({
  propertyList: {
    type: Array,
    default: () => [],
  },
});

const emit = defineEmits<{
  success: [];
}>();

const attributeList = ref<any[]>([]); // 商品属性列表
const attributeOptions = ref<MallPropertyApi.Property[]>([]); // 商品属性名称下拉框

watch(
  () => props.propertyList,
  (data) => {
    if (!data) {
      return;
    }
    attributeList.value = data as any[];
  },
  {
    deep: true,
    immediate: true,
  },
);

const formSchema: VbenFormSchema[] = [
  {
    fieldName: 'name',
    label: '属性名称',
    component: 'ApiSelect',
    componentProps: {
      api: async () => {
        const data = await getPropertySimpleList();
        attributeOptions.value = data;
        return data.map((item: MallPropertyApi.Property) => ({
          label: item.name,
          value: item.name,
        }));
      },
      showSearch: true,
      filterOption: true,
      placeholder: '请选择属性名称。如果不存在，可手动输入选择',
      mode: 'tags',
      allowClear: true,
    },
    rules: 'required',
  },
];

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 80,
  },
  layout: 'horizontal',
  schema: formSchema,
  showDefaultActions: false,
});

const [Modal, modalApi] = useVbenModal({
  destroyOnClose: true,
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }
    modalApi.lock();
    const values = await formApi.getValues();
    // name 为数组，遍历数组，进行重复添加校验
    const names = values.name;
    for (const name of names) {
      // 重复添加校验
      for (const attrItem of attributeList.value) {
        if (attrItem.name === name) {
          message.error('该属性已存在，请勿重复添加');
          return;
        }
      }
    }

    for (const name of names) {
      const existProperty = attributeOptions.value.find(
        (item: MallPropertyApi.Property) => item.name === name,
      );
      if (existProperty) {
        // 情况一：如果属性已存在，则直接使用并结束
        attributeList.value.push({
          id: existProperty.id,
          name,
          values: [],
        });
      } else {
        // 情况二：如果是不存在的属性，则需要执行新增
        const propertyId = await createProperty({ name });
        attributeList.value.push({
          id: propertyId,
          name,
          values: [],
        });
      }
    }
    message.success($t('ui.actionMessage.operationSuccess'));
    modalApi.unlock();
    await modalApi.close();
    emit('success');
  },

  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    await formApi.resetForm();
  },
});
</script>

<template>
  <Modal title="添加商品属性">
    <Form />
  </Modal>
</template>
