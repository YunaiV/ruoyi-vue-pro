import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { MpMaterialApi } from '#/api/mp/material';

// TODO @dylan：看看 ele 要迁移一个么？
/** 视频表格列配置 */
export function useVideoGridColumns(): VxeTableGridOptions<MpMaterialApi.Material>['columns'] {
  return [
    {
      field: 'mediaId',
      title: '编号',
      align: 'center',
      width: 160,
    },
    {
      field: 'name',
      title: '文件名',
      align: 'center',
      minWidth: 100,
    },
    {
      field: 'title',
      title: '标题',
      align: 'center',
      minWidth: 200,
    },
    {
      field: 'introduction',
      title: '介绍',
      align: 'center',
      minWidth: 220,
    },
    {
      field: 'video',
      title: '视频',
      align: 'center',
      width: 220,
      slots: { default: 'video' },
    },
    {
      field: 'createTime',
      title: '上传时间',
      align: 'center',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'actions',
      title: '操作',
      align: 'center',
      fixed: 'right',
      width: 180,
      slots: { default: 'actions' },
    },
  ];
}

/** 语音表格列配置 */
export function useVoiceGridColumns(): VxeTableGridOptions<MpMaterialApi.Material>['columns'] {
  return [
    {
      field: 'mediaId',
      title: '编号',
      align: 'center',
      width: 160,
    },
    {
      field: 'name',
      title: '文件名',
      align: 'center',
      minWidth: 100,
    },
    {
      field: 'voice',
      title: '语音',
      align: 'center',
      width: 220,
      slots: { default: 'voice' },
    },
    {
      field: 'createTime',
      title: '上传时间',
      align: 'center',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'actions',
      title: '操作',
      align: 'center',
      fixed: 'right',
      width: 160,
      slots: { default: 'actions' },
    },
  ];
}

/** 图片表格列配置 */
export function useImageGridColumns(): VxeTableGridOptions<MpMaterialApi.Material>['columns'] {
  return [
    {
      field: 'mediaId',
      title: '编号',
      align: 'center',
      width: 400,
    },
    {
      field: 'name',
      title: '文件名',
      align: 'center',
      width: 200,
    },
    {
      field: 'url',
      title: '图片',
      align: 'center',
      width: 200,
      slots: { default: 'image' },
    },
    {
      field: 'createTime',
      title: '上传时间',
      align: 'center',
      width: 180,
      formatter: 'formatDateTime',
    },
    {
      field: 'actions',
      title: '操作',
      align: 'center',
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
