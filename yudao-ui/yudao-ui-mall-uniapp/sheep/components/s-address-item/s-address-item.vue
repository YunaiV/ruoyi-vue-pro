<!-- 地址卡片 -->
<template>
  <view
    class="address-item ss-flex ss-row-between ss-col-center"
    :class="[{ 'border-bottom': props.hasBorderBottom }]"
  >
    <view class="item-left" v-if="!isEmpty(props.item)">
      <view class="area-text ss-flex ss-col-center">
        <uni-tag
          class="ss-m-r-10"
          size="small"
          custom-style="background-color: var(--ui-BG-Main); border-color: var(--ui-BG-Main); color: #fff;"
          v-if="props.item.defaultStatus"
          text="默认"
        />
        {{ props.item.areaName }}
      </view>
      <view class="address-text">
        {{ props.item.detailAddress }}
      </view>
      <view class="person-text"> {{ props.item.name }} {{ props.item.mobile }} </view>
    </view>
    <view v-else>
      <view class="address-text ss-m-b-10">请选择收货地址</view>
    </view>
    <slot>
      <button class="ss-reset-button edit-btn" @tap.stop="onEdit">
        <view class="edit-icon ss-flex ss-row-center ss-col-center">
          <image :src="sheep.$url.static('/static/img/shop/user/address/edit.png')" />
        </view>
      </button>
    </slot>
  </view>
</template>

<script setup>
  /**
   * 基础组件 - 地址卡片
   *
   * @param {String}  icon = _icon-edit    - icon
   *
   * @event {Function()} click			 - 点击
   * @event {Function()} actionClick		 - 点击工具栏
   *
   * @slot 								 - 默认插槽
   */
  import sheep from '@/sheep';
  import { isEmpty } from 'lodash-es';
  const props = defineProps({
    item: {
      type: Object,
      default() {},
    },
    hasBorderBottom: {
      type: Boolean,
      defult: true,
    },
  });

  const onEdit = () => {
    sheep.$router.go('/pages/user/address/edit', {
      id: props.item.id,
    });
  };
</script>

<style lang="scss" scoped>
  .address-item {
    padding: 24rpx 30rpx;

    .item-left {
      width: 600rpx;
    }

    .area-text {
      font-size: 26rpx;
      font-weight: 400;
      color: $dark-9;
    }

    .address-text {
      font-size: 32rpx;
      font-weight: 500;
      color: #333333;
      line-height: 48rpx;
    }

    .person-text {
      font-size: 28rpx;
      font-weight: 400;
      color: $dark-9;
    }
  }

  .edit-btn {
    width: 44rpx;
    height: 44rpx;
    background: $gray-f;
    border-radius: 50%;

    .edit-icon {
      width: 24rpx;
      height: 24rpx;
    }
  }
  image {
    width: 100%;
    height: 100%;
  }
</style>
