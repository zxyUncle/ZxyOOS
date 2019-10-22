package com.zxy.oos

import android.content.Context
import android.util.Log
import com.alibaba.sdk.android.oss.*
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback
import com.alibaba.sdk.android.oss.common.auth.OSSAuthCredentialsProvider
import com.alibaba.sdk.android.oss.model.PutObjectRequest
import com.alibaba.sdk.android.oss.model.PutObjectResult
import java.util.*
import com.alibaba.sdk.android.oss.ServiceException
import com.alibaba.sdk.android.oss.ClientException
import com.alibaba.sdk.android.oss.model.GetObjectRequest
import com.alibaba.sdk.android.oss.model.GetObjectResult
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask
import com.alibaba.sdk.android.oss.common.OSSLog
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


/**
 * Created by zxy on 2019/10/22-14:56
 * Class functions
 * ******************************************
 * *
 * ******************************************
 */
class OssUtils private constructor() {
    /**
     * 单例模式
     */
    companion object {
        lateinit var oSSClient: OSSClient
        val instance: OssUtils  by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            OssUtils()
        }
    }

    /**
     * 回调接口
     */
    interface OssPutListener {
        fun onOssSuccess(request: PutObjectRequest?, result: PutObjectResult?, fileName: String)
        fun onOssFailure(
            request: PutObjectRequest?,
            clientException: ClientException?,
            serviceException: ServiceException?
        )

        //异步上传时可以设置进度回调
        fun onOssProgress(request: PutObjectRequest, currentSize: Long, totalSize: Long)
    }

    /**
     * 回调接口
     */
    interface OssGetListener {
        fun onOssSuccess(request: GetObjectRequest, result: GetObjectResult,filePath:String)
        fun onOssFailure()
        //异步上传时可以设置进度回调
        fun onOssProgress(request: GetObjectRequest, currentSize: Long, totalSize: Long)
    }

    /**
     * 初始化
     * @param mContext Context
     * @param endpoint String   "http://oss-cn-hangzhou.aliyuncs.com"
     * @param stsServer String  "STS应用服务器地址，例如http://abc.com
     * @return OSSClient
     */
    fun initOSS(mContext: Context, endpoint: String, stsServer: String): OSSClient {
        // 推荐使用OSSAuthCredentialsProvider。token过期可以及时更新。
        var credentialProvider = OSSAuthCredentialsProvider(stsServer)
        // 配置类如果不设置，会有默认配置。
        var conf = ClientConfiguration()
        conf.connectionTimeout = 15 * 1000 // 连接超时，默认15秒。
        conf.socketTimeout = 15 * 1000 // socket超时，默认15秒。
        conf.maxConcurrentRequest = 5 // 最大并发请求数，默认5个。
        conf.maxErrorRetry = 2// 失败后最大重试次数，默认2次。
        oSSClient = OSSClient(mContext, endpoint, credentialProvider)
        return oSSClient
    }

    /**
     * 上传文件
     * @param bucketName String 服务器给的
     * @param fileName String   文件在oss服务器上的路径+文件的名字+拓展名
     * @param filePath String   文件在手机上的路径
     * @param ossListener OssListener
     */
    fun putFile(bucketName: String, fileName: String, filePath: String, ossPutListener: OssPutListener) {
        //2、构建PutObjectRequest 请求
        var putObjectRequest = PutObjectRequest(bucketName, fileName, filePath)

        //3、异步上传时可以设置进度回调
        putObjectRequest.setProgressCallback { request, currentSize, totalSize ->
            Log.d("PutObject", "currentSize: $currentSize totalSize: $totalSize")
            ossPutListener.onOssProgress(request, currentSize, totalSize)
        }
        //4、开始上传
        oSSClient.asyncPutObject(putObjectRequest, object :
            OSSCompletedCallback<PutObjectRequest, PutObjectResult> {
            override fun onSuccess(request: PutObjectRequest?, result: PutObjectResult?) {
                ossPutListener.onOssSuccess(request, result, fileName)
            }

            override fun onFailure(
                request: PutObjectRequest?,
                clientException: ClientException?,
                serviceException: ServiceException?
            ) {
                ossPutListener.onOssFailure(request, clientException, serviceException)
            }

        })
    }

    /**
     * 下载文件
     * @param bucketName String  服务器给的
     * @param filePath String    本地文件路径
     * @param ossGetListener OssGetListener
     */
    fun getFile(bucketName: String, filePath: String, ossGetListener: OssGetListener) {
        var get = GetObjectRequest(bucketName, filePath)
        //设置下载进度回调
        get.setProgressListener { request, currentSize, totalSize ->
            OSSLog.logDebug(
                "getobj_progress: $currentSize  total_size: $totalSize",
                false
            )
            ossGetListener.onOssProgress(request, currentSize, totalSize)
        }
        //开始下载
        var task = oSSClient.asyncGetObject(get, object : OSSCompletedCallback<GetObjectRequest, GetObjectResult> {
            override fun onSuccess(request: GetObjectRequest, result: GetObjectResult) {
                var file = File(filePath)
                var fileOutputStream = FileOutputStream(file)
                // 请求成功
                var inputStream = result.objectContent
                var buffer = ByteArray(2048)
                var len: Int
                try {
                    while (inputStream!!.read(buffer).apply { len = this } != -1) {
                        // 处理下载的数据
                        fileOutputStream.write(buffer, 0, len)
                    }
                    fileOutputStream.flush()
                } catch (e: IOException) {
                    e.printStackTrace()
                    ossGetListener.onOssFailure()
                } finally {
                    if (inputStream != null) {
                        inputStream.close()
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close()
                    }
                }
                ossGetListener.onOssSuccess(request,result,filePath)
            }

            override fun onFailure(
                request: GetObjectRequest,
                clientExcepion: ClientException?,
                serviceException: ServiceException?
            ) {
                // 请求异常
                clientExcepion?.printStackTrace()
                if (serviceException != null) {
                    // 服务异常
                    Log.e("ErrorCode", serviceException.errorCode)
                    Log.e("RequestId", serviceException.requestId)
                    Log.e("HostId", serviceException.hostId)
                    Log.e("RawMessage", serviceException.rawMessage)
                    ossGetListener.onOssFailure()
                }
            }
        })
    }


}