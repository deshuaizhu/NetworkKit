package com.che168.ahnetwork.http.interceptor;

import java.util.Map;

/**
 * 公共参数拦截器
 *
 * @author zhudeshuai
 * @since 17/2/28
 */

public interface ParamsInterceptor {
	Map<String, String> checkParams(Map<String, String> params);
}
