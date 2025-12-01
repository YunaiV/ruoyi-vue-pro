<script setup lang="ts">
import { computed, toRaw, unref, watch } from 'vue';

import { useSimpleLocale } from '@vben-core/composables';
import { VbenExpandableArrow } from '@vben-core/shadcn-ui';
import { cn, isFunction, triggerWindowResize } from '@vben-core/shared/utils';

import { COMPONENT_MAP } from '../config';
import { injectFormProps } from '../use-form-context';

const { $t } = useSimpleLocale();

const [rootProps, form] = injectFormProps();

const collapsed = defineModel({ default: false });

const resetButtonOptions = computed(() => {
  return {
    content: `${$t.value('reset')}`,
    show: true,
    ...unref(rootProps).resetButtonOptions,
  };
});

const submitButtonOptions = computed(() => {
  return {
    content: `${$t.value('submit')}`,
    show: true,
    ...unref(rootProps).submitButtonOptions,
  };
});

// const isQueryForm = computed(() => {
//   return !!unref(rootProps).showCollapseButton;
// });

async function handleSubmit(e: Event) {
  e?.preventDefault();
  e?.stopPropagation();
  const props = unref(rootProps);
  if (!props.formApi) {
    return;
  }

  const { valid } = await props.formApi.validate();
  if (!valid) {
    return;
  }

  const values = toRaw(await props.formApi.getValues()) ?? {};
  await props.handleSubmit?.(values);
}

async function handleReset(e: Event) {
  e?.preventDefault();
  e?.stopPropagation();
  const props = unref(rootProps);

  const values = toRaw(await props.formApi?.getValues()) ?? {};

  if (isFunction(props.handleReset)) {
    await props.handleReset?.(values);
  } else {
    form.resetForm();
  }
}

watch(
  () => collapsed.value,
  () => {
    const props = unref(rootProps);
    if (props.collapseTriggerResize) {
      triggerWindowResize();
    }
  },
);

const actionWrapperClass = computed(() => {
  const props = unref(rootProps);
  const actionLayout = props.actionLayout || 'rowEnd';
  const actionPosition = props.actionPosition || 'right';

  const cls = [
    'flex',
    'items-center',
    'gap-3',
    props.compact ? 'pb-2' : 'pb-4',
    props.layout === 'vertical' ? 'self-end' : 'self-center',
    props.layout === 'inline' ? '' : 'w-full',
    props.actionWrapperClass,
  ];

  switch (actionLayout) {
    case 'newLine': {
      cls.push('col-span-full');
      break;
    }
    case 'rowEnd': {
      cls.push('col-[-2/-1]');
      break;
    }
    // 'inline' 不需要额外类名，保持默认
  }

  switch (actionPosition) {
    case 'center': {
      cls.push('justify-center');
      break;
    }
    case 'left': {
      cls.push('justify-start');
      break;
    }
    default: {
      // case 'right': 默认右对齐
      cls.push('justify-end');
      break;
    }
  }

  return cls.join(' ');
});

defineExpose({
  handleReset,
  handleSubmit,
});
</script>
<template>
  <div :class="cn(actionWrapperClass)">
    <template v-if="rootProps.actionButtonsReverse">
      <!-- 提交按钮前 -->
      <slot name="submit-before"></slot>

      <component
        :is="COMPONENT_MAP.PrimaryButton"
        v-if="submitButtonOptions.show"
        type="button"
        @click="handleSubmit"
        v-bind="submitButtonOptions"
      >
        {{ submitButtonOptions.content }}
      </component>
    </template>

    <!-- 重置按钮前 -->
    <slot name="reset-before"></slot>

    <component
      :is="COMPONENT_MAP.DefaultButton"
      v-if="resetButtonOptions.show"
      type="button"
      @click="handleReset"
      v-bind="resetButtonOptions"
    >
      {{ resetButtonOptions.content }}
    </component>

    <template v-if="!rootProps.actionButtonsReverse">
      <!-- 提交按钮前 -->
      <slot name="submit-before"></slot>

      <component
        :is="COMPONENT_MAP.PrimaryButton"
        v-if="submitButtonOptions.show"
        type="button"
        @click="handleSubmit"
        v-bind="submitButtonOptions"
      >
        {{ submitButtonOptions.content }}
      </component>
    </template>

    <!-- 展开按钮前 -->
    <slot name="expand-before"></slot>

    <VbenExpandableArrow
      class="ml-[-0.3em]"
      v-if="rootProps.showCollapseButton"
      v-model:model-value="collapsed"
    >
      <span>{{ collapsed ? $t('expand') : $t('collapse') }}</span>
    </VbenExpandableArrow>

    <!-- 展开按钮后 -->
    <slot name="expand-after"></slot>
  </div>
</template>
