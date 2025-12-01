<script lang="ts" setup>
import { computed, nextTick, ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import { message } from 'ant-design-vue';

import { useVbenForm } from '#/adapter/form';
import {
  createDataRule,
  getDataRule,
  updateDataRule,
} from '#/api/iot/rule/data/rule';
import { getDataSinkSimpleList } from '#/api/iot/rule/data/sink';
import { $t } from '#/locales';

import SourceConfigForm from './components/source-config-form.vue';
import { useRuleFormSchema } from './data';

const emit = defineEmits(['success']);
const formData = ref<any>();
const sourceConfigRef = ref();

// TODO @haohao：应该放到 modules
const getTitle = computed(() => {
  return formData.value?.id
    ? $t('ui.actionTitle.edit', ['数据规则'])
    : $t('ui.actionTitle.create', ['数据规则']);
});

const [Form, formApi] = useVbenForm({
  commonConfig: {
    componentProps: {
      class: 'w-full',
    },
    formItemClass: 'col-span-2',
    labelWidth: 100,
  },
  layout: 'horizontal',
  schema: useRuleFormSchema(),
  showDefaultActions: false,
});

// TODO @haohao：这里需要优化下，参考别的模块写法；
const [Modal, modalApi] = useVbenModal({
  async onConfirm() {
    const { valid } = await formApi.validate();
    if (!valid) {
      return;
    }

    // 校验数据源配置
    await sourceConfigRef.value?.validate();

    modalApi.lock();
    // 提交表单
    const data = (await formApi.getValues()) as any;
    data.sourceConfigs = sourceConfigRef.value?.getData() || [];

    try {
      await (formData.value?.id ? updateDataRule(data) : createDataRule(data));
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
      sourceConfigRef.value?.setData([]);
      return;
    }
    // 加载数据
    const data = modalApi.getData<any>();

    // 加载数据目的列表
    const sinkList = await getDataSinkSimpleList();
    formApi.updateSchema([
      {
        fieldName: 'sinkIds',
        componentProps: {
          options: sinkList.map((item: any) => ({
            label: item.name,
            value: item.id,
          })),
        },
      },
    ]);

    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formData.value = await getDataRule(data.id);
      // 设置到 values
      await formApi.setValues(formData.value);
      // 设置数据源配置
      await nextTick();
      sourceConfigRef.value?.setData(formData.value.sourceConfigs || []);
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal class="w-4/5" :title="getTitle">
    <Form class="mx-4" />
    <div class="mx-4 mt-4">
      <div class="mb-2 text-sm font-medium">数据源配置</div>
      <SourceConfigForm ref="sourceConfigRef" />
    </div>
  </Modal>
</template>
