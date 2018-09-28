package com.object;

import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import com.Face.JNIInterface;
import com.entity.People;
import com.example.FFDThread;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.sy.utils.JNIDispatcher;

public class TraverseMongodb {

	// ES的客户端对象
	private static String ip; // mongo服务器ip地址
	private static int port; // mongo端口号
	private static String dbName; // mongo数据库名称
	private static String collectionName; // collection名称
	private static MongoCollection<Document> collection; // mongodb的collection对象
	private double[] source_cv = null; // 获取外部图片特征值
	private static double[] input_cv = null; // 对传入图片特征码的提取
	private double like;// 相似度

	private Object offset;
	private String cv;
	private Object di;
	private Object pt;
	private Object pu;

	private People peo;

	private long trace_TimeMillis; // 循环遍历耗时
	private long time1; // 获取特征值时间
	private long time2; // 切分时间
	private long time3; // 转换时间
	private long time4; // 比对时间
	private long time5; // 封装时间

	public static void main(String[] args) {
		// TODO
		TraverseMongodb trmg = new TraverseMongodb();
		
		 long start = System.currentTimeMillis();
		 List<People> list = trmg.comparison_mongo(args[0],Double.parseDouble(args[1]));
		 System.out.println("共检索出" + list.size() + "条数据");
		 System.out.println("共耗时" + (System.currentTimeMillis() - start) + "毫秒");
		 
		 System.out.println("循环用时：" + trmg.trace_TimeMillis + "毫秒");
		 System.out.println("获取特征值用时：" + trmg.time1 + "毫秒");
		 System.out.println("切分用时：" + trmg.time2 + "毫秒");
		 System.out.println("转换用时：" + trmg.time3 + "毫秒");
		 System.out.println("比对用时：" + trmg.time4 + "毫秒");
		 System.out.println("封装用时：" + trmg.time5 + "毫秒");

		// 插入数据
//		 trmg.insertData();
		
		//遍历数据
//		trmg.trave();

	}

	// 获取mongodb的collection对象
	static {
		// 通过外部xml加载信息
		SAXReader reader = new SAXReader();
		try {
//			org.dom4j.Document doc = reader.read("src/main/resources/mongodbconfig.xml");
			org.dom4j.Document doc = reader.read("./mongodbconfig.xml");

			Element root = doc.getRootElement();

			@SuppressWarnings("unchecked")
			List<Element> param = root.elements();
			for (Element element : param) {
				if (element.attributeValue("key").equals("mongo_ip")) {
					ip = element.getText();
				}

				if (element.attributeValue("key").equals("mongo_port")) {
					port = Integer.parseInt(element.getText());
				}
				if (element.attributeValue("key").equals("mongodbName")) {
					dbName = element.getText();
				}
				if (element.attributeValue("key").equals("collectionName")) {
					collectionName = element.getText();
				}

			}
			// 连接到mongodb服务
			@SuppressWarnings("resource")
			MongoClient mongoClient = new MongoClient(ip, port);

			// 连接到数据库
			MongoDatabase mongoDatabase = mongoClient.getDatabase(dbName);

			collection = mongoDatabase.getCollection(collectionName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 从mongodb检索数据进行比对
	public List<People> comparison_mongo(String picPath, double likeValue) {
		// 获取传入图片的特征码
		input_cv = new FFDThread().getFeaPath(JNIDispatcher.getInstance().getJNI(), picPath);
		// 判断两个double[]的相似度对象
		JNIInterface jni = JNIDispatcher.getInstance().getJNI();
		// 创建list数组，存放符合要求的people对象
		List<People> list = new ArrayList<People>();

		// 循环遍历
		FindIterable<org.bson.Document> find = collection.find();
		MongoCursor<org.bson.Document> it = find.iterator();

		long mg_startTime = System.currentTimeMillis();
		while (it.hasNext()) {
			Document document = (Document) it.next();

			long start1 = System.currentTimeMillis();
			String source = document.get("charac_value").toString();
			time1 += (System.currentTimeMillis() - start1);

			long start2 = System.currentTimeMillis();
			cv = source.substring(1, source.length() - 1);
			// 将string类型的特征值转换为double[]
			String[] split = cv.toString().split(",");
			time2 += (System.currentTimeMillis() - start2);

			long start3 = System.currentTimeMillis();
			source_cv = new double[split.length];// 存放十万条数据中的每一个特征值
			for (int j = 0; j < split.length; j++) {
				source_cv[j] = Double.parseDouble(split[j]);
			}
			time3 += (System.currentTimeMillis() - start3);
			// =================================================================

			long start4 = System.currentTimeMillis();
			// 比对
			like = jni.FFSimilarity(input_cv, source_cv);
			time4 += (System.currentTimeMillis() - start4);
			// System.out.println("相似度："+like);

			// 封裝時間
			long start5 = System.currentTimeMillis();
			if (like >= likeValue) {
				// 获取剩余字段的值
				offset = document.get("offset");
				di = document.get("device_id");
				pt = document.get("picture_time");
				pu = document.get("picture_url");

				peo = new People(offset.toString(), di.toString(), pu.toString(), cv, pt.toString(), like);

				list.add(peo);
			}
			time5 += (System.currentTimeMillis() - start5);
		}
		trace_TimeMillis = System.currentTimeMillis() - mg_startTime;
		return list;
	}

	// mongodb插入数据
	public void insertData() {
		// 插入文档
		
		// for (int i = 1; i <= 100; i++) {
		// double[] d = new double[] { 1.1 + i, 2.4 + i, 5.6 + i, 0.2 + i };
		//
		// Document document = new Document("key", i).append("charac_value", d);
		//
		// List<Document> documents = new ArrayList<Document>();
		// documents.add(document);
		// collection.insertMany(documents);
		// System.out.println("文档插入成功");
		// }

		double[] d = new double[]{23.5,21.2,45.3,65.6};
		Object obj = d;
		Document document = new Document("key", 1111).append("charac_value", obj);

		List<Document> documents = new ArrayList<Document>();
		documents.add(document);
		collection.insertMany(documents);
		System.out.println("文档插入成功");
	}

	// 遍历mongodb
	public void trave() {
		// 循环遍历
		FindIterable<org.bson.Document> find = collection.find();
		MongoCursor<org.bson.Document> it = find.iterator();
		
		while (it.hasNext()) {
			Document document = (Document) it.next();
			Object object = document.get("charac_value");
			
			if(object instanceof double[]){
				System.out.println("yes");
			}else{
				System.out.println("no");
			}
	
		}
	}

	//=============================================================================
	// 从mongodb检索数据进行比对
	public List<People> comp_mongo(String picPath, double likeValue) {
		// 获取传入图片的特征码
		input_cv = new FFDThread().getFeaPath(JNIDispatcher.getInstance().getJNI(), picPath);
		// 判断两个double[]的相似度对象
		JNIInterface jni = JNIDispatcher.getInstance().getJNI();
		// 创建list数组，存放符合要求的people对象
		List<People> list = new ArrayList<People>();

		// 循环遍历
		FindIterable<org.bson.Document> find = collection.find();
		MongoCursor<org.bson.Document> it = find.iterator();

		while (it.hasNext()) {
			Document document = (Document) it.next();
			
			String source = document.get("charac_value").toString();

			cv = source.substring(1, source.length() - 1);
			// 将string类型的特征值转换为double[]
			String[] split = cv.toString().split(",");

			source_cv = new double[split.length];// 存放十万条数据中的每一个特征值
			for (int j = 0; j < split.length; j++) {
				source_cv[j] = Double.parseDouble(split[j]);
			}

			// 比对
			like = jni.FFSimilarity(input_cv, source_cv);
			// 封裝時間
			if (like >= likeValue) {
				// 获取剩余字段的值
				offset = document.get("offset");
				di = document.get("device_id");
				pt = document.get("picture_time");
				pu = document.get("picture_url");

				peo = new People(offset.toString(), di.toString(), pu.toString(), cv, pt.toString(), like);

				list.add(peo);
			}
		}
		return list;
	}
}
