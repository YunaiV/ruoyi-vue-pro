<script setup lang="ts">
import type { PayOrderApi } from '#/api/pay/order';

import { onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';

import { Page, useVbenModal } from '@vben/common-ui';
import {
  PayChannelEnum,
  PayDisplayModeEnum,
  PayOrderStatusEnum,
} from '@vben/constants';
import { useTabs } from '@vben/hooks';
import { fenToYuan, formatDate } from '@vben/utils';

import {
  ElButton,
  ElCard,
  ElDescriptions,
  ElImage,
  ElInput,
  ElMessage,
} from 'element-plus';

import { getOrder, submitOrder } from '#/api/pay/order';

import { channelsAlipay, channelsMock, channelsWechat } from './data';

defineOptions({ name: 'PayCashier' });

const route = useRoute();
const { push } = useRouter();
const { closeCurrentTab } = useTabs();

const id = ref(); // 支付单号
const title = ref('支付订单');
const returnUrl = ref<string>(); // 支付完的回调地址
const payOrder = ref<PayOrderApi.Order>();
const interval = ref<any>(undefined); // 定时任务，轮询是否完成支付

const [Modal, modalApi] = useVbenModal({
  showConfirmButton: false,
  destroyOnClose: true,
});

/** 展示形式：二维码 */
const qrCode = ref({
  url: '',
  visible: false,
});

/** 展示形式：条形码 */
const barCode = ref({
  channelCode: '',
  value: '',
  visible: false,
});

/** 获得支付信息 */
async function getDetail() {
  // 1. 获取路由参数
  id.value = route.query.id;
  if (route.query.returnUrl) {
    returnUrl.value = decodeURIComponent(route.query.returnUrl as string);
  }
  // 1.1 未传递订单编号
  if (!id.value) {
    ElMessage.error('未传递支付单号，无法查看对应的支付信息');
    goReturnUrl('cancel');
    return;
  }
  const res = await getOrder(id.value);
  // 1.2 无法查询到支付信息
  if (!res) {
    ElMessage.error('支付订单不存在，请检查！');
    goReturnUrl('cancel');
    return;
  }
  // 1.3 如果已支付、或者已关闭，则直接跳转
  if (res.status === PayOrderStatusEnum.SUCCESS.status) {
    ElMessage.success('支付成功');
    goReturnUrl('success');
    return;
  } else if (res.status === PayOrderStatusEnum.CLOSED.status) {
    ElMessage.error('无法支付，原因：订单已关闭');
    goReturnUrl('close');
    return;
  }
  // 2. 正常展示支付信息
  payOrder.value = res;
}

/** 处理支付 */
function handlePay(channelCode: string) {
  switch (channelCode) {
    // 条形码支付，需要特殊处理
    case PayChannelEnum.ALIPAY_BAR.code: {
      title.value = '“支付宝”条码支付';
      barCode.value = {
        channelCode,
        value: '',
        visible: true,
      };
      modalApi.open();
      break;
    }
    case PayChannelEnum.WX_BAR.code: {
      title.value = '“微信”条码支付';
      barCode.value = {
        channelCode,
        value: '',
        visible: true,
      };
      modalApi.open();
      break;
    }
    // 微信公众号、小程序支付，无法在 PC 网页中进行
    case PayChannelEnum.WX_LITE.code: {
      ElMessage.error('微信小程序：不支持 PC 网站');
      break;
    }
    case PayChannelEnum.WX_PUB.code: {
      ElMessage.error('微信公众号支付：不支持 PC 网站');
      break;
    }
    default: {
      submit(channelCode);
      break;
    }
  }
}

/** 提交支付 */
async function submit(channelCode: string) {
  try {
    const submitParam = {
      id: id.value,
      channelCode,
      returnUrl: location.href, // 支付成功后，支付渠道跳转回当前页；再由当前页，跳转回 {@link returnUrl} 对应的地址
      ...buildSubmitParam(channelCode),
    };
    const data = await submitOrder(submitParam);
    // 直接返回已支付的情况，例如说扫码支付
    if (data.status === PayOrderStatusEnum.SUCCESS.status) {
      clearQueryInterval();
      ElMessage.success('支付成功！');
      goReturnUrl('success');
      return;
    }

    // 展示对应的界面
    switch (data.displayMode) {
      case PayDisplayModeEnum.APP.mode: {
        displayApp(channelCode);
        break;
      }
      case PayDisplayModeEnum.QR_CODE.mode: {
        displayQrCode(channelCode, data);
        break;
      }
      case PayDisplayModeEnum.URL.mode: {
        displayUrl(data);
        break;
      }
      // No default
    }

    // 打开轮询任务
    createQueryInterval();
  } finally {
    //
  }
}

/** 构建提交支付的额外参数 */
function buildSubmitParam(channelCode: string) {
  // ① 支付宝 BarCode 支付时，需要传递 authCode 条形码
  if (channelCode === PayChannelEnum.ALIPAY_BAR.code) {
    return {
      channelExtras: {
        auth_code: barCode.value.value,
      },
    };
  }
  // ② 微信 BarCode 支付时，需要传递 authCode 条形码
  if (channelCode === PayChannelEnum.WX_BAR.code) {
    return {
      channelExtras: {
        authCode: barCode.value.value,
      },
    };
  }
  return {};
}

/** 提交支付后，URL 的展示形式 */
function displayUrl(data: any) {
  location.href = data.displayContent;
}

/** 提交支付后（扫码支付） */
function displayQrCode(channelCode: string, data: any) {
  title.value = '请使用手机浏览器“扫一扫”';
  if (channelCode === PayChannelEnum.ALIPAY_WAP.code) {
    // 考虑到 WAP 测试，所以引导手机浏览器搞
  } else if (channelCode.indexOf('alipay_') === 0) {
    title.value = '请使用支付宝“扫一扫”扫码支付';
  } else if (channelCode.indexOf('wx_') === 0) {
    title.value = '请使用微信“扫一扫”扫码支付';
  }
  qrCode.value = {
    url: data.displayContent,
    visible: true,
  };
}

/** 提交支付后（App） */
function displayApp(channelCode: string) {
  if (channelCode === PayChannelEnum.ALIPAY_APP.code) {
    ElMessage.error('支付宝 App 支付：无法在网页支付！');
  }
  if (channelCode === PayChannelEnum.WX_APP.code) {
    ElMessage.error('微信 App 支付：无法在网页支付！');
  }
}

/** 轮询查询任务 */
function createQueryInterval() {
  if (interval.value) {
    return;
  }
  interval.value = setInterval(async () => {
    const data = await getOrder(id.value);
    // 已支付
    if (data.status === PayOrderStatusEnum.SUCCESS.status) {
      clearQueryInterval();
      ElMessage.success('支付成功！');
      goReturnUrl('success');
    }
    // 已取消
    if (data.status === PayOrderStatusEnum.CLOSED.status) {
      clearQueryInterval();
      ElMessage.error('支付已关闭！');
      goReturnUrl('close');
    }
  }, 1000 * 2);
}

/** 清空查询任务 */
function clearQueryInterval() {
  // 清空数据
  qrCode.value = {
    url: '',
    visible: false,
  };
  barCode.value = {
    channelCode: '',
    value: '',
    visible: false,
  };
  // 清空任务
  clearInterval(interval.value);
  interval.value = undefined;
}

/**
 * 回到业务的 URL
 *
 * @param payResult 支付结果
 *  ① success：支付成功
 *  ② cancel：取消支付
 *  ③ close：支付已关闭
 */
function goReturnUrl(payResult: string) {
  // 清理任务
  clearQueryInterval();

  // 未配置的情况下，只能关闭
  if (!returnUrl.value) {
    closeCurrentTab();
    return;
  }

  const url = returnUrl.value.includes('?')
    ? `${returnUrl.value}&payResult=${payResult}`
    : `${returnUrl.value}?payResult=${payResult}`;
  // 如果有配置，且是 http 开头，则浏览器跳转
  if (returnUrl.value.indexOf('http') === 0) {
    location.href = url;
  } else {
    closeCurrentTab();
    push({ path: url });
  }
}

/** 页面加载时，获取支付信息 */
onMounted(async () => {
  await getDetail();
});

/** 页面卸载时，清理定时任务 */
onBeforeUnmount(() => {
  clearQueryInterval();
});
</script>
<template>
  <Page auto-content-height>
    <ElCard class="mt-4">
      <ElDescriptions :column="3" :title="payOrder?.subject ?? '商品详情'">
        <ElDescriptions.Item label="支付单号">
          {{ payOrder?.id }}
        </ElDescriptions.Item>
        <ElDescriptions.Item label="商品标题">
          {{ payOrder?.subject }}
        </ElDescriptions.Item>
        <ElDescriptions.Item label="商品内容">
          {{ payOrder?.body }}
        </ElDescriptions.Item>
        <ElDescriptions.Item label="支付金额">
          {{ `￥${fenToYuan(payOrder?.price || 0)}` }}
        </ElDescriptions.Item>
        <ElDescriptions.Item label="创建时间">
          {{ formatDate(payOrder?.createTime) }}
        </ElDescriptions.Item>
        <ElDescriptions.Item label="过期时间">
          {{ formatDate(payOrder?.expireTime) }}
        </ElDescriptions.Item>
      </ElDescriptions>
    </ElCard>
    <ElCard title="选择支付宝支付" class="mt-4">
      <div class="flex">
        <div
          class="mr-4 w-40 cursor-pointer items-center border-2 border-gray-200 pb-1 pt-4 text-center hover:border-blue-500"
          v-for="channel in channelsAlipay"
          :key="channel.code"
          @click="handlePay(channel.code)"
        >
          <div class="flex items-center justify-center">
            <component :is="channel.icon" class="h-10 w-10" />
          </div>
          <div class="mt-2 pt-1 text-center">{{ channel.name }}</div>
        </div>
      </div>
    </ElCard>
    <ElCard title="选择微信支付" class="mt-4">
      <div class="flex">
        <div
          class="mr-4 w-40 cursor-pointer items-center border-2 border-gray-200 pb-1 pt-4 text-center hover:border-blue-500"
          v-for="channel in channelsWechat"
          :key="channel.code"
          @click="handlePay(channel.code)"
        >
          <div class="flex items-center justify-center">
            <component :is="channel.icon" class="h-10 w-10" />
          </div>
          <div class="mt-2 pt-1 text-center">{{ channel.name }}</div>
        </div>
      </div>
    </ElCard>
    <ElCard title="选择其它支付" class="mt-4">
      <div class="flex">
        <div
          class="mr-4 w-40 cursor-pointer items-center border-2 border-gray-200 pb-1 pt-4 text-center hover:border-blue-500"
          v-for="channel in channelsMock"
          :key="channel.code"
          @click="handlePay(channel.code)"
        >
          <div class="flex items-center justify-center">
            <component :is="channel.icon" class="h-10 w-10" />
          </div>
          <div class="mt-2 pt-1 text-center">{{ channel.name }}</div>
        </div>
      </div>
    </ElCard>
    <Modal class="w-2/5" :title="title">
      <ElImage v-if="qrCode.visible" :src="qrCode.url" />
      <ElInput
        v-if="barCode.visible"
        v-model:value="barCode.value"
        placeholder="请输入条形码"
        required
      />
      <div class="text-right" v-if="barCode.visible">
        或使用
        <ElButton
          type="danger"
          link
          target="_blank"
          href="https://baike.baidu.com/item/条码支付/10711903"
        >
          (扫码枪/扫码盒)
        </ElButton>
        扫码
      </div>
    </Modal>
  </Page>
</template>
