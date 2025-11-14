<!-- 微信公众号的登录回调页 -->
<template>
  <!-- 空登陆页 -->
  <view />
</template>

<script setup>
  import sheep from '@/sheep';
  import { onLoad } from '@dcloudio/uni-app';

  onLoad(async (options) => {
    // #ifdef H5
    // 将 search 参数赋值到 options 中，方便下面解析
    new URLSearchParams(location.search).forEach((value, key) => {
      options[key] = value;
    });
    // 执行登录 or 绑定，注意需要 await 绑定
    const event = options.event;
    const code = options.code;
    const state = options.state;
    if (event === 'login') { // 场景一：登录
      await sheep.$platform.useProvider().login(code, state);
    } else if (event === 'bind') { // 场景二：绑定
      await sheep.$platform.useProvider().bind(code, state);
    }

    // 检测 H5 登录回调
    let returnUrl = uni.getStorageSync('returnUrl');
    if (returnUrl) {
      uni.removeStorage({key:'returnUrl'});
      location.replace(returnUrl);
    } else {
      uni.switchTab({
        url: '/',
      });
    }
    // #endif
  });
</script>
