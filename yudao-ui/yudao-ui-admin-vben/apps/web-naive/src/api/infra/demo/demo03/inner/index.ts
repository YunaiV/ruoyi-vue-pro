import type { Dayjs } from 'dayjs';

import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace Demo03StudentApi {
  /** 学生课程信息 */
  export interface Demo03Course {
    id: number; // 编号
    studentId?: number; // 学生编号
    name?: string; // 名字
    score?: number; // 分数
  }

  /** 学生班级信息 */
  export interface Demo03Grade {
    id: number; // 编号
    studentId?: number; // 学生编号
    name?: string; // 名字
    teacher?: string; // 班主任
  }

  /** 学生信息 */
  export interface Demo03Student {
    id: number; // 编号
    name?: string; // 名字
    sex?: number; // 性别
    birthday?: Dayjs | string; // 出生日期
    description?: string; // 简介
    demo03courses?: Demo03Course[];
    demo03grade?: Demo03Grade;
  }
}

/** 查询学生分页 */
export function getDemo03StudentPage(params: PageParam) {
  return requestClient.get<PageResult<Demo03StudentApi.Demo03Student>>(
    '/infra/demo03-student-inner/page',
    { params },
  );
}

/** 查询学生详情 */
export function getDemo03Student(id: number) {
  return requestClient.get<Demo03StudentApi.Demo03Student>(
    `/infra/demo03-student-inner/get?id=${id}`,
  );
}

/** 新增学生 */
export function createDemo03Student(data: Demo03StudentApi.Demo03Student) {
  return requestClient.post('/infra/demo03-student-inner/create', data);
}

/** 修改学生 */
export function updateDemo03Student(data: Demo03StudentApi.Demo03Student) {
  return requestClient.put('/infra/demo03-student-inner/update', data);
}

/** 删除学生 */
export function deleteDemo03Student(id: number) {
  return requestClient.delete(`/infra/demo03-student-inner/delete?id=${id}`);
}

/** 批量删除学生 */
export function deleteDemo03StudentList(ids: number[]) {
  return requestClient.delete(
    `/infra/demo03-student-inner/delete-list?ids=${ids.join(',')}`,
  );
}

/** 导出学生 */
export function exportDemo03Student(params: any) {
  return requestClient.download('/infra/demo03-student-inner/export-excel', {
    params,
  });
}

// ==================== 子表（学生课程） ====================

/** 获得学生课程列表 */
export function getDemo03CourseListByStudentId(studentId: number) {
  return requestClient.get<Demo03StudentApi.Demo03Course[]>(
    `/infra/demo03-student-inner/demo03-course/list-by-student-id?studentId=${studentId}`,
  );
}

// ==================== 子表（学生班级） ====================

/** 获得学生班级 */
export function getDemo03GradeByStudentId(studentId: number) {
  return requestClient.get<Demo03StudentApi.Demo03Grade>(
    `/infra/demo03-student-inner/demo03-grade/get-by-student-id?studentId=${studentId}`,
  );
}
