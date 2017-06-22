package com.che168.ahnetwork.http.exception;

/**
 * 基础api异常
 *
 * @author zhudeshuai
 * @since 17/3/16
 */

public class BaseApiException extends BaseHttpException {
	private String msg;

	public BaseApiException(TypeException mTypeException) {
		super(mTypeException);
	}

	public BaseApiException(String msg) {
		this.msg = msg;
	}

	@Override
	public String toString() {
		if (getTypeException() != null)
			return getTypeException().toString();
		else
			return this.msg;
	}
}
