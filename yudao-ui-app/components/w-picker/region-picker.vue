<template>
	<view class="w-picker-view">
		<picker-view class="d-picker-view" :indicator-style="itemHeight" :value="pickVal" @change="handlerChange">
			<picker-view-column>
				<view class="w-picker-item" v-for="(item,index) in range.provinces" :key="index">{{item.label}}</view>
			</picker-view-column>
			<picker-view-column>
				<view class="w-picker-item" v-for="(item,index) in range.citys" :key="index">{{item.label}}</view>
			</picker-view-column>
			<picker-view-column v-if="!hideArea">
				<view class="w-picker-item" v-for="(item,index) in range.areas" :key="index">{{item.label}}</view>
			</picker-view-column>
		</picker-view>
	</view>
</template>

<script>
	import areaData from "./areadata/areadata.js"
	export default {
		data() {
			return {
				pickVal:[],
				range:{
					provinces:[],
					citys:[],
					areas:[]
				},
				checkObj:{}
			};
		},
		props:{
			itemHeight:{
				type:String,
				default:"44px"
			},
			value:{
				type:[Array,String],
				default:""
			},
			defaultType:{
				type:String,
				default:"label"
			},
			hideArea:{
				type:Boolean,
				default:false
			}
		},
		watch:{
			value(val){
				this.initData();
			}
		},
		created() {
			this.initData();
		},
		methods:{
			getData(){
				//用来处理初始化数据
				let provinces=areaData;
				let dVal=[];
				let value=this.value;
				let a1=value[0];//默认值省
				let a2=value[1];//默认值市
				let a3=value[2];//默认值区、县
				let province,city,area;
				let provinceIndex=provinces.findIndex((v)=>{
					return v[this.defaultType]==a1
				});
				provinceIndex=value?(provinceIndex!=-1?provinceIndex:0):0;
				let citys=provinces[provinceIndex].children;
				let cityIndex=citys.findIndex((v)=>{
					return v[this.defaultType]==a2
				});
				cityIndex=value?(cityIndex!=-1?cityIndex:0):0;
				let areas=citys[cityIndex].children;
				let areaIndex=areas.findIndex((v)=>{
					return v[this.defaultType]==a3;
				});
				areaIndex=value?(areaIndex!=-1?areaIndex:0):0;
				dVal=this.hideArea?[provinceIndex,cityIndex]:[provinceIndex,cityIndex,areaIndex];
				province=provinces[provinceIndex];
				city=citys[cityIndex];
				area=areas[areaIndex];
				let obj=this.hideArea?{
					province,
					city
				}:{
					province,
					city,
					area
				}
				return this.hideArea?{
					provinces,
					citys,
					dVal,
					obj
				}:{
					provinces,
					citys,
					areas,
					dVal,
					obj
				}
			},
			initData(){
				let dataData=this.getData();
				let provinces=dataData.provinces;
				let citys=dataData.citys;
				let areas=this.hideArea?[]:dataData.areas;
				let obj=dataData.obj;
				let province=obj.province,city=obj.city,area=this.hideArea?{}:obj.area;
				let value=this.hideArea?[province.value,city.value]:[province.value,city.value,area.value];
				let result=this.hideArea?`${province.label+city.label}`:`${province.label+city.label+area.label}`;
				this.range=this.hideArea?{
					provinces,
					citys,
				}:{
					provinces,
					citys,
					areas
				};
				this.checkObj=obj;
				this.$nextTick(()=>{
					this.pickVal=dataData.dVal;
				});
				this.$emit("change",{
					result:result,
					value:value,
					obj:obj
				})
			},
			handlerChange(e){
				let arr=[...e.detail.value];
				let provinceIndex=arr[0],cityIndex=arr[1],areaIndex=this.hideArea?0:arr[2];
				let provinces=areaData;
				let citys=(provinces[provinceIndex]&&provinces[provinceIndex].children)||provinces[provinces.length-1].children||[];
				let areas=this.hideArea?[]:((citys[cityIndex]&&citys[cityIndex].children)||citys[citys.length-1].children||[]);
				let province=provinces[provinceIndex]||provinces[provinces.length-1],
				city=citys[cityIndex]||[citys.length-1],
				area=this.hideArea?{}:(areas[areaIndex]||[areas.length-1]);
				let obj=this.hideArea?{
					province,
					city
				}:{
					province,
					city,
					area
				}
				if(this.checkObj.province.label!=province.label){
					//当省更新的时候需要刷新市、区县的数据;
					this.range.citys=citys;
					if(!this.hideArea){
						this.range.areas=areas;
					}
					
				}
				if(this.checkObj.city.label!=city.label){
					//当市更新的时候需要刷新区县的数据;
					if(!this.hideArea){
						this.range.areas=areas;
					}
				}
				this.checkObj=obj;
				this.$nextTick(()=>{
					this.pickVal=arr;
				})
				let result=this.hideArea?`${province.label+city.label}`:`${province.label+city.label+area.label}`;
				let value=this.hideArea?[province.value,city.value]:[province.value,city.value,area.value];
				this.$emit("change",{
					result:result,
					value:value,
					obj:obj
				})
			}
		}
	}
</script>

<style lang="scss">
	@import "./w-picker.css";	
</style>

