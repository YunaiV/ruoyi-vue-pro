<template>
	<view :class="'interlayer '+(c||'')" :style="s">
		<block v-for="(n, i) in nodes" v-bind:key="i">
			<!--图片-->
			<view v-if="n.name=='img'" :class="'_img '+n.attrs.class" :style="n.attrs.style" :data-attrs="n.attrs" @tap="imgtap">
				<rich-text v-if="controls[i]!=0" :nodes="[{attrs:{src:loading&&(controls[i]||0)<2?loading:(lazyLoad&&!controls[i]?placeholder:(controls[i]==3?errorImg:n.attrs.src||'')),alt:n.attrs.alt||'',width:n.attrs.width||'',style:'-webkit-touch-callout:none;max-width:100%;display:block'+(n.attrs.height?';height:'+n.attrs.height:'')},name:'img'}]" />
				<image class="_image" :src="lazyLoad&&!controls[i]?placeholder:n.attrs.src" :lazy-load="lazyLoad"
				 :show-menu-by-longpress="!n.attrs.ignore" :data-i="i" :data-index="n.attrs.i" data-source="img" @load="loadImg"
				 @error="error" />
			</view>
			<!--文本-->
			<text v-else-if="n.type=='text'" decode>{{n.text}}</text>
			<!--#ifndef MP-BAIDU-->
			<text v-else-if="n.name=='br'">\n</text>
			<!--#endif-->
			<!--视频-->
			<view v-else-if="((n.lazyLoad&&!n.attrs.autoplay)||(n.name=='video'&&!loadVideo))&&controls[i]==undefined" :id="n.attrs.id" :class="'_video '+(n.attrs.class||'')"
			 :style="n.attrs.style" :data-i="i" @tap="_loadVideo" />
			<video v-else-if="n.name=='video'" :id="n.attrs.id" :class="n.attrs.class" :style="n.attrs.style" :autoplay="n.attrs.autoplay||controls[i]==0"
			 :controls="!n.attrs.autoplay||n.attrs.controls" :loop="n.attrs.loop" :muted="n.attrs.muted" :poster="n.attrs.poster" :src="n.attrs.source[controls[i]||0]"
			 :unit-id="n.attrs['unit-id']" :data-id="n.attrs.id" :data-i="i" data-source="video" @error="error" @play="play" />
			<!--音频-->
			<audio v-else-if="n.name=='audio'" :ref="n.attrs.id" :class="n.attrs.class" :style="n.attrs.style" :author="n.attrs.author"
			 :autoplay="n.attrs.autoplay" :controls="!n.attrs.autoplay||n.attrs.controls" :loop="n.attrs.loop" :name="n.attrs.name" :poster="n.attrs.poster"
			 :src="n.attrs.source[controls[i]||0]" :data-i="i" :data-id="n.attrs.id" data-source="audio"
			 @error.native="error" @play.native="play" />
			<!--链接-->
			<view v-else-if="n.name=='a'" :id="n.attrs.id" :class="'_a '+(n.attrs.class||'')" hover-class="_hover" :style="n.attrs.style"
			 :data-attrs="n.attrs" @tap="linkpress">
				<trees class="_span" c="_span" :nodes="n.children" />
			</view>
			<!--广告-->
			<!--<ad v-else-if="n.name=='ad'" :class="n.attrs.class" :style="n.attrs.style" :unit-id="n.attrs['unit-id']" :appid="n.attrs.appid" :apid="n.attrs.apid" :type="n.attrs.type" :adpid="n.attrs.adpid" data-source="ad" @error="error" />-->
			<!--列表-->
			<view v-else-if="n.name=='li'" :id="n.attrs.id" :class="n.attrs.class" :style="(n.attrs.style||'')+';display:flex'">
				<view v-if="n.type=='ol'" class="_ol-bef">{{n.num}}</view>
				<view v-else class="_ul-bef">
					<view v-if="n.floor%3==0" class="_ul-p1">█</view>
					<view v-else-if="n.floor%3==2" class="_ul-p2" />
					<view v-else class="_ul-p1" style="border-radius:50%">█</view>
				</view>
				<trees class="_li" c="_li" :nodes="n.children" :lazyLoad="lazyLoad" :loading="loading" />
			</view>
			<!--表格-->
			<view v-else-if="n.name=='table'&&n.c" :id="n.attrs.id" :class="n.attrs.class" :style="(n.attrs.style||'')+';display:table'">
				<view v-for="(tbody, o) in n.children" v-bind:key="o" :class="tbody.attrs.class" :style="(tbody.attrs.style||'')+(tbody.name[0]=='t'?';display:table-'+(tbody.name=='tr'?'row':'row-group'):'')">
					<view v-for="(tr, p) in tbody.children" v-bind:key="p" :class="tr.attrs.class" :style="(tr.attrs.style||'')+(tr.name[0]=='t'?';display:table-'+(tr.name=='tr'?'row':'cell'):'')">
						<trees v-if="tr.name=='td'" :nodes="tr.children" />
						<trees v-else v-for="(td, q) in tr.children" v-bind:key="q" :class="td.attrs.class" :c="td.attrs.class" :style="(td.attrs.style||'')+(td.name[0]=='t'?';display:table-'+(td.name=='tr'?'row':'cell'):'')"
						 :s="(td.attrs.style||'')+(td.name[0]=='t'?';display:table-'+(td.name=='tr'?'row':'cell'):'')" :nodes="td.children" />
					</view>
				</view>
			</view>
			<!--#ifdef APP-PLUS-->
			<iframe v-else-if="n.name=='iframe'" :style="n.attrs.style" :allowfullscreen="n.attrs.allowfullscreen" :frameborder="n.attrs.frameborder"
			 :width="n.attrs.width" :height="n.attrs.height" :src="n.attrs.src" />
			<embed v-else-if="n.name=='embed'" :style="n.attrs.style" :width="n.attrs.width" :height="n.attrs.height" :src="n.attrs.src" />
			<!--#endif-->
			<!--富文本-->
			<!--#ifdef MP-WEIXIN || MP-QQ || APP-PLUS-->
			<rich-text v-else-if="handler.use(n)" :id="n.attrs.id" :class="'_p __'+n.name" :nodes="[n]" />
			<!--#endif-->
			<!--#ifndef MP-WEIXIN || MP-QQ || APP-PLUS-->
			<rich-text v-else-if="!n.c" :id="n.attrs.id" :nodes="[n]" style="display:inline" />
			<!--#endif-->
			<trees v-else :class="(n.attrs.id||'')+' _'+n.name+' '+(n.attrs.class||'')" :c="(n.attrs.id||'')+' _'+n.name+' '+(n.attrs.class||'')"
			 :style="n.attrs.style" :s="n.attrs.style" :nodes="n.children" :lazyLoad="lazyLoad" :loading="loading" />
		</block>
	</view>
</template>
<script module="handler" lang="wxs" src="./handler.wxs"></script>
<script>
	global.Parser = {};
	import trees from './trees'
	const errorImg = require('../libs/config.js').errorImg;
	export default {
		components: {
			trees
		},
		name: 'trees',
		data() {
			return {
				controls: [],
				placeholder: 'data:image/svg+xml;utf8,<svg xmlns="http://www.w3.org/2000/svg" width="300" height="225"/>',
				errorImg,
				loadVideo: typeof plus == 'undefined',
				// #ifndef MP-ALIPAY
				c: '',
				s: ''
				// #endif
			}
		},
		props: {
			nodes: Array,
			lazyLoad: Boolean,
			loading: String,
			// #ifdef MP-ALIPAY
			c: String,
			s: String
			// #endif
		},
		mounted() {
			for (this.top = this.$parent; this.top.$options.name != 'parser'; this.top = this.top.$parent);
			this.init();
		},
		// #ifdef APP-PLUS
		beforeDestroy() {
			this.observer && this.observer.disconnect();
		},
		// #endif
		methods: {
			init() {
				for (var i = this.nodes.length, n; n = this.nodes[--i];) {
					if (n.name == 'img') {
						this.top.imgList.setItem(n.attrs.i, n.attrs.src);
						// #ifdef APP-PLUS
						if (this.lazyLoad && !this.observer) {
							this.observer = uni.createIntersectionObserver(this).relativeToViewport({
								top: 500,
								bottom: 500
							});
							setTimeout(() => {
								this.observer.observe('._img', res => {
									if (res.intersectionRatio) {
										for (var j = this.nodes.length; j--;)
											if (this.nodes[j].name == 'img')
												this.$set(this.controls, j, 1);
										this.observer.disconnect();
									}
								})
							}, 0)
						}
						// #endif
					} else if (n.name == 'video' || n.name == 'audio') {
						var ctx;
						if (n.name == 'video') {
							ctx = uni.createVideoContext(n.attrs.id
								// #ifndef MP-BAIDU
								, this
								// #endif
							);
						} else if (this.$refs[n.attrs.id])
							ctx = this.$refs[n.attrs.id][0];
						if (ctx) {
							ctx.id = n.attrs.id;
							this.top.videoContexts.push(ctx);
						}
					}
				}
				// #ifdef APP-PLUS
				// APP 上避免 video 错位需要延时渲染
				setTimeout(() => {
					this.loadVideo = true;
				}, 1000)
				// #endif
			},
			play(e) {
				var contexts = this.top.videoContexts;
				if (contexts.length > 1 && this.top.autopause)
					for (var i = contexts.length; i--;)
						if (contexts[i].id != e.currentTarget.dataset.id)
							contexts[i].pause();
			},
			imgtap(e) {
				var attrs = e.currentTarget.dataset.attrs;
				if (!attrs.ignore) {
					var preview = true,
						data = {
							id: e.target.id,
							src: attrs.src,
							ignore: () => preview = false
						};
					global.Parser.onImgtap && global.Parser.onImgtap(data);
					this.top.$emit('imgtap', data);
					if (preview) {
						var urls = this.top.imgList,
							current = urls[attrs.i] ? parseInt(attrs.i) : (urls = [attrs.src], 0);
						uni.previewImage({
							current,
							urls
						})
					}
				}
			},
			loadImg(e) {
				var i = e.currentTarget.dataset.i;
				if (this.lazyLoad && !this.controls[i]) {
					// #ifdef QUICKAPP-WEBVIEW
					this.$set(this.controls, i, 0);
					this.$nextTick(function() {
						// #endif
						// #ifndef APP-PLUS
						this.$set(this.controls, i, 1);
						// #endif
						// #ifdef QUICKAPP-WEBVIEW
					})
					// #endif
				} else if (this.loading && this.controls[i] != 2) {
					// #ifdef QUICKAPP-WEBVIEW
					this.$set(this.controls, i, 0);
					this.$nextTick(function() {
						// #endif
						this.$set(this.controls, i, 2);
						// #ifdef QUICKAPP-WEBVIEW
					})
					// #endif
				}
			},
			linkpress(e) {
				var jump = true,
					attrs = e.currentTarget.dataset.attrs;
				attrs.ignore = () => jump = false;
				global.Parser.onLinkpress && global.Parser.onLinkpress(attrs);
				this.top.$emit('linkpress', attrs);
				if (jump) {
					// #ifdef MP
					if (attrs['app-id']) {
						return uni.navigateToMiniProgram({
							appId: attrs['app-id'],
							path: attrs.path
						})
					}
					// #endif
					if (attrs.href) {
						if (attrs.href[0] == '#') {
							if (this.top.useAnchor)
								this.top.navigateTo({
									id: attrs.href.substring(1)
								})
						} else if (attrs.href.indexOf('http') == 0 || attrs.href.indexOf('//') == 0) {
							// #ifdef APP-PLUS
							plus.runtime.openWeb(attrs.href);
							// #endif
							// #ifndef APP-PLUS
							uni.setClipboardData({
								data: attrs.href,
								success: () =>
									uni.showToast({
										title: '链接已复制'
									})
							})
							// #endif
						} else
							uni.navigateTo({
								url: attrs.href,
								fail() {
									uni.switchTab({
										url: attrs.href,
									})
								}
							})
					}
				}
			},
			error(e) {
				var target = e.currentTarget,
					source = target.dataset.source,
					i = target.dataset.i;
				if (source == 'video' || source == 'audio') {
					// 加载其他 source
					var index = this.controls[i] ? this.controls[i].i + 1 : 1;
					if (index < this.nodes[i].attrs.source.length)
						this.$set(this.controls, i, index);
					if (e.detail.__args__)
						e.detail = e.detail.__args__[0];
				} else if (errorImg && source == 'img') {
					this.top.imgList.setItem(target.dataset.index, errorImg);
					this.$set(this.controls, i, 3);
				}
				this.top && this.top.$emit('error', {
					source,
					target,
					errMsg: e.detail.errMsg
				});
			},
			_loadVideo(e) {
				this.$set(this.controls, e.target.dataset.i, 0);
			}
		}
	}
</script>

<style>
	/* 在这里引入自定义样式 */

	/* 链接和图片效果 */
	._a {
		display: inline;
		padding: 1.5px 0 1.5px 0;
		color: #366092;
		word-break: break-all;
	}

	._hover {
		text-decoration: underline;
		opacity: 0.7;
	}

	._img {
		display: inline-block;
		max-width: 100%;
		overflow: hidden;
	}

	/* #ifdef MP-WEIXIN */
	:host {
		display: inline;
	}

	/* #endif */

	/* #ifndef MP-ALIPAY || APP-PLUS */
	.interlayer {
		display: inherit;
		flex-direction: inherit;
		flex-wrap: inherit;
		align-content: inherit;
		align-items: inherit;
		justify-content: inherit;
		width: 100%;
		white-space: inherit;
	}

	/* #endif */

	._b,
	._strong {
		font-weight: bold;
	}

	/* #ifndef MP-ALIPAY */
	._blockquote,
	._div,
	._p,
	._ol,
	._ul,
	._li {
		display: block;
	}
	
	/* #endif */

	._code {
		font-family: monospace;
	}

	._del {
		text-decoration: line-through;
	}

	._em,
	._i {
		font-style: italic;
	}

	._h1 {
		font-size: 2em;
	}

	._h2 {
		font-size: 1.5em;
	}

	._h3 {
		font-size: 1.17em;
	}

	._h5 {
		font-size: 0.83em;
	}

	._h6 {
		font-size: 0.67em;
	}

	._h1,
	._h2,
	._h3,
	._h4,
	._h5,
	._h6 {
		display: block;
		font-weight: bold;
	}

	._image {
		display: block;
		width: 100%;
		height: 360px;
		margin-top: -360px;
		opacity: 0;
	}

	._ins {
		text-decoration: underline;
	}

	._li {
		flex: 1;
		width: 0;
	}

	._ol-bef {
		width: 36px;
		margin-right: 5px;
		text-align: right;
	}

	._ul-bef {
		margin: 0 12px 0 23px;
		line-height: normal;
	}

	._ol-bef,
	._ul_bef {
		flex: none;
		user-select: none;
	}

	._ul-p1 {
		display: inline-block;
		width: 0.3em;
		height: 0.3em;
		overflow: hidden;
		line-height: 0.3em;
	}

	._ul-p2 {
		display: inline-block;
		width: 0.23em;
		height: 0.23em;
		border: 0.05em solid black;
		border-radius: 50%;
	}

	._q::before {
		content: '"';
	}

	._q::after {
		content: '"';
	}

	._sub {
		font-size: smaller;
		vertical-align: sub;
	}

	._sup {
		font-size: smaller;
		vertical-align: super;
	}

	/* #ifdef MP-ALIPAY || APP-PLUS || QUICKAPP-WEBVIEW */
	._abbr,
	._b,
	._code,
	._del,
	._em,
	._i,
	._ins,
	._label,
	._q,
	._span,
	._strong,
	._sub,
	._sup {
		display: inline;
	}

	/* #endif */

	/* #ifdef MP-WEIXIN || MP-QQ */
	.__bdo,
	.__bdi,
	.__ruby,
	.__rt {
		display: inline-block;
	}

	/* #endif */
	._video {
		position: relative;
		display: inline-block;
		width: 300px;
		height: 225px;
		background-color: black;
	}

	._video::after {
		position: absolute;
		top: 50%;
		left: 50%;
		margin: -15px 0 0 -15px;
		content: '';
		border-color: transparent transparent transparent white;
		border-style: solid;
		border-width: 15px 0 15px 30px;
	}
</style>
