<script setup lang="ts">
import type { VbenFormProps } from './types';

import { ref, watchEffect } from 'vue';

import { useForwardPropsEmits } from '@vben-core/composables';

import FormActions from './components/form-actions.vue';
import {
  COMPONENT_BIND_EVENT_MAP,
  COMPONENT_MAP,
  DEFAULT_FORM_COMMON_CONFIG,
} from './config';
import { Form } from './form-render';
import { provideFormProps, useFormInitial } from './use-form-context';

// 通过 extends 会导致热更新卡死
interface Props extends VbenFormProps {}
const props = withDefaults(defineProps<Props>(), {
  actionWrapperClass: '',
  collapsed: false,
  collapsedRows: 1,
  commonConfig: () => ({}),
  handleReset: undefined,
  handleSubmit: undefined,
  layout: 'horizontal',
  resetButtonOptions: () => ({}),
  showCollapseButton: false,
  showDefaultActions: true,
  submitButtonOptions: () => ({}),
  wrapperClass: 'grid-cols-1',
});

const forward = useForwardPropsEmits(props);

const currentCollapsed = ref(false);

const { delegatedSlots, form } = useFormInitial(props);

provideFormProps([props, form]);

const handleUpdateCollapsed = (value: boolean) => {
  currentCollapsed.value = !!value;
};

watchEffect(() => {
  currentCollapsed.value = props.collapsed;
});
</script>

<template>
  <Form
    v-bind="forward"
    :collapsed="currentCollapsed"
    :component-bind-event-map="COMPONENT_BIND_EVENT_MAP"
    :component-map="COMPONENT_MAP"
    :form="form"
    :global-common-config="DEFAULT_FORM_COMMON_CONFIG"
  >
    <template
      v-for="slotName in delegatedSlots"
      :key="slotName"
      #[slotName]="slotProps"
    >
      <slot :name="slotName" v-bind="slotProps"></slot>
    </template>
    <template #default="slotProps">
      <slot v-bind="slotProps">
        <FormActions
          v-if="showDefaultActions"
          :model-value="currentCollapsed"
          @update:model-value="handleUpdateCollapsed"
        />
      </slot>
    </template>
  </Form>
</template>
