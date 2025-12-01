import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { Demo03StudentApi } from '#/api/infra/demo/demo03/inner';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getRangePickerDefaultProps } from '#/utils';

/** 新增/修改的表单 */
export function useFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'name',
      label: '名字',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入名字',
      },
    },
    {
      fieldName: 'sex',
      label: '性别',
      rules: 'required',
      component: 'RadioGroup',
      componentProps: {
        options: getDictOptions(DICT_TYPE.SYSTEM_USER_SEX, 'number'),
        buttonStyle: 'solid',
        optionType: 'button',
      },
    },
    {
      fieldName: 'birthday',
      label: '出生日期',
      rules: 'required',
      component: 'DatePicker',
      componentProps: {
        showTime: true,
        format: 'YYYY-MM-DD HH:mm:ss',
        valueFormat: 'x',
      },
    },
    {
      fieldName: 'description',
      label: '简介',
      rules: 'required',
      component: 'RichTextarea',
    },
  ];
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '名字',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入名字',
      },
    },
    {
      fieldName: 'sex',
      label: '性别',
      component: 'Select',
      componentProps: {
        allowClear: true,
        options: getDictOptions(DICT_TYPE.SYSTEM_USER_SEX, 'number'),
        placeholder: '请选择性别',
      },
    },
    {
      fieldName: 'description',
      label: '简介',
      component: 'Input',
      componentProps: {
        allowClear: true,
        placeholder: '请输入简介',
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
export function useGridColumns(): VxeTableGridOptions<Demo03StudentApi.Demo03Student>['columns'] {
  return [
    { type: 'checkbox', width: 40 },
    { type: 'expand', width: 80, slots: { content: 'expand_content' } },
    {
      field: 'id',
      title: '编号',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '名字',
      minWidth: 120,
    },
    {
      field: 'sex',
      title: '性别',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.SYSTEM_USER_SEX },
      },
    },
    {
      field: 'birthday',
      title: '出生日期',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
    {
      field: 'description',
      title: '简介',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 120,
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

// ==================== 子表（学生课程） ====================

/** 新增/修改列表的字段 */
export function useDemo03CourseGridEditColumns(): VxeTableGridOptions<Demo03StudentApi.Demo03Course>['columns'] {
  return [
    {
      field: 'name',
      title: '名字',
      minWidth: 120,
      slots: { default: 'name' },
    },
    {
      field: 'score',
      title: '分数',
      minWidth: 120,
      slots: { default: 'score' },
    },
    {
      title: '操作',
      width: 280,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 列表的字段 */
export function useDemo03CourseGridColumns(): VxeTableGridOptions<Demo03StudentApi.Demo03Course>['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 120,
    },
    {
      field: 'studentId',
      title: '学生编号',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '名字',
      minWidth: 120,
    },
    {
      field: 'score',
      title: '分数',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
  ];
}

// ==================== 子表（学生班级） ====================

/** 新增/修改的表单 */
export function useDemo03GradeFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      component: 'Input',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      fieldName: 'name',
      label: '名字',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入名字',
      },
    },
    {
      fieldName: 'teacher',
      label: '班主任',
      rules: 'required',
      component: 'Input',
      componentProps: {
        placeholder: '请输入班主任',
      },
    },
  ];
}

/** 列表的字段 */
export function useDemo03GradeGridColumns(): VxeTableGridOptions<Demo03StudentApi.Demo03Grade>['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 120,
    },
    {
      field: 'studentId',
      title: '学生编号',
      minWidth: 120,
    },
    {
      field: 'name',
      title: '名字',
      minWidth: 120,
    },
    {
      field: 'teacher',
      title: '班主任',
      minWidth: 120,
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 120,
      formatter: 'formatDateTime',
    },
  ];
}
