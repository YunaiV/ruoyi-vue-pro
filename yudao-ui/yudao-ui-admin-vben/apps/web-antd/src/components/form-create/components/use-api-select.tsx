import type { ApiSelectProps } from '#/components/form-create/typing';

import { defineComponent, onMounted, ref, useAttrs } from 'vue';

import { isEmpty } from '@vben/utils';

import {
  Checkbox,
  CheckboxGroup,
  Radio,
  RadioGroup,
  Select,
  SelectOption,
} from 'ant-design-vue';

import { requestClient } from '#/api/request';

export function useApiSelect(option: ApiSelectProps) {
  return defineComponent({
    name: option.name,
    props: {
      // 选项标签
      labelField: {
        type: String,
        default: () => option.labelField ?? 'label',
      },
      // 选项的值
      valueField: {
        type: String,
        default: () => option.valueField ?? 'value',
      },
      // api 接口
      url: {
        type: String,
        default: () => option.url ?? '',
      },
      // 请求类型
      method: {
        type: String,
        default: 'GET',
      },
      // 选项解析函数
      parseFunc: {
        type: String,
        default: '',
      },
      // 请求参数
      data: {
        type: String,
        default: '',
      },
      // 选择器类型，下拉框 select、多选框 checkbox、单选框 radio
      selectType: {
        type: String,
        default: 'select',
      },
      // 是否多选
      multiple: {
        type: Boolean,
        default: false,
      },
      // 是否远程搜索
      remote: {
        type: Boolean,
        default: false,
      },
      // 远程搜索时携带的参数
      remoteField: {
        type: String,
        default: 'label',
      },
      // 返回值类型（用于部门选择器等）：id 返回 ID，name 返回名称
      returnType: {
        type: String,
        default: 'id',
      },
    },
    setup(props) {
      const attrs = useAttrs();
      const options = ref<any[]>([]); // 下拉数据
      const loading = ref(false); // 是否正在从远程获取数据
      const queryParam = ref<any>(); // 当前输入的值
      const getOptions = async () => {
        options.value = [];
        // 接口选择器
        if (isEmpty(props.url)) {
          return;
        }

        switch (props.method) {
          case 'GET': {
            let url: string = props.url;
            if (props.remote && queryParam.value !== undefined) {
              url = url.includes('?')
                ? `${url}&${props.remoteField}=${queryParam.value}`
                : `${url}?${props.remoteField}=${queryParam.value}`;
            }
            parseOptions(await requestClient.get(url));
            break;
          }
          case 'POST': {
            const data: any = JSON.parse(props.data);
            if (props.remote) {
              data[props.remoteField] = queryParam.value;
            }
            parseOptions(await requestClient.post(props.url, data));
            break;
          }
        }
      };

      function parseOptions(data: any) {
        //  情况一：如果有自定义解析函数优先使用自定义解析
        if (!isEmpty(props.parseFunc)) {
          options.value = parseFunc()?.(data);
          return;
        }
        // 情况二：返回的直接是一个列表
        if (Array.isArray(data)) {
          parseOptions0(data);
          return;
        }
        // 情况二：返回的是分页数据,尝试读取 list
        data = data.list;
        if (!!data && Array.isArray(data)) {
          parseOptions0(data);
          return;
        }
        // 情况三：不是 yudao-vue-pro 标准返回
        console.warn(
          `接口[${props.url}] 返回结果不是 yudao-vue-pro 标准返回建议采用自定义解析函数处理`,
        );
      }

      function parseOptions0(data: any[]) {
        if (Array.isArray(data)) {
          options.value = data.map((item: any) => {
            const label = parseExpression(item, props.labelField);
            let value = parseExpression(item, props.valueField);

            // 根据 returnType 决定返回值
            // 如果设置了 returnType 为 'name'，则返回 label 作为 value
            if (props.returnType === 'name') {
              value = label;
            }

            return {
              label,
              value,
            };
          });
          return;
        }
        console.warn(`接口[${props.url}] 返回结果不是一个数组`);
      }

      function parseFunc() {
        let parse: any = null;
        if (props.parseFunc) {
          // 解析字符串函数
          // eslint-disable-next-line no-new-func
          parse = new Function(`return ${props.parseFunc}`)();
        }
        return parse;
      }

      function parseExpression(data: any, template: string) {
        // 检测是否使用了表达式
        if (!template.includes('${')) {
          return data[template];
        }
        // 正则表达式匹配模板字符串中的 ${...}
        const pattern = /\$\{([^}]*)\}/g;
        // 使用replace函数配合正则表达式和回调函数来进行替换
        return template.replaceAll(pattern, (_, expr) => {
          // expr 是匹配到的 ${} 内的表达式（这里是属性名），从 data 中获取对应的值
          const result = data[expr.trim()]; // 去除前后空白，以防用户输入带空格的属性名
          if (!result) {
            console.warn(
              `接口选择器选项模版[${template}][${expr.trim()}] 解析值失败结果为[${result}], 请检查属性名称是否存在于接口返回值中,存在则忽略此条！！！`,
            );
          }
          return result;
        });
      }

      const remoteMethod = async (query: any) => {
        if (!query) {
          return;
        }
        loading.value = true;
        try {
          queryParam.value = query;
          await getOptions();
        } finally {
          loading.value = false;
        }
      };

      onMounted(async () => {
        await getOptions();
      });

      const buildSelect = () => {
        const {
          modelValue,
          'onUpdate:modelValue': onUpdateModelValue,
          ...restAttrs
        } = attrs;

        if (props.multiple) {
          // fix：多写此步是为了解决 multiple 属性问题
          return (
            <Select
              class="w-full"
              loading={loading.value}
              mode="multiple"
              onUpdate:value={onUpdateModelValue as any}
              value={modelValue as any}
              {...restAttrs}
              // TODO @xingyu remote 对等实现, 还是说没作用
              // remote={props.remote}
              {...(props.remote && { remoteMethod })}
            >
              {options.value.map(
                (item: { label: any; value: any }, index: any) => (
                  <SelectOption key={index} value={item.value}>
                    {item.label}
                  </SelectOption>
                ),
              )}
            </Select>
          );
        }
        return (
          <Select
            class="w-full"
            loading={loading.value}
            onUpdate:value={onUpdateModelValue as any}
            value={modelValue as any}
            {...restAttrs}
            // TODO: @xingyu remote 对等实现, 还是说没作用
            // remote={props.remote}
            {...(props.remote && { remoteMethod })}
          >
            {options.value.map(
              (item: { label: any; value: any }, index: any) => (
                <SelectOption key={index} value={item.value}>
                  {item.label}
                </SelectOption>
              ),
            )}
          </Select>
        );
      };
      const buildCheckbox = () => {
        const {
          modelValue,
          'onUpdate:modelValue': onUpdateModelValue,
          ...restAttrs
        } = attrs;
        if (isEmpty(options.value)) {
          options.value = [
            { label: '选项1', value: '选项1' },
            { label: '选项2', value: '选项2' },
          ];
        }
        return (
          <CheckboxGroup
            class="w-full"
            onUpdate:value={onUpdateModelValue as any}
            value={modelValue as any}
            {...restAttrs}
          >
            {options.value.map(
              (item: { label: any; value: any }, index: any) => (
                <Checkbox key={index} value={item.value}>
                  {item.label}
                </Checkbox>
              ),
            )}
          </CheckboxGroup>
        );
      };
      const buildRadio = () => {
        const {
          modelValue,
          'onUpdate:modelValue': onUpdateModelValue,
          ...restAttrs
        } = attrs;
        if (isEmpty(options.value)) {
          options.value = [
            { label: '选项1', value: '选项1' },
            { label: '选项2', value: '选项2' },
          ];
        }
        return (
          <RadioGroup
            class="w-full"
            onUpdate:value={onUpdateModelValue as any}
            value={modelValue as any}
            {...restAttrs}
          >
            {options.value.map(
              (item: { label: any; value: any }, index: any) => (
                <Radio key={index} value={item.value}>
                  {item.label}
                </Radio>
              ),
            )}
          </RadioGroup>
        );
      };
      return () => (
        <>
          {(() => {
            switch (props.selectType) {
              case 'checkbox': {
                return buildCheckbox();
              }
              case 'radio': {
                return buildRadio();
              }
              case 'select': {
                return buildSelect();
              }
              default: {
                return buildSelect();
              }
            }
          })()}
        </>
      );
    },
  });
}
