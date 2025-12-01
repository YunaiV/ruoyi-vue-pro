import type { VbenFormSchema } from '#/adapter/form';

import { getSimpleAccountList } from '#/api/mp/account';

/** 菜单未选中标识 */
export const MENU_NOT_SELECTED = '__MENU_NOT_SELECTED__';

/** 菜单级别枚举 */
export enum Level {
  Child = '2',
  Parent = '1',
  Undefined = '0',
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'accountId',
      label: '公众号',
      component: 'ApiSelect',
      componentProps: {
        api: getSimpleAccountList,
        labelField: 'name',
        valueField: 'id',
        autoSelect: 'first',
        placeholder: '请选择公众号',
      },
    },
  ];
}
