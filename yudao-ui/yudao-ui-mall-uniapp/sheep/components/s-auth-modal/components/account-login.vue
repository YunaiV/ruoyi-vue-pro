<!-- 账号密码登录 accountLogin  -->
<template>
  <view>
    <!-- 标题栏 -->
    <view class="head-box ss-m-b-60 ss-flex-col">
      <view class="ss-flex ss-m-b-20">
        <view class="head-title-active head-title-line" @tap="showAuthModal('smsLogin')">
          短信登录
        </view>
        <view class="head-title ss-m-r-40 head-title-animation">账号登录</view>
      </view>
      <view class="head-subtitle">如果未设置过密码，请点击忘记密码</view>
    </view>

    <!-- 表单项 -->
    <uni-forms
      ref="accountLoginRef"
      v-model="state.model"
      :rules="state.rules"
      validateTrigger="bind"
      labelWidth="140"
      labelAlign="center"
    >
      <uni-forms-item name="mobile" label="账号">
        <uni-easyinput placeholder="请输入账号" v-model="state.model.mobile" :inputBorder="false">
          <template v-slot:right>
            <button class="ss-reset-button forgot-btn" @tap="showAuthModal('resetPassword')">
              忘记密码
            </button>
          </template>
        </uni-easyinput>
      </uni-forms-item>

      <uni-forms-item name="password" label="密码">
        <uni-easyinput
          type="password"
          placeholder="请输入密码"
          v-model="state.model.password"
          :inputBorder="false"
        >
          <template v-slot:right>
            <button class="ss-reset-button login-btn-start" @tap="accountLoginSubmit">登录</button>
          </template>
        </uni-easyinput>
      </uni-forms-item>
    </uni-forms>
  </view>
</template>

<script setup>
  import { ref, reactive, unref } from 'vue';
  import sheep from '@/sheep';
  import { mobile, password } from '@/sheep/validate/form';
  import { showAuthModal, closeAuthModal } from '@/sheep/hooks/useModal';
  import AuthUtil from '@/sheep/api/member/auth';

  const accountLoginRef = ref(null);

  const emits = defineEmits(['onConfirm']);

  const props = defineProps({
    agreeStatus: {
      type: [Boolean, null],
      default: null,
    },
  });

  // 数据
  const state = reactive({
    model: {
      mobile: '', // 账号
      password: '', // 密码
    },
    rules: {
      mobile,
      password,
    },
  });

  // 账号登录
  async function accountLoginSubmit() {
    // 表单验证
    const validate = await unref(accountLoginRef)
      .validate()
      .catch((error) => {
        console.log('error: ', error);
      });
    if (!validate) return;

    // 检查协议状态
    if (props.agreeStatus !== true) {
      emits('onConfirm', true);
      if (props.agreeStatus === false) {
        sheep.$helper.toast('您已拒绝协议，无法继续登录');
      } else {
        sheep.$helper.toast('请选择是否同意协议');
      }
      return;
    }

    // 提交数据
    const { code, data } = await AuthUtil.login(state.model);
    if (code === 0) {
      closeAuthModal();
    }
  }
</script>

<style lang="scss" scoped>
  @import '../index.scss';
</style>
