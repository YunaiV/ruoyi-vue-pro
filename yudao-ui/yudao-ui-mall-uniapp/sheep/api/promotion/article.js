import request from '@/sheep/request';

export default {
    // 获得文章详情
    getArticle: (id, title) => {
        return request({
            url: '/promotion/article/get',
            method: 'GET',
            params: { id, title }
        });
    }
}
