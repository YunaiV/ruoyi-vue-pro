<script lang="ts" setup>
import type { Arrayable } from '@vueuse/core';

import type { ValueType, VbenButtonGroupProps } from './button';

import { computed, ref, watch } from 'vue';

import { Circle, CircleCheckBig, LoaderCircle } from '@vben-core/icons';
import { cn, isFunction } from '@vben-core/shared/utils';

import { objectOmit } from '@vueuse/core';

import { VbenRenderContent } from '../render-content';
import VbenButtonGroup from './button-group.vue';
import Button from './button.vue';

const props = withDefaults(defineProps<VbenButtonGroupProps>(), {
  gap: 0,
  multiple: false,
  showIcon: true,
  size: 'middle',
  allowClear: false,
  maxCount: 0,
});
const emit = defineEmits(['btnClick']);
const btnDefaultProps = computed(() => {
  return {
    ...objectOmit(props, ['options', 'btnClass', 'size', 'disabled']),
    class: cn(props.btnClass),
  };
});
const modelValue = defineModel<Arrayable<ValueType> | undefined>();

const innerValue = ref<Array<ValueType>>([]);
const loadingValues = ref<Array<ValueType>>([]);
watch(
  () => props.multiple,
  (val) => {
    if (val) {
      modelValue.value = innerValue.value;
    } else {
      modelValue.value =
        innerValue.value.length > 0 ? innerValue.value[0] : undefined;
    }
  },
);

watch(
  () => modelValue.value,
  (val) => {
    if (Array.isArray(val)) {
      const arrVal = val.filter((v) => v !== undefined);
      if (arrVal.length > 0) {
        innerValue.value = props.multiple
          ? [...arrVal]
          : [arrVal[0] as ValueType];
      } else {
        innerValue.value = [];
      }
    } else {
      innerValue.value = val === undefined ? [] : [val as ValueType];
    }
  },
  { deep: true, immediate: true },
);

async function onBtnClick(value: ValueType) {
  if (props.beforeChange && isFunction(props.beforeChange)) {
    try {
      loadingValues.value.push(value);
      const canChange = await props.beforeChange(
        value,
        !innerValue.value.includes(value),
      );
      if (canChange === false) {
        return;
      }
    } finally {
      loadingValues.value.splice(loadingValues.value.indexOf(value), 1);
    }
  }

  if (props.multiple) {
    if (innerValue.value.includes(value)) {
      innerValue.value = innerValue.value.filter((item) => item !== value);
    } else {
      if (props.maxCount > 0 && innerValue.value.length >= props.maxCount) {
        innerValue.value = innerValue.value.slice(0, props.maxCount - 1);
      }
      innerValue.value.push(value);
    }
    modelValue.value = innerValue.value;
  } else {
    if (props.allowClear && innerValue.value.includes(value)) {
      innerValue.value = [];
      modelValue.value = undefined;
      emit('btnClick', undefined);
      return;
    } else {
      innerValue.value = [value];
      modelValue.value = value;
    }
  }
  emit('btnClick', value);
}
</script>
<template>
  <VbenButtonGroup
    :size="props.size"
    :gap="props.gap"
    class="vben-check-button-group"
  >
    <Button
      v-for="(btn, index) in props.options"
      :key="index"
      :class="cn('border', props.btnClass)"
      :disabled="
        props.disabled ||
        loadingValues.includes(btn.value) ||
        (!props.multiple && loadingValues.length > 0)
      "
      v-bind="btnDefaultProps"
      :variant="innerValue.includes(btn.value) ? 'default' : 'outline'"
      @click="onBtnClick(btn.value)"
      type="button"
    >
      <div class="icon-wrapper" v-if="props.showIcon">
        <slot
          name="icon"
          :loading="loadingValues.includes(btn.value)"
          :checked="innerValue.includes(btn.value)"
        >
          <LoaderCircle
            class="animate-spin"
            v-if="loadingValues.includes(btn.value)"
          />
          <CircleCheckBig v-else-if="innerValue.includes(btn.value)" />
          <Circle v-else />
        </slot>
      </div>
      <slot name="option" :label="btn.label" :value="btn.value" :data="btn">
        <VbenRenderContent :content="btn.label" />
      </slot>
    </Button>
  </VbenButtonGroup>
</template>
<style lang="scss" scoped>
.vben-check-button-group {
  display: flex;
  flex-wrap: wrap;

  &:deep(.size-large) button {
    .icon-wrapper {
      margin-right: 0.3rem;

      svg {
        width: 1rem;
        height: 1rem;
      }
    }
  }

  &:deep(.size-middle) button {
    .icon-wrapper {
      margin-right: 0.2rem;

      svg {
        width: 0.75rem;
        height: 0.75rem;
      }
    }
  }

  &:deep(.size-small) button {
    .icon-wrapper {
      margin-right: 0.1rem;

      svg {
        width: 0.65rem;
        height: 0.65rem;
      }
    }
  }

  &.no-gap > :deep(button):nth-of-type(1) {
    border-right-width: 0;
  }

  &.no-gap {
    :deep(button + button) {
      margin-right: -1px;
      border-left-width: 1px;
    }
  }
}
</style>
