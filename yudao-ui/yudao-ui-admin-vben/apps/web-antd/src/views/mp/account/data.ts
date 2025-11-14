import type { VbenFormSchema } from '#/adapter/form';
import type { VxeGridPropTypes } from '#/adapter/vxe-table';

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
      fieldName: 'name',
      label: '名称',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入名称',
      },
    },
    {
      fieldName: 'account',
      label: '微信号',
      component: 'Input',
      help: '在微信公众平台（mp.weixin.qq.com）的菜单 [设置与开发 - 公众号设置 - 账号详情] 中能找到「微信号」',
      rules: 'required',
      componentProps: {
        placeholder: '请输入微信号',
      },
    },
    {
      fieldName: 'appId',
      label: 'appId',
      component: 'Input',
      help: '在微信公众平台（mp.weixin.qq.com）的菜单 [设置与开发 - 公众号设置 - 基本设置] 中能找到「开发者ID(AppID)」',
      rules: 'required',
      componentProps: {
        placeholder: '请输入appId',
      },
    },
    {
      fieldName: 'appSecret',
      label: 'appSecret',
      component: 'Input',
      help: '在微信公众平台（mp.weixin.qq.com）的菜单 [设置与开发 - 公众号设置 - 基本设置] 中能找到「开发者密码(AppSecret)」',
      rules: 'required',
      componentProps: {
        placeholder: '请输入appSecret',
      },
    },
    {
      fieldName: 'token',
      label: 'token',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入token',
      },
    },
    {
      fieldName: 'aesKey',
      label: '消息加解密密钥',
      component: 'Input',
      componentProps: {
        placeholder: '请输入aesKey',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 搜索表单配置 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '名称',
      component: 'Input',
      componentProps: {
        placeholder: '请输入名称',
      },
    },
  ];
}

/** 表格列配置 */
export function useGridColumns(): VxeGridPropTypes.Columns {
  return [
    {
      title: '名称',
      field: 'name',
    },
    {
      title: '微信号',
      field: 'account',
    },
    {
      title: 'appId',
      field: 'appId',
    },
    {
      title: '服务器地址(URL)',
      field: 'utl',
      slots: {
        default: ({ row }) => {
          return `http://服务端地址/admin-api/mp/open/${row.appId}`;
        },
      },
    },
    {
      title: '二维码',
      field: 'qrCodeUrl',
      slots: {
        default: 'qrCodeUrl',
      },
    },
    {
      title: '备注',
      field: 'remark',
    },
    {
      title: '操作',
      width: 260,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}
