<script lang="ts" setup>
import type { VbenFormSchema } from '#/adapter/form';
import type { VxeGridProps, VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MemberExperienceRecordApi } from '#/api/member/experience-record';

import { h } from 'vue';

import { DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { ElTag } from 'element-plus';

import { useVbenVxeGrid } from '#/adapter/vxe-table';
import { getExperienceRecordPage } from '#/api/member/experience-record';
import { getRangePickerDefaultProps } from '#/utils';

const props = defineProps<{
  userId: number;
}>();

/** 表单搜索 schema */
function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'bizType',
      label: '业务类型',
      component: 'Select',
      componentProps: {
        options: getDictOptions(DICT_TYPE.MEMBER_EXPERIENCE_BIZ_TYPE, 'number'),
        placeholder: '请选择业务类型',
        clearable: true,
      },
    },
    {
      fieldName: 'title',
      label: '标题',
      component: 'Input',
      componentProps: {
        placeholder: '请输入标题',
        clearable: true,
      },
    },
    {
      fieldName: 'createDate',
      label: '获得时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 表格列配置 */
function useGridColumns(): VxeGridProps['columns'] {
  return [
    {
      field: 'id',
      title: '编号',
      minWidth: 100,
    },
    {
      field: 'createTime',
      title: '获得时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'experience',
      title: '经验',
      minWidth: 100,
      slots: {
        default: ({ row }) => {
          return h(
            ElTag,
            {
              type: row.experience > 0 ? 'primary' : 'danger',
            },
            () => (row.experience > 0 ? `+${row.experience}` : row.experience),
          );
        },
      },
    },
    {
      field: 'totalExperience',
      title: '总经验',
      minWidth: 100,
    },
    {
      field: 'title',
      title: '标题',
      minWidth: 200,
    },
    {
      field: 'description',
      title: '描述',
      minWidth: 250,
    },
    {
      field: 'bizId',
      title: '业务编号',
      minWidth: 120,
    },
    {
      field: 'bizType',
      title: '业务类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MEMBER_EXPERIENCE_BIZ_TYPE },
      },
    },
  ];
}

const [Grid] = useVbenVxeGrid({
  formOptions: {
    schema: useGridFormSchema(),
  },
  gridOptions: {
    columns: useGridColumns(),
    keepSource: true,
    proxyConfig: {
      ajax: {
        query: async ({ page }, formValues) => {
          return await getExperienceRecordPage({
            pageNo: page.currentPage,
            pageSize: page.pageSize,
            userId: props.userId,
            ...formValues,
          });
        },
      },
    },
    rowConfig: {
      keyField: 'id',
      isHover: true,
    },
    toolbarConfig: {
      refresh: true,
      search: true,
    },
  } as VxeTableGridOptions<MemberExperienceRecordApi.ExperienceRecord>,
});
</script>

<template>
  <Grid />
</template>
