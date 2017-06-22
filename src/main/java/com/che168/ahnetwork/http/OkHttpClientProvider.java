package com.che168.ahnetwork.http;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;

/**
 * 默认的OkHttpClient
 *
 * @author zhudeshuai
 * @since 17/2/28
 */

public class OkHttpClientProvider {
	static OkHttpClient okHttpClient;

	public static OkHttpClient okHttpClient() {
		if (okHttpClient == null) {
			synchronized (OkHttpClientProvider.class) {
				if (okHttpClient == null) {
					OkHttpClient client = new OkHttpClient.Builder()
							.retryOnConnectionFailure(false)
							.connectTimeout(5, TimeUnit.SECONDS)
							.readTimeout(8, TimeUnit.SECONDS)
							.writeTimeout(8, TimeUnit.SECONDS)
							.build();

					okHttpClient = client;
				}

			}

		}
		return okHttpClient;

	}

}
