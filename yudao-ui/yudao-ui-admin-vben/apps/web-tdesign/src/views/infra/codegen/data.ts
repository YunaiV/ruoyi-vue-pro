import type { Recordable } from '@vben/types';

import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { InfraCodegenApi } from '#/api/infra/codegen';
import type { SystemMenuApi } from '#/api/system/menu';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';
import { IconifyIcon } from '@vben/icons';
import { handleTree } from '@vben/utils';

import { getDataSourceConfigList } from '#/api/infra/data-source-config';
import { getMenuList } from '#/api/system/menu';
import { $t } from '#/locales';
import { getRangePickerDefaultProps } from '#/utils';

/** 导入数据库表的表单 */
export function useImportTableFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'dataSourceConfigId',
      label: '数据源',
      component: 'ApiSelect',
      componentProps: {
        api: getDataSourceConfigList,
        labelField: 'name',
        valueField: 'id',
        autoSelect: 'first',
        placeholder: '请选择数据源',
      },
      rules: 'selectRequired',
    },
    {
      fieldName: 'name',
      label: '表名称',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入表名称',
      },
    },
    {
      fieldName: 'comment',
      label: '表描述',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入表描述',
      },
    },
  ];
}

/** 导入数据库表表格列定义 */
export function useImportTableColumns(): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { field: 'name', title: '表名称', minWidth: 200 },
    { field: 'comment', title: '表描述', minWidth: 200 },
  ];
}

/** 基本信息表单的 schema */
export function useBasicInfoFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'tableName',
      label: '表名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入仓库名称',
      },
      rules: 'required',
    },
    {
      fieldName: 'tableComment',
      label: '表描述',
      component: 'Input',
      componentProps: {
        placeholder: '请输入表描述',
      },
      rules: 'required',
    },
    {
      fieldName: 'className',
      label: '实体类名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入实体类名称',
      },
      rules: 'required',
      help: '默认去除表名的前缀。如果存在重复，则需要手动添加前缀，避免 MyBatis 报 Alias 重复的问题。',
    },
    {
      fieldName: 'author',
      label: '作者',
      component: 'Input',
      componentProps: {
        placeholder: '请输入作者',
      },
      rules: 'required',
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        rows: 3,
        placeholder: '请输入备注',
      },
      formItemClass: 'md:col-span-2',
    },
  ];
}

/** 生成信息表单基础 schema */
export function useGenerationInfoBaseFormSchema(): VbenFormSchema[] {
  return [
    {
      component: 'Select',
      fieldName: 'templateType',
      label: '生成模板',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.INFRA_CODEGEN_TEMPLATE_TYPE,
          'number',
        ),
        class: 'w-full',
      },
      rules: 'selectRequired',
    },
    {
      component: 'Select',
      fieldName: 'frontType',
      label: '前端类型',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_CODEGEN_FRONT_TYPE, 'number'),
        class: 'w-full',
      },
      rules: 'selectRequired',
    },
    {
      component: 'Select',
      fieldName: 'scene',
      label: '生成场景',
      componentProps: {
        options: getDictOptions(DICT_TYPE.INFRA_CODEGEN_SCENE, 'number'),
        class: 'w-full',
      },
      rules: 'selectRequired',
    },
    {
      fieldName: 'parentMenuId',
      label: '上级菜单',
      help: '分配到指定菜单下，例如 系统管理',
      component: 'ApiTreeSelect',
      componentProps: {
        allowClear: true,
        api: async () => {
          const data = await getMenuList();
          data.unshift({
            id: 0,
            name: '顶级菜单',
          } as SystemMenuApi.Menu);
          return handleTree(data);
        },
        class: 'w-full',
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
      component: 'Input',
      fieldName: 'moduleName',
      label: '模块名',
      help: '模块名，即一级目录，例如 system、infra、tool 等等',
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'businessName',
      label: '业务名',
      help: '业务名，即二级目录，例如 user、permission、dict 等等',
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'className',
      label: '类名称',
      help: '类名称（首字母大写），例如SysUser、SysMenu、SysDictData 等等',
      rules: 'required',
    },
    {
      component: 'Input',
      fieldName: 'classComment',
      label: '类描述',
      help: '用作类描述，例如 用户',
      rules: 'required',
    },
  ];
}

/** 树表信息 schema */
export function useGenerationInfoTreeFormSchema(
  columns: InfraCodegenApi.CodegenColumn[] = [],
): VbenFormSchema[] {
  return [
    {
      component: 'Divider',
      fieldName: 'treeDivider',
      label: '',
      renderComponentContent: () => {
        return {
          default: () => ['树表信息'],
        };
      },
      formItemClass: 'md:col-span-2',
    },
    {
      component: 'Select',
      fieldName: 'treeParentColumnId',
      label: '父编号字段',
      help: '树显示的父编码字段名，例如 parent_Id',
      componentProps: {
        class: 'w-full',
        allowClear: true,
        placeholder: '请选择',
        options: columns.map((column) => ({
          label: column.columnName,
          value: column.id,
        })),
      },
      rules: 'selectRequired',
    },
    {
      component: 'Select',
      fieldName: 'treeNameColumnId',
      label: '名称字段',
      help: '树节点显示的名称字段，一般是 name',
      componentProps: {
        class: 'w-full',
        allowClear: true,
        placeholder: '请选择名称字段',
        options: columns.map((column) => ({
          label: column.columnName,
          value: column.id,
        })),
      },
      rules: 'selectRequired',
    },
  ];
}

/** 主子表信息 schema */
export function useGenerationInfoSubTableFormSchema(
  columns: InfraCodegenApi.CodegenColumn[] = [],
  tables: InfraCodegenApi.CodegenTable[] = [],
): VbenFormSchema[] {
  return [
    {
      component: 'Divider',
      fieldName: 'subDivider',
      label: '',
      renderComponentContent: () => {
        return {
          default: () => ['主子表信息'],
        };
      },
      formItemClass: 'md:col-span-2',
    },
    {
      component: 'Select',
      fieldName: 'masterTableId',
      label: '关联的主表',
      help: '关联主表（父表）的表名， 如：system_user',
      componentProps: {
        class: 'w-full',
        allowClear: true,
        placeholder: '请选择',
        options: tables.map((table) => ({
          label: `${table.tableName}：${table.tableComment}`,
          value: table.id,
        })),
      },
      rules: 'selectRequired',
    },
    {
      component: 'Select',
      fieldName: 'subJoinColumnId',
      label: '子表关联的字段',
      help: '子表关联的字段， 如：user_id',
      componentProps: {
        class: 'w-full',
        allowClear: true,
        placeholder: '请选择',
        options: columns.map((column) => ({
          label: `${column.columnName}:${column.columnComment}`,
          value: column.id,
        })),
      },
      rules: 'selectRequired',
    },
    {
      component: 'RadioGroup',
      fieldName: 'subJoinMany',
      label: '关联关系',
      help: '主表与子表的关联关系',
      componentProps: {
        class: 'w-full',
        allowClear: true,
        placeholder: '请选择',
        options: [
          {
            label: '一对多',
            value: true,
          },
          {
            label: '一对一',
            value: false,
          },
        ],
      },
      rules: 'required',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'tableName',
      label: '表名称',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入表名称',
      },
    },
    {
      fieldName: 'tableComment',
      label: '表描述',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入表描述',
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(
  getDataSourceConfigName?: (dataSourceConfigId: number) => string | undefined,
): VxeTableGridOptions['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    {
      field: 'dataSourceConfigId',
      title: '数据源',
      minWidth: 120,
      formatter: ({ cellValue }) => getDataSourceConfigName?.(cellValue) || '-',
    },
    {
      field: 'tableName',
      title: '表名称',
      minWidth: 200,
    },
    {
      field: 'tableComment',
      title: '表描述',
      minWidth: 200,
    },
    {
      field: 'className',
      title: '实体',
      minWidth: 200,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'updateTime',
      title: '更新时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 280,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 代码生成表格列定义 */
export function useCodegenColumnTableColumns(): VxeTableGridOptions['columns'] {
  return [
    { field: 'columnName', title: '字段列名', minWidth: 130 },
    {
      field: 'columnComment',
      title: '字段描述',
      minWidth: 100,
      slots: { default: 'columnComment' },
    },
    { field: 'dataType', title: '物理类型', minWidth: 100 },
    {
      field: 'javaType',
      title: 'Java 类型',
      minWidth: 130,
      slots: { default: 'javaType' },
      params: {
        options: [
          { label: 'Long', value: 'Long' },
          { label: 'String', value: 'String' },
          { label: 'Integer', value: 'Integer' },
          { label: 'Double', value: 'Double' },
          { label: 'BigDecimal', value: 'BigDecimal' },
          { label: 'LocalDateTime', value: 'LocalDateTime' },
          { label: 'Boolean', value: 'Boolean' },
        ],
      },
    },
    {
      field: 'javaField',
      title: 'Java 属性',
      minWidth: 100,
      slots: { default: 'javaField' },
    },
    {
      field: 'createOperation',
      title: '插入',
      width: 40,
      slots: { default: 'createOperation' },
    },
    {
      field: 'updateOperation',
      title: '编辑',
      width: 40,
      slots: { default: 'updateOperation' },
    },
    {
      field: 'listOperationResult',
      title: '列表',
      width: 40,
      slots: { default: 'listOperationResult' },
    },
    {
      field: 'listOperation',
      title: '查询',
      width: 40,
      slots: { default: 'listOperation' },
    },
    {
      field: 'listOperationCondition',
      title: '查询方式',
      minWidth: 100,
      slots: { default: 'listOperationCondition' },
      params: {
        options: [
          { label: '=', value: '=' },
          { label: '!=', value: '!=' },
          { label: '>', value: '>' },
          { label: '>=', value: '>=' },
          { label: '<', value: '<' },
          { label: '<=', value: '<=' },
          { label: 'LIKE', value: 'LIKE' },
          { label: 'BETWEEN', value: 'BETWEEN' },
        ],
      },
    },
    {
      field: 'nullable',
      title: '允许空',
      width: 60,
      slots: { default: 'nullable' },
    },
    {
      field: 'htmlType',
      title: '显示类型',
      width: 130,
      slots: { default: 'htmlType' },
      params: {
        options: [
          { label: '文本框', value: 'input' },
          { label: '文本域', value: 'textarea' },
          { label: '下拉框', value: 'select' },
          { label: '单选框', value: 'radio' },
          { label: '复选框', value: 'checkbox' },
          { label: '日期控件', value: 'datetime' },
          { label: '图片上传', value: 'imageUpload' },
          { label: '文件上传', value: 'fileUpload' },
          { label: '富文本控件', value: 'editor' },
        ],
      },
    },
    {
      field: 'dictType',
      title: '字典类型',
      width: 120,
      slots: { default: 'dictType' },
    },
    {
      field: 'example',
      title: '示例',
      minWidth: 100,
      slots: { default: 'example' },
    },
  ];
}
