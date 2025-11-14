import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { getCategorySimpleList } from '#/api/bpm/category';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    // {
    //   fieldName: 'startUserId',
    //   label: '发起人',
    //   component: 'ApiSelect',
    //   componentProps: {
    //     placeholder: '请选择发起人',
    //     allowClear: true,
    //     api: getSimpleUserList,
    //     labelField: 'nickname',
    //     valueField: 'id',
    //   },
    // },
    {
      fieldName: 'name',
      label: '流程名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入流程名称',
        allowClear: true,
      },
    },
    {
      fieldName: 'processDefinitionId',
      label: '所属流程',
      component: 'Input',
      componentProps: {
        placeholder: '请输入流程定义的编号',
        allowClear: true,
      },
    },
    // 流程分类
    {
      fieldName: 'category',
      label: '流程分类',
      component: 'ApiSelect',
      componentProps: {
        placeholder: '请输入流程分类',
        allowClear: true,
        api: getCategorySimpleList,
        labelField: 'name',
        valueField: 'code',
      },
    },
    // 流程状态
    {
      fieldName: 'status',
      label: '流程状态',
      component: 'Select',
      componentProps: {
        options: getDictOptions(
          DICT_TYPE.BPM_PROCESS_INSTANCE_STATUS,
          'number',
        ),
        placeholder: '请选择流程状态',
        allowClear: true,
      },
    },
    // 发起时间
    {
      fieldName: 'createTime',
      label: '发起时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        allowClear: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'name',
      title: '流程名称',
      minWidth: 200,
      fixed: 'left',
    },
    {
      field: 'summary',
      title: '摘要',
      minWidth: 200,
      slots: {
        default: 'slot-summary',
      },
    },

    {
      field: 'categoryName',
      title: '流程分类',
      minWidth: 120,
      fixed: 'left',
    },

    // 流程状态
    {
      field: 'status',
      title: '流程状态',
      minWidth: 250,
      slots: {
        default: 'slot-status',
      },
    },

    {
      field: 'startTime',
      title: '发起时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'endTime',
      title: '结束时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 180,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
