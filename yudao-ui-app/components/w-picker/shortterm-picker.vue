<template>
	<view class="w-picker-view">
		<picker-view class="d-picker-view" :indicator-style="itemHeight" :value="pickVal" @change="handlerChange">
			<picker-view-column>
				<view class="w-picker-item" v-for="(item,index) in range.dates" :key="index">{{item.label}}</view>
			</picker-view-column>
			<picker-view-column>
				<view class="w-picker-item" v-for="(item,index) in range.hours" :key="index">{{item.label}}时</view>
			</picker-view-column>
			<picker-view-column>
				<view class="w-picker-item" v-for="(item,index) in range.minutes" :key="index">{{item.label}}分</view>
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
				type:[String,Array,Number],
				default:""
			},
			current:{//是否默认选中当前日期
				type:Boolean,
				default:false
			},
			expand:{
				type:[Number,String],
				default:30
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
				let strReg=/^\d{4}-\d{2}-\d{2} \d{2}:\d{2}(:\d{2})?$/,example="2019-12-12 18:05:00或者2019-12-12 18:05";
				if(!strReg.test(value)){
					console.log(new Error("请传入与mode、fields匹配的value值，例value="+example+""))
				}
				return strReg.test(value);
			},
			resetData(year,month,day){
				let curDate=this.getCurrenDate();
				let curFlag=this.current;
				let curYear=curDate.curYear;
				let curMonth=curDate.curMonth;
				let curDay=curDate.curDay;
				let curHour=curDate.curHour;
				let months=[],days=[],sections=[];
				let disabledAfter=this.disabledAfter;
				let monthsLen=disabledAfter?(year*1<curYear?12:curMonth):12;
				let totalDays=new Date(year,month,0).getDate();//计算当月有几天;
				for(let month=1;month<=monthsLen;month++){
					months.push(this.formatNum(month));
				};
				for(let day=1;day<=daysLen;day++){
					days.push(this.formatNum(day));
				}
				return{
					months,
					days,
					sections
				}
			},
			getData(dVal){
				//用来处理初始化数据
				let curFlag=this.current;
				let disabledAfter=this.disabledAfter;
				let dates=[],hours=[],minutes=[];
				let curDate=new Date();
				let curYear=curDate.getFullYear();
				let curMonth=curDate.getMonth();
				let curDay=curDate.getDate();
				let aDate=new Date(curYear,curMonth,curDay);
				for(let i=0;i<this.expand*1;i++){
					aDate=new Date(curYear,curMonth,curDay+i);
					let year=aDate.getFullYear();
					let month=aDate.getMonth()+1;
					let day=aDate.getDate();
					let label=year+"-"+this.formatNum(month)+"-"+this.formatNum(day);
					switch(i){
						case 0:
							label="今天";
							break;
						case 1:
							label="明天";
							break;
						case 2:
							label="后天";
							break
					}
					dates.push({
						label:label,
						value:year+"-"+this.formatNum(month)+"-"+this.formatNum(day)
					})
				};
				for(let i=0;i<24;i++){
					hours.push({
						label:this.formatNum(i),
						value:this.formatNum(i)
					})
				}
				for(let i=0;i<60;i++){
					minutes.push({
						label:this.formatNum(i),
						value:this.formatNum(i)
					})
				}
				return {
					dates,
					hours,
					minutes
				}
			},
			getDefaultDate(){
				let value=this.value;
				let reg=/-/g;
				let defaultDate=value?new Date(value.replace(reg,"/")):new Date();
				let defaultYear=defaultDate.getFullYear();
				let defaultMonth=defaultDate.getMonth()+1;
				let defaultDay=defaultDate.getDate();
				let defaultDays=new Date(defaultYear,defaultMonth,0).getDate()*1;
				return{
					defaultDate,
					defaultYear,
					defaultMonth,
					defaultDay,
					defaultDays
				}
			},
			getDval(){
				let value=this.value;
				let dVal=null;
				let aDate=new Date();
				let year=this.formatNum(aDate.getFullYear());
				let month=this.formatNum(aDate.getMonth()+1);
				let day=this.formatNum(aDate.getDate());
				let date=this.formatNum(year)+"-"+this.formatNum(month)+"-"+this.formatNum(day);
				let hour=aDate.getHours();
				let minute=aDate.getMinutes();
				if(value){
					let flag=this.checkValue(value);
					if(!flag){
						dVal=[date,hour,minute]
					}else{
						let v=value.split(" ");
						dVal=[v[0],...v[1].split(":")];
					}
				}else{
					dVal=[date,hour,minute]
				}
				return dVal;
			},
			initData(){
				let startDate,endDate,startYear,endYear,startMonth,endMonth,startDay,endDay;
				let dates=[],hours=[],minutes=[];
				let dVal=[],pickVal=[];
				let value=this.value;
				let reg=/-/g;
				let range={};
				let result="",full="",date,hour,minute,obj={};
				let defaultDate=this.getDefaultDate();
				let defaultYear=defaultDate.defaultYear;
				let defaultMonth=defaultDate.defaultMonth;
				let defaultDay=defaultDate.defaultDay;
				let defaultDays=defaultDate.defaultDays;
				let curFlag=this.current;
				let disabledAfter=this.disabledAfter;
				let dateData=[];
				dVal=this.getDval();
				dateData=this.getData(dVal);
				dates=dateData.dates;
				hours=dateData.hours;
				minutes=dateData.minutes;
				pickVal=[
					dates.findIndex(n => n.value == dVal[0])!=-1?dates.findIndex(n => n.value == dVal[0]):0,
					hours.findIndex(n => n.value == dVal[1])!=-1?hours.findIndex(n => n.value == dVal[1]):0,
					minutes.findIndex(n => n.value == dVal[2])!=-1?minutes.findIndex(n => n.value == dVal[2]):0,
				];
				range={dates,hours,minutes};
				date=dVal[0]?dVal[0]:dates[0].label;
				hour=dVal[1]?dVal[1]:hours[0].label;
				minute=dVal[2]?dVal[2]:minutes[0].label;
				result=full=`${date+' '+hour+':'+minute}`;
				obj={
					date,
					hour,
					minute
				}
				this.range=range;
				this.checkObj=obj;
				this.$nextTick(()=>{
					this.pickVal=pickVal;
				});
				this.$emit("change",{
					result:result,
					value:full,
					obj:obj
				})
			},
			handlerChange(e){
				let arr=[...e.detail.value];
				let data=this.range;
				let date="",hour="",minute="";
				let result="",full="",obj={};
				let disabledAfter=this.disabledAfter;
				date=(arr[0]||arr[0]==0)?data.dates[arr[0]]||data.dates[data.dates.length-1]:"";
				hour=(arr[1]||arr[1]==0)?data.hours[arr[1]]||data.hours[data.hours.length-1]:"";
				minute=(arr[2]||arr[2]==0)?data.minutes[arr[2]]||data.minutes[data.minutes.length-1]:"";
				result=full=`${date.label+' '+hour.label+':'+minute.label+':00'}`;
				obj={
					date,
					hour,
					minute
				}
				this.checkObj=obj;
				this.$emit("change",{
					result:result,
					value:full,
					obj:obj
				})
			}
		}
	}
</script>

<style lang="scss">
	@import "./w-picker.css";
</style>
