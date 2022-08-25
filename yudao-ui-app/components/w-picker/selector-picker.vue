<template>
	<view class="w-picker-view">
		<picker-view class="d-picker-view" :indicator-style="itemHeight" :value="pickVal" @change="handlerChange">
			<picker-view-column>
				<view class="w-picker-item" v-for="(item,index) in range" :key="index">{{item[nodeKey]}}</view>
			</picker-view-column>
		</picker-view>
	</view>
</template>

<script>
	export default {
		props:{
			itemHeight:{
				type:String,
				default:"44px"
			},
			options:{
				type:[Array,Object],
				default(){
					return []
				}
			},
			value:{
				type:String,
				default:""
			},
			defaultType:{
				type:String,
				default:"label"
			},
			defaultProps:{
				type:Object,
				default(){
					return{
						label:"label",
						value:"value"
					}
				}
			}
		},
		data() {
			return {
				pickVal:[]
			};
		},
		computed:{
			nodeKey(){
				return this.defaultProps.label;
			},
			nodeValue(){
				return this.defaultProps.value;
			},
			range(){
				return this.options
			}
		},
		watch:{
			value(val){
				if(this.options.length!=0){
					this.initData();
				}
			},
			options(val){
				this.initData();
			}
		},
		created() {
			if(this.options.length!=0){
				this.initData();
			}
		},
		methods:{
			initData(){
				let dVal=this.value||"";
				let data=this.range;
				let pickVal=[0];
				let cur=null;
				let label="";
				let value,idx;
				if(this.defaultType==this.nodeValue){
					value=data.find((v)=>v[this.nodeValue]==dVal);
					idx=data.findIndex((v)=>v[this.nodeValue]==dVal);
				}else{
					value=data.find((v)=>v[this.nodeKey]==dVal);
					idx=data.findIndex((v)=>v[this.nodeKey]==dVal);
				}
				pickVal=[idx!=-1?idx:0];
				this.$nextTick(()=>{
					this.pickVal=pickVal;
				});
				if(this.defaultType==this.nodeValue){
					this.$emit("change",{
						result:value?value[this.nodeKey]:data[0][this.nodeKey],
						value:dVal||data[0][this.nodeKey],
						obj:value?value:data[0]
					})
				}else{
					this.$emit("change",{
						result:dVal||data[0][this.nodeKey],
						value:value?value[this.nodeValue]:data[0][this.nodeValue],
						obj:value?value:data[0]
					})
				}
				
			},
			handlerChange(e){
				let arr=[...e.detail.value];
				let pickVal=[arr[0]||0];
				let data=this.range;
				let cur=data[arr[0]];
				let label="";
				let value="";
				this.$nextTick(()=>{
					this.pickVal=pickVal;
				});
				this.$emit("change",{
					result:cur[this.nodeKey],
					value:cur[this.nodeValue],
					obj:cur
				})
			}
		}
	}
</script>

<style lang="scss">
	@import "./w-picker.css";
</style>
