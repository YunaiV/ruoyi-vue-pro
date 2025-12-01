import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { CrmProductCategoryApi } from '#/api/crm/product/category';

import { handleTree } from '@vben/utils';

import { getProductCategoryList } from '#/api/crm/product/category';

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Input',
      fieldName: 'id',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'parentId',
      label: '上级分类',
      component: 'ApiTreeSelect',
      componentProps: {
        allowClear: true,
        api: async () => {
          const data = await getProductCategoryList();
          data.unshift({
            id: 0,
            name: '顶级分类',
          } as CrmProductCategoryApi.ProductCategory);
          return handleTree(data);
        },
        fieldNames: { label: 'name', value: 'id', children: 'children' },
        placeholder: '请选择上级分类',
        showSearch: true,
        treeDefaultExpandAll: true,
      },
      rules: 'selectRequired',
    },
    {
      fieldName: 'name',
      label: '分类名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入分类名称',
      },
      rules: 'required',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '分类名称',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入分类名称',
      },
    },
  ];
}

/** 表格列配置 */
export function useGridColumns(): VxeTableGridOptions<CrmProductCategoryApi.ProductCategory>['columns'] {
  return [
    {
      field: 'name',
      title: '分类名称',
      treeNode: true,
    },
    {
      field: 'id',
      title: '分类编号',
    },
    {
      field: 'createTime',
      title: '创建时间',
      formatter: 'formatDateTime',
    },
    {
      field: 'actions',
      title: '操作',
      width: 250,
      fixed: 'right',
      slots: {
        default: 'actions',
      },
    },
  ];
}
