<template>
	<view class="w-picker-view">
		<picker-view  class="d-picker-view" :indicator-style="itemHeight" :value="pickVal" @change="handlerChange">
			<picker-view-column class="w-picker-flex2">
				<view class="w-picker-item" v-for="(item,index) in range.fyears" :key="index">{{item}}年</view>
			</picker-view-column>
			<picker-view-column class="w-picker-flex2">
				<view class="w-picker-item" v-for="(item,index) in range.fmonths" :key="index">{{item}}月</view>
			</picker-view-column>
			<picker-view-column class="w-picker-flex2">
				<view class="w-picker-item" v-for="(item,index) in range.fdays" :key="index">{{item}}日</view>
			</picker-view-column>
			<picker-view-column class="w-picker-flex1">
				<view class="w-picker-item">-</view>
			</picker-view-column>
			<picker-view-column class="w-picker-flex2">
				<view class="w-picker-item" v-for="(item,index) in range.tyears" :key="index">{{item}}年</view>
			</picker-view-column>
			<picker-view-column class="w-picker-flex2">
				<view class="w-picker-item" v-for="(item,index) in range.tmonths" :key="index">{{item}}月</view>
			</picker-view-column>
			<picker-view-column class="w-picker-flex2">
				<view class="w-picker-item" v-for="(item,index) in range.tdays" :key="index">{{item}}日</view>
			</picker-view-column>
		</picker-view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				pickVal:[],
				range:{},
				checkObj:{}
			};
		},
		props:{
			itemHeight:{
				type:String,
				default:"44px"
			},
			value:{
				type:[String,Array],
				default(){
					return []
				}
			},
			current:{//是否默认选中当前日期
				type:Boolean,
				default:false
			},
			startYear:{
				type:[String,Number],
				default:1970
			},
			endYear:{
				type:[String,Number],
				default:new Date().getFullYear()
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
			formatNum(n){
				return (Number(n)<10?'0'+Number(n):Number(n)+'');
			},
			checkValue(value){
				let strReg=/^\d{4}-\d{2}-\d{2}$/,example="2020-04-03";
				if(!strReg.test(value[0])||!strReg.test(value[1])){
					console.log(new Error("请传入与mode匹配的value值，例["+example+","+example+"]"))
				}
				return strReg.test(value[0])&&strReg.test(value[1]);
			},
			resetToData(fmonth,fday,tyear,tmonth){
				let range=this.range;
				let tmonths=[],tdays=[];
				let yearFlag=tyear!=range.tyears[0];
				let monthFlag=tyear!=range.tyears[0]||tmonth!=range.tmonths[0];
				let ttotal=new Date(tyear,tmonth,0).getDate();
				for(let i=yearFlag?1:fmonth*1;i<=12;i++){
					tmonths.push(this.formatNum(i))
				}
				for(let i=monthFlag?1:fday*1;i<=ttotal;i++){
					tdays.push(this.formatNum(i))
				}
				return{
					tmonths,
					tdays
				}
			},
			resetData(fyear,fmonth,fday,tyear,tmonth){
				let fyears=[],fmonths=[],fdays=[],tyears=[],tmonths=[],tdays=[];
				let startYear=this.startYear;
				let endYear=this.endYear;
				let ftotal=new Date(fyear,fmonth,0).getDate();
				let ttotal=new Date(tyear,tmonth,0).getDate();
				for(let i=startYear*1;i<=endYear;i++){
					fyears.push(this.formatNum(i))
				}
				for(let i=1;i<=12;i++){
					fmonths.push(this.formatNum(i))
				}
				for(let i=1;i<=ftotal;i++){
					fdays.push(this.formatNum(i))
				}
				for(let i=fyear*1;i<=endYear;i++){
					tyears.push(this.formatNum(i))
				}
				for(let i=fmonth*1;i<=12;i++){
					tmonths.push(this.formatNum(i))
				}
				for(let i=fday*1;i<=ttotal;i++){
					tdays.push(this.formatNum(i))
				}
				return {
					fyears,
					fmonths,
					fdays,
					tyears,
					tmonths,
					tdays
				}
			},
			getData(dVal){
				let start=this.startYear*1;
				let end=this.endYear*1;
				let value=dVal;
				let flag=this.current;
				let aToday=new Date();
				let tYear,tMonth,tDay,tHours,tMinutes,tSeconds,pickVal=[];
				let initstartDate=new Date(start.toString());
				let endDate=new Date(end.toString());
				if(start>end){
					initstartDate=new Date(end.toString());
					endDate=new Date(start.toString());
				};
				let startYear=initstartDate.getFullYear();
				let startMonth=initstartDate.getMonth()+1;
				let endYear=endDate.getFullYear();
				let fyears=[],fmonths=[],fdays=[],tyears=[],tmonths=[],tdays=[],returnArr=[],startDVal=[],endDVal=[];
				let curMonth=flag?value[1]*1:(startDVal[1]*1+1);
				let curMonth1=flag?value[5][1]*1:(value[5]*1+1);
				let totalDays=new Date(value[0],value[1],0).getDate();
				let totalDays1=new Date(value[4],value[5],0).getDate();
				for(let s=startYear;s<=endYear;s++){
					fyears.push(this.formatNum(s));
				};
				for(let m=1;m<=12;m++){
					fmonths.push(this.formatNum(m));
				};
				for(let d=1;d<=totalDays;d++){
					fdays.push(this.formatNum(d));
				};
				for(let s=value[0]*1;s<=endYear;s++){
					tyears.push(this.formatNum(s));
				};
				
				if(value[4]*1>value[0]*1){
					for(let m=1;m<=12;m++){
						tmonths.push(this.formatNum(m));
					};
					for(let d=1;d<=totalDays1;d++){
						tdays.push(this.formatNum(d));
					};
				}else{
					for(let m=value[1]*1;m<=12;m++){
						tmonths.push(this.formatNum(m));
					};
					for(let d=value[2]*1;d<=totalDays1;d++){
						tdays.push(this.formatNum(d));
					};
				};
				
				pickVal=[
					fyears.indexOf(value[0])==-1?0:fyears.indexOf(value[0]),
					fmonths.indexOf(value[1])==-1?0:fmonths.indexOf(value[1]),
					fdays.indexOf(value[2])==-1?0:fdays.indexOf(value[2]),
					0,
					tyears.indexOf(value[4])==-1?0:tyears.indexOf(value[4]),
					tmonths.indexOf(value[5])==-1?0:tmonths.indexOf(value[5]),
					tdays.indexOf(value[6])==-1?0:tdays.indexOf(value[6])
				];
				return {
					fyears,
					fmonths,
					fdays,
					tyears,
					tmonths,
					tdays,
					pickVal
				}
			},
			getDval(){
				let value=this.value;
				let fields=this.fields;
				let dVal=null;
				let aDate=new Date();
				let fyear=this.formatNum(aDate.getFullYear());
				let fmonth=this.formatNum(aDate.getMonth()+1);
				let fday=this.formatNum(aDate.getDate());
				let tyear=this.formatNum(aDate.getFullYear());
				let tmonth=this.formatNum(aDate.getMonth()+1);
				let tday=this.formatNum(aDate.getDate());
				if(value&&value.length>0){
					let flag=this.checkValue(value);
					if(!flag){
						dVal=[fyear,fmonth,fday,"-",tyear,tmonth,tday]
					}else{
						dVal=[...value[0].split("-"),"-",...value[1].split("-")];
					}
				}else{
					dVal=[fyear,fmonth,fday,"-",tyear,tmonth,tday]
				}
				return dVal;
			},
			initData(){
				let range=[],pickVal=[];
				let result="",full="",obj={};
				let dVal=this.getDval();
				let dateData=this.getData(dVal);
				let fyears=[],fmonths=[],fdays=[],tyears=[],tmonths=[],tdays=[];
				let fyear,fmonth,fday,tyear,tmonth,tday;
				pickVal=dateData.pickVal;
				fyears=dateData.fyears;
				fmonths=dateData.fmonths;
				fdays=dateData.fdays;
				tyears=dateData.tyears;
				tmonths=dateData.tmonths;
				tdays=dateData.tdays;
				range={
					fyears,
					fmonths,
					fdays,
					tyears,
					tmonths,
					tdays,
				}
				fyear=range.fyears[pickVal[0]];
				fmonth=range.fmonths[pickVal[1]];
				fday=range.fdays[pickVal[2]];
				tyear=range.tyears[pickVal[4]];
				tmonth=range.tmonths[pickVal[5]];
				tday=range.tdays[pickVal[6]];
				obj={
					fyear,
					fmonth,
					fday,
					tyear,
					tmonth,
					tday
				}
				result=`${fyear+'-'+fmonth+'-'+fday+'至'+tyear+'-'+tmonth+'-'+tday}`;
				this.range=range;
				this.checkObj=obj;
				this.$nextTick(()=>{
					this.pickVal=pickVal;
				});
				this.$emit("change",{
					result:result,
					value:result.split("至"),
					obj:obj
				})
			},
			handlerChange(e){
				let arr=[...e.detail.value];
				let result="",full="",obj={};
				let year="",month="",day="",hour="",minute="",second="",note=[],province,city,area;
				let checkObj=this.checkObj;
				let days=[],months=[],endYears=[],endMonths=[],endDays=[],startDays=[];
				let mode=this.mode;
				let col1,col2,col3,d,a,h,m;
				let xDate=new Date().getTime();
				let range=this.range;
				let fyear=range.fyears[arr[0]]||range.fyears[range.fyears.length-1];
				let fmonth=range.fmonths[arr[1]]||range.fmonths[range.fmonths.length-1];
				let fday=range.fdays[arr[2]]||range.fdays[range.fdays.length-1];
				let tyear=range.tyears[arr[4]]||range.tyears[range.tyears.length-1];
				let tmonth=range.tmonths[arr[5]]||range.tmonths[range.tmonths.length-1];
				let tday=range.tdays[arr[6]]||range.tdays[range.tdays.length-1];
				let resetData=this.resetData(fyear,fmonth,fday,tyear,tmonth);
				if(fyear!=checkObj.fyear||fmonth!=checkObj.fmonth||fday!=checkObj.fday){
					arr[4]=0;
					arr[5]=0;
					arr[6]=0;
					range.tyears=resetData.tyears;
					range.tmonths=resetData.tmonths;
					range.tdays=resetData.tdays;
					tyear=range.tyears[0];
					checkObj.tyears=range.tyears[0];
					tmonth=range.tmonths[0];
					checkObj.tmonths=range.tmonths[0];
					tday=range.tdays[0];
					checkObj.tdays=range.tdays[0];
				}
				if(fyear!=checkObj.fyear||fmonth!=checkObj.fmonth){
					range.fdays=resetData.fdays;
				};
				if(tyear!=checkObj.tyear){
					arr[5]=0;
					arr[6]=0;
					let toData=this.resetToData(fmonth,fday,tyear,tmonth);
					range.tmonths=toData.tmonths;
					range.tdays=toData.tdays;
					tmonth=range.tmonths[0];
					checkObj.tmonths=range.tmonths[0];
					tday=range.tdays[0];
					checkObj.tdays=range.tdays[0];
				};
				if(tmonth!=checkObj.tmonth){
					arr[6]=0;
					let toData=this.resetToData(fmonth,fday,tyear,tmonth);
					range.tdays=toData.tdays;
					tday=range.tdays[0];
					checkObj.tdays=range.tdays[0];
				};
				result=`${fyear+'-'+fmonth+'-'+fday+'至'+tyear+'-'+tmonth+'-'+tday}`;
				obj={
					fyear,fmonth,fday,tyear,tmonth,tday
				}
				this.checkObj=obj;
				this.$nextTick(()=>{
					this.pickVal=arr;
				})
				this.$emit("change",{
					result:result,
					value:result.split("至"),
					obj:obj
				})
				
			}
		}
	}
</script>

<style lang="scss">
	@import "./w-picker.css";
</style>
