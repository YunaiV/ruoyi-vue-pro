<!-- 自定义底部导航项 -->
<template>
  <view class="u-tabbar-item" :style="[addStyle(customStyle)]">
    <view v-if="isCenter" class="tabbar-center-item">
      <image class="center-image" :src="centerImage" mode="aspectFill"></image>
    </view>
    <template v-else>
      <view class="u-tabbar-item__icon">
        <uni-badge
          absolute="rightTop"
          size="small"
          :text="badge || (dot ? 1 : null)"
          :customStyle="badgeStyle"
          :isDot="dot"
        >
          <image
            v-if="icon"
            :name="icon"
            :color="isActive ? parentData.activeColor : parentData.inactiveColor"
            :size="20"
          ></image>
          <block v-else>
            <slot v-if="isActive" name="active-icon" />
            <slot v-else name="inactive-icon" />
          </block>
        </uni-badge>
      </view>

      <slot name="text">
        <text
          class="u-tabbar-item__text"
          :style="{
            color: isActive ? parentData.activeColor : parentData.inactiveColor,
          }"
        >
          {{ text }}
        </text>
      </slot>
    </template>
  </view>
</template>

<script>
  /**
   * TabbarItem 底部导航栏子组件
   * @description 此组件提供了自定义tabbar的能力。
   * @property {String | Number}  name    item标签的名称，作为与u-tabbar的value参数匹配的标识符
   * @property {String}      icon    uView内置图标或者绝对路径的图片
   * @property {String | Number}  badge    右上角的角标提示信息
   * @property {Boolean}      dot      是否显示圆点，将会覆盖badge参数（默认 false ）
   * @property {String}      text    描述文本
   * @property {Object | String}  badgeStyle  控制徽标的位置，对象或者字符串形式，可以设置top和right属性（默认 'top: 6px;right:2px;' ）
   * @property {Object}      customStyle  定义需要用到的外部样式
   *
   */
  import { $parent, addStyle } from '@/sheep/helper';

  export default {
    name: 'su-tabbar-item',
    props: {
      customStyle: {
        type: [Object, String],
        default: () => ({}),
      },
      customClass: {
        type: String,
        default: '',
      },
      // 跳转的页面路径
      url: {
        type: String,
        default: '',
      },
      // 页面跳转的类型
      linkType: {
        type: String,
        default: 'navigateTo',
      },
      // item标签的名称，作为与u-tabbar的value参数匹配的标识符
      name: {
        type: [String, Number, null],
        default: '',
      },
      // uView内置图标或者绝对路径的图片
      icon: {
        icon: String,
        default: '',
      },
      // 右上角的角标提示信息
      badge: {
        type: [String, Number, null],
        default: '',
      },
      // 是否显示圆点，将会覆盖badge参数
      dot: {
        type: Boolean,
        default: false,
      },
      // 描述文本
      text: {
        type: String,
        default: '',
      },
      // 控制徽标的位置，对象或者字符串形式，可以设置top和right属性
      badgeStyle: {
        type: Object,
        default: ()=>{},
      },
      isCenter: {
        type: Boolean,
        default: false,
      },
      centerImage: {
        type: String,
        default: '',
      },
    },
    data() {
      return {
        isActive: false, // 是否处于激活状态
        addStyle,
        parentData: {
          value: null,
          activeColor: '', // 选中标签的颜色
          inactiveColor: '', // 未选中标签的颜色
        },
        parent: {},
      };
    },
    created() {
      this.init();
    },
    methods: {
      getParentData(parentName = '') {
        // 避免在created中去定义parent变量
        if (!this.parent) this.parent = {};
        // 这里的本质原理是，通过获取父组件实例(也即类似u-radio的父组件u-radio-group的this)
        // 将父组件this中对应的参数，赋值给本组件(u-radio的this)的parentData对象中对应的属性
        // 之所以需要这么做，是因为所有端中，头条小程序不支持通过this.parent.xxx去监听父组件参数的变化
        // 此处并不会自动更新子组件的数据，而是依赖父组件u-radio-group去监听data的变化，手动调用更新子组件的方法去重新获取
        this.parent = $parent.call(this, parentName);
        if (this.parent.children) {
          // 如果父组件的children不存在本组件的实例，才将本实例添加到父组件的children中
          this.parent.children.indexOf(this) === -1 && this.parent.children.push(this);
        }
        if (this.parent && this.parentData) {
          // 历遍parentData中的属性，将parent中的同名属性赋值给parentData
          Object.keys(this.parentData).map((key) => {
            this.parentData[key] = this.parent[key];
          });
        }
      },
      init() {
        // 支付宝小程序不支持provide/inject，所以使用这个方法获取整个父组件，在created定义，避免循环引用
        this.updateParentData();
        if (!this.parent) {
          console.log('u-tabbar-item必须搭配u-tabbar组件使用');
        }
        // 本子组件在u-tabbar的children数组中的索引
        const index = this.parent.children.indexOf(this);
        // 判断本组件的name(如果没有定义name，就用index索引)是否等于父组件的value参数
        this.isActive = (this.name.split('?')[0] || index) === this.parentData.value;
      },
      updateParentData() {
        // 此方法在mixin中
        this.getParentData('su-tabbar');
      },
      // 此方法将会被父组件u-tabbar调用
      updateFromParent() {
        // 重新初始化
        this.init();
      },
      clickHandler() {
        this.$nextTick(() => {
          const index = this.parent.children.indexOf(this);
          const name = this.name || index;
          // 点击的item为非激活的item才发出change事件
          if (name !== this.parent.value) {
            this.parent.$emit('change', name);
          }
          this.$emit('click', name);
        });
      },
    },
  };
</script>

<style lang="scss" scoped>
  .tabbar-center-item {
    height: 40px;
    width: 40px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background-color: rebeccapurple;
    transform: scale(1.3) translateY(-6px);
    position: absolute;
    z-index: 2;

    .center-image {
      width: 25px;
      height: 25px;
    }
  }

  .u-tabbar-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    flex: 1;
    position: relative;
    z-index: 1;

    &__icon {
      display: flex;
      position: relative;
      width: 150rpx;
      justify-content: center;
    }

    &__text {
      margin-top: 2px;
      font-size: 12px;
      color: var(--textSize);
    }
  }

  /* #ifdef MP */
  // 由于小程序都使用shadow DOM形式实现，需要给影子宿主设置flex: 1才能让其撑开
  :host {
    flex: 1;
  }

  /* #endif */
</style>
