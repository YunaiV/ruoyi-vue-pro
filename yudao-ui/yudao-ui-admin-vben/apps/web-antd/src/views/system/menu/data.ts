import type { Recordable } from '@vben/types';

import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { SystemMenuApi } from '#/api/system/menu';

import { h } from 'vue';

import {
  CommonStatusEnum,
  DICT_TYPE,
  SystemMenuTypeEnum,
} from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';
import { handleTree, isHttpUrl } from '@vben/utils';

import { z } from '#/adapter/form';
import { getMenuList } from '#/api/system/menu';
import { $t } from '#/locales';
import { componentKeys } from '#/router/routes';

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
      label: '上级菜单',
      component: 'ApiTreeSelect',
      componentProps: {
        allowClear: true,
        api: async () => {
          const data = await getMenuList();
          data.unshift({
            id: 0,
            name: '顶级部门',
          } as SystemMenuApi.Menu);
          return handleTree(data);
        },
        labelField: 'name',
        valueField: 'id',
        childrenField: 'children',
        placeholder: '请选择上级菜单',
        filterTreeNode(input: string, node: Recordable<any>) {
          if (!input || input.length === 0) {
            return true;
          }
          const name: string = node.label ?? '';
          if (!name) return false;
          return name.includes(input) || $t(name).includes(input);
        },
        showSearch: true,
        treeDefaultExpandedKeys: [0],
      },
      rules: 'selectRequired',
      renderComponentContent() {
        return {
          title({ label, icon }: { icon: string; label: string }) {
            const components = [];
            if (!label) return '';
            if (icon) {
              components.push(h(IconifyIcon, { class: 'size-4', icon }));
            }
            components.push(h('span', { class: '' }, $t(label || '')));
            return h('div', { class: 'flex items-center gap-1' }, components);
          },
        };
      },
    },
    {
      fieldName: 'name',
      label: '菜单名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入菜单名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'type',
      label: '菜单类型',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_MENU_TYPE, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(SystemMenuTypeEnum.DIR),
    },
    {
      fieldName: 'icon',
      label: '菜单图标',
      component: 'IconPicker',
      componentProps: {
        placeholder: '请选择菜单图标',
        prefix: 'carbon',
      },
      rules: 'required',
      dependencies: {
        triggerFields: ['type'],
        show: (values) => {
          return [SystemMenuTypeEnum.DIR, SystemMenuTypeEnum.MENU].includes(
            values.type,
          );
        },
      },
    },
    {
      fieldName: 'path',
      label: '路由地址',
      component: 'Input',
      componentProps: {
        placeholder: '请输入路由地址',
      },
      rules: z.string(),
      help: '访问的路由地址，如：`user`。如需外网地址时，则以 `http(s)://` 开头',
      dependencies: {
        triggerFields: ['type', 'parentId'],
        show: (values) => {
          return [SystemMenuTypeEnum.DIR, SystemMenuTypeEnum.MENU].includes(
            values.type,
          );
        },
        rules: (values) => {
          const schema = z.string().min(1, '路由地址不能为空');
          if (isHttpUrl(values.path)) {
            return schema;
          }
          if (values.parentId === 0) {
            return schema.refine(
              (path) => path.charAt(0) === '/',
              '路径必须以 / 开头',
            );
          }
          return schema.refine(
            (path) => path.charAt(0) !== '/',
            '路径不能以 / 开头',
          );
        },
      },
    },
    {
      fieldName: 'component',
      label: '组件地址',
      component: 'Input',
      componentProps: {
        placeholder: '请输入组件地址',
      },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => {
          return [SystemMenuTypeEnum.MENU].includes(values.type);
        },
      },
    },
    {
      fieldName: 'componentName',
      label: '组件名称',
      component: 'AutoComplete',
      componentProps: {
        allowClear: true,
        filterOption(input: string, option: { value: string }) {
          return option.value.toLowerCase().includes(input.toLowerCase());
        },
        placeholder: '请选择组件名称',
        options: componentKeys.map((v) => ({ value: v })),
      },
      dependencies: {
        triggerFields: ['type'],
        show: (values) => {
          return [SystemMenuTypeEnum.MENU].includes(values.type);
        },
      },
    },
    {
      fieldName: 'permission',
      label: '权限标识',
      component: 'Input',
      componentProps: {
        placeholder: '请输入菜单描述',
      },
      dependencies: {
        show: (values) => {
          return [SystemMenuTypeEnum.BUTTON, SystemMenuTypeEnum.MENU].includes(
            values.type,
          );
        },
        triggerFields: ['type'],
      },
    },
    {
      fieldName: 'sort',
      label: '显示顺序',
      component: 'InputNumber',
      componentProps: {
        min: 0,
        placeholder: '请输入显示顺序',
      },
      rules: 'required',
    },
    {
      fieldName: 'status',
      label: '菜单状态',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: z.number().default(CommonStatusEnum.ENABLE),
    },
    {
      fieldName: 'alwaysShow',
      label: '总是显示',
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '总是', value: true },
          { label: '不是', value: false },
        ],
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: 'required',
      defaultValue: true,
      help: '选择不是时，当该菜单只有一个子菜单时，不展示自己，直接展示子菜单',
      dependencies: {
        triggerFields: ['type'],
        show: (values) => {
          return [SystemMenuTypeEnum.MENU].includes(values.type);
        },
      },
    },
    {
      fieldName: 'keepAlive',
      label: '缓存状态',
      component: 'RadioGroup',
      componentProps: {
        options: [
          { label: '缓存', value: true },
          { label: '不缓存', value: false },
        ],
        buttonStyle: 'solid',
        optionType: 'button',
      },
      rules: 'required',
      defaultValue: true,
      help: '选择缓存时，则会被 `keep-alive` 缓存，必须填写「组件名称」字段',
      dependencies: {
        triggerFields: ['type'],
        show: (values) => {
          return [SystemMenuTypeEnum.MENU].includes(values.type);
        },
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions<SystemMenuApi.Menu>['columns'] {
  return [
    {
      field: 'name',
      title: '菜单名称',
      minWidth: 250,
      align: 'left',
      fixed: 'left',
      slots: { default: 'name' },
      treeNode: true,
    },
    {
      field: 'type',
      title: '菜单类型',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_MENU_TYPE },
      },
    },
    {
      field: 'sort',
      title: '显示排序',
      minWidth: 100,
    },
    {
      field: 'permission',
      title: '权限标识',
      minWidth: 200,
    },
    {
      field: 'path',
      title: '组件路径',
      minWidth: 200,
    },
    {
      field: 'componentName',
      title: '组件名称',
      minWidth: 200,
    },
    {
      field: 'status',
      title: '状态',
      minWidth: 100,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.COMMON_STATUS },
      },
    },
    {
      title: '操作',
      width: 220,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
