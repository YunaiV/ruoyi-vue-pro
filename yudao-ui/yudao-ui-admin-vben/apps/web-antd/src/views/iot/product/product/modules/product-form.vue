<script setup lang="ts">
import type { IotProductApi } from '#/api/iot/product/product';

import { computed, ref } from 'vue';

import { useVbenForm, useVbenModal } from '@vben/common-ui';

import { Collapse, message } from 'ant-design-vue';

import {
  createProduct,
  getProduct,
  updateProduct,
} from '#/api/iot/product/product';
import { $t } from '#/locales';

import {
  generateProductKey,
  useAdvancedFormSchema,
  useBasicFormSchema,
} from '../data';

// TODO @haohao：应该是 form.vue；

defineOptions({ name: 'IoTProductForm' });

const emit = defineEmits(['success']);

const CollapsePanel = Collapse.Panel;

const formData = ref<any>();
const getTitle = computed(() => {
  return formData.value?.id ? '编辑产品' : '新增产品';
});
const activeKey = ref<string[]>([]); // 折叠面板的激活 key，默认不展开

// TODO @haohao：每一行一个；
const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  wrapperClass: 'grid-cols-2',
  layout: 'horizontal',
  schema: [],
  showDefaultActions: false,
});

// TODO @haohao：每一行一个；
const [AdvancedForm, advancedFormApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
  },
  wrapperClass: 'grid-cols-2',
  layout: 'horizontal',
  schema: [],
  showDefaultActions: false,
});

// TODO @haohao：看看是不是可以参考别的 form 模块，优化表单这块的逻辑；从 61 到 156 行。体感有点冗余、以及代码风格，不够统一；
formApi.setState({ schema: useBasicFormSchema(formApi) });
advancedFormApi.setState({ schema: useAdvancedFormSchema() });

const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    // 只验证基础表单
    const { valid: basicValid } = await formApi.validate();
    if (!basicValid) {
      return;
    }

    modalApi.lock();
    try {
      // 提交表单 - 合并两个表单的值
      const basicValues = await formApi.getValues();

      // 如果折叠面板展开，则获取高级表单的值，否则保留原有值（编辑时）或使用空值（新增时）
      let advancedValues: any = {};
      if (activeKey.value.includes('advanced')) {
        advancedValues = await advancedFormApi.getValues();
      } else if (formData.value?.id) {
        // 编辑时保留原有的高级字段值
        advancedValues = {
          icon: formData.value.icon,
          picUrl: formData.value.picUrl,
          description: formData.value.description,
        };
      }

      const values = {
        ...basicValues,
        ...advancedValues,
      } as IotProductApi.Product;
      const data = formData.value?.id
        ? { ...values, id: formData.value.id }
        : values;

      await (formData.value?.id ? updateProduct(data) : createProduct(data));
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
      // 重置折叠面板状态
      activeKey.value = [];
      return;
    }
    // 加载数据
    const data = modalApi.getData<any>();
    if (!data || !data.id) {
      // 设置默认值
      await formApi.setValues({
        productKey: generateProductKey(), // 自动生成 ProductKey
        // deviceType: 0, // 默认直连设备
        // codecType: 'Alink', // 默认 Alink
        // dataFormat: 1, // 默认 JSON
        // validateType: 1, // 默认设备密钥
        status: 0, // 默认启用
      });
      return;
    }
    try {
      formData.value = await getProduct(data.id);
      // 设置基础表单
      await formApi.setValues(formData.value);

      // 先设置高级表单的值（不等待）
      advancedFormApi.setValues({
        icon: formData.value.icon,
        picUrl: formData.value.picUrl,
        description: formData.value.description,
      });

      // 如果有图标、图片或描述，自动展开折叠面板以便显示
      if (
        formData.value.icon ||
        formData.value.picUrl ||
        formData.value.description
      ) {
        activeKey.value = ['advanced'];
      }
    } catch (error) {
      console.error('加载产品数据失败:', error);
      message.error('加载产品数据失败，请重试');
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal :title="getTitle" class="w-2/5">
    <div class="mx-4">
      <Form />
      <Collapse v-model:active-key="activeKey" class="mt-4">
        <CollapsePanel key="advanced" header="更多设置">
          <AdvancedForm />
        </CollapsePanel>
      </Collapse>
    </div>
  </Modal>
</template>
