package com.che168.ahnetwork.http.callback;

import okhttp3.Headers;

/**
 * 响应header回调
 *
 * @author zhudeshuai
 * @since 17/3/23
 */

public interface ResponseHeaderCallback {
	void header(Headers headers);
}
