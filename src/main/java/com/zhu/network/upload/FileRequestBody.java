package com.che168.network.upload;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * 扩展OkHttp的请求体，实现上传时的进度提示
 *
 * @param <T>
 */
public class FileRequestBody<T> extends RequestBody {
    /**
     * 实际请求体
     */
    private RequestBody requestBody;
    /**
     * 上传回调接口
     */
    private UploadListener uploadListener;
    /**
     * 包装完成的BufferedSink
     */
    private BufferedSink bufferedSink;

    /**
     * 上传文件本地路径
     */
    private String mFilePath;

    long[] longs = new long[2];

    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(uploadListener != null){
                uploadListener.progress(mFilePath, longs[0], longs[1]);
            }
        }
    };


    public FileRequestBody(String filePath, RequestBody requestBody, UploadListener uploadListener) {
        super();
        this.requestBody    = requestBody;
        this.uploadListener = uploadListener;
        this.mFilePath      = filePath;
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        if (bufferedSink == null) {
            //包装
            bufferedSink = Okio.buffer(sink(sink));
        }
        //写入
        requestBody.writeTo(bufferedSink);
        //必须调用flush，否则最后一部分数据可能不会被写入
        bufferedSink.flush();
    }

    /**
     * 写入，回调进度接口
     * @param sink Sink
     * @return Sink
     */
    private Sink sink(Sink sink) {
        return new ForwardingSink(sink) {
            //当前写入字节数
            long bytesWritten = 0L;
            //总字节长度，避免多次调用contentLength()方法
            long contentLength = 0L;

            @Override
            public void write(Buffer source, final long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength == 0) {
                    //获得contentLength的值，后续不再调用
                    contentLength = contentLength();
                }
                //增加当前写入的字节数
                bytesWritten += byteCount;
                //回调

                longs[0] = bytesWritten;
                longs[1] = contentLength;
                mHandler.sendEmptyMessage(0);
            }
        };
    }

    /**
     * 上传图片进度监听
     */
    public static interface UploadListener{
        void progress(String filePath, long uploaded, long total);
    }
}
