package com.che168.ahnetwork.http;

import android.support.annotation.NonNull;
import android.util.Log;

import com.che168.ahnetwork.http.callback.Failed;
import com.che168.ahnetwork.http.callback.FileUploadCallback;
import com.che168.ahnetwork.http.callback.Progress;
import com.che168.ahnetwork.http.callback.ResponseHeaderCallback;
import com.che168.ahnetwork.http.callback.Success;
import com.che168.ahnetwork.http.converter.StringConverterFactory;
import com.che168.ahnetwork.http.exception.BaseApiException;
import com.che168.ahnetwork.http.exception.BaseHttpException;
import com.che168.ahnetwork.http.interceptor.HeadersInterceptor;
import com.che168.ahnetwork.http.interceptor.ParamsInterceptor;
import com.che168.ahnetwork.http.util.ThreadPoolFactory;
import com.che168.ahnetwork.http.util.WriteFileUtil;
import com.che168.ahnetwork.upload.FileRequestBody;

import java.io.File;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

/**
 * http请求工具
 *
 * @author zhudeshuai
 * @since 17/2/28
 */

public class HttpUtil {

	public static final String TAG = "HttpUtil";

	final static Map<String, Call> CALL_MAP = new HashMap<>();

	private static HttpUtil sInstance;
	/**
	 * 请求公共服务类，定义GET，POST请求
	 */
	private static HttpService mHttpService;

	/**
	 * 请求头处理器
	 */
	private HeadersInterceptor mHeadersInterceptor;

	/**
	 * 参数处理器
	 */
	private ParamsInterceptor mParamsInterceptor;

	private HttpUtil(HttpService httpService, HeadersInterceptor headersInterceptor, ParamsInterceptor paramsInterceptor) {
		this.mHttpService = httpService;
		this.mHeadersInterceptor = headersInterceptor;
		this.mParamsInterceptor = paramsInterceptor;
	}

	/**
	 * 构造每个单独的请求
	 */
	public static class Builder {

		/**
		 * 请求参数
		 */
		private Map<String, String> mParams = new TreeMap<>();

		/**
		 * 请求头
		 */
		private Map<String, String> mHeaders = new TreeMap<>();

		/**
		 * 请求url
		 */
		private String mUrl;

		/**
		 * 请求tag，结束请求的标识
		 */
		private Object mTag;

		/**
		 * 成功回调
		 */
		private Success mSuccess;


		/**
		 * 失败回调
		 */
		private Failed mFailed;

		/**
		 * 下载进度回调
		 */
		private Progress mProgress;

		/**
		 * 下载文件保存目录、上传文件本地地址
		 */
		private String mSavePath;

		/**
		 * 请求方式(默认为GET请求)
		 */
		private Method mMethod = Method.GET;

		/**
		 * 上传文件回调监听
		 */
		private FileUploadCallback mFileUploadCallback;

		/**
		 * 响应header回调
		 */
		private ResponseHeaderCallback mResponseHeaderCallback;


		/**
		 * 添加响应header回调
		 *
		 * @param headerCallback
		 * @return
		 */
		public Builder addResponseHeaderCallback(ResponseHeaderCallback headerCallback) {
			this.mResponseHeaderCallback = headerCallback;
			return this;
		}


		/**
		 * 设置请求方式
		 *
		 * @param method
		 * @return
		 */
		public Builder method(Method method) {
			this.mMethod = method;
			return this;
		}

		/**
		 * 设置url
		 *
		 * @param url
		 * @return
		 */
		public Builder url(String url) {
			this.mUrl = url;
			return this;
		}

		/**
		 * 设置请求结束标识
		 *
		 * @param tag
		 * @return
		 */
		public Builder tag(Object tag) {
			this.mTag = tag;
			return this;
		}

		/**
		 * 设置请求参数集合
		 *
		 * @param params
		 * @return
		 */
		public Builder params(Map<String, String> params) {
			this.mParams.putAll(params);
			return this;
		}

		/**
		 * 设置请求参数
		 *
		 * @param key
		 * @param value
		 * @return
		 */
		public Builder param(@NonNull String key, String value) {
			this.mParams.put(key, value);
			return this;
		}

		/**
		 * 设置请求头集合
		 *
		 * @param headers
		 * @return
		 */
		public Builder headers(Map<String, String> headers) {
			this.mHeaders.putAll(headers);
			return this;
		}

		/**
		 * 设置请求头
		 *
		 * @param key
		 * @param value
		 * @return
		 */
		public Builder header(@NonNull String key, String value) {
			this.mHeaders.put(key, value);
			return this;
		}

		/**
		 * 设置成功回调
		 *
		 * @param success
		 * @return
		 */
		public Builder success(Success success) {
			this.mSuccess = success;
			return this;
		}


		/**
		 * 设置失败回调
		 *
		 * @param failed
		 * @return
		 */
		public Builder failed(Failed failed) {
			this.mFailed = failed;
			return this;
		}

		/**
		 * 设置下载进度监听
		 *
		 * @param progress
		 * @return
		 */
		public Builder progress(Progress progress) {
			this.mProgress = progress;
			return this;
		}

		public Builder fileUploadCallback(FileUploadCallback fileUploadCallback) {
			this.mFileUploadCallback = fileUploadCallback;
			return this;
		}

		/**
		 * 设置下载文件保存目录
		 *
		 * @param savePath
		 * @return
		 */
		public Builder savePath(String savePath) {
			this.mSavePath = savePath;
			return this;
		}

		/**
		 * 执行请求
		 */
		public void doRequest() {
			doRequest(true);
		}


		/**
		 * 执行请求
		 *
		 * @param useGlobalConfig 是否使用全局配置
		 */
		public void doRequest(boolean useGlobalConfig) {
			if (sInstance == null) {
				Log.e(TAG, "HttpUtil.ConfigBuilder must be build!");
				return;
			}
			Call<String> call = null;
			switch (mMethod) {
				case GET:
					call = mHttpService.get(
							useGlobalConfig ? addHeaders(mHeaders) : mHeaders,
							useGlobalConfig ? checkUrl(mUrl) : mUrl,
							useGlobalConfig ? checkParams(mParams) : mParams
					);
					break;
				case POST:
					call = mHttpService.post(
							useGlobalConfig ? addHeaders(mHeaders) : mHeaders,
							useGlobalConfig ? checkUrl(mUrl) : mUrl,
							useGlobalConfig ? checkParams(mParams) : mParams
					);
					break;
			}
			if (call != null) {
				putCall(mTag, mUrl, call);
				call.enqueue(new Callback<String>() {
					@Override
					public void onResponse(Call call, Response response) {
						//回调响应头
						if (mResponseHeaderCallback != null && response != null) {
							mResponseHeaderCallback.header(response.headers());
						}

						if (response != null && response.code() == 200) {
							if (mSuccess != null) {
								mSuccess.success(response.body().toString());
							}
						} else {
							if (mFailed != null) {
								mFailed.failed(handlerErrorMsg(response == null ?
										"Response is null" : response.message()));
							}
						}
						if (mTag != null) {
							removeCall(mUrl);
						}
					}

					@Override
					public void onFailure(Call call, Throwable t) {
						if (mFailed != null) {
							mFailed.failed(handlerException(t));
						}
						if (mTag != null) {
							removeCall(mUrl);
						}
					}
				});
			}


		}


		/**
		 * 尝试添加公共参数
		 *
		 * @param params
		 * @return
		 */
		private Map<String, String> checkParams(Map<String, String> params) {
			if (params == null) {
				params = new TreeMap<>();
			}
			if (sInstance.mParamsInterceptor != null) {
				params = sInstance.mParamsInterceptor.checkParams(params);
			}

			return params;
		}

		/**
		 * url检测
		 *
		 * @param url
		 * @return
		 */
		private String checkUrl(String url) {
			return url;
		}

		/**
		 * 尝试添加公共头
		 *
		 * @param headers
		 * @return
		 */
		private Map<String, String> addHeaders(Map<String, String> headers) {

			if (headers == null) {
				headers = new TreeMap<>();
			}
			if (sInstance.mHeadersInterceptor != null) {
				headers = sInstance.mHeadersInterceptor.checkHeader(headers);
			}
			return headers;
		}

		/**
		 * 处理上传文件参数
		 */
		private MultipartBody.Builder handleUploadParams(MultipartBody.Builder builder) {
			if (builder == null) {
				return null;
			}

			mParams = checkParams(mParams);

			for (String key : mParams.keySet()) {
				builder.addFormDataPart(key, mParams.get(key));
			}

			return builder;

		}


		/**
		 * 错误消息处理
		 *
		 * @param mes
		 * @return
		 */
		private static BaseApiException handlerErrorMsg(String mes) {
			if (checkNULL(mes)) {
				return new BaseApiException(BaseHttpException.TypeException.NO_NET_WORK);
			}
			if (mes.equals("timeout") || mes.equals("SSL handshake timed out")) {
				return new BaseApiException(BaseHttpException.TypeException.TIME_OUT);
			} else {
				return new BaseApiException(BaseHttpException.TypeException.UNKNOWN);
			}

		}

		/**
		 * 异常消息处理
		 *
		 * @param e
		 * @return
		 */
		private static BaseApiException handlerException(Throwable e) {
			BaseHttpException.TypeException typeException;
			if (e instanceof ConnectException) {
				typeException = BaseHttpException.TypeException.NO_NET_WORK;
			} else if (e instanceof SocketTimeoutException) {
				typeException = BaseHttpException.TypeException.TIME_OUT;
			} else {
				typeException = BaseHttpException.TypeException.UNKNOWN;
			}
			return new BaseApiException(typeException);
		}


		/**
		 * 下载方法
		 */
		public void download() {
			Call<ResponseBody> downloadCall = mHttpService.download(mHeaders, mUrl, mParams);
			downloadCall.enqueue(new Callback<ResponseBody>() {
				@Override
				public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
					//回调响应头
					if (mResponseHeaderCallback != null && response != null) {
						mResponseHeaderCallback.header(response.headers());
					}
					if (response.isSuccessful()) {
						ThreadPoolFactory.postBackgroundIORunnable(new ThreadPoolFactory.ThreadRunnable() {
							@Override
							public Object run() {
								WriteFileUtil.writeFile(response.body(), mSavePath, mProgress, mSuccess, mFailed);
								return null;
							}
						});

					} else {
						Log.e(TAG, "server connect failed");
					}
				}

				@Override
				public void onFailure(Call<ResponseBody> call, Throwable t) {
					if (mFailed != null) {
						mFailed.failed(new BaseApiException(t.getMessage()));
					}
					Log.e(TAG, t != null ? t.toString() : "error");
				}
			});

		}

		/**
		 * 上传文件方法
		 */
		public void upload(String contentType, String formDataName) {
			File file = new File(mSavePath);
			RequestBody body = RequestBody.create(MediaType.parse(contentType), file);
			//通过该行代码将RequestBody转换成特定的FileRequestBody
			FileRequestBody fileBody = new FileRequestBody(mSavePath, body, new FileRequestBody.UploadListener() {
				@Override
				public void progress(String filePath, long uploaded, long total) {
					if (mFileUploadCallback != null) {
						mFileUploadCallback.progress(filePath, uploaded, total);
					}
				}
			});

			MultipartBody.Part part = MultipartBody.Part.createFormData(formDataName, file.getName(), fileBody);

			MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
			builder = handleUploadParams(builder);
			builder.addPart(part);

			Call<String> uploadCall = mHttpService.upload(mUrl, builder.build());

			putCall(mTag, mUrl, uploadCall);
			uploadCall.enqueue(new Callback<String>() {
				@Override
				public void onResponse(Call<String> call, Response<String> response) {
					//回调响应头
					if (mResponseHeaderCallback != null && response != null) {
						mResponseHeaderCallback.header(response.headers());
					}
					if (response.isSuccessful()) {
						if (mFileUploadCallback != null) {
							mFileUploadCallback.success(mSavePath, response.body().toString());
						}
					} else {
						if (mFileUploadCallback != null) {
							mFileUploadCallback.failed(mSavePath, handlerErrorMsg(response.message()));
						}
					}
				}

				@Override
				public void onFailure(Call<String> call, Throwable t) {
					if (mFileUploadCallback != null && !"Canceled".equals(t.getMessage())) {
						mFileUploadCallback.failed(mSavePath, handlerException(t));
					}
				}
			});
		}
	}

	/**
	 * 添加某个请求
	 *
	 * @param tag
	 * @param url
	 * @param call
	 */
	private static void putCall(Object tag, String url, Call call) {
		if (tag == null)
			return;
		synchronized (CALL_MAP) {
			CALL_MAP.put(tag.toString() + url, call);
		}
	}

	/**
	 * 取消某个tag的所有请求
	 *
	 * @param tag
	 */
	public static void cancel(Object tag) {
		if (tag == null)
			return;
		List<String> list = new ArrayList<>();
		synchronized (CALL_MAP) {
			for (String key : CALL_MAP.keySet()) {
				if (key.startsWith(tag.toString())) {
					CALL_MAP.get(key).cancel();
					list.add(key);
				}
			}
		}
		for (String s : list) {
			removeCall(s);
		}
	}

	/**
	 * 移除某个请求
	 *
	 * @param tagUrl
	 */
	private static void removeCall(String tagUrl) {
		synchronized (CALL_MAP) {
			for (String key : CALL_MAP.keySet()) {
				if (key.contains(tagUrl)) {
					tagUrl = key;
					break;
				}
			}
			CALL_MAP.remove(tagUrl);
		}
	}


	/**
	 * 全局配置
	 */
	public static class ConfigBuilder {
		private String baseUrl;
		private ParamsInterceptor paramsInterceptor;
		private HeadersInterceptor headersInterceptor;
		OkHttpClient client;

		/**
		 * 配置基础Url
		 *
		 * @param baseUrl
		 * @return
		 */
		public ConfigBuilder baseUrl(String baseUrl) {
			this.baseUrl = baseUrl;
			return this;
		}

		/**
		 * 配置OkHttpclient
		 *
		 * @param client
		 * @return
		 */
		public ConfigBuilder client(OkHttpClient client) {
			this.client = client;
			return this;
		}

		/**
		 * 配置公共参数拦截器
		 *
		 * @param interceptor
		 * @return
		 */
		public ConfigBuilder paramsInterceptor(ParamsInterceptor interceptor) {
			this.paramsInterceptor = interceptor;
			return this;
		}

		/**
		 * 配置公共请求头拦截器
		 *
		 * @param interceptor
		 * @return
		 */
		public ConfigBuilder headersInterceptor(HeadersInterceptor interceptor) {
			this.headersInterceptor = interceptor;
			return this;
		}

		/**
		 * 执行所有配置
		 */
		public void build() {
			if (checkNULL(this.baseUrl)) {
				throw new NullPointerException("base url can not be null");
			}
			if (client == null) {
				client = OkHttpClientProvider.okHttpClient();
			}

			Retrofit.Builder builder = new Retrofit.Builder();
			Retrofit retrofit = builder
					.baseUrl(this.baseUrl)
					.addConverterFactory(StringConverterFactory.create())
					.build();
			HttpService httpService = retrofit.create(HttpService.class);
			sInstance = new HttpUtil(httpService, headersInterceptor, paramsInterceptor);
		}


	}

	// 判断是否NULL
	public static boolean checkNULL(String str) {
		return str == null || "null".equals(str) || "".equals(str);

	}

	/**
	 * 请求方式
	 */
	public enum Method {
		GET, POST
	}

}
