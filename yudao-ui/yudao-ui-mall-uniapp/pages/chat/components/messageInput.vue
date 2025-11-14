<template>
  <view class="send-wrap ss-flex">
    <view class="left ss-flex ss-flex-1">
      <optimize-input
        class="ss-flex-1 ss-p-l-22"
        :inputBorder="false"
        :clearable="false"
        v-model="message"
        placeholder="请输入你要咨询的问题"
      ></optimize-input>
    </view>
    <text class="sicon-basic bq" @tap.stop="onTools('emoji')"></text>
    <text
      v-if="!message"
      class="sicon-edit"
      :class="{ 'is-active': toolsMode === 'tools' }"
      @tap.stop="onTools('tools')"
    />
    <button
      v-if="message"
      class="ss-reset-button send-btn"
      @tap="sendMessage"
      :disabled="isDisabled || sending"
      :class="{ disabled: isDisabled || sending }"
    >
      <text v-if="sending">发送中</text>
      <text v-else>发送</text>
    </button>
  </view>
</template>

<script setup>
  import { computed, ref, onUnmounted } from 'vue';
  import OptimizeInput from '@/pages/chat/components/optimize-input.vue';
  /**
   * 消息发送组件
   */
  const props = defineProps({
    // 消息
    modelValue: {
      type: String,
      default: '',
    },
    // 工具模式
    toolsMode: {
      type: String,
      default: '',
    },
  });
  const emits = defineEmits(['update:modelValue', 'onTools', 'sendMessage']);
  const message = computed({
    get() {
      return props.modelValue;
    },
    set(newValue) {
      emits(`update:modelValue`, newValue);
    }
  });


  // 打开工具菜单
  function onTools(mode) {
    emits('onTools', mode);
  }

  // 发送消息
  function sendMessage() {
    emits('sendMessage');
  }
</script>

<style scoped lang="scss">
  .send-wrap {
    padding: 18rpx 20rpx;
    background: #fff;

    .left {
      height: 64rpx;
      border-radius: 32rpx;
      background: var(--ui-BG-1);
    }

    .bq {
      font-size: 50rpx;
      margin-left: 10rpx;
    }

    .sicon-edit {
      font-size: 50rpx;
      margin-left: 10rpx;
      transform: rotate(0deg);
      transition: all linear 0.2s;

      &.is-active {
        transform: rotate(45deg);
      }
    }

    .send-btn {
      width: 100rpx;
      height: 60rpx;
      line-height: 60rpx;
      border-radius: 30rpx;
      background: linear-gradient(90deg, var(--ui-BG-Main), var(--ui-BG-Main-gradient));
      font-size: 26rpx;
      color: #fff;
      margin-left: 11rpx;
    }
  }
</style>
