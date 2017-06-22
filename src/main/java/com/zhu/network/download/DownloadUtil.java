package com.che168.network.download;

import com.che168.network.http.HttpUtil;
import com.che168.network.http.callback.Failed;
import com.che168.network.http.callback.Progress;
import com.che168.network.http.callback.Success;
import com.che168.network.http.exception.BaseHttpException;

/**
 * 下载文件工具类
 *
 * @author zhudeshuai
 * @since 17/3/1
 */

public class DownloadUtil {


	/**
	 * 下载（暂不支持暂停，继续功能)
	 *
	 * @param tag         下载标识，通过此标识可以停止下载
	 * @param downloadUrl 下载地址
	 * @param savePath    下载文件保存位置
	 * @param listener    下载监听
	 */
	public static void download(Object tag, String downloadUrl, String savePath, final DownloadListener listener) {
		if (downloadUrl == null || "".equals(downloadUrl)) {
			throw new NullPointerException("download url can not be null");
		}
		if (savePath == null || "".equals(savePath)) {
			throw new NullPointerException("file path can not be null");
		}
		HttpUtil.Builder builder = new HttpUtil.Builder();
		builder
				.tag(tag)
				.method(HttpUtil.Method.GET)
				.url(downloadUrl)
				.savePath(savePath)
				.progress(new Progress() {
					@Override
					public void progress(long hasDownload, long total) {
						if (listener != null) {
							listener.progress(hasDownload, total);
						}
					}
				})
				.success(new Success() {
					@Override
					public void success(String path) {
						if (listener != null) {
							listener.success(path);
						}
					}
				})
				.failed(new Failed() {
					@Override
					public void failed(BaseHttpException exception) {
						if (listener != null) {
							listener.failed();
						}
					}
				})
				.download();

	}

	/**
	 * 下载监听
	 */
	public interface DownloadListener {
		void success(String path);

		void progress(long download, long total);

		void failed();
	}
}
