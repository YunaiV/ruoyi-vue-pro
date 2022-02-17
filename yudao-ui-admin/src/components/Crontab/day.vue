<template>
	<el-form size="small">
		<el-form-item>
			<el-radio v-model='radioValue' :label="1">
				日，允许的通配符[, - * ? / L W]
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="2">
				不指定
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="3">
				周期从
				<el-input-number v-model='cycle01' :min="1" :max="30" /> -
				<el-input-number v-model='cycle02' :min="cycle01 ? cycle01 + 1 : 2" :max="31" /> 日
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="4">
				从
				<el-input-number v-model='average01' :min="1" :max="30" /> 号开始，每
				<el-input-number v-model='average02' :min="1" :max="31 - average01 || 1" /> 日执行一次
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="5">
				每月
				<el-input-number v-model='workday' :min="1" :max="31" /> 号最近的那个工作日
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="6">
				本月最后一天
			</el-radio>
		</el-form-item>

		<el-form-item>
			<el-radio v-model='radioValue' :label="7">
				指定
				<el-select clearable v-model="checkboxList" placeholder="可多选" multiple style="width:100%">
					<el-option v-for="item in 31" :key="item" :value="item">{{item}}</el-option>
				</el-select>
			</el-radio>
		</el-form-item>
	</el-form>
</template>

<script>
export default {
	data() {
		return {
			radioValue: 1,
			workday: 1,
			cycle01: 1,
			cycle02: 2,
			average01: 1,
			average02: 1,
			checkboxList: [],
			checkNum: this.$options.propsData.check
		}
	},
	name: 'crontab-day',
	props: ['check', 'cron'],
	methods: {
		// 单选按钮值变化时
		radioChange() {
			('day rachange');
			if (this.radioValue !== 2 && this.cron.week !== '?') {
				this.$emit('update', 'week', '?', 'day')
			}

			switch (this.radioValue) {
				case 1:
					this.$emit('update', 'day', '*');
					break;
				case 2:
					this.$emit('update', 'day', '?');
					break;
				case 3:
					this.$emit('update', 'day', this.cycleTotal);
					break;
				case 4:
					this.$emit('update', 'day', this.averageTotal);
					break;
				case 5:
					this.$emit('update', 'day', this.workday + 'W');
					break;
				case 6:
					this.$emit('update', 'day', 'L');
					break;
				case 7:
					this.$emit('update', 'day', this.checkboxString);
					break;
			}
			('day rachange end');
		},
		// 周期两个值变化时
		cycleChange() {
			if (this.radioValue == '3') {
				this.$emit('update', 'day', this.cycleTotal);
			}
		},
		// 平均两个值变化时
		averageChange() {
			if (this.radioValue == '4') {
				this.$emit('update', 'day', this.averageTotal);
			}
		},
		// 最近工作日值变化时
		workdayChange() {
			if (this.radioValue == '5') {
				this.$emit('update', 'day', this.workdayCheck + 'W');
			}
		},
		// checkbox值变化时
		checkboxChange() {
			if (this.radioValue == '7') {
				this.$emit('update', 'day', this.checkboxString);
			}
		}
	},
	watch: {
		'radioValue': 'radioChange',
		'cycleTotal': 'cycleChange',
		'averageTotal': 'averageChange',
		'workdayCheck': 'workdayChange',
		'checkboxString': 'checkboxChange',
	},
	computed: {
		// 计算两个周期值
		cycleTotal: function () {
			const cycle01 = this.checkNum(this.cycle01, 1, 30)
			const cycle02 = this.checkNum(this.cycle02, cycle01 ? cycle01 + 1 : 2, 31, 31)
			return cycle01 + '-' + cycle02;
		},
		// 计算平均用到的值
		averageTotal: function () {
			const average01 = this.checkNum(this.average01, 1, 30)
			const average02 = this.checkNum(this.average02, 1, 31 - average01 || 0)
			return average01 + '/' + average02;
		},
		// 计算工作日格式
		workdayCheck: function () {
			const workday = this.checkNum(this.workday, 1, 31)
			return workday;
		},
		// 计算勾选的checkbox值合集
		checkboxString: function () {
			let str = this.checkboxList.join();
			return str == '' ? '*' : str;
		}
	}
}
</script>
