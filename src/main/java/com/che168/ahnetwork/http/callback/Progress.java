package com.che168.ahnetwork.http.callback;

/**
 * 下载进度
 *
 * @author zhudeshuai
 * @since 17/3/1
 */

public interface Progress {
	void progress(long hasDownload, long total);
}
