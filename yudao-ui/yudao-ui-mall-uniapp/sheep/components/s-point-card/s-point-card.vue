<!-- 装修商品组件：【积分商城】商品卡片 -->
<template>
  <!-- 商品卡片 -->
  <view>
    <!-- 布局1. 单列大图（上图，下内容）-->
    <view
      v-if="state.property.layoutType === LayoutTypeEnum.ONE_COL_BIG_IMG && state.spuList.length"
      class="goods-sl-box"
    >
      <view
        class="goods-box"
        v-for="item in state.spuList"
        :key="item.id"
        :style="[{ marginBottom: state.property.space * 2 + 'rpx' }]"
      >
        <s-goods-column
          class=""
          size="sl"
          :goodsFields="state.property.fields"
          :tagStyle="state.property.badge"
          :data="item"
          :titleColor="state.property.fields.name?.color"
          :subTitleColor="state.property.fields.introduction.color"
          :topRadius="state.property.borderRadiusTop"
          :bottomRadius="state.property.borderRadiusBottom"
          @click="sheep.$router.go('/pages/goods/point', { id: item.activityId })"
        >
          <!-- 购买按钮 -->
          <template v-slot:cart>
            <button class="ss-reset-button cart-btn" :style="[buyStyle]">
              {{ state.property.btnBuy.type === 'text' ? state.property.btnBuy.text : '' }}
            </button>
          </template>
        </s-goods-column>
      </view>
    </view>

    <!-- 布局2. 单列小图（左图，右内容） -->
    <view
      v-if="state.property.layoutType === LayoutTypeEnum.ONE_COL_SMALL_IMG && state.spuList.length"
      class="goods-lg-box"
    >
      <view
        class="goods-box"
        :style="[{ marginBottom: state.property.space + 'px' }]"
        v-for="item in state.spuList"
        :key="item.id"
      >
        <s-goods-column
          class="goods-card"
          size="lg"
          :goodsFields="state.property.fields"
          :data="item"
          :tagStyle="state.property.badge"
          :titleColor="state.property.fields.name?.color"
          :subTitleColor="state.property.fields.introduction.color"
          :topRadius="state.property.borderRadiusTop"
          :bottomRadius="state.property.borderRadiusBottom"
          @tap="sheep.$router.go('/pages/goods/point', { id: item.activityId })"
        >
          <!-- 购买按钮 -->
          <template v-slot:cart>
            <button class="ss-reset-button cart-btn" :style="[buyStyle]">
              {{ state.property.btnBuy.type === 'text' ? state.property.btnBuy.text : '' }}
            </button>
          </template>
        </s-goods-column>
      </view>
    </view>

    <!-- 布局3. 双列（每一列：上图，下内容）-->
    <view
      v-if="state.property.layoutType === LayoutTypeEnum.TWO_COL && state.spuList.length"
      class="goods-md-wrap ss-flex ss-flex-wrap ss-col-top"
    >
      <view class="goods-list-box">
        <view
          class="left-list"
          :style="[
            {
              paddingRight: state.property.space + 'rpx',
              marginBottom: state.property.space + 'px',
            },
          ]"
          v-for="item in state.leftSpuList"
          :key="item.id"
        >
          <s-goods-column
            class="goods-md-box"
            size="md"
            :goodsFields="state.property.fields"
            :tagStyle="state.property.badge"
            :data="item"
            :titleColor="state.property.fields.name?.color"
            :subTitleColor="state.property.fields.introduction.color"
            :topRadius="state.property.borderRadiusTop"
            :bottomRadius="state.property.borderRadiusBottom"
            :titleWidth="330 - marginLeft - marginRight"
            @click="sheep.$router.go('/pages/goods/point', { id: item.activityId })"
            @getHeight="calculateGoodsColumn($event, 'left')"
          >
            <!-- 购买按钮 -->
            <template v-slot:cart>
              <button class="ss-reset-button cart-btn" :style="[buyStyle]">
                {{ state.property.btnBuy.type === 'text' ? state.property.btnBuy.text : '' }}
              </button>
            </template>
          </s-goods-column>
        </view>
      </view>
      <view class="goods-list-box">
        <view
          class="right-list"
          :style="[
            {
              paddingLeft: state.property.space + 'rpx',
              marginBottom: state.property.space + 'px',
            },
          ]"
          v-for="item in state.rightSpuList"
          :key="item.id"
        >
          <s-goods-column
            class="goods-md-box"
            size="md"
            :goodsFields="state.property.fields"
            :tagStyle="state.property.badge"
            :data="item"
            :titleColor="state.property.fields.name?.color"
            :subTitleColor="state.property.fields.introduction.color"
            :topRadius="state.property.borderRadiusTop"
            :bottomRadius="state.property.borderRadiusBottom"
            :titleWidth="330 - marginLeft - marginRight"
            @click="sheep.$router.go('/pages/goods/point', { id: item.activityId })"
            @getHeight="calculateGoodsColumn($event, 'right')"
          >
            <!-- 购买按钮 -->
            <template v-slot:cart>
              <button class="ss-reset-button cart-btn" :style="[buyStyle]">
                {{ state.property.btnBuy.type === 'text' ? state.property.btnBuy.text : '' }}
              </button>
            </template>
          </s-goods-column>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
  /**
   * 商品卡片
   */
  import { computed, reactive, watch } from 'vue';
  import sheep from '@/sheep';
  import SpuApi from '@/sheep/api/product/spu';
  import { PromotionActivityTypeEnum } from '@/sheep/helper/const';
  import { isEmpty } from '@/sheep/helper/utils';

  // 布局类型
  const LayoutTypeEnum = {
    // 单列大图
    ONE_COL_BIG_IMG: 'oneColBigImg',
    // 双列
    TWO_COL: 'twoCol',
    // 单列小图
    ONE_COL_SMALL_IMG: 'oneColSmallImg',
  };

  const state = reactive({
    spuList: [],
    leftSpuList: [],
    rightSpuList: [],
    property: {
      layoutType: 'oneColBigImg',
      fields: {
        name: {
          show: true,
          color: '#000',
        },
        introduction: {
          show: true,
          color: '#999',
        },
        price: {
          show: true,
          color: '#ff3000',
        },
        marketPrice: {
          show: true,
          color: '#c4c4c4',
        },
        salesCount: {
          show: true,
          color: '#c4c4c4',
        },
        stock: {
          show: true,
          color: '#c4c4c4',
        },
      },
      badge: {
        show: false,
        imgUrl: '',
      },
      btnBuy: {
        type: 'text',
        text: '立即兑换',
        bgBeginColor: '#FF6000',
        bgEndColor: '#FE832A',
        imgUrl: '',
      },
      borderRadiusTop: 8,
      borderRadiusBottom: 8,
      space: 8,
      style: {
        bgType: 'color',
        bgColor: '',
        marginLeft: 8,
        marginRight: 8,
        marginBottom: 8,
      },
    },
  });
  const props = defineProps({
    property: {
      type: Object,
      default: () => ({}),
    },
  });
  // 动态更新 property
  watch(
    () => props.property,
    (newVal) => {
      state.property = { ...state.property, ...newVal };
    },
    { immediate: true, deep: true },
  );
  const { marginLeft, marginRight } = state.property.styles || {};

  // 购买按钮样式
  const buyStyle = computed(() => {
    if (state.property.btnBuy.type === 'text') {
      // 文字按钮：线性渐变背景颜色
      return {
        background: `linear-gradient(to right, ${state.property.btnBuy.bgBeginColor}, ${state.property.btnBuy.bgEndColor})`,
      };
    }
    if (state.property.btnBuy.type === 'img') {
      // 图片按钮
      return {
        width: '54rpx',
        height: '54rpx',
        background: `url(${sheep.$url.cdn(state.property.btnBuy.imgUrl)}) no-repeat`,
        backgroundSize: '100% 100%',
      };
    }
  });

  //region 商品瀑布流布局
  // 下一个要处理的商品索引
  let count = 0;
  // 左列的高度
  let leftHeight = 0;
  // 右列的高度
  let rightHeight = 0;

  /**
   * 计算商品在左列还是右列
   * @param height 商品的高度
   * @param where 添加到哪一列
   */
  function calculateGoodsColumn(height = 0, where = 'left') {
    // 处理完
    if (!state.spuList[count]) return;
    // 增加列的高度
    if (where === 'left') leftHeight += height;
    if (where === 'right') rightHeight += height;
    // 添加到矮的一列
    if (leftHeight <= rightHeight) {
      state.leftSpuList.push(state.spuList[count]);
    } else {
      state.rightSpuList.push(state.spuList[count]);
    }
    // 计数
    count++;
  }

  //endregion

  /**
   * 根据商品编号，获取商品详情
   * @param ids 商品编号列表
   * @return {Promise<undefined>} 商品列表
   */
  async function getSpuDetail(ids) {
    const { data: spu } = await SpuApi.getSpuDetail(ids);
    return spu;
  }

  async function concatActivity(list) {
    if (isEmpty(list)) {
      return;
    }
    // 循环获取活动商品SPU详情并添加到spuList
    for (const activity of list) {
      state.spuList.push(await getSpuDetail(activity.spuId));
    }

    // 循环活动列表
    list.forEach((activity) => {
      // 查找对应的 spu 并更新价格
      const spu = state.spuList.find((spu) => activity.spuId === spu.id);
      if (spu) {
        spu.pointStock = activity.stock;
        spu.pointTotalStock = activity.totalStock;
        spu.point = activity.point;
        spu.pointPrice = activity.price;
        // 赋值活动ID，为了点击跳转详情页
        spu.activityId = activity.id;
        // 赋值活动类型
        spu.activityType = PromotionActivityTypeEnum.POINT.type;
      }
    });
    // 只有双列布局时需要
    if (state.property.layoutType === LayoutTypeEnum.TWO_COL) {
      // 分列
      calculateGoodsColumn();
    }
  }
  function getActivityCount() {
    return state.spuList.length;
  }
  defineExpose({ concatActivity, getActivityCount, calculateGoodsColumn });
</script>

<style lang="scss" scoped>
  .goods-md-wrap {
    width: 100%;
  }

  .goods-list-box {
    width: 50%;
    box-sizing: border-box;

    .left-list {
      &:nth-last-child(1) {
        margin-bottom: 0 !important;
      }
    }

    .right-list {
      &:nth-last-child(1) {
        margin-bottom: 0 !important;
      }
    }
  }

  .goods-box {
    &:nth-last-of-type(1) {
      margin-bottom: 0 !important;
    }
  }

  .goods-md-box,
  .goods-sl-box,
  .goods-lg-box {
    position: relative;

    .cart-btn {
      position: absolute;
      bottom: 18rpx;
      right: 20rpx;
      z-index: 11;
      height: 50rpx;
      line-height: 50rpx;
      padding: 0 20rpx;
      border-radius: 25rpx;
      font-size: 24rpx;
      color: #fff;
    }
  }
</style>
