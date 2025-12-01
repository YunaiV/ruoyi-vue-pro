<script setup lang="ts">
import type { ZodType } from 'zod';

import type { FormSchema, MaybeComponentProps } from '../types';

import { computed, nextTick, onUnmounted, useTemplateRef, watch } from 'vue';

import { CircleAlert } from '@vben-core/icons';
import {
  FormControl,
  FormDescription,
  FormField,
  FormItem,
  FormMessage,
  VbenRenderContent,
  VbenTooltip,
} from '@vben-core/shadcn-ui';
import { cn, isFunction, isObject, isString } from '@vben-core/shared/utils';

import { toTypedSchema } from '@vee-validate/zod';
import { useFieldError, useFormValues } from 'vee-validate';

import { injectComponentRefMap } from '../use-form-context';
import { injectRenderFormProps, useFormContext } from './context';
import useDependencies from './dependencies';
import FormLabel from './form-label.vue';
import { isEventObjectLike } from './helper';

interface Props extends FormSchema {}

const {
  colon,
  commonComponentProps,
  component,
  componentProps,
  dependencies,
  description,
  disabled,
  disabledOnChangeListener,
  disabledOnInputListener,
  emptyStateValue,
  fieldName,
  formFieldProps,
  hide,
  label,
  labelClass,
  labelWidth,
  modelPropName,
  renderComponentContent,
  rules,
} = defineProps<
  Props & {
    commonComponentProps: MaybeComponentProps;
  }
>();

const { componentBindEventMap, componentMap, isVertical } = useFormContext();
const formRenderProps = injectRenderFormProps();
const values = useFormValues();
const errors = useFieldError(fieldName);
const fieldComponentRef = useTemplateRef<HTMLInputElement>('fieldComponentRef');
const formApi = formRenderProps.form;
const compact = computed(() => formRenderProps.compact);
const isInValid = computed(() => errors.value?.length > 0);

const FieldComponent = computed(() => {
  const finalComponent = isString(component)
    ? componentMap.value[component]
    : component;
  if (!finalComponent) {
    // 组件未注册
    console.warn(`Component ${component} is not registered`);
  }
  return finalComponent;
});

const {
  dynamicComponentProps,
  dynamicRules,
  isDisabled,
  isIf,
  isRequired,
  isShow,
} = useDependencies(() => dependencies);

const labelStyle = computed(() => {
  return labelClass?.includes('w-') || isVertical.value
    ? {}
    : {
        width: `${labelWidth}px`,
      };
});

const currentRules = computed(() => {
  return dynamicRules.value || rules;
});

const visible = computed(() => {
  return !hide && isIf.value && isShow.value;
});

const shouldRequired = computed(() => {
  if (!visible.value) {
    return false;
  }

  if (!currentRules.value) {
    return isRequired.value;
  }

  if (isRequired.value) {
    return true;
  }

  if (isString(currentRules.value)) {
    return ['mobileRequired', 'required', 'selectRequired'].includes(
      currentRules.value,
    );
  }

  let isOptional = currentRules?.value?.isOptional?.();

  // 如果有设置默认值，则不是必填，需要特殊处理
  const typeName = currentRules?.value?._def?.typeName;
  if (typeName === 'ZodDefault') {
    const innerType = currentRules?.value?._def.innerType;
    if (innerType) {
      isOptional = innerType.isOptional?.();
    }
  }

  return !isOptional;
});

const fieldRules = computed(() => {
  if (!visible.value) {
    return null;
  }

  let rules = currentRules.value;
  if (!rules) {
    return isRequired.value ? 'required' : null;
  }

  if (isString(rules)) {
    return rules;
  }

  const isOptional = !shouldRequired.value;
  if (!isOptional) {
    const unwrappedRules = (rules as any)?.unwrap?.();
    if (unwrappedRules) {
      rules = unwrappedRules;
    }
  }
  return toTypedSchema(rules as ZodType);
});

const computedProps = computed(() => {
  const finalComponentProps = isFunction(componentProps)
    ? componentProps(values.value, formApi!)
    : componentProps;

  return {
    ...commonComponentProps,
    ...finalComponentProps,
    ...dynamicComponentProps.value,
  };
});

watch(
  () => computedProps.value?.autofocus,
  (value) => {
    if (value === true) {
      nextTick(() => {
        autofocus();
      });
    }
  },
  { immediate: true },
);

const shouldDisabled = computed(() => {
  return isDisabled.value || disabled || computedProps.value?.disabled;
});

const customContentRender = computed(() => {
  if (!isFunction(renderComponentContent)) {
    return {};
  }
  return renderComponentContent(values.value, formApi!);
});

const renderContentKey = computed(() => {
  return Object.keys(customContentRender.value);
});

const fieldProps = computed(() => {
  const rules = fieldRules.value;
  return {
    keepValue: true,
    label: isString(label) ? label : '',
    ...(rules ? { rules } : {}),
    ...(formFieldProps as Record<string, any>),
  };
});

function fieldBindEvent(slotProps: Record<string, any>) {
  const modelValue = slotProps.componentField.modelValue;
  const handler = slotProps.componentField['onUpdate:modelValue'];

  const bindEventField =
    modelPropName ||
    (isString(component) ? componentBindEventMap.value?.[component] : null);

  let value = modelValue;
  // antd design 的一些组件会传递一个 event 对象
  if (modelValue && isObject(modelValue) && bindEventField) {
    value = isEventObjectLike(modelValue)
      ? modelValue?.target?.[bindEventField]
      : (modelValue?.[bindEventField] ?? modelValue);
  }

  if (bindEventField) {
    return {
      [`onUpdate:${bindEventField}`]: handler,
      [bindEventField]: value === undefined ? emptyStateValue : value,
      onChange: disabledOnChangeListener
        ? undefined
        : (e: Record<string, any>) => {
            const shouldUnwrap = isEventObjectLike(e);
            const onChange = slotProps?.componentField?.onChange;
            if (!shouldUnwrap) {
              return onChange?.(e);
            }

            return onChange?.(e?.target?.[bindEventField] ?? e);
          },
      ...(disabledOnInputListener ? { onInput: undefined } : {}),
    };
  }
  return {
    ...(disabledOnInputListener ? { onInput: undefined } : {}),
    ...(disabledOnChangeListener ? { onChange: undefined } : {}),
  };
}

function createComponentProps(slotProps: Record<string, any>) {
  const bindEvents = fieldBindEvent(slotProps);

  const binds = {
    ...slotProps.componentField,
    ...computedProps.value,
    ...bindEvents,
    ...(Reflect.has(computedProps.value, 'onChange')
      ? { onChange: computedProps.value.onChange }
      : {}),
    ...(Reflect.has(computedProps.value, 'onInput')
      ? { onInput: computedProps.value.onInput }
      : {}),
  };

  return binds;
}

function autofocus() {
  if (
    fieldComponentRef.value &&
    isFunction(fieldComponentRef.value.focus) &&
    // 检查当前是否有元素被聚焦
    document.activeElement !== fieldComponentRef.value
  ) {
    fieldComponentRef.value?.focus?.();
  }
}
const componentRefMap = injectComponentRefMap();
watch(fieldComponentRef, (componentRef) => {
  componentRefMap?.set(fieldName, componentRef);
});
onUnmounted(() => {
  if (componentRefMap?.has(fieldName)) {
    componentRefMap.delete(fieldName);
  }
});
</script>

<template>
  <FormField
    v-if="!hide && isIf"
    v-bind="fieldProps"
    v-slot="slotProps"
    :name="fieldName"
  >
    <FormItem
      v-show="isShow"
      :class="{
        'form-valid-error': isInValid,
        'form-is-required': shouldRequired,
        'flex-col': isVertical,
        'flex-row items-center': !isVertical,
        'pb-4': !compact,
        'pb-2': compact,
      }"
      class="relative flex"
      v-bind="$attrs"
    >
      <FormLabel
        v-if="!hideLabel"
        :class="
          cn(
            'flex leading-6',
            {
              'mr-2 flex-shrink-0 justify-end': !isVertical,
              'mb-1 flex-row': isVertical,
            },
            labelClass,
          )
        "
        :help="help"
        :colon="colon"
        :label="label"
        :required="shouldRequired && !hideRequiredMark"
        :style="labelStyle"
      >
        <template v-if="label">
          <VbenRenderContent :content="label" />
        </template>
      </FormLabel>
      <div class="flex-auto overflow-hidden p-[1px]">
        <div :class="cn('relative flex w-full items-center', wrapperClass)">
          <FormControl :class="cn(controlClass)">
            <slot
              v-bind="{
                ...slotProps,
                ...createComponentProps(slotProps),
                disabled: shouldDisabled,
                isInValid,
              }"
            >
              <component
                :is="FieldComponent"
                ref="fieldComponentRef"
                :class="{
                  'border-destructive hover:border-destructive/80 focus:border-destructive focus:shadow-[0_0_0_2px_rgba(255,38,5,0.06)]':
                    isInValid,
                }"
                v-bind="createComponentProps(slotProps)"
                :disabled="shouldDisabled"
              >
                <template
                  v-for="name in renderContentKey"
                  :key="name"
                  #[name]="renderSlotProps"
                >
                  <VbenRenderContent
                    :content="customContentRender[name]"
                    v-bind="{ ...renderSlotProps, formContext: slotProps }"
                  />
                </template>
                <!-- <slot></slot> -->
              </component>
              <VbenTooltip
                v-if="compact && isInValid"
                :delay-duration="300"
                side="left"
              >
                <template #trigger>
                  <slot name="trigger">
                    <CircleAlert
                      :class="
                        cn(
                          'inline-flex size-5 cursor-pointer text-foreground/80 hover:text-foreground',
                        )
                      "
                    />
                  </slot>
                </template>
                <FormMessage />
              </VbenTooltip>
            </slot>
          </FormControl>
          <!-- 自定义后缀 -->
          <div v-if="suffix" class="ml-1">
            <VbenRenderContent :content="suffix" />
          </div>
          <FormDescription v-if="description" class="ml-1">
            <VbenRenderContent :content="description" />
          </FormDescription>
        </div>

        <Transition name="slide-up" v-if="!compact">
          <FormMessage class="absolute" />
        </Transition>
      </div>
    </FormItem>
  </FormField>
</template>
