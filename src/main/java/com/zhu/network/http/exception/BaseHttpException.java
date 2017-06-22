package com.che168.network.http.exception;

/**
 * Http请求异常类
 *
 * @author zhudeshuai
 * @since 17/3/1
 */

public class BaseHttpException extends Exception {

	private TypeException mTypeException;

	public BaseHttpException() {
	}

	public BaseHttpException(TypeException mTypeException) {
		this.mTypeException = mTypeException;
	}

	/**
	 * 获取异常类型
	 *
	 * @return
	 */
	public TypeException getTypeException() {
		return mTypeException;
	}

	/**
	 * 请求异常类型
	 */
	public enum TypeException {
		NO_NET_WORK("似乎已断开与互联网连接"),
		TIME_OUT("网络连接失败，请稍后重试"),
		UNKNOWN("未知服务器异常"),
		JSON_PARSE_FAILED("服务器返回数据格式异常"),
		DOWNLOAD_IO("下载流写入异常"),
		;

		private String meg;

		TypeException(String meg) {
			this.meg = meg;
		}

		@Override
		public String toString() {
			return this.meg;
		}
	}
}
