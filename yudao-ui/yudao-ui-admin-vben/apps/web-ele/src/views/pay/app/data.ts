import type { VbenFormSchema } from '#/adapter/form';
import type { VxeTableGridOptions } from '#/adapter/vxe-table';
import type { PayAppApi } from '#/api/pay/app';

import { h } from 'vue';

import { CommonStatusEnum, DICT_TYPE } from '@vben/constants';
import { getDictOptions } from '@vben/hooks';

import { z } from '#/adapter/form';
import { InputUpload } from '#/components/upload';
import { getRangePickerDefaultProps } from '#/utils';

/** 列表的搜索表单 */
export function useGridFormSchema(): VbenFormSchema[] {
  return [
    {
      fieldName: 'name',
      label: '应用名',
      component: 'Input',
      componentProps: {
        placeholder: '请输入应用名',
        clearable: true,
      },
    },
    {
      fieldName: 'status',
      label: '开启状态',
      component: 'Select',
      componentProps: {
        placeholder: '请选择开启状态',
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
        clearable: true,
      },
    },
    {
      fieldName: 'createTime',
      label: '创建时间',
      component: 'RangePicker',
      componentProps: {
        ...getRangePickerDefaultProps(),
        clearable: true,
      },
    },
  ];
}

/** 列表的字段 */
export function useGridColumns(
  onStatusChange?: (
    newStatus: number,
    row: PayAppApi.App,
  ) => PromiseLike<boolean | undefined>,
): VxeTableGridOptions['columns'] {
  return [
    {
      field: 'appKey',
      title: '应用标识',
      minWidth: 40,
    },
    {
      field: 'name',
      title: '应用名',
      minWidth: 40,
    },
    {
      field: 'status',
      title: '状态',
      align: 'center',
      minWidth: 40,
      cellRender: {
        attrs: { beforeChange: onStatusChange },
        name: 'CellSwitch',
        props: {
          activeValue: CommonStatusEnum.ENABLE,
          inactiveValue: CommonStatusEnum.DISABLE,
        },
      },
    },
    {
      title: '支付宝配置',
      children: [
        {
          title: 'APP',
          slots: {
            default: 'alipayAppConfig',
          },
        },
        {
          title: 'PC 网站',
          slots: {
            default: 'alipayPCConfig',
          },
        },
        {
          title: 'WAP 网站',
          slots: {
            default: 'alipayWAPConfig',
          },
          minWidth: 10,
        },
        {
          title: '扫码',
          slots: {
            default: 'alipayQrConfig',
          },
        },
        {
          title: '条码',
          slots: {
            default: 'alipayBarConfig',
          },
        },
      ],
    },
    {
      title: '微信配置',
      children: [
        {
          title: '小程序',
          slots: {
            default: 'wxLiteConfig',
          },
        },
        {
          title: 'JSAPI',
          slots: {
            default: 'wxPubConfig',
          },
        },
        {
          title: 'APP',
          slots: {
            default: 'wxAppConfig',
          },
        },
        {
          title: 'Native',
          slots: {
            default: 'wxNativeConfig',
          },
        },
        {
          title: 'WAP 网站',
          slots: {
            default: 'wxWapConfig',
          },
          minWidth: 10,
        },
        {
          title: '条码',
          slots: {
            default: 'wxBarConfig',
          },
        },
      ],
    },
    {
      title: '钱包支付配置',
      field: 'walletConfig',
      slots: {
        default: 'walletConfig',
      },
    },
    {
      title: '模拟支付配置',
      field: 'mockConfig',
      slots: {
        default: 'mockConfig',
      },
    },
    {
      title: '操作',
      width: 140,
      fixed: 'right',
      slots: { default: 'actions' },
    },
  ];
}

/** 应用新增/修改的表单 */
export function useAppFormSchema(): VbenFormSchema[] {
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
      label: '应用名',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入应用名',
      },
    },
    {
      fieldName: 'appKey',
      label: '应用标识',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入应用标识',
      },
    },
    {
      fieldName: 'status',
      label: '开启状态',
      component: 'RadioGroup',
      rules: z.number().default(CommonStatusEnum.ENABLE),
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
    },
    {
      fieldName: 'orderNotifyUrl',
      label: '支付结果的回调地址',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入支付结果的回调地址',
      },
    },
    {
      fieldName: 'refundNotifyUrl',
      label: '退款结果的回调地址',
      component: 'Input',
      rules: 'required',
      componentProps: {
        placeholder: '请输入退款结果的回调地址',
      },
    },
    {
      fieldName: 'transferNotifyUrl',
      label: '转账结果的回调地址',
      component: 'Input',
      componentProps: {
        placeholder: '请输入转账结果的回调地址',
      },
    },
    {
      fieldName: 'remark',
      label: '备注',
      component: 'Textarea',
      componentProps: {
        rows: 3,
        placeholder: '请输入备注',
      },
    },
  ];
}

/** 渠道新增/修改的表单 */
export function useChannelFormSchema(formType: string = ''): VbenFormSchema[] {
  const schema: VbenFormSchema[] = [
    {
      component: 'Input',
      fieldName: 'id',
      dependencies: {
        triggerFields: [''],
        show: () => false,
      },
    },
    {
      label: '应用编号',
      fieldName: 'appId',
      component: 'Input',
      dependencies: {
        show: () => false,
        triggerFields: [''],
      },
    },
    {
      label: '渠道编码',
      fieldName: 'code',
      component: 'Input',
      dependencies: {
        show: () => false,
        triggerFields: [''],
      },
    },
    {
      label: '渠道费率',
      fieldName: 'feeRate',
      component: 'InputNumber',
      rules: 'required',
      componentProps: {
        placeholder: '请输入渠道费率',
        addonAfter: '%',
        controlsPosition: 'right',
        class: '!w-full',
      },
      defaultValue: 0,
    },
    {
      label: '渠道状态',
      fieldName: 'status',
      component: 'RadioGroup',
      rules: z.number().default(CommonStatusEnum.ENABLE),
      componentProps: {
        options: getDictOptions(DICT_TYPE.COMMON_STATUS, 'number'),
      },
    },
  ];
  // 添加通用字段
  // 根据类型添加特定字段
  if (formType.includes('alipay_')) {
    schema.push(
      {
        label: '开放平台 APPID',
        fieldName: 'config.appId',
        component: 'Input',
        rules: 'required',
        componentProps: {
          placeholder: '请输入开放平台 APPID',
        },
      },
      {
        label: '网关地址',
        fieldName: 'config.serverUrl',
        component: 'RadioGroup',
        rules: 'required',
        componentProps: {
          options: [
            {
              value: 'https://openapi.alipay.com/gateway.do',
              label: '线上环境',
            },
            {
              value: 'https://openapi-sandbox.dl.alipaydev.com/gateway.do',
              label: '沙箱环境',
            },
          ],
        },
      },
      {
        label: '算法类型',
        fieldName: 'config.signType',
        component: 'RadioGroup',
        rules: 'required',
        componentProps: {
          options: [
            {
              value: 'RSA2',
              label: 'RSA2',
            },
          ],
        },
        defaultValue: 'RSA2',
      },
      {
        label: '公钥类型',
        fieldName: 'config.mode',
        component: 'RadioGroup',
        rules: 'required',
        componentProps: {
          options: [
            {
              value: 1,
              label: '公钥模式',
            },
            {
              value: 2,
              label: '证书模式',
            },
          ],
        },
      },
      {
        label: '应用私钥',
        fieldName: 'config.privateKey',
        component: 'Textarea',
        rules: 'required',
        componentProps: {
          placeholder: '请输入应用私钥',
          rows: 3,
        },
      },
      {
        label: '支付宝公钥',
        fieldName: 'config.alipayPublicKey',
        component: 'Textarea',
        rules: 'required',
        componentProps: {
          placeholder: '请输入支付宝公钥',
          rows: 3,
        },
        dependencies: {
          show(values: any) {
            return values?.config?.mode === 1;
          },
          triggerFields: ['config.mode', 'mode', 'config'],
        },
      },
      {
        label: '商户公钥应用证书',
        fieldName: 'config.appCertContent',
        component: h(InputUpload, {
          inputType: 'textarea',
          textareaProps: {
            rows: 3,
            placeholder: '请上传商户公钥应用证书',
          },
          fileUploadProps: {
            accept: ['crt'],
          },
        }),
        rules: 'required',
        dependencies: {
          show(values: any) {
            return values?.config?.mode === 2;
          },
          triggerFields: ['config.mode', 'mode', 'config'],
        },
      },
      {
        label: '支付宝公钥证书',
        fieldName: 'config.alipayPublicCertContent',
        component: h(InputUpload, {
          inputType: 'textarea',
          textareaProps: { rows: 3, placeholder: '请上传支付宝公钥证书' },
          fileUploadProps: {
            accept: ['crt'],
          },
        }),
        rules: 'required',
        dependencies: {
          show(values: any) {
            return values?.config?.mode === 2;
          },
          triggerFields: ['config.mode', 'mode', 'config'],
        },
      },
      {
        label: '根证书',
        fieldName: 'config.rootCertContent',
        component: h(InputUpload, {
          inputType: 'textarea',
          textareaProps: { rows: 3, placeholder: '请上传根证书' },
          fileUploadProps: {
            accept: ['crt'],
          },
        }),
        rules: 'required',
        dependencies: {
          show(values: any) {
            return values?.config?.mode === 2;
          },
          triggerFields: ['config.mode', 'mode', 'config'],
        },
      },
      {
        label: '接口内容加密方式',
        fieldName: 'config.encryptType',
        component: 'RadioGroup',
        rules: 'required',
        componentProps: {
          options: [
            {
              value: 'NONE',
              label: '无加密',
            },
            {
              value: 'AES',
              label: 'AES',
            },
          ],
        },
        defaultValue: 'NONE',
      },
      {
        label: '接口内容加密密钥',
        fieldName: 'config.encryptKey',
        component: 'Input',
        rules: 'required',
        dependencies: {
          show(values: any) {
            return values?.config?.encryptType === 'AES';
          },
          triggerFields: ['config.encryptType', 'encryptType', 'config'],
        },
      },
    );
  } else if (formType.includes('wx_')) {
    schema.push(
      {
        label: '微信 APPID',
        fieldName: 'config.appId',
        help: '前往微信商户平台[https://pay.weixin.qq.com/index.php/extend/merchant_appid/mapay_platform/account_manage]查看 APPID',
        component: 'Input',
        rules: 'required',
        componentProps: {
          placeholder: '请输入微信 APPID',
        },
      },
      {
        label: '商户号',
        fieldName: 'config.mchId',
        help: '前往微信商户平台[https://pay.weixin.qq.com/index.php/extend/pay_setting]查看商户号',
        component: 'Input',
        rules: 'required',
        componentProps: {
          placeholder: '请输入商户号',
        },
      },
      {
        label: 'API 版本',
        fieldName: 'config.apiVersion',
        component: 'RadioGroup',
        rules: 'required',
        componentProps: {
          options: [
            {
              label: 'v2',
              value: 'v2',
            },
            {
              label: 'v3',
              value: 'v3',
            },
          ],
        },
      },
      {
        label: '商户密钥',
        fieldName: 'config.mchKey',
        component: 'Input',
        rules: 'required',
        componentProps: {
          placeholder: '请输入商户密钥',
        },
        dependencies: {
          show(values: any) {
            return values?.config?.apiVersion === 'v2';
          },
          triggerFields: ['config.mode', 'mode', 'config'],
        },
      },
      {
        label: 'apiclient_cert.p12 证书',
        fieldName: 'config.keyContent',
        component: h(InputUpload, {
          inputType: 'textarea',
          textareaProps: {
            rows: 3,
            placeholder: '请上传 apiclient_cert.p12 证书',
          },
          fileUploadProps: {
            accept: ['p12'],
          },
        }),
        rules: 'required',
        dependencies: {
          show(values: any) {
            return values?.config?.apiVersion === 'v2';
          },
          triggerFields: ['config.mode', 'mode', 'config'],
        },
      },
      {
        label: 'API V3 密钥',
        fieldName: 'config.apiV3Key',
        component: 'Input',
        rules: 'required',
        componentProps: {
          placeholder: '请输入 API V3 密钥',
        },
        dependencies: {
          show(values: any) {
            return values?.config?.apiVersion === 'v3';
          },
          triggerFields: ['config.mode', 'mode', 'config'],
        },
      },
      {
        label: 'apiclient_key.pem 证书',
        fieldName: 'config.privateKeyContent',
        component: h(InputUpload, {
          inputType: 'textarea',
          textareaProps: {
            rows: 3,
            placeholder: '请上传 apiclient_key.pem 证书',
          },
          fileUploadProps: {
            accept: ['pem'],
          },
        }),
        rules: 'required',
        dependencies: {
          show(values: any) {
            return values?.config?.apiVersion === 'v3';
          },
          triggerFields: ['config.mode', 'mode', 'config'],
        },
      },
      {
        label: '证书序列号',
        fieldName: 'config.certSerialNo',
        component: 'Input',
        help: '前往微信商户平台[https://pay.weixin.qq.com/index.php/core/cert/api_cert#/api-cert-manage]查看证书序列号',
        rules: 'required',
        componentProps: {
          placeholder: '请输入证书序列号',
        },
        dependencies: {
          show(values: any) {
            return values?.config?.apiVersion === 'v3';
          },
          triggerFields: ['config.mode', 'mode', 'config'],
        },
      },
      {
        label: 'public_key.pem 证书',
        fieldName: 'config.publicKeyContent',
        component: h(InputUpload, {
          inputType: 'textarea',
          textareaProps: {
            rows: 3,
            placeholder: '请上传 public_key.pem 证书',
          },
          fileUploadProps: {
            accept: ['pem'],
          },
        }),
        dependencies: {
          show(values: any) {
            return values?.config?.apiVersion === 'v3';
          },
          triggerFields: ['config.mode', 'mode', 'config'],
        },
      },
      {
        label: '公钥 ID',
        fieldName: 'config.publicKeyId',
        component: 'Input',
        help: '微信支付公钥产品简介及使用说明[https://pay.weixin.qq.com/doc/v3/merchant/4012153196]',
        rules: 'required',
        componentProps: {
          placeholder: '请输入公钥 ID',
        },
        dependencies: {
          show(values: any) {
            return values?.config?.apiVersion === 'v3';
          },
          triggerFields: ['config.mode', 'mode', 'config'],
        },
      },
    );
  }
  // 添加备注字段（所有类型都有）
  schema.push({
    label: '备注',
    fieldName: 'remark',
    component: 'Input',
    componentProps: {
      placeholder: '请输入备注',
    },
  });
  return schema;
}
