<template>
  <view class="ui-switch" :class="[{ disabled: props.disabled }, props.ui]">
    <view class="ui-switch-wrapper" @tap="change">
      <view
        class="ui-switch-input"
        :class="[
          { 'ui-switch-input-checked': props.modelValue },
          props.modelValue ? props.bg : '',
          props.text,
          props.size,
        ]"
      ></view>
    </view>
  </view>
</template>

<script>
  export default {
    name: 'UiSwitch',
  };
</script>
<script setup>
  const props = defineProps({
    modelValue: {
      type: [Boolean, Number],
      default: false,
    },
    ui: {
      type: String,
      default: '',
    },
    bg: {
      type: String,
      default: 'ui-BG-Main',
    },
    text: {
      type: String,
      default: '',
    },
    size: {
      type: String,
      default: 'sm',
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  });
  const emits = defineEmits(['update:modelValue']);

  const change = () => {
    emits('update:modelValue', !props.modelValue);
  };
</script>

<style lang="scss" scoped>
  .ui-switch {
    display: inline-block;
    cursor: pointer;
    .ui-switch-wrapper {
      display: inline-flex;
      align-items: center;
      vertical-align: middle;
    }
    .ui-switch-input {
      appearance: none;
      position: relative;
      width: 47px;
      height: 26px;
      outline: 0;
      border-radius: 16px;
      box-sizing: border-box;
      background-color: rgba(119, 119, 119, 0.3);
      transition: background-color 0.1s, border 0.1s;
      &:after {
        content: ' ';
        position: absolute;
        top: 0;
        left: 0;
        border-radius: 200px;
        transition: transform 0.3s;
        width: 20px;
        height: 20px;
        margin: 3px;
        background-color: #fff;
      }
      &.ui-switch-input-checked {
        &:after {
          transform: translateX(21px);
        }
      }
    }
    &.disabled {
      cursor: not-allowed;
      .ui-switch-input {
        opacity: 0.7;
      }
    }
  }
</style>
