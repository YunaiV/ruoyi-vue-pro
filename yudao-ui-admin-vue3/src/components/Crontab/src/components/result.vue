<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'

const props = defineProps({
  ex: {
    type: String,
    required: true
  }
})

watch(() => props.ex, expressionChange)

onMounted(() => {
  expressionChange()
})

const dayRule = ref('')
const dayRuleSup = ref<string | number | any[]>([])
const dateArr = ref<any[][]>([])
const resultList = ref<string[]>([])
const isShow = ref(false)

function expressionChange() {
  // 计算开始-隐藏结果
  isShow.value = false
  // 获取规则数组[0秒、1分、2时、3日、4月、5星期、6年]
  let ruleArr = props.ex.split(' ')
  // 用于记录进入循环的次数
  let nums = 0
  // 用于暂时存符号时间规则结果的数组
  let resultArr: string[] = []
  // 获取当前时间精确至[年、月、日、时、分、秒]
  let nTime = new Date()
  let nYear = nTime.getFullYear()
  let nMonth = nTime.getMonth() + 1
  let nDay = nTime.getDate()
  let nHour = nTime.getHours()
  let nMin = nTime.getMinutes()
  let nSecond = nTime.getSeconds()
  // 根据规则获取到近100年可能年数组、月数组等等
  getSecondArr(ruleArr[0])
  getMinArr(ruleArr[1])
  getHourArr(ruleArr[2])
  getDayArr(ruleArr[3])
  getMonthArr(ruleArr[4])
  getWeekArr(ruleArr[5])
  getYearArr(ruleArr[6], nYear)
  // 将获取到的数组赋值-方便使用
  let sDate = dateArr.value[0]
  let mDate = dateArr.value[1]
  let hDate = dateArr.value[2]
  let DDate = dateArr.value[3]
  let MDate = dateArr.value[4]
  let YDate = dateArr.value[5]
  // 获取当前时间在数组中的索引
  let sIdx = getIndex(sDate, nSecond)
  let mIdx = getIndex(mDate, nMin)
  let hIdx = getIndex(hDate, nHour)
  let DIdx = getIndex(DDate, nDay)
  let MIdx = getIndex(MDate, nMonth)
  let YIdx = getIndex(YDate, nYear)
  // 重置月日时分秒的函数(后面用的比较多)
  const resetSecond = function () {
    sIdx = 0
    nSecond = sDate[sIdx]
  }
  const resetMin = function () {
    mIdx = 0
    nMin = mDate[mIdx]
    resetSecond()
  }
  const resetHour = function () {
    hIdx = 0
    nHour = hDate[hIdx]
    resetMin()
  }
  const resetDay = function () {
    DIdx = 0
    nDay = DDate[DIdx]
    resetHour()
  }
  const resetMonth = function () {
    MIdx = 0
    nMonth = MDate[MIdx]
    resetDay()
  }
  // 如果当前年份不为数组中当前值
  if (nYear !== YDate[YIdx]) {
    resetMonth()
  }
  // 如果当前月份不为数组中当前值
  if (nMonth !== MDate[MIdx]) {
    resetDay()
  }
  // 如果当前“日”不为数组中当前值
  if (nDay !== DDate[DIdx]) {
    resetHour()
  }
  // 如果当前“时”不为数组中当前值
  if (nHour !== hDate[hIdx]) {
    resetMin()
  }
  // 如果当前“分”不为数组中当前值
  if (nMin !== mDate[mIdx]) {
    resetSecond()
  }

  // 循环年份数组
  goYear: for (let Yi = YIdx; Yi < YDate.length; Yi++) {
    let YY = YDate[Yi]
    // 如果到达最大值时
    if (nMonth > MDate[MDate.length - 1]) {
      resetMonth()
      continue
    }
    // 循环月份数组
    goMonth: for (let Mi = MIdx; Mi < MDate.length; Mi++) {
      // 赋值、方便后面运算
      let MM = MDate[Mi]
      MM = MM < 10 ? '0' + MM : MM
      // 如果到达最大值时
      if (nDay > DDate[DDate.length - 1]) {
        resetDay()
        if (Mi == MDate.length - 1) {
          resetMonth()
          continue goYear
        }
        continue
      }
      // 循环日期数组
      goDay: for (let Di = DIdx; Di < DDate.length; Di++) {
        // 赋值、方便后面运算
        let DD = DDate[Di]
        let thisDD = DD < 10 ? '0' + DD : DD

        // 如果到达最大值时
        if (nHour > hDate[hDate.length - 1]) {
          resetHour()
          if (Di == DDate.length - 1) {
            resetDay()
            if (Mi == MDate.length - 1) {
              resetMonth()
              continue goYear
            }
            continue goMonth
          }
          continue
        }

        // 判断日期的合法性，不合法的话也是跳出当前循环
        if (
          checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true &&
          dayRule.value !== 'workDay' &&
          dayRule.value !== 'lastWeek' &&
          dayRule.value !== 'lastDay'
        ) {
          resetDay()
          continue goMonth
        }
        // 如果日期规则中有值时
        if (dayRule.value == 'lastDay') {
          // 如果不是合法日期则需要将前将日期调到合法日期即月末最后一天

          if (checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
            while (DD > 0 && checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
              DD--

              thisDD = DD < 10 ? '0' + DD : DD
            }
          }
        } else if (dayRule.value == 'workDay') {
          // 校验并调整如果是2月30号这种日期传进来时需调整至正常月底
          if (checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
            while (DD > 0 && checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
              DD--
              thisDD = DD < 10 ? '0' + DD : DD
            }
          }
          // 获取达到条件的日期是星期X
          let thisWeek = formatDate(new Date(YY + '-' + MM + '-' + thisDD + ' 00:00:00'), 'week')
          // 当星期日时
          if (thisWeek == 1) {
            // 先找下一个日，并判断是否为月底
            DD++
            thisDD = DD < 10 ? '0' + DD : DD
            // 判断下一日已经不是合法日期
            if (checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
              DD -= 3
            }
          } else if (thisWeek == 7) {
            // 当星期6时只需判断不是1号就可进行操作
            if (dayRuleSup.value !== 1) {
              DD--
            } else {
              DD += 2
            }
          }
        } else if (dayRule.value == 'weekDay') {
          // 如果指定了是星期几
          // 获取当前日期是属于星期几
          let thisWeek = formatDate(new Date(YY + '-' + MM + '-' + DD + ' 00:00:00'), 'week')
          // 校验当前星期是否在星期池（dayRuleSup）中
          if ((dayRuleSup.value as any[]).indexOf(thisWeek) < 0) {
            // 如果到达最大值时
            if (Di == DDate.length - 1) {
              resetDay()
              if (Mi == MDate.length - 1) {
                resetMonth()
                continue goYear
              }
              continue goMonth
            }
            continue
          }
        } else if (dayRule.value == 'assWeek') {
          // 如果指定了是第几周的星期几
          // 获取每月1号是属于星期几
          let thisWeek = formatDate(new Date(YY + '-' + MM + '-' + DD + ' 00:00:00'), 'week')
          if (dayRuleSup.value[1] >= thisWeek) {
            DD = (dayRuleSup.value[0] - 1) * 7 + dayRuleSup.value[1] - thisWeek + 1
          } else {
            DD = dayRuleSup.value[0] * 7 + dayRuleSup.value[1] - thisWeek + 1
          }
        } else if (dayRule.value == 'lastWeek') {
          // 如果指定了每月最后一个星期几
          // 校验并调整如果是2月30号这种日期传进来时需调整至正常月底
          if (checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
            while (DD > 0 && checkDate(YY + '-' + MM + '-' + thisDD + ' 00:00:00') !== true) {
              DD--
              thisDD = DD < 10 ? '0' + DD : DD
            }
          }
          // 获取月末最后一天是星期几
          let thisWeek = formatDate(new Date(YY + '-' + MM + '-' + thisDD + ' 00:00:00'), 'week')
          // 找到要求中最近的那个星期几
          if (dayRuleSup.value < thisWeek) {
            DD -= thisWeek - (dayRuleSup.value as number)
          } else if (dayRuleSup.value > thisWeek) {
            DD -= 7 - ((dayRuleSup.value as number) - thisWeek)
          }
        }
        // 判断时间值是否小于10置换成“05”这种格式
        DD = DD < 10 ? '0' + DD : DD

        // 循环“时”数组
        goHour: for (let hi = hIdx; hi < hDate.length; hi++) {
          let hh = hDate[hi] < 10 ? '0' + hDate[hi] : hDate[hi]

          // 如果到达最大值时
          if (nMin > mDate[mDate.length - 1]) {
            resetMin()
            if (hi == hDate.length - 1) {
              resetHour()
              if (Di == DDate.length - 1) {
                resetDay()
                if (Mi == MDate.length - 1) {
                  resetMonth()
                  continue goYear
                }
                continue goMonth
              }
              continue goDay
            }
            continue
          }
          // 循环"分"数组
          goMin: for (let mi = mIdx; mi < mDate.length; mi++) {
            let mm = mDate[mi] < 10 ? '0' + mDate[mi] : mDate[mi]

            // 如果到达最大值时
            if (nSecond > sDate[sDate.length - 1]) {
              resetSecond()
              if (mi == mDate.length - 1) {
                resetMin()
                if (hi == hDate.length - 1) {
                  resetHour()
                  if (Di == DDate.length - 1) {
                    resetDay()
                    if (Mi == MDate.length - 1) {
                      resetMonth()
                      continue goYear
                    }
                    continue goMonth
                  }
                  continue goDay
                }
                continue goHour
              }
              continue
            }
            // 循环"秒"数组
            goSecond: for (let si = sIdx; si <= sDate.length - 1; si++) {
              let ss = sDate[si] < 10 ? '0' + sDate[si] : sDate[si]
              // 添加当前时间（时间合法性在日期循环时已经判断）
              if (MM !== '00' && DD !== '00') {
                resultArr.push(YY + '-' + MM + '-' + DD + ' ' + hh + ':' + mm + ':' + ss)
                nums++
              }
              // 如果条数满了就退出循环
              if (nums == 5) break goYear
              // 如果到达最大值时
              if (si == sDate.length - 1) {
                resetSecond()
                if (mi == mDate.length - 1) {
                  resetMin()
                  if (hi == hDate.length - 1) {
                    resetHour()
                    if (Di == DDate.length - 1) {
                      resetDay()
                      if (Mi == MDate.length - 1) {
                        resetMonth()
                        continue goYear
                      }
                      continue goMonth
                    }
                    continue goDay
                  }
                  continue goHour
                }
                continue goMin
              }
            } //goSecond
          } //goMin
        } //goHour
      } //goDay
    } //goMonth
  }
  // 判断100年内的结果条数
  if (resultArr.length == 0) {
    resultList.value = ['没有达到条件的结果！']
  } else {
    resultList.value = resultArr
    if (resultArr.length !== 5) {
      resultList.value.push('最近100年内只有上面' + resultArr.length + '条结果！')
    }
  }
  // 计算完成-显示结果
  isShow.value = true
}

/**
 * 用于计算某位数字在数组中的索引
 */
function getIndex(arr, value): number {
  if (value <= arr[0] || value > arr[arr.length - 1]) {
    return 0
  } else {
    for (let i = 0; i < arr.length - 1; i++) {
      if (value > arr[i] && value <= arr[i + 1]) {
        return i + 1
      }
    }
  }
  return 0
}

function getYearArr(rule, year) {
  dateArr.value[5] = getOrderArr(year, year + 100)
  if (rule !== undefined) {
    if (rule.indexOf('-') >= 0) {
      dateArr.value[5] = getCycleArr(rule, year + 100, false)
    } else if (rule.indexOf('/') >= 0) {
      dateArr.value[5] = getAverageArr(rule, year + 100)
    } else if (rule !== '*') {
      dateArr.value[5] = getAssignArr(rule)
    }
  }
}

function getMonthArr(rule) {
  dateArr.value[4] = getOrderArr(1, 12)
  if (rule.indexOf('-') >= 0) {
    dateArr.value[4] = getCycleArr(rule, 12, false)
  } else if (rule.indexOf('/') >= 0) {
    dateArr.value[4] = getAverageArr(rule, 12)
  } else if (rule !== '*') {
    dateArr.value[4] = getAssignArr(rule)
  }
}

function getWeekArr(rule) {
  // 只有当日期规则的两个值均为“”时则表达日期是有选项的
  if (dayRule.value === '' && dayRuleSup.value === '') {
    if (rule.indexOf('-') >= 0) {
      dayRule.value = 'weekDay'
      dayRuleSup.value = getCycleArr(rule, 7, false)
    } else if (rule.indexOf('#') >= 0) {
      dayRule.value = 'assWeek'
      let matchRule = rule.match(/[0-9]{1}/g)
      dayRuleSup.value = [Number(matchRule[1]), Number(matchRule[0])]
      dateArr.value[3] = [1]
      if (dayRuleSup.value[1] == 7) {
        dayRuleSup.value[1] = 0
      }
    } else if (rule.indexOf('L') >= 0) {
      dayRule.value = 'lastWeek'
      dayRuleSup.value = Number(rule.match(/[0-9]{1,2}/g)[0])
      dateArr.value[3] = [31]
      if (dayRuleSup.value == 7) {
        dayRuleSup.value = 0
      }
    } else if (rule !== '*' && rule !== '?') {
      dayRule.value = 'weekDay'
      dayRuleSup.value = getAssignArr(rule)
    }
  }
}

function getDayArr(rule) {
  dateArr.value[3] = getOrderArr(1, 31)
  dayRule.value = ''
  dayRuleSup.value = ''
  if (rule.indexOf('-') >= 0) {
    dateArr.value[3] = getCycleArr(rule, 31, false)
    dayRuleSup.value = 'null'
  } else if (rule.indexOf('/') >= 0) {
    dateArr.value[3] = getAverageArr(rule, 31)
    dayRuleSup.value = 'null'
  } else if (rule.indexOf('W') >= 0) {
    dayRule.value = 'workDay'
    dayRuleSup.value = Number(rule.match(/[0-9]{1,2}/g)[0])
    dateArr.value[3] = [dayRuleSup.value]
  } else if (rule.indexOf('L') >= 0) {
    dayRule.value = 'lastDay'
    dayRuleSup.value = 'null'
    dateArr.value[3] = [31]
  } else if (rule !== '*' && rule !== '?') {
    dateArr.value[3] = getAssignArr(rule)
    dayRuleSup.value = 'null'
  } else if (rule == '*') {
    dayRuleSup.value = 'null'
  }
}

function getHourArr(rule) {
  dateArr.value[2] = getOrderArr(0, 23)
  if (rule.indexOf('-') >= 0) {
    dateArr.value[2] = getCycleArr(rule, 24, true)
  } else if (rule.indexOf('/') >= 0) {
    dateArr.value[2] = getAverageArr(rule, 23)
  } else if (rule !== '*') {
    dateArr.value[2] = getAssignArr(rule)
  }
}

function getMinArr(rule) {
  dateArr.value[1] = getOrderArr(0, 59)
  if (rule.indexOf('-') >= 0) {
    dateArr.value[1] = getCycleArr(rule, 60, true)
  } else if (rule.indexOf('/') >= 0) {
    dateArr.value[1] = getAverageArr(rule, 59)
  } else if (rule !== '*') {
    dateArr.value[1] = getAssignArr(rule)
  }
}

function getSecondArr(rule) {
  dateArr.value[0] = getOrderArr(0, 59)
  if (rule.indexOf('-') >= 0) {
    dateArr.value[0] = getCycleArr(rule, 60, true)
  } else if (rule.indexOf('/') >= 0) {
    dateArr.value[0] = getAverageArr(rule, 59)
  } else if (rule !== '*') {
    dateArr.value[0] = getAssignArr(rule)
  }
}

function getOrderArr(min: number, max: number): number[] {
  let arr: number[] = []
  for (let i = min; i <= max; i++) {
    arr.push(i)
  }
  return arr
}

function getAssignArr(rule) {
  let arr: number[] = []
  let assiginArr = rule.split(',')
  for (let i = 0; i < assiginArr.length; i++) {
    arr[i] = Number(assiginArr[i])
  }
  arr.sort(compare)
  return arr
}

function compare(value1, value2) {
  if (value2 - value1 > 0) {
    return -1
  } else {
    return 1
  }
}

function getAverageArr(rule, limit): number[] {
  let arr: number[] = []
  let agArr = rule.split('/')
  let min = Number(agArr[0])
  let step = Number(agArr[1])
  while (min <= limit) {
    arr.push(min)
    min += step
  }
  return arr
}

function getCycleArr(rule, limit: number, status): number[] {
  // status--表示是否从0开始（则从1开始）
  let arr: number[] = []
  let cycleArr = rule.split('-')
  let min = Number(cycleArr[0])
  let max = Number(cycleArr[1])
  if (min > max) {
    max += limit
  }
  for (let i = min; i <= max; i++) {
    let add = 0
    if (status == false && i % limit == 0) {
      add = limit
    }
    arr.push(Math.round((i % limit) + add))
  }
  arr.sort(compare)
  return arr
}

function formatDate(value, type?) {
  // 计算日期相关值
  let time = typeof value == 'number' ? new Date(value) : value
  let Y = time.getFullYear()
  let M = time.getMonth() + 1
  let D = time.getDate()
  let h = time.getHours()
  let m = time.getMinutes()
  let s = time.getSeconds()
  let week = time.getDay()
  // 如果传递了type的话
  if (type == undefined) {
    return (
      Y +
      '-' +
      (M < 10 ? '0' + M : M) +
      '-' +
      (D < 10 ? '0' + D : D) +
      ' ' +
      (h < 10 ? '0' + h : h) +
      ':' +
      (m < 10 ? '0' + m : m) +
      ':' +
      (s < 10 ? '0' + s : s)
    )
  } else if (type == 'week') {
    // 在quartz中 1为星期日
    return week + 1
  }
}

function checkDate(value) {
  let time = new Date(value)
  let format = formatDate(time)
  return value === format
}
</script>
<template>
  <div class="popup-result">
    <p class="title">最近5次运行时间</p>
    <ul class="popup-result-scroll">
      <template v-if="isShow">
        <li v-for="item in resultList" :key="item">{{ item }}</li>
      </template>
      <li v-else>计算结果中...</li>
    </ul>
  </div>
</template>
