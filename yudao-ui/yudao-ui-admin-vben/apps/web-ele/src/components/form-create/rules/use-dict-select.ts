import { onMounted, ref } from 'vue';

import { buildUUID, cloneDeep } from '@vben/utils';

import * as DictDataApi from '#/api/system/dict/type';
import {
  localeProps,
  makeRequiredRule,
} from '#/components/form-create/helpers';
import { selectRule } from '#/components/form-create/rules/data';

/**
 * 字典选择器规则，如果规则使用到动态数据则需要单独配置不能使用 useSelectRule
 */
export const useDictSelectRule = () => {
  const label = '字典选择器';
  const name = 'DictSelect';
  const rules = cloneDeep(selectRule);
  const dictOptions = ref<{ label: string; value: string }[]>([]); // 字典类型下拉数据
  onMounted(async () => {
    const data = await DictDataApi.getSimpleDictTypeList();
    if (!data || data.length === 0) {
      return;
    }
    dictOptions.value =
      data?.map((item: DictDataApi.SystemDictTypeApi.DictType) => ({
        label: item.name,
        value: item.type,
      })) ?? [];
  });
  return {
    icon: 'icon-descriptions',
    label,
    name,
    rule() {
      return {
        type: name,
        field: buildUUID(),
        title: label,
        info: '',
        $required: false,
      };
    },
    props(_: any, { t }: any) {
      return localeProps(t, `${name}.props`, [
        makeRequiredRule(),
        {
          type: 'select',
          field: 'dictType',
          title: '字典类型',
          value: '',
          options: dictOptions.value,
        },
        {
          type: 'select',
          field: 'valueType',
          title: '字典值类型',
          value: 'str',
          options: [
            { label: '数字', value: 'int' },
            { label: '字符串', value: 'str' },
            { label: '布尔值', value: 'bool' },
          ],
        },
        ...rules,
      ]);
    },
  };
};
