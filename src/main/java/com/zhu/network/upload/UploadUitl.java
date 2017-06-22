package com.che168.network.upload;

import android.text.TextUtils;

import com.che168.network.http.HttpUtil;
import com.che168.network.http.callback.FileUploadCallback;
import com.che168.network.http.exception.BaseApiException;

import java.util.Map;

/**
 * 上传文件
 * Created by mengfanshuai on 2017/3/13.
 */

public class UploadUitl {


    /**
     * 上传文件contentType
     */
    public enum ContentType{
        FILE("file"),
        IMAGE("image/pjpeg");

        private String value;

        private ContentType(String value){
            this.value = value;
        }
    }

    /**
     * 上传文件FromDataName
     */
    public enum FromDataName{
        IMAGE("imagefile");

        private String value;

        private FromDataName(String value){
            this.value = value;
        }
    }



    /**
     * 上传图片
     * @param filePath          图片本地地址
     * @param params            参数
     * @param uploadUrl         图片上传地址
     * @param uploadListener    回调监听
     */
    public static <T> void upload(String filePath, Map<String, String> params, String uploadUrl, ContentType contentType, FromDataName formDataName,
                                  final UploadListener<T> uploadListener){

        if (TextUtils.isEmpty(uploadUrl)) {
            throw new NullPointerException("download url can not be null");
        }
        if (TextUtils.isEmpty(filePath)) {
            throw new NullPointerException("file path can not be null");
        }

        HttpUtil.Builder builder = new HttpUtil.Builder();

        builder
                .tag(filePath)
                .method(HttpUtil.Method.POST)
                .params(params)
                .savePath(filePath).url(uploadUrl)
                .fileUploadCallback(new FileUploadCallback() {
                    @Override
                    public void success(String path, String result) {
                        if(uploadListener != null){
                            uploadListener.success(path, result);
                        }
                    }

                    @Override
                    public void progress(String path, long hasUpload, long total) {
                        if(uploadListener != null){
                            uploadListener.progress(path, hasUpload, total);
                        }
                    }

                    @Override
                    public void failed(String path, BaseApiException exception) {
                        if(uploadListener != null){
                            uploadListener.fail(path, exception.getMessage());
                        }
                    }
                }).upload(contentType.value, formDataName.value);
    }

    /**
     * 取消上传功能
     * @param path  文件路径
     */
    public static void cancel(String path){
        HttpUtil.cancel(path);
    }


    public interface UploadListener<T>{

        /**
         * 正在上传
         * @param path              上传图片原地址
         * @param bytesReceived     已上传
         * @param totalBytes
         */
        public void progress(String path, long bytesReceived, long totalBytes);

        /**
         * 上传失败
         * @param path      上传图片原地址
         * @param message   失败信息
         */
        public void fail(String path, String message);

        /**
         *上传成功
         * @param path      上传图片原地址
         * @param json      上传成功后的返回数据
         */
        public void success(String path, String json);

    }


}
