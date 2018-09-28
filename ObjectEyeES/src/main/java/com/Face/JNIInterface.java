package com.Face;

public class JNIInterface {
	static{	
		//.so文件是linux下的库文件，对应于windows下的.dll文件
		//System.load("/home/jdserver/local/mylib/liboil_jni.so");
		/*
		 * 人脸检测
		 */
		//System.load("/home/jdserver/local/mylib/libFaceDetectJNI.so");
		//System.load("/home/jdserver/local/mylib/libFaceDetect.so");
		//System.load("/home/jdserver/local/mylib/libFacialFeaPoint.so");
		/*
		 * 人脸识别
		 */
		//System.load("/home/jdserver/local/mylib/libFaceFeature.so");
		//System.load("/home/jdserver/local/mylib/libFaceFeatureJNI.so");
		System.loadLibrary("FaceDetectJNI");
		System.loadLibrary("FaceFeatureJNI");
		System.loadLibrary("FacialFeaPoint");
		System.loadLibrary("FaceDetect");
		System.loadLibrary("FaceFeature");
	}
	
	//用于存储C++层的对象指针
	//private int counter;
	//private long address;
	
	//测试时写入固定值，实际应用时可以将这些参数封装成对应的Javabean
	private int mode;  //运行模式 GPU_MODE (10) 或者 CPU_MODE (11) 本版本支持CPU模式，设置为11
	private int gpuid;  //显卡号 （本版本不支持 默认设置为0）
	private long[] handles;  //句柄，handles的第一个元素为有效值
	private int processMinL;  //短边 建议360
	private int processMaxL;  //长边 建议2000
	private float detectThresld;  //阈值 建议0.7
	
	public long[] DetectHandle = new long[1];  //检测资源释放
	public long[] FeatureHandle = new long[1];  //识别资源释放
	//模型初始化
	public JNIInterface(){
		//System.out.println("JNI init ....");
		int flag = FDInit(11,0,DetectHandle,360,2000,0.7f);
		if(flag<0){
			//这里用log记录日志信息
			//System.out.println("人脸检测初始化失败，请查看具体出错信息");
			return ;
		//}else{
		//	System.out.println("人脸检测初始化成功！");
		}
		flag = FFInit(11,0,FeatureHandle);
		if(flag<0){
			//这里用log记录日志信息
			//System.out.println("人脸识别初始化失败，请查看具体出错信息");
			return ;
		//}else{
		//	System.out.println("人脸识别初始化成功！");
		}
		//System.out.println("DetectHandle = " + DetectHandle[0]);
		//System.out.println("FeatureHandle = " + FeatureHandle[0]);
		//System.out.println("FDVersion = " + FDgetVersion());
		//System.out.println("FFVersion = " + FFgetVersion());
		
	}
	//释放检测和识别的资源
	public void Destroy(){
		FDDestroy(DetectHandle[0]);
		FFDestroy(FeatureHandle[0]);
	}
    /**
     * 人脸检测
     */
    /**
     * function:FDInit()
     * purpose:资源初始化
     * param:
     * mode:  -- 运行模式 GPU_MODE (10) 或者 CPU_MODE (11) 本版本支持CPU模式，设置为11
     * gpuid:  -- 显卡号 （本版本不支持 默认设置为0）
     * handles:  -- 句柄，handles的第一个元素为有效值
     * processMinL:  -- 短边 建议360
     * processMaxL:  -- 长边 建议2000
     * detectThresld:  -- 阈值 建议0.7
     * return:int  --成功(0)或错误代码(<0)
     * */
    public native synchronized int FDInit(int mode, int gpuid, long[] handles, int processMinL, int processMaxL, float detectThresld);
    /**
     * function:FDDestroy()
     * purpose:资源释放
     * param:
     * handles:  --句柄
     * */
    public native synchronized void  FDDestroy(long handle);
    /**
     * function:FDgetVersion()
     * purpose:获取版本号
     * param: NULL
     * return: 相似度值
     * */
    public native synchronized String FDgetVersion();
    /**
     * function:FDDetect()
     * purpose:人脸检测BGR图像方式
     * param:
     * handle:  --句柄
     * bgr:  --bgr元素
     * width:  --宽
     * height: --高
     * rect: --检测结果
     * return:  --人脸个数
     * */
    public native synchronized int FDDetect(long handle, byte[] bgr, int width , int height, double[] rect);
    /**
     * fucntion:FDDetectpath()
     * purpose:人脸特征提取，图片路径方式
     * param:
     * hadnle:  --句柄
     * imgpath:  --图片路径
     * width:  --宽
     * height:  --高
     * rect:  --人脸检测结果
     * return:  --人脸个数
     * */
    public native synchronized long FDDetectpath(long handle, String imgpath, double[] rect);
    /**
     * 人脸识别
     */
    /**
     * function:FFInit()
     * purpose:初始化
     * param:
     * mode:  --运行模式
     * gpuid:  --显卡号
     * handles:  --句柄，handles的第一个元素为有效值
     * return:  --成功(0)或错误代码(<0)
     * 
     */
    public native synchronized int FFInit(int mode, int gpuid, long[] handles);
    /**
     * function:FFDestroy()
     * purpose:资源释放
     * param:
     * handles:  --句柄
     * return:  --成功(0)或错误代码(<0)
     */
    public native synchronized void FFDestroy(long handles);
    /**
     * function:FFFeaExtract()
     * purpose:提取特征
     * param:
     * handle:  --句柄
     * bgr:  --bgr元素
     * width:  --宽
     * height:  --高
     * fea:  --特征值
     * rect:  --人脸检测结果(依赖于人脸检测SDK)
     * return:  --成功(0)或错误代码(<0)
     */
    public native synchronized int FFFeaExtract(long handle, byte[] bgr, int width, int height, double[] fea, double[] rect);
    /**
     * function:FFFeaExtractPath()
     * purpose:提取特征
     * param:
     * handle:  --句柄
     * imgpath:  --图片路径
     * width:  --宽
     * height:  --高
     * fea:  --特征值
     * rect:  --人脸检测结果(依赖于人脸检测SDK)
     */
    public native synchronized int FFFeaExtractPath(long handle, String imgpath, double[] fea, double[] rect);
    /**
     * function:FFSimilarity()
     * purpose:人脸对比
     * param:
     * feaA:  --图片A的特征
     * feaB:  --图片B的特征
     * return:  相似度值
     * 
     */
    public native synchronized float FFSimilarity(double[] feaA, double[] feaB);
    /**
     * fucntion:FFgetVersion()
     * purpose:版本号
     * param:  NULL
     * return:  版本号
     */
    public native synchronized String FFgetVersion();
}
