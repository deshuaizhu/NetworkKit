package com.che168.ahnetwork.http.converter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import retrofit2.Converter;

/**
 * 请求体字符转换
 *
 * @author zhudeshuai
 * @since 17/2/28
 */
public class StringRequestBodyConverter implements Converter<String, RequestBody> {
	private static final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=UTF-8");
	private static final Charset UTF_8 = Charset.forName("UTF-8");

	StringRequestBodyConverter() {
	}

	@Override
	public RequestBody convert(String value) throws IOException {
		Buffer buffer = new Buffer();
		Writer writer = new OutputStreamWriter(buffer.outputStream(), UTF_8);
		writer.write(value);
		writer.close();
		return RequestBody.create(MEDIA_TYPE, buffer.readByteString());
	}
}
