package cn.iocoder.yudao.framework.file.core.client.s3;

import software.amazon.awssdk.core.interceptor.Context;
import software.amazon.awssdk.core.interceptor.ExecutionAttributes;
import software.amazon.awssdk.core.interceptor.ExecutionInterceptor;
import software.amazon.awssdk.http.SdkHttpRequest;

/**
 * S3 修改路径的拦截器，移除多余的 Bucket 前缀。
 * 如果不使用该拦截器，希望上传的路径是 /tudou.jpg 时，会被添加成 /bucket/tudou.jpg
 *
 * @author 芋道源码
 */
public class S3ModifyPathInterceptor implements ExecutionInterceptor {

	private final String bucket;

	public S3ModifyPathInterceptor(String bucket) {
		this.bucket = "/" + bucket;
	}

	@Override
	public SdkHttpRequest modifyHttpRequest(Context.ModifyHttpRequest context, ExecutionAttributes executionAttributes) {
		SdkHttpRequest request = context.httpRequest();
		SdkHttpRequest.Builder rb = SdkHttpRequest.builder().protocol(request.protocol()).host(request.host()).port(request.port())
				.method(request.method()).headers(request.headers()).rawQueryParameters(request.rawQueryParameters());
		// 移除 path 前的 bucket 路径
		if (request.encodedPath().startsWith(bucket)) {
			rb.encodedPath(request.encodedPath().substring(bucket.length()));
		} else {
			rb.encodedPath(request.encodedPath());
		}
		return rb.build();
	}

}
