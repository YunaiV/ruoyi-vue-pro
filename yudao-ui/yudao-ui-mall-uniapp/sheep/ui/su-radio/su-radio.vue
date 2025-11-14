<template>
  <view
    class="ui-radio ss-flex ss-col-center"
    @tap="onRaido"
    :class="[{ disabled: disabled }, { img: src }, ui]"
    :style="[customStyle]"
  >
    <slot name="leftLabel"></slot>
    <view
      v-if="!none"
      class="ui-radio-input"
      :class="[isChecked ? 'cur ' + bg : unbg, src ? 'radius' : 'round']"
    ></view>
    <image class="ui-radio-img radius" v-if="src" :src="src" mode="aspectFill"></image>
    <view class="ui-radio-content" v-else>
      <slot>
        <view class="ui-label-text" :style="[labelStyle]">{{ label }}</view>
      </slot>
    </view>
    <view
      v-if="ui.includes('card')"
      class="ui-radio-bg round"
      :class="[isChecked ? 'cur ' + bg : '']"
    ></view>
  </view>
</template>

<script setup>
  /**
   * 单选 - radio
   *
   *
   * property {Object} customStyle 												- 自定义样式
   * property {String} ui 														- radio样式Class
   * property {String} modelValue													- 绑定值
   * property {Boolean} disabled													- 是否禁用
   * property {String} bg															- 选中时背景Class
   * property {String} unbg														- 未选中时背景Class
   * property {String} src														- 图片选中radio
   * property {String} label														- label文本
   * property {Boolean} none														- 是否隐藏raido按钮
   *
   * @slot default																- 自定义label样式
   * @event {Function} change														- change事件
   *
   */
  import { computed, reactive, watchPostEffect, getCurrentInstance } from 'vue';
  const vm = getCurrentInstance();

  // 组件数据
  const state = reactive({
    currentValue: false,
  });

  // 定义事件
  const emits = defineEmits(['change', 'update:modelValue']);

  // 接收参数
  const props = defineProps({
    customStyle: {
      type: Object,
      default: () => ({}),
    },
    ui: {
      type: String,
      default: 'check', //check line
    },
    modelValue: {
      type: [String, Number, Boolean],
      default: false,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    bg: {
      type: String,
      default: 'ui-BG-Main',
    },
    unbg: {
      type: String,
      default: 'borderss',
    },
    src: {
      type: String,
      default: '',
    },
    label: {
      type: String,
      default: '',
    },
    labelStyle: {
      type: Object,
      default: () => ({}),
    },
    none: {
      type: Boolean,
      default: false,
    },
  });

  watchPostEffect(() => {
    state.currentValue = props.modelValue;
    emits('update:modelValue', state.currentValue);
  });

  // 是否选中
  const isChecked = computed(() => state.currentValue);

  // 点击
  const onRaido = () => {
    if (props.disabled) return;
    state.currentValue = !state.currentValue;
    emits('update:modelValue', state.currentValue);
    emits('change', {
      label: props.label,
      value: state.currentValue,
    });
  };
</script>

<style lang="scss" scoped>
  .ui-radio {
    display: flex;
    align-items: center;
    margin: 0 0.5em 0 0;
    height: 18px;
    .ui-radio-input {
      margin: 0 0.5em 0 0;
      display: inline-block;
      width: 18px;
      height: 18px;
      vertical-align: middle;
      line-height: 18px;

      &::before {
        content: '';
        position: absolute;
        width: 0;
        height: 0;
        background-color: currentColor;
        border-radius: 18px;
        @include position-center;
      }
    }

    .ui-radio-input.cur {
      position: relative;

      &::before {
        width: 10px;
        height: 10px;
        transition: $transition-base;
      }
    }

    &:last-child {
      margin: 0 0.14286em;
    }

    &.check {
      .ui-radio-input {
        &::before {
          font-family: 'colorui';
          content: '\e69f';
          width: 18px;
          height: 18px;
          font-size: 0;
          background-color: transparent;
        }
      }

      .ui-radio-input.cur {
        &::before {
          width: 18px;
          height: 18px;
          font-size: 1em;
          transform: scale(0.8);
          text-align: center;
          line-height: 18px;
        }
      }
    }

    &.line {
      .ui-radio-input.cur {
        &::before {
          width: calc(100% - 2px);
          height: calc(100% - 2px);
          background-color: var(--ui-BG);
        }

        &::after {
          content: '';
          position: absolute;
          width: 10px;
          height: 10px;
          background-color: inherit;
          border-radius: 50%;
          @include position-center;
        }
      }
    }

    &.lg {
      .ui-radio-input {
        font-size: 18px;
      }
    }

    &.img {
      position: relative;
      margin: 0 0.28572em 0;

      .ui-radio-input {
        width: 42px;
        height: 42px;
        border-radius: 0px;
        position: absolute;
        margin: 0;
        left: -1px;
        top: -1px;

        &::before {
          width: 40px;
          height: 40px;
          border-radius: $radius;
        }

        &.cur {
          width: 44px;
          height: 44px;
          top: -2px;
          left: -2px;
          border-radius: 7px !important;
          opacity: 0.8;
        }
      }

      .ui-radio-img {
        width: 40px;
        height: 40px;
        display: block;
        overflow: hidden;
        border-radius: 10px;
      }
    }

    &.card {
      display: flex;
      margin: 30rpx;
      padding: 30rpx;
      position: relative;
      border-radius: $radius !important;
      flex-direction: row-reverse;
      justify-content: space-between;

      .ui-radio-bg {
        content: '';
        position: absolute;
        width: 200%;
        height: 200%;
        transform: scale(0.5);
        border-radius: #{$radius * 2} !important;
        z-index: 0;
        left: 0;
        top: 0;
        transform-origin: 0 0;
        background-color: var(--ui-BG);
      }

      .ui-radio-input {
        position: relative;
        z-index: 1;
        margin-right: 0;
      }

      .ui-radio-bg::after {
        content: '';
        position: absolute;
        width: calc(200% - 16px);
        height: calc(200% - 16px);
        transform: scale(0.5);
        transform-origin: 0 0;
        background-color: var(--ui-BG) !important;
        left: 4px;
        top: 4px;
        border-radius: #{$radius * 2 + 8} !important;
        z-index: 0;
      }

      .ui-radio-content {
        position: relative;
        z-index: 1;
        display: flex;
        align-items: center;
        flex: 1;
      }
    }
  }
</style>
