package com.example;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

import javax.imageio.ImageIO;

import com.Face.JNIInterface;
import com.sy.utils.ImageFomatTransFor;
import com.sy.utils.JNIDispatcher;

import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;

public class FFDThread implements Runnable {

	//private static final String TAG = FFDThread.class.getSimpleName();
	private static int FACEFEATURESIZE = 512;
	private static int MAXFACECOUNT = 50;
	private static int PARAMSIZE = 61;
	
	private SimpleDateFormat df;
	//private String imagePath;
	/*public FFDThread(){}
	public FFDThread(String imagePath){
		this.imagePath = imagePath;
	}*/
	//灏嗗浘鐗囪浆鎹㈡垚绗﹀悎鐭╅樀鐨凚GR鏍煎紡鐨勫浘鐗�
	public byte[] getPiexelsBGR(BufferedImage image){
		return ImageFomatTransFor.getMatrixBGR(image);
	}
	//娴嬭瘯浜鸿劯鐗瑰緛鎻愬彇BGR鍥惧儚鏂瑰紡
	public void testBGR(JNIInterface jni) throws FileNotFoundException{
		Date d1, d2;
		long diff;
		
		double [] feaA = new double[FACEFEATURESIZE];
		double [] feaB = new double[FACEFEATURESIZE];
		double [] rectarrayA = new double[PARAMSIZE*MAXFACECOUNT];
		double [] rectarrayB = new double[PARAMSIZE*MAXFACECOUNT];
		int facecountA = 0;
		int facecountB = 0;
		
		//A鍥撅細妫�娴嬩汉鑴革紝鎻愬彇鐗瑰緛
		String filePathA = "/usr/local/picture/2.jpg";//浜鸿劯鍥剧墖鐨勪綅缃�
		//FileInputStream fis = new FileInputStream(filePathA);
		BufferedImage bufmapA = getBufferedImage(getImageStr(filePathA));
		byte[] BGRA = getPiexelsBGR(bufmapA);
		//鑾峰彇褰撳墠鏃堕棿
		d1 = new Date(System.currentTimeMillis());
		//鍗曚緥妯″紡鑾峰彇jni
		//JNIInterface jni = JNIDispatcher.getInstance().getJNI();
		facecountA = jni.FDDetect(jni.DetectHandle[0], BGRA, bufmapA.getWidth(), bufmapA.getHeight(), rectarrayA);
		
		//鑾峰彇褰撳墠鏃堕棿
		d2 = new Date(System.currentTimeMillis());
		//杩欐牱寰楀埌鐨勫樊鍊兼槸寰绾у埆
		diff = (d2.getTime() - d1.getTime());
		System.out.println("A: Detect time cost:"  + diff + " " + bufmapA.getWidth() + " " + bufmapA.getHeight());
		if(facecountA > 0){
			//鎵撳嵃鏃ュ織绾у埆锛屽彲浠ョ敤log4j璁板綍锛岀瓑涓嬪啀鏀�
			System.out.println(rectarrayA[0]+" "+rectarrayA[1] + " " + rectarrayA[2] + " " + rectarrayA[3]);
			System.out.println("A: roll:"+rectarrayA[5] + " yaw:" + rectarrayA[6] + " pitch:" + rectarrayA[7]);
			System.out.println("A: quality : " + rectarrayA[4]+" brightness:"+rectarrayA[20] + " clarity:" + rectarrayA[21] + "; ");
			System.out.println("A: ldmk1:"+rectarrayA[10] + " ldmk2:" + rectarrayA[11] + " ldmk3:" + rectarrayA[12] + "; ");
			jni.FFFeaExtract(jni.FeatureHandle[0], BGRA, bufmapA.getWidth(), bufmapA.getHeight(), feaA, rectarrayA);
			d2 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
			diff = (d2.getTime() - d1.getTime());//杩欐牱寰楀埌鐨勫樊鍊兼槸寰绾у埆鐨�
			System.out.println("A: FeaExtract time cost:" + diff);
		}else{
			System.out.println("A: detect no face!");
			return ;
		}
		//////////////////////////////////////////////////////////////////////////////
		//B鍥撅細妫�娴嬩汉鑴革紝鎻愬彇鐗瑰緛
		String filePathB = "/usr/local/picture/2.jpg";//浜鸿劯鍥剧墖鐨勪綅缃�
		//FileInputStream fis = new FileInputStream(filePathA);
		BufferedImage bufmapB = getBufferedImage(getImageStr(filePathB));
		byte[] BGRB = getPiexelsBGR(bufmapB);
		//鑾峰彇褰撳墠鏃堕棿
		d1 = new Date(System.currentTimeMillis());
		//JNIInterface jni1 = JNIDispatcher.getInstance().getJNI();
		facecountB = jni.FDDetect(jni.DetectHandle[0], BGRB, bufmapB.getWidth(), bufmapB.getHeight(), rectarrayB);
		
		//鑾峰彇褰撳墠鏃堕棿
		d2 = new Date(System.currentTimeMillis());
		//杩欐牱寰楀埌鐨勫樊鍊兼槸寰绾у埆
		diff = (d2.getTime() - d1.getTime());
		System.out.println("A: Detect time cost:"  + diff + " " + bufmapA.getWidth() + " " + bufmapA.getHeight());
		if(facecountB > 0){
			//鎵撳嵃鏃ュ織绾у埆锛屽彲浠ョ敤log4j璁板綍锛岀瓑涓嬪啀鏀�
			System.out.println(rectarrayB[0]+" "+rectarrayB[1] + " " + rectarrayB[2] + " " + rectarrayB[3]);
			System.out.println("B: roll:"+rectarrayB[5] + " yaw:" + rectarrayB[6] + " pitch:" + rectarrayB[7]);
			System.out.println("B: quality : " + rectarrayB[4]+" brightness:"+rectarrayB[20] + " clarity:" + rectarrayB[21] + "; ");
			System.out.println("B: ldmk1:"+rectarrayB[10] + " ldmk2:" + rectarrayB[11] + " ldmk3:" + rectarrayB[12] + "; ");
			jni.FFFeaExtract(jni.FeatureHandle[0], BGRB, bufmapB.getWidth(), bufmapB.getHeight(), feaB, rectarrayB);
			d2 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
			diff = (d2.getTime() - d1.getTime());//杩欐牱寰楀埌鐨勫樊鍊兼槸寰绾у埆鐨�
			System.out.println("B: FeaExtract time cost:" + diff);
		}else{
			System.out.println("B: detect no face!");
			return ;
		}
		//浜鸿劯鐩镐技搴︽娴�
		if(facecountA > 0 && facecountB > 0){
			//浜鸿劯鐩镐技搴︽娴嬶紝杩斿洖0~1涔嬮棿鐨勪竴涓猣olat鍊�
			float score = jni.FFSimilarity(feaA, feaB);
			System.out.println("score = :" + score);
		}
	}
	
	/////////////////////////////////////////////////////////////
	//娴嬭瘯浜鸿劯鐗瑰緛鎻愬彇URL鏂瑰紡
	public void test(JNIInterface jni){
		Date d1,d2;
		long diff;
		
		double [] feaA = new double[FACEFEATURESIZE];
		double [] feaB = new double[FACEFEATURESIZE];
		double [] rectarrayA = new double[PARAMSIZE*MAXFACECOUNT];
		double [] rectarrayB = new double[PARAMSIZE*MAXFACECOUNT];
		long facecountA = 0;
		long facecountB = 0;
		
		//A鍥撅細妫�娴嬩汉鑴革紝鎻愬彇鐗瑰緛
		String filePathA = "/usr/local/picture/2.jpg";//浜鸿劯鍥剧墖鐨勪綅缃�
		System.out.println("filePathA:%s" + filePathA);
		df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		d1 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
		
		facecountA = jni.FDDetectpath(jni.DetectHandle[0], filePathA, rectarrayA);
		System.out.println("A: facecount = " + facecountA);
		d2 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
		diff = (d2.getTime() - d1.getTime());//杩欐牱寰楀埌鐨勫樊鍊兼槸寰绾у埆
		System.out.println("A: Detectpath time cost:" + diff);
		for(int i=0; i<facecountA; i++){
			//璁板綍鏃ュ織淇℃伅
			System.out.println("facecount i = " + i + "confidence:" + rectarrayA[60]);
		}
		if(facecountA > 0){
			d1 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
			jni.FFFeaExtractPath(jni.FeatureHandle[0], filePathA, feaA, rectarrayA);
			d2 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
			diff = (d2.getTime() - d1.getTime());//寰绾у埆
			System.out.println("A: FeaExtractPath time cost:" + diff);
		}else{
			System.out.println("A: detect no face!");
			return ;
		}
		//B鍥撅細妫�娴嬩汉鑴革紝鎻愬彇鐗瑰緛
		String filePathB = "/usr/local/picture/2.jpg";//浜鸿劯鍥剧墖鐨勪綅缃�
		d1 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
		facecountB = jni.FDDetectpath(jni.DetectHandle[0], filePathB, rectarrayB);
		d2 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
		diff = (d2.getTime() - d1.getTime());//杩欐牱寰楀埌鐨勫樊鍊兼槸寰绾у埆
		System.out.println("B: Detectpath time cost:" + diff);
		if(facecountB > 0){
			d1 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
			jni.FFFeaExtractPath(jni.FeatureHandle[0], filePathB, feaB, rectarrayB);
			d2 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
			diff = (d2.getTime() - d1.getTime());//杩欐牱寰楀埌鐨勫樊鍊兼槸寰绾у埆
			System.out.println("B: FeaExtractPath time cost:" + diff);
		}else{
			System.out.println("B: detect no face");
			return ;
		}
		if(facecountA > 0 && facecountB > 0){
			float score = jni.FFSimilarity(feaA, feaB);
			System.out.println("score = :" + score);
		}
	}
	/**
	 * function:getFeaBGR
	 * purpose:浠GR鏂瑰紡鑾峰彇鐗瑰緛鍊�
	 * param:JNI
	 * return:double
	 */
    public double[] getFeaBGR(JNIInterface jni)throws FileNotFoundException{
    	Date d1, d2;
		long diff;
		
		double [] feaImage = new double[FACEFEATURESIZE];
		double [] rectarray = new double[PARAMSIZE*MAXFACECOUNT];
		int facecount = 0;
		String imagePath = "/usr/local/picture/2.jpg";//浜鸿劯鍥剧墖鐨勪綅缃�
		BufferedImage bufmap = getBufferedImage(getImageStr(imagePath));
		byte[] BGR = getPiexelsBGR(bufmap);
		//鑾峰彇褰撳墠鏃堕棿
		d1 = new Date(System.currentTimeMillis());
		//鍗曚緥妯″紡鑾峰彇jni
		//JNIInterface jni = JNIDispatcher.getInstance().getJNI();
		facecount = jni.FDDetect(jni.DetectHandle[0], BGR, bufmap.getWidth(), bufmap.getHeight(), rectarray);
		
		//鑾峰彇褰撳墠鏃堕棿
		d2 = new Date(System.currentTimeMillis());
		//杩欐牱寰楀埌鐨勫樊鍊兼槸寰绾у埆
		diff = (d2.getTime() - d1.getTime());
		System.out.println("Image: Detect time cost:"  + diff + " " + bufmap.getWidth() + " " + bufmap.getHeight());
		if(facecount > 0){
			//鎵撳嵃鏃ュ織绾у埆锛屽彲浠ョ敤log4j璁板綍锛岀瓑涓嬪啀鏀�
			System.out.println(rectarray[0]+" "+rectarray[1] + " " + rectarray[2] + " " + rectarray[3]);
			System.out.println("Image: roll:"+rectarray[5] + " yaw:" + rectarray[6] + " pitch:" + rectarray[7]);
			System.out.println("Image: quality : " + rectarray[4]+" brightness:"+rectarray[20] + " clarity:" + rectarray[21] + "; ");
			System.out.println("Image: ldmk1:"+rectarray[10] + " ldmk2:" + rectarray[11] + " ldmk3:" + rectarray[12] + "; ");
			jni.FFFeaExtract(jni.FeatureHandle[0], BGR, bufmap.getWidth(), bufmap.getHeight(), feaImage, rectarray);
			d2 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
			diff = (d2.getTime() - d1.getTime());//杩欐牱寰楀埌鐨勫樊鍊兼槸寰绾у埆鐨�
			System.out.println("Image: FeaExtract time cost:" + diff);
		}else{
			System.out.println("Image: detect no face!");
		}
		return feaImage;
    }
	/**
	 * function:getFeaPath
	 * purpose:浠magePath鏂瑰紡鑾峰彇鐗瑰緛鍊�
	 * param:JNI
	 * return:double
	 */
	public double[] getFeaPath(JNIInterface jni,String imagePath){
		//Date d1,d2;
		//long diff;
		
		double [] feaImage = new double[FACEFEATURESIZE];
		double [] rectarray = new double[PARAMSIZE*MAXFACECOUNT];
		long facecount = 0;
		//闈欐�佸浘鐗囪矾寰勬娴�
		//String filePath = "/usr/local/picture/2.jpg";//浜鸿劯鍥剧墖鐨勪綅缃�
		//System.out.println("filePath:%s" + imagePath);
		//df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		//d1 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
		
		facecount = jni.FDDetectpath(jni.DetectHandle[0], imagePath, rectarray);
		//System.out.println("Image: facecount = " + facecount);
		//d2 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
		//diff = (d2.getTime() - d1.getTime());//杩欐牱寰楀埌鐨勫樊鍊兼槸寰绾у埆
		//System.out.println("Image: Detectpath time cost:" + diff);
		//for(int i=0; i<facecount; i++){
			//璁板綍鏃ュ織淇℃伅
			//System.out.println("facecount i = " + i + "confidence:" + rectarray[60]);
		//}
		if(facecount > 0){
			//d1 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
			jni.FFFeaExtractPath(jni.FeatureHandle[0], imagePath, feaImage, rectarray);
			//d2 = new Date(System.currentTimeMillis());//鑾峰彇褰撳墠鏃堕棿
			//diff = (d2.getTime() - d1.getTime());//寰绾у埆
			//System.out.println("Image: FeaExtractPath time cost:" + diff);
		//}else{
			//System.out.println("Image: detect no face!");
		}
		//System.out.println("浜鸿劯鐗瑰緛鍊艰鍙栵細" + feaImage.toString());
		return feaImage;
	}
	//灏嗘湰鍦板浘鐗囨枃浠惰浆鎴怋ase64瀛楃涓�
		public static String getImageStr(String filePath){
			//灏嗗浘鐗囨枃浠惰浆鍖栦负瀛楄妭鏁扮粍瀛楃涓诧紝骞跺鍏惰繘琛孊ase64缂栫爜澶勭悊
			byte[] data = null;
			//璇诲彇鍥剧墖瀛楄妭鏁扮粍
			try{
				InputStream in = new FileInputStream(filePath);
				data = new byte[in.available()];
				in.read(data);
				in.close();//鍦╢inally涓叧闂洿濂�,绛変笅涓�璧锋敼
			}catch(IOException e){
				e.printStackTrace();
			}
			//瀵瑰瓧鑺傛暟缁凚ase64缂栫爜
			BASE64Encoder encoder = new BASE64Encoder();
			return encoder.encode(data);
		}
		//鏈湴鍥剧墖杞垚InputStream娴佸璞�.闇�瑕佷竴涓弬鏁帮紝base64缂栫爜鐨勫瓧绗︿覆
		public static InputStream baseToInputStream(String base64string){
			ByteArrayInputStream stream = null;
			try{
				BASE64Decoder decoder = new BASE64Decoder();
				byte[] bytes1 = decoder.decodeBuffer(base64string);
				stream = new ByteArrayInputStream(bytes1);
			}catch(Exception e){
				e.printStackTrace();
			}
			return stream;
		}
		//鏈湴鍥剧墖杞垚BufferedImage娴佸璞°�傞渶瑕佷竴涓弬鏁帮紝base64缂栫爜鐨勫瓧绗︿覆
		public static BufferedImage getBufferedImage(String filePath){
			BufferedImage image = null;
			try{
				InputStream stream = baseToInputStream(filePath);
				image = ImageIO.read(stream);
				System.out.println(">>>" + image.getWidth() + "," + image.getHeight() + "<<<");
			}catch(IOException e){
				e.printStackTrace();
			}
			return image;
		}
		public void run() {
			//JNIInterface jniIngerface = new JNIInterface();
			// TODO Auto-generated method stub
			//for(int i = 0 ; i<100; i++){
			//}
				//test(jniIngerface);
			/*try {
				getFeaBGR(jniIngerface);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}*/
			//double[] iamgeFea = getFeaPath(jniIngerface);
			//System.out.println("*****************" + iamgeFea.toString() + "***********************");
			//System.out.println("************" + Arrays.toString(iamgeFea) + "************");
			//System.out.println("********" + "鏁扮粍闀垮害涓猴細 " + iamgeFea.length + "*******************");
			//jniIngerface.Destroy()
		}
}
