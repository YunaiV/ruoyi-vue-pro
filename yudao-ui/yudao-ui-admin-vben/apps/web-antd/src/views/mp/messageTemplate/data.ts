import type { VbenFormSchema } from '#/adapter/form';
import type { VxeGridPropTypes } from '#/adapter/vxe-table';

import { getUserPage } from '#/api/mp/user';

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

/** 发送消息模板表单 */
export function useSendFormSchema(accountId?: number): VbenFormSchema[] {
  return [
    {
      fieldName: 'id',
      label: '模板编号',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'title',
      label: '模板标题',
      component: 'Input',
      componentProps: {
        disabled: true,
      },
    },
    {
      fieldName: 'userId',
      label: '用户',
      component: 'ApiSelect',
      componentProps: {
        api: async () => {
          if (!accountId) {
            return [];
          }
          const data = await getUserPage({
            pageNo: 1,
            pageSize: 100,
            accountId,
          });
          return (data.list || []).map((user) => ({
            label: user.nickname || user.openid,
            value: user.id,
          }));
        },
        showSearch: true,
        placeholder: '请选择用户',
      },
      rules: 'required',
    },
    {
      fieldName: 'data',
      label: '模板数据',
      component: 'Textarea',
      componentProps: {
        rows: 4,
        placeholder:
          '请输入模板数据（JSON 格式），例如：{"keyword1": {"value": "测试内容"}}',
      },
    },
    {
      fieldName: 'url',
      label: '跳转链接',
      component: 'Input',
      componentProps: {
        placeholder: '请输入跳转链接',
      },
    },
    {
      fieldName: 'miniProgramAppId',
      label: '小程序 appId',
      component: 'Input',
      componentProps: {
        placeholder: '请输入小程序 appId',
      },
    },
    {
      fieldName: 'miniProgramPagePath',
      label: '小程序页面路径',
      component: 'Input',
      componentProps: {
        placeholder: '请输入小程序页面路径',
      },
    },
  ];
}

/** 表格列配置 */
export function useGridColumns(): VxeGridPropTypes.Columns {
  return [
    {
      title: '公众号模板 ID',
      field: 'templateId',
      minWidth: 400,
    },
    {
      title: '标题',
      field: 'title',
      minWidth: 150,
    },
    {
      title: '模板内容',
      field: 'content',
      minWidth: 400,
    },
    {
      title: '模板示例',
      field: 'example',
      minWidth: 200,
    },
    {
      title: '一级行业',
      field: 'primaryIndustry',
      minWidth: 120,
    },
    {
      title: '二级行业',
      field: 'deputyIndustry',
      minWidth: 120,
    },
    {
      title: '创建时间',
      field: 'createTime',
      formatter: 'formatDateTime',
      minWidth: 180,
    },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
