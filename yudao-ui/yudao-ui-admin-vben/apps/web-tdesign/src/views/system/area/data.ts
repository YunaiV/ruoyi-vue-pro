import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemAreaApi } from '#/api/system/area';

import { z } from '#/adapter/form';

/** 查询 IP 的表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'ip',
      label: 'IP 地址',
      component: 'Input',
      componentProps: {
        placeholder: '请输入 IP 地址',
      },
      rules: z.string().ip({ message: '请输入正确的 IP 地址' }),
    },
    {
      fieldName: 'result',
      label: '地址',
      component: 'Input',
      componentProps: {
        placeholder: '展示查询 IP 结果',
        readonly: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<SystemAreaApi.Area>['columns'] {
  return [
    {
      field: 'id',
      title: '地区编码',
      minWidth: 120,
      align: 'left',
      fixed: 'left',
      treeNode: true,
    },
    {
      field: 'name',
      title: '地区名称',
      minWidth: 200,
    },
  ];
}
