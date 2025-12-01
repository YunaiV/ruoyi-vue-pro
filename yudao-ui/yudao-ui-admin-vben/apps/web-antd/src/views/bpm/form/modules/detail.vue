<script lang="ts" setup>
import { ref } from 'vue';

import { useVbenModal } from '@vben/common-ui';

import FormCreate from '@form-create/ant-design-vue';

import { getForm } from '#/api/bpm/form';
import { setConfAndFields2 } from '#/components/form-create';

const formConfig = ref<any>({});

const [Modal, modalApi] = useVbenModal({
  footer: false,
  async onOpenChange(isOpen: boolean) {
    if (!isOpen) {
      return;
    }
    // 加载数据
    const data = modalApi.getData();
    if (!data || !data.id) {
      return;
    }
    modalApi.lock();
    try {
      formConfig.value = await getForm(data.id);
      setConfAndFields2(
        formConfig.value,
        formConfig.value.conf,
        formConfig.value.fields,
      );
    } finally {
      modalApi.unlock();
    }
  },
});
</script>

<template>
  <Modal
    class="w-2/5"
    title="流程表单详情"
    :body-style="{
      maxHeight: '100px',
    }"
  >
    <FormCreate :option="formConfig.option" :rule="formConfig.rule" />
  </Modal>
</template>
