package com.zhu.network.http;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * 基础请求服务接口
 *
 * @author zhudeshuai
 * @since 17/2/28
 */

public interface HttpService {

	@GET()
	Call<String> get(@HeaderMap Map<String, String> headers, @Url String url, @QueryMap Map<String, String> params);

	@FormUrlEncoded
	@POST()
	Call<String> post(@HeaderMap Map<String, String> headers, @Url String url, @FieldMap Map<String, String> params);

	@Streaming
	@GET()
	Call<ResponseBody> download(@HeaderMap Map<String, String> headers, @Url String url, @QueryMap Map<String, String> params);

	@POST()
	Call<String> upload(@Url String url,  @Body MultipartBody multipartBody);
}
