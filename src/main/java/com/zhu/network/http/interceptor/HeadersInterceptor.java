package com.zhu.network.http.interceptor;

import java.util.Map;

/**
 * 公共头部拦截器
 *
 * @author zhudeshuai
 * @since 17/2/28
 */

public interface HeadersInterceptor {
	Map<String, String> checkHeader(Map<String, String> params);
}
