<template>
	<view class="w-picker-view">
		<picker-view class="d-picker-view" :indicator-style="itemHeight" :value="pickVal" @change="handlerChange">
			<picker-view-column>
				<view class="w-picker-item" v-for="(item,index) in range.years" :key="index">{{item}}年</view>
			</picker-view-column>
			<picker-view-column>
				<view class="w-picker-item" v-for="(item,index) in range.months" :key="index">{{item}}月</view>
			</picker-view-column>
			<picker-view-column>
				<view class="w-picker-item" v-for="(item,index) in range.days" :key="index">{{item}}日</view>
			</picker-view-column>
			<picker-view-column>
				<view class="w-picker-item" v-for="(item,index) in range.sections" :key="index">{{item}}</view>
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
			startYear:{
				type:String,
				default:""
			},
			endYear:{
				type:String,
				default:""
			},
			value:{
				type:[String,Array,Number],
				default:""
			},
			current:{//是否默认选中当前日期
				type:Boolean,
				default:false
			},
			disabledAfter:{//是否禁用当前之后的日期
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
			formatNum(n){
				return (Number(n)<10?'0'+Number(n):Number(n)+'');
			},
			checkValue(value){
				let strReg=/^\d{4}-\d{2}-\d{2} [\u4e00-\u9fa5]{2}$/,example;
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
				let daysLen=disabledAfter?((year*1<curYear||month*1<curMonth)?totalDays:curDay):totalDays;
				let sectionFlag=disabledAfter?((year*1<curYear||month*1<curMonth||day*1<curDay)==true?false:true):(curHour>12==true?true:false);
				sections=["上午","下午"];
				for(let month=1;month<=monthsLen;month++){
					months.push(this.formatNum(month));
				};
				for(let day=1;day<=daysLen;day++){
					days.push(this.formatNum(day));
				}
				if(sectionFlag){
					sections=["上午"];
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
				let curDate=this.getCurrenDate();
				let curYear=curDate.curYear;
				let curMonthdays=curDate.curMonthdays;
				let curMonth=curDate.curMonth;
				let curDay=curDate.curDay;
				let curHour=curDate.curHour;
				let defaultDate=this.getDefaultDate();
				let startYear=this.getStartDate().getFullYear();
				let endYear=this.getEndDate().getFullYear();
				let years=[],months=[],days=[],sections=[];
				let year=dVal[0]*1;
				let month=dVal[1]*1;
				let day=dVal[2]*1;
				let monthsLen=disabledAfter?(year<curYear?12:curDate.curMonth):12;
				let daysLen=disabledAfter?((year<curYear||month<curMonth)?defaultDate.defaultDays:curDay):(curFlag?curMonthdays:defaultDate.defaultDays);
				let sectionFlag=disabledAfter?((year*1<curYear||month*1<curMonth||day*1<curDay)==true?false:true):(curHour>12==true?true:false);
				for(let year=startYear;year<=(disabledAfter?curYear:endYear);year++){
					years.push(year.toString())
				}
				for(let month=1;month<=monthsLen;month++){
					months.push(this.formatNum(month));
				}
				for(let day=1;day<=daysLen;day++){
					days.push(this.formatNum(day));
				}
				if(sectionFlag){
					sections=["下午"];
				}else{
					sections=["上午","下午"];
				}
				return {
					years,
					months,
					days,
					sections
				}
			},
			getCurrenDate(){
				let curDate=new Date();
				let curYear=curDate.getFullYear();
				let curMonth=curDate.getMonth()+1;
				let curMonthdays=new Date(curYear,curMonth,0).getDate();
				let curDay=curDate.getDate();
				let curHour=curDate.getHours();
				let curSection="上午";
				if(curHour>=12){
					curSection="下午";
				}
				return{
					curDate,
					curYear,
					curMonth,
					curMonthdays,
					curDay,
					curHour,
					curSection
				}
			},
			getDefaultDate(){
				let value=this.value;
				let reg=/-/g;
				let defaultDate=value?new Date(value.split(" ")[0].replace(reg,"/")):new Date();
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
			getStartDate(){
				let start=this.startYear;
				let startDate="";
				let reg=/-/g;
				if(start){
					startDate=new Date(start+"/01/01");
				}else{
					startDate=new Date("1970/01/01");
				}
				return startDate;
			},
			getEndDate(){
				let end=this.endYear;
				let reg=/-/g;
				let endDate="";
				if(end){
					endDate=new Date(end+"/12/31");
				}else{
					endDate=new Date();
				}
				return endDate;
			},
			getDval(){
				let value=this.value;
				let dVal=null;
				let aDate=new Date();
				let year=this.formatNum(aDate.getFullYear());
				let month=this.formatNum(aDate.getMonth()+1);
				let day=this.formatNum(aDate.getDate());
				let hour=aDate.getHours();
				let section="上午";
				if(hour>=12)section="下午";
				if(value){
					let flag=this.checkValue(value);
					if(!flag){
						dVal=[year,month,day,section]
					}else{
						let v=value.split(" ");
						dVal=[...v[0].split("-"),v[1]];
					}
				}else{
					dVal=[year,month,day,section]
				}
				return dVal;
			},
			initData(){
				let startDate,endDate,startYear,endYear,startMonth,endMonth,startDay,endDay;
				let years=[],months=[],days=[],sections=[];
				let dVal=[],pickVal=[];
				let value=this.value;
				let reg=/-/g;
				let range={};
				let result="",full="",year,month,day,section,obj={};
				let defaultDate=this.getDefaultDate();
				let defaultYear=defaultDate.defaultYear;
				let defaultMonth=defaultDate.defaultMonth;
				let defaultDay=defaultDate.defaultDay;
				let defaultDays=defaultDate.defaultDays;
				let curFlag=this.current;
				let disabledAfter=this.disabledAfter;
				let curDate=this.getCurrenDate();
				let curYear=curDate.curYear;
				let curMonth=curDate.curMonth;
				let curMonthdays=curDate.curMonthdays;
				let curDay=curDate.curDay;
				let curSection=curDate.curSection;
				let dateData=[];
				dVal=this.getDval();
				startDate=this.getStartDate();
				endDate=this.getEndDate();
				startYear=startDate.getFullYear();
				startMonth=startDate.getMonth();
				startDay=startDate.getDate();
				endYear=endDate.getFullYear();
				endMonth=endDate.getMonth();
				endDay=endDate.getDate();
				dateData=this.getData(dVal);
				years=dateData.years;
				months=dateData.months;
				days=dateData.days;
				sections=dateData.sections;
				pickVal=disabledAfter?[
					dVal[0]&&years.indexOf(dVal[0])!=-1?years.indexOf(dVal[0]):0,
					dVal[1]&&months.indexOf(dVal[1])!=-1?months.indexOf(dVal[1]):0,
					dVal[2]&&days.indexOf(dVal[2])!=-1?days.indexOf(dVal[2]):0,
					dVal[3]&&sections.indexOf(dVal[3])!=-1?sections.indexOf(dVal[3]):0
				]:(curFlag?[
					years.indexOf(curYear+''),
					months.indexOf(this.formatNum(curMonth)),
					days.indexOf(this.formatNum(curDay)),
					sections.indexOf(curSection),
				]:[
					dVal[0]&&years.indexOf(dVal[0])!=-1?years.indexOf(dVal[0]):0,
					dVal[1]&&months.indexOf(dVal[1])!=-1?months.indexOf(dVal[1]):0,
					dVal[2]&&days.indexOf(dVal[2])!=-1?days.indexOf(dVal[2]):0,
					dVal[3]&&sections.indexOf(dVal[3])!=-1?sections.indexOf(dVal[3]):0
				]);
				range={years,months,days,sections};
				year=dVal[0]?dVal[0]:years[0];
				month=dVal[1]?dVal[1]:months[0];
				day=dVal[2]?dVal[2]:days[0];
				section=dVal[3]?dVal[3]:sections[0];
				result=full=`${year+'-'+month+'-'+day+' '+section}`;
				obj={
					year,
					month,
					day,
					section
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
				let year="",month="",day="",section="";
				let result="",full="",obj={};
				let months=null,days=null,sections=null;
				let disabledAfter=this.disabledAfter;
				year=(arr[0]||arr[0]==0)?data.years[arr[0]]||data.years[data.years.length-1]:"";
				month=(arr[1]||arr[1]==0)?data.months[arr[1]]||data.months[data.months.length-1]:"";
				day=(arr[2]||arr[2]==0)?data.days[arr[2]]||data.days[data.days.length-1]:"";
				section=(arr[3]||arr[3]==0)?data.sections[arr[3]]||data.sections[data.sections.length-1]:"";
				result=full=`${year+'-'+month+'-'+day+' '+section}`;
				let resetData=this.resetData(year,month,day);
				if(this.disabledAfter){
					months=resetData.months;
					days=resetData.days;
					sections=resetData.sections;
				}else{
					if(year%4==0||(month!=this.checkObj.month)){
						days=resetData.days;
					}
				}
				if(months)this.range.months=months;
				if(days)this.range.days=days;
				if(sections)this.range.sections=sections;
				obj={
					year,
					month,
					day,
					section
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
