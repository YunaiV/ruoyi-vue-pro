import type { PageParam, PageResult } from '@vben/request';

import { requestClient } from '#/api/request';

export namespace MemberExperienceRecordApi {
  /** 会员经验记录信息 */
  export interface ExperienceRecord {
    id?: number;
    userId: number;
    bizId: string;
    bizType: number;
    title: string;
    description: string;
    experience: number;
    totalExperience: number;
  }
}

/** 查询会员经验记录列表 */
export function getExperienceRecordPage(params: PageParam) {
  return requestClient.get<
    PageResult<MemberExperienceRecordApi.ExperienceRecord>
  >('/member/experience-record/page', {
    params,
  });
}

/** 查询会员经验记录详情 */
export function getExperienceRecord(id: number) {
  return requestClient.get<MemberExperienceRecordApi.ExperienceRecord>(
    `/member/experience-record/get?id=${id}`,
  );
}
