<template>
    <view class="u-album">
        <view
            class="u-album__row"
            ref="u-album__row"
            v-for="(arr, index) in showUrls"
            :forComputedUse="albumWidth"
            :key="index"
        >
            <view
                class="u-album__row__wrapper"
                v-for="(item, index1) in arr"
                :key="index1"
                :style="[imageStyle(index + 1, index1 + 1)]"
                @tap="previewFullImage ? onPreviewTap(getSrc(item)) : ''"
            >
                <image
                    :src="getSrc(item)"
                    :mode="
                        urls.length === 1
                            ? imageHeight > 0
                                ? singleMode
                                : 'widthFix'
                            : multipleMode
                    "
                    :style="[
                        {
                            width: imageWidth,
                            height: imageHeight
                        }
                    ]"
                ></image>
                <view
                    v-if="
                        showMore &&
                        urls.length > rowCount * showUrls.length &&
                        index === showUrls.length - 1 &&
                        index1 === showUrls[showUrls.length - 1].length - 1
                    "
                    class="u-album__row__wrapper__text"
                >
                    <u--text
                        :text="`+${urls.length - maxCount}`"
                        color="#fff"
                        :size="multipleSize * 0.3"
                        align="center"
                        customStyle="justify-content: center"
                    ></u--text>
                </view>
            </view>
        </view>
    </view>
</template>

<script>
import props from './props.js'
// #ifdef APP-NVUE
// 由于weex为阿里的KPI业绩考核的产物，所以不支持百分比单位，这里需要通过dom查询组件的宽度
const dom = uni.requireNativePlugin('dom')
// #endif

/**
 * Album 相册
 * @description 本组件提供一个类似相册的功能，让开发者开发起来更加得心应手。减少重复的模板代码
 * @tutorial https://www.uviewui.com/components/album.html
 *
 * @property {Array}           urls             图片地址列表 Array<String>|Array<Object>形式
 * @property {String}          keyName          指定从数组的对象元素中读取哪个属性作为图片地址
 * @property {String | Number} singleSize       单图时，图片长边的长度  （默认 180 ）
 * @property {String | Number} multipleSize     多图时，图片边长 （默认 70 ）
 * @property {String | Number} space            多图时，图片水平和垂直之间的间隔 （默认 6 ）
 * @property {String}          singleMode       单图时，图片缩放裁剪的模式 （默认 'scaleToFill' ）
 * @property {String}          multipleMode     多图时，图片缩放裁剪的模式 （默认 'aspectFill' ）
 * @property {String | Number} maxCount         取消按钮的提示文字 （默认 9 ）
 * @property {Boolean}         previewFullImage 是否可以预览图片 （默认 true ）
 * @property {String | Number} rowCount         每行展示图片数量，如设置，singleSize和multipleSize将会无效	（默认 3 ）
 * @property {Boolean}         showMore         超出maxCount时是否显示查看更多的提示 （默认 true ）
 *
 * @event    {Function}        albumWidth       某些特殊的情况下，需要让文字与相册的宽度相等，这里事件的形式对外发送  （回调参数 width ）
 * @example <u-album :urls="urls2" @albumWidth="width => albumWidth = width" multipleSize="68" ></u-album>
 */
export default {
    name: 'u-album',
    mixins: [uni.$u.mpMixin, uni.$u.mixin, props],
    data() {
        return {
            // 单图的宽度
            singleWidth: 0,
            // 单图的高度
            singleHeight: 0,
            // 单图时，如果无法获取图片的尺寸信息，让图片宽度默认为容器的一定百分比
            singlePercent: 0.6
        }
    },
    watch: {
        urls: {
            immediate: true,
            handler(newVal) {
                if (newVal.length === 1) {
                    this.getImageRect()
                }
            }
        }
    },
    computed: {
        imageStyle() {
            return (index1, index2) => {
                const { space, rowCount, multipleSize, urls } = this,
                    { addUnit, addStyle } = uni.$u,
                    rowLen = this.showUrls.length,
                    allLen = this.urls.length
                const style = {
                    marginRight: addUnit(space),
                    marginBottom: addUnit(space)
                }
                // 如果为最后一行，则每个图片都无需下边框
                if (index1 === rowLen) style.marginBottom = 0
                // 每行的最右边一张和总长度的最后一张无需右边框
                if (
                    index2 === rowCount ||
                    (index1 === rowLen &&
                        index2 === this.showUrls[index1 - 1].length)
                )
                    style.marginRight = 0
                return style
            }
        },
        // 将数组划分为二维数组
        showUrls() {
            const arr = []
            this.urls.map((item, index) => {
                // 限制最大展示数量
                if (index + 1 <= this.maxCount) {
                    // 计算该元素为第几个素组内
                    const itemIndex = Math.floor(index / this.rowCount)
                    // 判断对应的索引是否存在
                    if (!arr[itemIndex]) {
                        arr[itemIndex] = []
                    }
                    arr[itemIndex].push(item)
                }
            })
            return arr
        },
        imageWidth() {
            return uni.$u.addUnit(
                this.urls.length === 1 ? this.singleWidth : this.multipleSize
            )
        },
        imageHeight() {
            return uni.$u.addUnit(
                this.urls.length === 1 ? this.singleHeight : this.multipleSize
            )
        },
        // 此变量无实际用途，仅仅是为了利用computed特性，让其在urls长度等变化时，重新计算图片的宽度
        // 因为用户在某些特殊的情况下，需要让文字与相册的宽度相等，所以这里事件的形式对外发送
        albumWidth() {
            let width = 0
            if (this.urls.length === 1) {
                width = this.singleWidth
            } else {
                width =
                    this.showUrls[0].length * this.multipleSize +
                    this.space * (this.showUrls[0].length - 1)
            }
            this.$emit('albumWidth', width)
            return width
        }
    },
    methods: {
        // 预览图片
        onPreviewTap(url) {
            const urls = this.urls.map((item) => {
                return this.getSrc(item)
            })
            uni.previewImage({
                current: url,
                urls
            })
        },
        // 获取图片的路径
        getSrc(item) {
            return uni.$u.test.object(item)
                ? (this.keyName && item[this.keyName]) || item.src
                : item
        },
        // 单图时，获取图片的尺寸
        // 在小程序中，需要将网络图片的的域名添加到小程序的download域名才可能获取尺寸
        // 在没有添加的情况下，让单图宽度默认为盒子的一定宽度(singlePercent)
        getImageRect() {
            const src = this.getSrc(this.urls[0])
            uni.getImageInfo({
                src,
                success: (res) => {
                    // 判断图片横向还是竖向展示方式
                    const isHorizotal = res.width >= res.height
                    this.singleWidth = isHorizotal
                        ? this.singleSize
                        : (res.width / res.height) * this.singleSize
                    this.singleHeight = !isHorizotal
                        ? this.singleSize
                        : (res.height / res.width) * this.singleWidth
                },
                fail: () => {
                    this.getComponentWidth()
                }
            })
        },
        // 获取组件的宽度
        async getComponentWidth() {
            // 延时一定时间，以获取dom尺寸
            await uni.$u.sleep(30)
            // #ifndef APP-NVUE
            this.$uGetRect('.u-album__row').then((size) => {
                this.singleWidth = size.width * this.singlePercent
            })
            // #endif

            // #ifdef APP-NVUE
            // 这里ref="u-album__row"所在的标签为通过for循环出来，导致this.$refs['u-album__row']是一个数组
            const ref = this.$refs['u-album__row'][0]
            ref &&
                dom.getComponentRect(ref, (res) => {
                    this.singleWidth = res.size.width * this.singlePercent
                })
            // #endif
        }
    }
}
</script>

<style lang="scss" scoped>
@import '../../libs/css/components.scss';

.u-album {
    @include flex(column);

    &__row {
        @include flex(row);
        flex-wrap: wrap;

        &__wrapper {
            position: relative;

            &__text {
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                bottom: 0;
                background-color: rgba(0, 0, 0, 0.3);
                @include flex(row);
                justify-content: center;
                align-items: center;
            }
        }
    }
}
</style>