<!-- 绑定/更换手机号 changeMobile  -->
<template>
  <view>
    <!-- 标题栏 -->
    <view class="head-box ss-m-b-60">
      <view class="head-title ss-m-b-20">
        {{ userInfo.mobile ? '更换手机号' : '绑定手机号' }}
      </view>
      <view class="head-subtitle">为了您的账号安全，请使用本人手机号码</view>
    </view>

    <!-- 表单项 -->
    <uni-forms
      ref="changeMobileRef"
      v-model="state.model"
      :rules="state.rules"
      validateTrigger="bind"
      labelWidth="140"
      labelAlign="center"
    >
      <uni-forms-item name="mobile" label="手机号">
        <uni-easyinput
          placeholder="请输入手机号"
          v-model="state.model.mobile"
          :inputBorder="false"
          type="number"
        >
          <template v-slot:right>
            <button
              class="ss-reset-button code-btn-start"
              :disabled="state.isMobileEnd"
              :class="{ 'code-btn-end': state.isMobileEnd }"
              @tap="getSmsCode('changeMobile', state.model.mobile)"
            >
              {{ getSmsTimer('changeMobile') }}
            </button>
          </template>
        </uni-easyinput>
      </uni-forms-item>

      <uni-forms-item name="code" label="验证码">
        <uni-easyinput
          placeholder="请输入验证码"
          v-model="state.model.code"
          :inputBorder="false"
          type="number"
          maxlength="4"
        >
          <template v-slot:right>
            <button class="ss-reset-button login-btn-start" @tap="changeMobileSubmit">
              确认
            </button>
          </template>
        </uni-easyinput>
      </uni-forms-item>
    </uni-forms>

    <!-- 微信独有：读取手机号 -->
    <button
      v-if="'WechatMiniProgram' === sheep.$platform.name"
      class="ss-reset-button type-btn"
      open-type="getPhoneNumber"
      @getphonenumber="getPhoneNumber"
    >
      使用微信手机号
    </button>
  </view>
</template>

<script setup>
  import { computed, ref, reactive, unref } from 'vue';
  import sheep from '@/sheep';
  import { code, mobile } from '@/sheep/validate/form';
  import { closeAuthModal, getSmsCode, getSmsTimer } from '@/sheep/hooks/useModal';
  import UserApi from '@/sheep/api/member/user';

  const changeMobileRef = ref(null);
  const userInfo = computed(() => sheep.$store('user').userInfo);

  // 数据
  const state = reactive({
    isMobileEnd: false, // 手机号输入完毕
    model: {
      mobile: '', // 手机号
      code: '', // 验证码
    },
    rules: {
      code,
      mobile,
    },
  });

  // 绑定手机号
  async function changeMobileSubmit() {
    const validate = await unref(changeMobileRef)
      .validate()
      .catch((error) => {
        console.log('error: ', error);
      });
    if (!validate) {
      return;
    }
    // 提交更新请求
    const { code } = await UserApi.updateUserMobile(state.model);
    if (code !== 0) {
      return;
    }
    sheep.$store('user').getInfo();
    closeAuthModal();
  }

  // 使用微信手机号
  async function getPhoneNumber(e) {
    if (e.detail.errMsg !== 'getPhoneNumber:ok') {
      return;
    }
    const result = await sheep.$platform.useProvider().bindUserPhoneNumber(e.detail);
    if (result) {
      sheep.$store('user').getInfo();
      closeAuthModal();
    }
  }
</script>

<style lang="scss" scoped>
  @import '../index.scss';
</style>
