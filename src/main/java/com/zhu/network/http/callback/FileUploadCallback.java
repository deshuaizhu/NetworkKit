package com.zhu.network.http.callback;

import com.zhu.network.http.exception.BaseApiException;

/**
 * Created by mengfanshuai on 2017/3/14.
 */

public interface FileUploadCallback{
    /**
     * 上传成功
     * @param path      上传文件本地地址
     * @param result    上传成功返回
     */
    void success(String path, String result);

    /**
     * 上传中
     * @param path          上传文件本地地址
     * @param hasUpload     已经上数据大小
     * @param total         文件总大小
     */
    void progress(String path, long hasUpload, long total);

    /**
     * 上传失败
     * @param path          上传文件本地地址
     * @param exception     失败原因
     */
    void failed(String path, BaseApiException exception);

}
