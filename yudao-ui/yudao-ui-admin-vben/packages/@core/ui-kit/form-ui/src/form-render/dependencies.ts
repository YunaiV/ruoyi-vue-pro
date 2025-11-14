import type {
  FormItemDependencies,
  FormSchemaRuleType,
  MaybeComponentProps,
} from '../types';

import { computed, ref, watch } from 'vue';

import { isBoolean, isFunction } from '@vben-core/shared/utils';

import { useFormValues } from 'vee-validate';

import { injectRenderFormProps } from './context';

export default function useDependencies(
  getDependencies: () => FormItemDependencies | undefined,
) {
  const values = useFormValues();

  const formRenderProps = injectRenderFormProps();

  // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
  const formApi = formRenderProps.form!;

  if (!values) {
    throw new Error('useDependencies should be used within <VbenForm>');
  }

  const isIf = ref(true);
  const isDisabled = ref(false);
  const isShow = ref(true);
  const isRequired = ref(false);
  const dynamicComponentProps = ref<MaybeComponentProps>({});
  const dynamicRules = ref<FormSchemaRuleType>();

  const triggerFieldValues = computed(() => {
    // 该字段可能会被多个字段触发
    const triggerFields = getDependencies()?.triggerFields ?? [];
    return triggerFields.map((dep) => {
      return values.value[dep];
    });
  });

  const resetConditionState = () => {
    isDisabled.value = false;
    isIf.value = true;
    isShow.value = true;
    isRequired.value = false;
    dynamicRules.value = undefined;
    dynamicComponentProps.value = {};
  };

  watch(
    [triggerFieldValues, getDependencies],
    async ([_values, dependencies]) => {
      if (!dependencies || !dependencies?.triggerFields?.length) {
        return;
      }
      resetConditionState();
      const {
        componentProps,
        disabled,
        if: whenIf,
        required,
        rules,
        show,
        trigger,
      } = dependencies;

      // 1. 优先判断if，如果if为false，则不渲染dom，后续判断也不再执行
      const formValues = values.value;

      if (isFunction(whenIf)) {
        isIf.value = !!(await whenIf(formValues, formApi));
        // 不渲染
        if (!isIf.value) return;
      } else if (isBoolean(whenIf)) {
        isIf.value = whenIf;
        if (!isIf.value) return;
      }

      // 2. 判断show，如果show为false，则隐藏
      if (isFunction(show)) {
        isShow.value = !!(await show(formValues, formApi));
        if (!isShow.value) return;
      } else if (isBoolean(show)) {
        isShow.value = show;
        if (!isShow.value) return;
      }

      if (isFunction(componentProps)) {
        dynamicComponentProps.value = await componentProps(formValues, formApi);
      }

      if (isFunction(rules)) {
        dynamicRules.value = await rules(formValues, formApi);
      }

      if (isFunction(disabled)) {
        isDisabled.value = !!(await disabled(formValues, formApi));
      } else if (isBoolean(disabled)) {
        isDisabled.value = disabled;
      }

      if (isFunction(required)) {
        isRequired.value = !!(await required(formValues, formApi));
      }

      if (isFunction(trigger)) {
        await trigger(formValues, formApi);
      }
    },
    { deep: true, immediate: true },
  );

  return {
    dynamicComponentProps,
    dynamicRules,
    isDisabled,
    isIf,
    isRequired,
    isShow,
  };
}
