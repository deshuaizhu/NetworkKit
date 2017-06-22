package com.che168.ahnetwork.http.callback;

import com.che168.ahnetwork.http.exception.BaseHttpException;

/**
 * 失败接口
 *
 * @author zhudeshuai
 * @since 17/2/28
 */

public interface Failed {
	void failed(BaseHttpException exception);
}
