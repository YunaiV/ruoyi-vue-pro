<script setup lang="ts">
import { useRoute } from 'vue-router';

import { SvgDingDingIcon } from '@vben/icons';
import { $t } from '@vben/locales';

import { alert, useVbenModal } from '@vben-core/popup-ui';
import { VbenIconButton } from '@vben-core/shadcn-ui';
import { loadScript } from '@vben-core/shared/utils';

interface Props {
  clientId: string;
  corpId: string;
  // 登录回调地址
  redirectUri?: string;
  // 是否内嵌二维码登录
  isQrCode?: boolean;
}

const props = defineProps<Props>();

const route = useRoute();

const [Modal, modalApi] = useVbenModal({
  header: false,
  footer: false,
  fullscreenButton: false,
  class: 'w-[302px] h-[302px] dingding-qrcode-login-modal',
  onOpened() {
    handleQrCodeLogin();
  },
});

const getRedirectUri = () => {
  const { redirectUri } = props;
  if (redirectUri) {
    return redirectUri;
  }
  return window.location.origin + route.fullPath;
};

/**
 * 内嵌二维码登录
 */
const handleQrCodeLogin = async () => {
  const { clientId, corpId } = props;
  if (!(window as any).DTFrameLogin) {
    // 二维码登录 加载资源
    await loadScript(
      'https://g.alicdn.com/dingding/h5-dingtalk-login/0.21.0/ddlogin.js',
    );
  }
  (window as any).DTFrameLogin(
    {
      id: 'dingding_qrcode_login_element',
      width: 300,
      height: 300,
    },
    {
      // 注意：redirect_uri 需为完整URL，扫码后钉钉会带code跳转到这里
      redirect_uri: encodeURIComponent(getRedirectUri()),
      client_id: clientId,
      scope: 'openid corpid',
      response_type: 'code',
      state: '1',
      prompt: 'consent',
      corpId,
    },
    (loginResult: any) => {
      const { redirectUrl } = loginResult;
      // 这里可以直接进行重定向
      window.location.href = redirectUrl;
    },
    (errorMsg: string) => {
      // 这里一般需要展示登录失败的具体原因
      alert(`Login Error: ${errorMsg}`);
    },
  );
};

const handleLogin = () => {
  const { clientId, corpId, isQrCode } = props;
  if (isQrCode) {
    // 内嵌二维码登录
    modalApi.open();
  } else {
    window.location.href = `https://login.dingtalk.com/oauth2/auth?redirect_uri=${encodeURIComponent(getRedirectUri())}&response_type=code&client_id=${clientId}&scope=openid&corpid=${corpId}&prompt=consent`;
  }
};
</script>

<template>
  <div>
    <VbenIconButton
      @click="handleLogin"
      :tooltip="$t('authentication.dingdingLogin')"
      tooltip-side="top"
    >
      <SvgDingDingIcon />
    </VbenIconButton>
    <Modal>
      <div id="dingding_qrcode_login_element"></div>
    </Modal>
  </div>
</template>

<style>
.dingding-qrcode-login-modal {
  .relative {
    padding: 0 !important;
  }
}
</style>
