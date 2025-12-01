import type { VbenFormSchema } from '#/adapter/form';
import type { VxeGridPropTypes } from '#/adapter/vxe-table';

import { markRaw } from 'vue';

import {
  AutoReplyMsgType,
  DICT_TYPE,
  RequestMessageTypes,
} from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { WxReply } from '#/views/mp/components';

/** 获取表格列配置 */
export function useGridColumns(
  msgType: AutoReplyMsgType,
): VxeGridPropTypes.Columns {
  const columns: VxeGridPropTypes.Columns = [];
  // 请求消息类型列（仅消息回复显示）
  if (msgType === AutoReplyMsgType.Message) {
    columns.push({
      field: 'requestMessageType',
      title: '请求消息类型',
      minWidth: 120,
    });
  }
  // 关键词列（仅关键词回复显示）
  if (msgType === AutoReplyMsgType.Keyword) {
    columns.push({
      field: 'requestKeyword',
      title: '关键词',
      minWidth: 150,
    });
  }
  // 匹配类型列（仅关键词回复显示）
  if (msgType === AutoReplyMsgType.Keyword) {
    columns.push({
      field: 'requestMatch',
      title: '匹配类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MP_AUTO_REPLY_REQUEST_MATCH },
      },
    });
  }
  // 回复消息类型列
  columns.push(
    {
      field: 'responseMessageType',
      title: '回复消息类型',
      minWidth: 120,
      cellRender: {
        name: 'CellDict',
        props: { type: DICT_TYPE.MP_MESSAGE_TYPE },
      },
    },
    {
      field: 'responseContent',
      title: '回复内容',
      minWidth: 200,
      slots: { default: 'replyContent' },
    },
    {
      field: 'createTime',
      title: '创建时间',
      minWidth: 180,
      formatter: 'formatDateTime',
    },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  );
  return columns;
}

/** 新增/修改的表单 */
export function useFormSchema(msgType: AutoReplyMsgType): VbenFormSchema[] {
  const schema: VbenFormSchema[] = [];
  // 消息类型（仅消息回复显示）
  if (msgType === AutoReplyMsgType.Message) {
    schema.push({
      fieldName: 'requestMessageType',
      label: '消息类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择',
        options: getDictOptions(DICT_TYPE.MP_MESSAGE_TYPE).filter((d) =>
          RequestMessageTypes.has(d.value as string),
        ),
      },
    });
  }
  // 匹配类型（仅关键词回复显示）
  if (msgType === AutoReplyMsgType.Keyword) {
    schema.push({
      fieldName: 'requestMatch',
      label: '匹配类型',
      component: 'Select',
      componentProps: {
        placeholder: '请选择匹配类型',
        allowClear: true,
        options: getDictOptions(
          DICT_TYPE.MP_AUTO_REPLY_REQUEST_MATCH,
          'number',
        ),
      },
      rules: 'required',
    });
  }
  // 关键词（仅关键词回复显示）
  if (msgType === AutoReplyMsgType.Keyword) {
    schema.push({
      fieldName: 'requestKeyword',
      label: '关键词',
      component: 'Input',
      componentProps: {
        placeholder: '请输入内容',
        allowClear: true,
      },
      rules: 'required',
    });
  }
  // 回复消息
  schema.push({
    fieldName: 'reply',
    label: '回复消息',
    component: markRaw(WxReply),
    modelPropName: 'modelValue',
  });
  return schema;
}

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'accountId',
      label: '公众号',
      component: 'Input',
    },
  ];
}
