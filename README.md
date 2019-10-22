
# Oss对象存储，上传，下载依赖库,持续更新




[![](https://jitpack.io/v/zxyUncle/ZxyOOS.svg)](https://jitpack.io/#zxyUncle/ZxyOOS)

Gradle   
-----   

    Step 1   
	 allprojects {   
		repositories {   
			...
			maven { url 'https://jitpack.io' }   
		}   
	} 

  

Step 2. Add the dependency   

        implementation 'com.github.zxyUncle:ZxyOOS:Tag'   

## 使用   

        OssUtils.instance.initOSS()//初始化   
        OssUtils.instance.putFile()//上传   
        OssUtils.instance.getFile()//下载   

##示例   

     /**
     * 初始化
     * @param mContext Context
     * @param endpoint String   "http://oss-cn-hangzhou.aliyuncs.com"
     * @param stsServer String  "STS应用服务器地址，例如http://abc.com
     * @return OSSClient
     */
     fun initOSS(mContext: Context, endpoint: String, stsServer: String): OSSClient {
    /**
     * 上传文件
     * @param bucketName String 服务器给的
     * @param fileName String   文件在oss服务器上的路径+文件的名字+拓展名
     * @param filePath String   文件在手机上的路径
     * @param ossListener OssListener
     */
     fun putFile(bucketName: String, fileName: String, filePath: String, ossPutListener: OssPutListener) {
     /**
     * 下载文件
     * @param bucketName String  服务器给的
     * @param filePath String    本地文件路径
     * @param ossGetListener OssGetListener
     */
    fun getFile(bucketName: String, filePath: String, ossGetListener: OssGetListener) {

##更新1.0：   
1、上传下载文件   
 
