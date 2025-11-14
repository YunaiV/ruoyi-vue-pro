<!-- 分销中心  -->
<template>
  <s-layout
    navbar="inner"
    class="index-wrap"
    title="分销中心"
    :bgStyle="bgStyle"
    :onShareAppMessage="shareInfo"
  >
    <!-- 分销商信息 -->
    <commission-info />
    <!-- 账户信息 -->
    <account-info />
    <!-- 菜单栏 -->
    <commission-menu />
    <!-- 分销记录 -->
    <commission-log />

    <!-- 权限弹窗 -->
    <commission-auth />
  </s-layout>
</template>

<script setup>
  import { computed } from 'vue';
  import commissionInfo from './components/commission-info.vue';
  import accountInfo from './components/account-info.vue';
  import commissionLog from './components/commission-log.vue';
  import commissionMenu from './components/commission-menu.vue';
  import commissionAuth from './components/commission-auth.vue';
  import sheep from '@/sheep';
  import { SharePageEnum } from '@/sheep/helper/const';

  /** 分销邀请 */
  const shareInfo = computed(() => {
    return sheep.$platform.share.getShareInfo(
      {
        params: {
          page: SharePageEnum.HOME.value, // 用户通邀请进入到首页
        },
      },
      {
        type: 'user',
      },
    );
  });

  const bgStyle = {
    color: '#F7D598',
  };
</script>

<style lang="scss" scoped>
  :deep(.page-main) {
    background-size: 100% 100% !important;
  }
</style>
