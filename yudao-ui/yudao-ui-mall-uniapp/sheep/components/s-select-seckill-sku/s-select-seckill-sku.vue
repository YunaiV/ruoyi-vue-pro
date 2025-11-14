<!-- 秒杀商品的 SKU 选择，和 s-select-sku.vue 类似 -->
<template>
  <!-- 规格弹窗 -->
  <su-popup :show="show" round="10" @close="emits('close')">
    <!-- SKU 信息 -->
    <view class="ss-modal-box bg-white ss-flex-col">
      <view class="modal-header ss-flex ss-col-center">
        <!-- 规格图片 -->
        <view class="header-left ss-m-r-30">
          <image
            class="sku-image"
            :src="sheep.$url.cdn(state.selectedSku.picUrl || state.goodsInfo.picUrl)"
            mode="aspectFill"
          >
          </image>
        </view>
        <view class="header-right ss-flex-col ss-row-between ss-flex-1">
          <!-- 名称 -->
          <view class="goods-title ss-line-2">{{ state.goodsInfo.name }}</view>
          <view class="header-right-bottom ss-flex ss-col-center ss-row-between">
            <!-- 价格 -->
            <view
              v-if="state.goodsInfo.activity_type === PromotionActivityTypeEnum.POINT.type"
              class="price-text ss-flex"
            >
              <image
                v-if="!isEmpty(state.selectedSku)"
                :src="sheep.$url.static('/static/img/shop/goods/score1.svg')"
                class="point-img"
              ></image>
              <text class="point-text ss-m-r-16">
                {{ getShowPriceText }}
              </text>
            </view>
            <view v-else class="price-text">
              ￥{{ fen2yuan(state.selectedSku.price || state.goodsInfo.price) }}
            </view>
            <!-- 秒杀价格标签 -->
            <view class="tig ss-flex ss-col-center">
              <view class="tig-icon ss-flex ss-col-center ss-row-center">
                <text class="cicon-alarm"></text>
              </view>
              <view class="tig-title">秒杀价</view>
            </view>
            <!-- 库存 -->
            <view class="stock-text ss-m-l-20">
              库存{{ state.selectedSku.stock || state.goodsInfo.stock }}件
            </view>
          </view>
        </view>
      </view>
      <view class="modal-content ss-flex-1">
        <scroll-view scroll-y="true" class="modal-content-scroll">
          <view class="sku-item ss-m-b-20" v-for="property in propertyList" :key="property.id">
            <view class="label-text ss-m-b-20">{{ property.name }}</view>
            <view class="ss-flex ss-col-center ss-flex-wrap">
              <button
                class="ss-reset-button spec-btn"
                v-for="value in property.values"
                :class="[
                  {
                    'checked-btn': state.currentPropertyArray[property.id] === value.id,
                  },
                  {
                    'disabled-btn': value.disabled === true,
                  },
                ]"
                :key="value.id"
                :disabled="value.disabled === true"
                @tap="onSelectSku(property.id, value.id)"
              >
                {{ value.name }}
              </button>
            </view>
          </view>
          <view class="buy-num-box ss-flex ss-col-center ss-row-between">
            <view class="label-text">购买数量</view>
            <su-number-box
              :min="1"
              :max="min([singleLimitCount, state.selectedSku.stock])"
              :step="1"
              v-model="state.selectedSku.count"
              @change="onBuyCountChange($event)"
              activity="seckill"
            ></su-number-box>
          </view>
        </scroll-view>
      </view>
      <view class="modal-footer">
        <view class="buy-box ss-flex ss-col-center ss-flex ss-col-center ss-row-center">
          <button class="ss-reset-button buy-btn" @tap="onBuy">确认</button>
        </view>
      </view>
    </view>
  </su-popup>
</template>

<script setup>
  /**
   * 秒杀活动SKU选择，
   * 与s-select-sku的区别：多一个秒杀价的标签、没有加入购物车按钮、立即购买按钮叫确认、秒杀有最大购买数量限制
   */
  // 按钮状态： active,nostock
  import { computed, reactive, watch } from 'vue';
  import sheep from '@/sheep';
  import { convertProductPropertyList, fen2yuan } from '@/sheep/hooks/useGoods';
  import { isEmpty, min } from 'lodash-es';
  import { PromotionActivityTypeEnum } from '@/sheep/helper/const';

  const emits = defineEmits(['change', 'addCart', 'buy', 'close']);
  const props = defineProps({
    modelValue: {
      type: Object,
      default() {},
    },
    show: {
      type: Boolean,
      default: false,
    },
    // 单次限购数量
    singleLimitCount: {
      type: Number,
      default: 1,
    },
  });
  const state = reactive({
    goodsInfo: computed(() => props.modelValue),
    selectedSku: {},
    currentPropertyArray: {},
  });
  const getShowPriceText = computed(() => {
    let priceText = `￥${fen2yuan(state.goodsInfo.price)}`;
    if (!isEmpty(state.selectedSku)) {
      const sku = state.selectedSku;
      priceText = `${sku.point}${!sku.pointPrice ? '' : `+￥${fen2yuan(sku.pointPrice)}`}`;
    }
    return priceText;
  });
  const propertyList = convertProductPropertyList(state.goodsInfo.skus);
  // SKU 列表
  const skuList = computed(() => {
    let skuPrices = state.goodsInfo.skus;
    for (let price of skuPrices) {
      price.value_id_array = price.properties.map((item) => item.valueId);
    }
    return skuPrices;
  });

  watch(
    () => state.selectedSku,
    (newVal) => {
      emits('change', newVal);
    },
    {
      immediate: true, // 立即执行
      deep: true, // 深度监听
    },
  );

  const onBuy = () => {
    if (state.selectedSku.id) {
      if (state.selectedSku.stock <= 0) {
        sheep.$helper.toast('库存不足');
      } else {
        emits('buy', state.selectedSku);
      }
    } else {
      sheep.$helper.toast('请选择规格');
    }
  };

  // 购买数量改变
  function onBuyCountChange(buyCount) {
    if (buyCount > 0 && state.selectedSku.count !== buyCount) {
      state.selectedSku.count = buyCount;
    }
  }

  // 改变禁用状态
  const changeDisabled = (isChecked = false, propertyId = 0, valueId = 0) => {
    let newSkus = []; // 所有可以选择的 sku 数组
    if (isChecked) {
      // 情况一：选中 property
      // 获得当前点击选中 property 的、所有可用 SKU
      for (let price of skuList.value) {
        if (price.stock <= 0) {
          continue;
        }
        if (price.value_id_array.indexOf(valueId) >= 0) {
          newSkus.push(price);
        }
      }
    } else {
      // 情况二：取消选中 property
      // 当前所选 property 下，所有可以选择的 SKU
      newSkus = getCanUseSkuList();
    }

    // 所有存在并且有库存未选择的 SKU 的 value 属性值 id
    let noChooseValueIds = [];
    for (let price of newSkus) {
      noChooseValueIds = noChooseValueIds.concat(price.value_id_array);
    }
    noChooseValueIds = Array.from(new Set(noChooseValueIds)); // 去重

    if (isChecked) {
      // 去除当前选中的 value 属性值 id
      let index = noChooseValueIds.indexOf(valueId);
      noChooseValueIds.splice(index, 1);
    } else {
      // 循环去除当前已选择的 value 属性值 id
      Object.entries(state.currentPropertyArray).forEach(([propertyId, currentPropertyId]) => {
        if (currentPropertyId.toString() !== '') {
          return;
        }
        // currentPropertyId 为空是反选 填充的
        let index = noChooseValueIds.indexOf(currentPropertyId);
        if (index >= 0) {
          // currentPropertyId 存在于 noChooseValueIds
          noChooseValueIds.splice(index, 1);
        }
      });
    }

    // 当前已选择的 property 数组
    let choosePropertyIds = [];
    if (!isChecked) {
      // 当前已选择的 property
      Object.entries(state.currentPropertyArray).forEach(([propertyId, currentValueId]) => {
        if (currentValueId !== '') {
          // currentPropertyId 为空是反选 填充的
          choosePropertyIds.push(currentValueId);
        }
      });
    } else {
      // 当前点击选择的 property
      choosePropertyIds = [propertyId];
    }

    for (let propertyIndex in propertyList) {
      // 当前点击的 property、或者取消选择时候，已选中的 property 不进行处理
      if (choosePropertyIds.indexOf(propertyList[propertyIndex]['id']) >= 0) {
        continue;
      }
      // 如果当前 property id 不存在于有库存的 SKU 中，则禁用
      for (let valueIndex in propertyList[propertyIndex]['values']) {
        propertyList[propertyIndex]['values'][valueIndex]['disabled'] =
          noChooseValueIds.indexOf(propertyList[propertyIndex]['values'][valueIndex]['id']) < 0; // true 禁用 or false 不禁用
      }
    }
  };

  // 获取可用的（有库存的）SKU 列表
  const getCanUseSkuList = () => {
    let newSkus = [];
    for (let sku of skuList.value) {
      if (sku.stock <= 0) {
        continue;
      }
      let isOk = true;
      Object.entries(state.currentPropertyArray).forEach(([propertyId, valueId]) => {
        // valueId 不为空，并且，这个 条 sku 没有被选中，则排除
        if (valueId.toString() !== '' && sku.value_id_array.indexOf(valueId) < 0) {
          isOk = false;
        }
      });
      if (isOk) {
        newSkus.push(sku);
      }
    }
    return newSkus;
  };

  // 选择规格
  const onSelectSku = (propertyId, valueId) => {
    // 清空已选择
    let isChecked = true; // 选中 or 取消选中
    if (
      state.currentPropertyArray[propertyId] !== undefined &&
      state.currentPropertyArray[propertyId] === valueId
    ) {
      // 点击已被选中的，删除并填充 ''
      isChecked = false;
      state.currentPropertyArray.splice(propertyId, 1, '');
    } else {
      // 选中
      state.currentPropertyArray[propertyId] = valueId;
    }

    // 选中的 property 大类
    let choosePropertyId = [];
    Object.entries(state.currentPropertyArray).forEach(([propertyId, currentPropertyId]) => {
      if (currentPropertyId !== '') {
        // currentPropertyId 为空是反选 填充的
        choosePropertyId.push(currentPropertyId);
      }
    });

    // 当前所选 property 下，所有可以选择的 SKU 们
    let newSkuList = getCanUseSkuList();

    // 判断所有 property 大类是否选择完成
    if (choosePropertyId.length === propertyList.length && newSkuList.length) {
      newSkuList[0].count = state.selectedSku.count || 1;
      state.selectedSku = newSkuList[0];
    } else {
      state.selectedSku = {};
    }

    // 改变 property 禁用状态
    changeDisabled(isChecked, propertyId, valueId);
  };

  changeDisabled(false);
</script>

<style lang="scss" scoped>
  // 购买
  .buy-box {
    padding: 10rpx 20rpx;

    .buy-btn {
      width: 100%;
      height: 80rpx;
      border-radius: 40rpx;
      background: linear-gradient(90deg, #ff5854, #ff2621);
      color: #fff;
    }
  }

  .point-img {
    width: 36rpx;
    height: 36rpx;
    margin: 0 4rpx;
  }

  .ss-modal-box {
    border-radius: 30rpx 30rpx 0 0;
    max-height: 1000rpx;

    .modal-header {
      position: relative;
      padding: 80rpx 20rpx 40rpx;

      .sku-image {
        width: 160rpx;
        height: 160rpx;
        border-radius: 10rpx;
      }

      .header-right {
        height: 160rpx;
      }

      .close-icon {
        position: absolute;
        top: 10rpx;
        right: 20rpx;
        font-size: 46rpx;
        opacity: 0.2;
      }

      .goods-title {
        font-size: 28rpx;
        font-weight: 500;
        line-height: 42rpx;
      }

      .price-text {
        font-size: 30rpx;
        font-weight: 500;
        color: $red;
        font-family: OPPOSANS;
      }

      .stock-text {
        font-size: 26rpx;
        color: #999999;
      }
    }

    .modal-content {
      padding: 0 20rpx;

      .modal-content-scroll {
        max-height: 600rpx;

        .label-text {
          font-size: 26rpx;
          font-weight: 500;
        }

        .buy-num-box {
          height: 100rpx;
        }

        .spec-btn {
          height: 60rpx;
          min-width: 100rpx;
          padding: 0 30rpx;
          background: #f4f4f4;
          border-radius: 30rpx;
          color: #434343;
          font-size: 26rpx;
          margin-right: 10rpx;
          margin-bottom: 10rpx;
        }

        .checked-btn {
          background: linear-gradient(90deg, #ff5854, #ff2621);
          font-weight: 500;
          color: #ffffff;
        }

        .disabled-btn {
          font-weight: 400;
          color: #c6c6c6;
          background: #f8f8f8;
        }
      }
    }
  }

  .tig {
    border: 2rpx solid #ff5854;
    border-radius: 4rpx;
    width: 126rpx;
    height: 38rpx;

    .tig-icon {
      width: 40rpx;
      height: 40rpx;
      background: #ff5854;
      border-radius: 4rpx 0 0 4rpx;

      .cicon-alarm {
        font-size: 32rpx;
        color: #fff;
      }
    }

    .tig-title {
      font-size: 24rpx;
      font-weight: 500;
      line-height: normal;
      color: #ff6000;
      width: 86rpx;
      display: flex;
      justify-content: center;
      align-items: center;
    }
  }
</style>
