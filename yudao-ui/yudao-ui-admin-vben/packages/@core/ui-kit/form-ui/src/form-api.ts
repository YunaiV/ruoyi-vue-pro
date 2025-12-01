import type {
  FormState,
  GenericObject,
  ResetFormOpts,
  ValidationOptions,
} from 'vee-validate';

import type { ComponentPublicInstance } from 'vue';

import type { Recordable } from '@vben-core/typings';

import type { FormActions, FormSchema, VbenFormProps } from './types';

import { isRef, toRaw } from 'vue';

import { Store } from '@vben-core/shared/store';
import {
  bindMethods,
  createMerge,
  formatDate,
  isDate,
  isDayjsObject,
  isFunction,
  isObject,
  mergeWithArrayOverride,
  StateHandler,
} from '@vben-core/shared/utils';

function getDefaultState(): VbenFormProps {
  return {
    actionWrapperClass: '',
    collapsed: false,
    collapsedRows: 1,
    collapseTriggerResize: false,
    commonConfig: {},
    handleReset: undefined,
    handleSubmit: undefined,
    handleValuesChange: undefined,
    handleCollapsedChange: undefined,
    layout: 'horizontal',
    resetButtonOptions: {},
    schema: [],
    scrollToFirstError: false,
    showCollapseButton: false,
    showDefaultActions: true,
    submitButtonOptions: {},
    submitOnChange: false,
    submitOnEnter: false,
    wrapperClass: 'grid-cols-1',
  };
}

export class FormApi {
  // private api: Pick<VbenFormProps, 'handleReset' | 'handleSubmit'>;
  public form = {} as FormActions;
  isMounted = false;

  public state: null | VbenFormProps = null;
  stateHandler: StateHandler;

  public store: Store<VbenFormProps>;

  /**
   * 组件实例映射
   */
  private componentRefMap: Map<string, unknown> = new Map();

  // 最后一次点击提交时的表单值
  private latestSubmissionValues: null | Recordable<any> = null;

  private prevState: null | VbenFormProps = null;

  constructor(options: VbenFormProps = {}) {
    const { ...storeState } = options;

    const defaultState = getDefaultState();

    this.store = new Store<VbenFormProps>(
      {
        ...defaultState,
        ...storeState,
      },
      {
        onUpdate: () => {
          this.prevState = this.state;
          this.state = this.store.state;
          this.updateState();
        },
      },
    );

    this.state = this.store.state;
    this.stateHandler = new StateHandler();
    bindMethods(this);
  }

  /**
   * 获取字段组件实例
   * @param fieldName 字段名
   * @returns 组件实例
   */
  getFieldComponentRef<T = ComponentPublicInstance>(
    fieldName: string,
  ): T | undefined {
    let target = this.componentRefMap.has(fieldName)
      ? (this.componentRefMap.get(fieldName) as ComponentPublicInstance)
      : undefined;
    if (
      target &&
      target.$.type.name === 'AsyncComponentWrapper' &&
      target.$.subTree.ref
    ) {
      if (Array.isArray(target.$.subTree.ref)) {
        if (
          target.$.subTree.ref.length > 0 &&
          isRef(target.$.subTree.ref[0]?.r)
        ) {
          target = target.$.subTree.ref[0]?.r.value as ComponentPublicInstance;
        }
      } else if (isRef(target.$.subTree.ref.r)) {
        target = target.$.subTree.ref.r.value as ComponentPublicInstance;
      }
    }
    return target as T;
  }

  /**
   * 获取当前聚焦的字段，如果没有聚焦的字段则返回undefined
   */
  getFocusedField() {
    for (const fieldName of this.componentRefMap.keys()) {
      const ref = this.getFieldComponentRef(fieldName);
      if (ref) {
        let el: HTMLElement | null = null;
        if (ref instanceof HTMLElement) {
          el = ref;
        } else if (ref.$el instanceof HTMLElement) {
          el = ref.$el;
        }
        if (!el) {
          continue;
        }
        if (
          el === document.activeElement ||
          el.contains(document.activeElement)
        ) {
          return fieldName;
        }
      }
    }
    return undefined;
  }

  getLatestSubmissionValues() {
    return this.latestSubmissionValues || {};
  }

  getState() {
    return this.state;
  }

  async getValues<T = Recordable<any>>() {
    const form = await this.getForm();
    return (form.values ? this.handleRangeTimeValue(form.values) : {}) as T;
  }

  async isFieldValid(fieldName: string) {
    const form = await this.getForm();
    return form.isFieldValid(fieldName);
  }

  merge(formApi: FormApi) {
    const chain = [this, formApi];
    const proxy = new Proxy(formApi, {
      get(target: any, prop: any) {
        if (prop === 'merge') {
          return (nextFormApi: FormApi) => {
            chain.push(nextFormApi);
            return proxy;
          };
        }
        if (prop === 'submitAllForm') {
          return async (needMerge: boolean = true) => {
            try {
              const results = await Promise.all(
                chain.map(async (api) => {
                  const validateResult = await api.validate();
                  if (!validateResult.valid) {
                    return;
                  }
                  const rawValues = toRaw((await api.getValues()) || {});
                  return rawValues;
                }),
              );
              if (needMerge) {
                const mergedResults = Object.assign({}, ...results);
                return mergedResults;
              }
              return results;
            } catch (error) {
              console.error('Validation error:', error);
            }
          };
        }
        return target[prop];
      },
    });

    return proxy;
  }

  mount(formActions: FormActions, componentRefMap: Map<string, unknown>) {
    if (!this.isMounted) {
      Object.assign(this.form, formActions);
      this.stateHandler.setConditionTrue();
      this.setLatestSubmissionValues({
        ...toRaw(this.handleRangeTimeValue(this.form.values)),
      });
      this.componentRefMap = componentRefMap;
      this.isMounted = true;
    }
  }

  /**
   * 根据字段名移除表单项
   * @param fields
   */
  async removeSchemaByFields(fields: string[]) {
    const fieldSet = new Set(fields);
    const schema = this.state?.schema ?? [];

    const filterSchema = schema.filter((item) => !fieldSet.has(item.fieldName));

    this.setState({
      schema: filterSchema,
    });
  }

  /**
   * 重置表单
   */
  async resetForm(
    state?: Partial<FormState<GenericObject>> | undefined,
    opts?: Partial<ResetFormOpts>,
  ) {
    const form = await this.getForm();
    return form.resetForm(state, opts);
  }

  async resetValidate() {
    const form = await this.getForm();
    const fields = Object.keys(form.errors.value);
    fields.forEach((field) => {
      form.setFieldError(field, undefined);
    });
  }

  /**
   * 滚动到第一个错误字段
   * @param errors 验证错误对象
   */
  scrollToFirstError(errors: Record<string, any> | string) {
    // https://github.com/logaretm/vee-validate/discussions/3835
    const firstErrorFieldName =
      typeof errors === 'string' ? errors : Object.keys(errors)[0];

    if (!firstErrorFieldName) {
      return;
    }

    let el = document.querySelector(
      `[name="${firstErrorFieldName}"]`,
    ) as HTMLElement;

    // 如果通过 name 属性找不到，尝试通过组件引用查找, 正常情况下不会走到这，怕哪天 vee-validate 改了 name 属性有个兜底的
    if (!el) {
      const componentRef = this.getFieldComponentRef(firstErrorFieldName);
      if (componentRef && componentRef.$el instanceof HTMLElement) {
        el = componentRef.$el;
      }
    }

    if (el) {
      // 滚动到错误字段，添加一些偏移量以确保字段完全可见
      el.scrollIntoView({
        behavior: 'smooth',
        block: 'center',
        inline: 'nearest',
      });
    }
  }

  /**
   * 设置表单禁用状态：用于非 Modal 中使用 Form 时，需要 Form 自己控制禁用状态
   * @author 芋道源码
   * @param disabled 是否禁用
   */
  setDisabled(disabled: boolean) {
    this.setState((prev) => ({
      ...prev,
      commonConfig: { ...prev.commonConfig, disabled },
    }));
  }

  async setFieldValue(field: string, value: any, shouldValidate?: boolean) {
    const form = await this.getForm();
    form.setFieldValue(field, value, shouldValidate);
  }

  setLatestSubmissionValues(values: null | Recordable<any>) {
    this.latestSubmissionValues = { ...toRaw(values) };
  }

  /**
   * 设置表单提交按钮的加载状态：用于非 Modal 中使用 Form 时，需要 Form 自己控制 loading 状态
   * @author 芋道源码
   * @param loading 是否加载中
   */
  setLoading(loading: boolean) {
    this.setState((prev) => ({
      ...prev,
      submitButtonOptions: { ...prev.submitButtonOptions, loading },
    }));
  }

  setState(
    stateOrFn:
      | ((prev: VbenFormProps) => Partial<VbenFormProps>)
      | Partial<VbenFormProps>,
  ) {
    if (isFunction(stateOrFn)) {
      this.store.setState((prev) => {
        return mergeWithArrayOverride(stateOrFn(prev), prev);
      });
    } else {
      this.store.setState((prev) => mergeWithArrayOverride(stateOrFn, prev));
    }
  }

  /**
   * 设置表单值
   * @param fields record
   * @param filterFields 过滤不在schema中定义的字段 默认为true
   * @param shouldValidate
   */
  async setValues(
    fields: Record<string, any>,
    filterFields: boolean = true,
    shouldValidate: boolean = false,
  ) {
    const form = await this.getForm();
    if (!filterFields) {
      form.setValues(fields, shouldValidate);
      return;
    }

    /**
     * 合并算法有待改进，目前的算法不支持object类型的值。
     * antd的日期时间相关组件的值类型为dayjs对象
     * element-plus的日期时间相关组件的值类型可能为Date对象
     * 以上两种类型需要排除深度合并
     */
    const fieldMergeFn = createMerge((obj, key, value) => {
      if (key in obj) {
        obj[key] =
          !Array.isArray(obj[key]) &&
          isObject(obj[key]) &&
          !isDayjsObject(obj[key]) &&
          !isDate(obj[key])
            ? fieldMergeFn(value, obj[key])
            : value;
      }
      return true;
    });
    const filteredFields = fieldMergeFn(fields, form.values);
    form.setValues(filteredFields, shouldValidate);
  }

  async submitForm(e?: Event) {
    e?.preventDefault();
    e?.stopPropagation();
    const form = await this.getForm();
    await form.submitForm();
    const rawValues = toRaw(await this.getValues());
    await this.state?.handleSubmit?.(rawValues);

    return rawValues;
  }

  unmount() {
    this.form?.resetForm?.();
    // this.state = null;
    this.latestSubmissionValues = null;
    this.isMounted = false;
    this.stateHandler.reset();
  }

  updateSchema(schema: Partial<FormSchema>[]) {
    const updated: Partial<FormSchema>[] = [...schema];
    const hasField = updated.every(
      (item) => Reflect.has(item, 'fieldName') && item.fieldName,
    );

    if (!hasField) {
      console.error(
        'All items in the schema array must have a valid `fieldName` property to be updated',
      );
      return;
    }
    const currentSchema = [...(this.state?.schema ?? [])];

    const updatedMap: Record<string, any> = {};

    updated.forEach((item) => {
      if (item.fieldName) {
        updatedMap[item.fieldName] = item;
      }
    });

    currentSchema.forEach((schema, index) => {
      const updatedData = updatedMap[schema.fieldName];
      if (updatedData) {
        currentSchema[index] = mergeWithArrayOverride(
          updatedData,
          schema,
        ) as FormSchema;
      }
    });
    this.setState({ schema: currentSchema });
  }

  async validate(opts?: Partial<ValidationOptions>) {
    const form = await this.getForm();

    const validateResult = await form.validate(opts);

    if (Object.keys(validateResult?.errors ?? {}).length > 0) {
      console.error('validate error', validateResult?.errors);

      if (this.state?.scrollToFirstError) {
        this.scrollToFirstError(validateResult.errors);
      }
    }
    return validateResult;
  }

  async validateAndSubmitForm() {
    const form = await this.getForm();
    const { valid, errors } = await form.validate();
    if (!valid) {
      if (this.state?.scrollToFirstError) {
        this.scrollToFirstError(errors);
      }
      return;
    }
    return await this.submitForm();
  }

  async validateField(fieldName: string, opts?: Partial<ValidationOptions>) {
    const form = await this.getForm();
    const validateResult = await form.validateField(fieldName, opts);

    if (Object.keys(validateResult?.errors ?? {}).length > 0) {
      console.error('validate error', validateResult?.errors);

      if (this.state?.scrollToFirstError) {
        this.scrollToFirstError(fieldName);
      }
    }
    return validateResult;
  }

  private async getForm() {
    if (!this.isMounted) {
      // 等待form挂载
      await this.stateHandler.waitForCondition();
    }
    if (!this.form?.meta) {
      throw new Error('<VbenForm /> is not mounted');
    }
    return this.form;
  }

  private handleMultiFields = (originValues: Record<string, any>) => {
    const arrayToStringFields = this.state?.arrayToStringFields;
    if (!arrayToStringFields || !Array.isArray(arrayToStringFields)) {
      return;
    }

    const processFields = (fields: string[], separator: string = ',') => {
      this.processFields(fields, separator, originValues, (value, sep) => {
        if (Array.isArray(value)) {
          return value.join(sep);
        } else if (typeof value === 'string') {
          // 处理空字符串的情况
          if (value === '') {
            return [];
          }
          // 处理复杂分隔符的情况
          const escapedSeparator = sep.replaceAll(
            /[.*+?^${}()|[\]\\]/g,
            String.raw`\$&`,
          );
          return value.split(new RegExp(escapedSeparator));
        } else {
          return value;
        }
      });
    };

    // 处理简单数组格式 ['field1', 'field2', ';'] 或 ['field1', 'field2']
    if (arrayToStringFields.every((item) => typeof item === 'string')) {
      const lastItem =
        arrayToStringFields[arrayToStringFields.length - 1] || '';
      const fields =
        lastItem.length === 1
          ? arrayToStringFields.slice(0, -1)
          : arrayToStringFields;
      const separator = lastItem.length === 1 ? lastItem : ',';
      processFields(fields, separator);
      return;
    }

    // 处理嵌套数组格式 [['field1'], ';']
    arrayToStringFields.forEach((fieldConfig) => {
      if (Array.isArray(fieldConfig)) {
        const [fields, separator = ','] = fieldConfig;
        // 根据类型定义，fields 应该始终是字符串数组
        if (!Array.isArray(fields)) {
          console.warn(
            `Invalid field configuration: fields should be an array of strings, got ${typeof fields}`,
          );
          return;
        }
        processFields(fields, separator);
      }
    });
  };

  private handleRangeTimeValue = (originValues: Record<string, any>) => {
    const values = { ...originValues };
    const fieldMappingTime = this.state?.fieldMappingTime;

    this.handleMultiFields(values);
    if (!fieldMappingTime || !Array.isArray(fieldMappingTime)) {
      return values;
    }

    fieldMappingTime.forEach(
      ([field, [startTimeKey, endTimeKey], format = 'YYYY-MM-DD']) => {
        if (startTimeKey && endTimeKey && values[field] === null) {
          Reflect.deleteProperty(values, startTimeKey);
          Reflect.deleteProperty(values, endTimeKey);
          // delete values[startTimeKey];
          // delete values[endTimeKey];
        }

        if (!values[field]) {
          Reflect.deleteProperty(values, field);
          // delete values[field];
          return;
        }

        const [startTime, endTime] = values[field];
        if (format === null) {
          values[startTimeKey] = startTime;
          values[endTimeKey] = endTime;
        } else if (isFunction(format)) {
          values[startTimeKey] = format(startTime, startTimeKey);
          values[endTimeKey] = format(endTime, endTimeKey);
        } else {
          const [startTimeFormat, endTimeFormat] = Array.isArray(format)
            ? format
            : [format, format];

          values[startTimeKey] = startTime
            ? formatDate(startTime, startTimeFormat)
            : undefined;
          values[endTimeKey] = endTime
            ? formatDate(endTime, endTimeFormat)
            : undefined;
        }
        // delete values[field];
        Reflect.deleteProperty(values, field);
      },
    );
    return values;
  };

  private processFields = (
    fields: string[],
    separator: string,
    originValues: Record<string, any>,
    transformFn: (value: any, separator: string) => any,
  ) => {
    fields.forEach((field) => {
      const value = originValues[field];
      if (value === undefined || value === null) {
        return;
      }
      originValues[field] = transformFn(value, separator);
    });
  };

  private updateState() {
    const currentSchema = this.state?.schema ?? [];
    const prevSchema = this.prevState?.schema ?? [];
    // 进行了删除schema操作
    if (currentSchema.length < prevSchema.length) {
      const currentFields = new Set(
        currentSchema.map((item) => item.fieldName),
      );
      const deletedSchema = prevSchema.filter(
        (item) => !currentFields.has(item.fieldName),
      );
      for (const schema of deletedSchema) {
        this.form?.setFieldValue?.(schema.fieldName, undefined);
      }
    }
  }
}
