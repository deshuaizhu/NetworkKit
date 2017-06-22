package com.zhu.network.http.util;

import android.os.Handler;
import android.os.Looper;

import com.zhu.network.http.callback.Failed;
import com.zhu.network.http.callback.Progress;
import com.zhu.network.http.callback.Success;
import com.zhu.network.http.exception.BaseHttpException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 * 下载流写入文件，提供进度信息
 *
 * @author zhudeshuai
 * @since 17/3/1
 */

public class WriteFileUtil {
	public static Handler mHandler = new Handler(Looper.getMainLooper());

	public static void writeFile(ResponseBody body, final String path, final Progress progress, final Success mSuccessCallBack, final Failed mErrorCallBack) {
		File saveFile = new File(path);
		if(!saveFile.exists()){
			try {
				saveFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		InputStream inputStream = null;
		OutputStream outputStream = null;
		final ProgressInfo progressInfo = new ProgressInfo();
		try {
			byte[] fileReader = new byte[4096];
			progressInfo.total = body.contentLength();
			progressInfo.read = 0;
			inputStream = body.byteStream();
			outputStream = new FileOutputStream(saveFile);
			while (true) {
				int read = inputStream.read(fileReader);
				if (read == -1) {
					break;
				}
				outputStream.write(fileReader, 0, read);
				progressInfo.read += read;
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						progress.progress(progressInfo.read, progressInfo.total);
					}
				});
			}
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mSuccessCallBack.success(path);
				}
			});
			outputStream.flush();
		} catch (IOException e) {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					mErrorCallBack.failed(new BaseHttpException(BaseHttpException.TypeException.DOWNLOAD_IO));
				}
			});
		} finally {
			try {
				if (inputStream != null) {
					inputStream.close();
				}
				if (outputStream != null) {
					outputStream.close();
				}
			} catch (IOException e) {
			}

		}

	}

	static class ProgressInfo {
		public long read = 0;
		public long total = 0;
	}
}
