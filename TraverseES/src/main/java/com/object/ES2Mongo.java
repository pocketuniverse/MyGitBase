package com.object;

import java.io.File;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortOrder;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.Face.JNIInterface;
import com.entity.People;
import com.example.FFDThread;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.sy.utils.JNIDispatcher;

import net.sf.json.JSONObject;

@SuppressWarnings("resource")
public class ES2Mongo {

	// ES的客户端对象
	private static Client client;
	private static String ip; // ES服务器ip地址
	private static String indexName;
	private static String typeName;
	private static String cluster_name;
	private JSONObject jso;
	private Object offset;
	private Object cv;
	private Object di;
	private Object pt;
	private Object pu;
	private Object key;
	private static OutputStream out = null;
	private static File file = null;
	private People peo;
	private MongoCollection<org.bson.Document> collection;
	// 获取特征值
	private double[] source_cv = null;
	// 对传入图片特征码的提取
	private static double[] input_cv = null;
	private double like;// 相似度
	private long string2double;
	private long trace_TimeMillis;

	public static void main(String[] args) {
		ES2Mongo em = new ES2Mongo();
		em.getMongodb();

		long totalstart = System.currentTimeMillis();
		List<People> list = em.comparison_mongo(args[0], Double.parseDouble(args[1]));
		// 调用排序规则
		Comparator<People> comparator = em.getComparator();
		list.sort(comparator);
		Iterator<People> it = list.iterator();
		while (it.hasNext()) {
			People people = (People) it.next();
			System.out.println("****相似度为：" + people.getLikeValue() + "----图片地址为：" + people.getPicture_url());
		}
		System.out.println("循环总共用时：" + em.trace_TimeMillis + "毫秒");
		System.out.println("总共用时：" + (System.currentTimeMillis() - totalstart) + "毫秒");
		System.out.println("符合要求的图片有" + list.size() + "张");
		System.out.println("转换共耗时：" + em.string2double + "毫秒");
	}

	// 遍历mongodb
	public void tra_es() {
		// 创建list数组，存放符合要求的people对象
		// List<People> list = new ArrayList<People>();

		// 指定一个index和type
		SearchRequestBuilder search = client.prepareSearch(indexName).setTypes(typeName);
		// 使用原生排序优化性能
		search.addSort("_doc", SortOrder.ASC);
		// 设置每批读取的数据量(<=10000)
		search.setSize(10000);
		// 默认是查询所有
		search.setQuery(QueryBuilders.queryStringQuery("*:*"));
		// 设置 search context 维护1分钟的有效期
		search.setScroll(TimeValue.timeValueMinutes(10));

		// 获得首次的查询结果
		SearchResponse scrollResp = search.get();
		// 打印命中数量
		System.out.println("共寻址了" + scrollResp.getHits().getTotalHits() + "张图片");
		// ===================================
		int i = 1;

		do {

			// 读取结果集数据
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				String source = hit.getSourceAsString();
				// System.out.println("==========================\n"+source+"\n====================");

				jso = JSONObject.fromObject(source);
				// 获取剩余字段的值
				cv = jso.get("charac_value").toString();
				offset = jso.get("offset");
				di = jso.get("device_id");
				pt = jso.get("picture_time");
				pu = jso.get("picture_url");
				key = jso.get("key");

				if (key != null) {
					insertData(offset.toString(), key.toString(), pu.toString(), cv.toString(), di.toString(),
							pt.toString());
				} else {
					insertData(offset.toString(), null, pu.toString(), cv.toString(), di.toString(), pt.toString());
				}

				// list.add(peo);
				System.out.println("正在打印第" + i + "条数据");
				i++;
			}

			// 将scorllId循环传递
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(TimeValue.timeValueMinutes(10))
					.execute().actionGet();

			// 当searchHits的数组为空的时候结束循环，至此数据全部读取完毕
		} while (scrollResp.getHits().getHits().length != 0);

	}

	// 获取mongodb的对象
	public void getMongodb() {
		// // 连接到MongoDB服务 如果是远程连接可以替换“localhost”为服务器所在IP地址
		// // ServerAddress()两个参数分别为 服务器地址 和 端口
		// ServerAddress serverAddress = new ServerAddress("192.168.10.30",
		// 27017);
		// List<ServerAddress> addrs = new ArrayList<ServerAddress>();
		// addrs.add(serverAddress);
		//
		// // MongoCredential.createScramSha1Credential()三个参数分别为 用户名 数据库名称 密码
		// MongoCredential credential =
		// MongoCredential.createScramSha1Credential("vita", "admin",
		// "123456".toCharArray());
		// List<MongoCredential> credentials = new ArrayList<MongoCredential>();
		// credentials.add(credential);
		//
		// // 通过连接认证获取MongoDB连接
		// MongoClient mongoClient = new MongoClient(addrs, credentials);
		//
		// // 连接到数据库
		// MongoDatabase mongoDatabase = mongoClient.getDatabase("vitadb");

		// 第二种通过ip+port的方式链接
		// 连接到mongodb服务
		MongoClient mongoClient = new MongoClient("192.168.10.56", 27017);

		// 连接到数据库
		MongoDatabase mongoDatabase = mongoClient.getDatabase("object");

		collection = mongoDatabase.getCollection("objecteye");

	}

	// mongodb循环遍历
	public void findAll() {
		// 循环遍历
		FindIterable<org.bson.Document> find = collection.find();
		MongoCursor<org.bson.Document> it = find.iterator();
		while (it.hasNext()) {
			org.bson.Document document = (org.bson.Document) it.next();
			System.out.println(document);
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
		int i = 1;
		long mg_startTime = System.currentTimeMillis();
		while (it.hasNext()) {
			org.bson.Document document = (org.bson.Document) it.next();

			String source = document.get("charac_value").toString();
			String cv = source.substring(1, source.length() - 1);
			// 将string类型的特征值转换为double[]
			String[] split = cv.toString().split(",");
			double[] source_cv = new double[split.length];// 存放十万条数据中的每一个特征值
			for (int j = 0; j < split.length; j++) {
				source_cv[j] = Double.parseDouble(split[j]);
			}
			// =================================================================

			like = jni.FFSimilarity(input_cv, source_cv);
			// System.out.println("相似度："+like);

			if (like >= likeValue) {
				// 获取剩余字段的值
				offset = document.get("offset");
				di = document.get("device_id");
				pt = document.get("picture_time");
				pu = document.get("picture_url");
//				key = document.get("key");

				peo = new People(offset.toString(), di.toString(), pu.toString(), cv, pt.toString(), like);
				list.add(peo);
				// System.out.println("正在打印第"+i+"条数据");
				i++;
			}

		}
		trace_TimeMillis = System.currentTimeMillis() - mg_startTime;
		return list;
	}

	// mongodb插入数据
	public void insertData(String offset, String key, String pu, String cv, String di, String pt) {
		// 插入文档
		/**
		 * 1. 创建文档 org.bson.Document 参数为key-value的格式 2. 创建文档集合List<Document> 3.
		 * 将文档集合插入数据库集合中 mongoCollection.insertMany(List<Document>) 插入单个文档可以用
		 * mongoCollection.insertOne(Document)
		 */
		org.bson.Document document = new org.bson.Document("offset", offset).append("key", key).append("device_id", di)
				.append("picture_url", pu).append("charac_value", cv).append("picture_time", pt);

		List<org.bson.Document> documents = new ArrayList<org.bson.Document>();
		documents.add(document);
		collection.insertMany(documents);
		System.out.println("文档插入成功");
	}

	/**
	 * 获取Transport client
	 */
	static {
		SAXReader reader = new SAXReader();
		Document doc;
		try {
			doc = reader.read("/root/esconfig.xml");
			// doc = reader.read("src/main/resources/esconfig.xml");

			Element root = doc.getRootElement();

			@SuppressWarnings("unchecked")
			List<Element> param = root.elements();
			for (Element element : param) {
				if (element.attributeValue("key").equals("es_ip")) {
					ip = element.getText();
				}

				if (element.attributeValue("key").equals("indexName")) {
					indexName = element.getText();
				}
				if (element.attributeValue("key").equals("typeName")) {
					typeName = element.getText();
				}
				if (element.attributeValue("key").equals("cluster.name")) {
					cluster_name = element.getText();
				}

			}

			// ElasticSearch服务默认端口9300

			Settings settings = Settings.builder().put("cluster.name", cluster_name).build();

			client = new PreBuiltTransportClient(settings)
					.addTransportAddress(new TransportAddress(InetAddress.getByName(ip), 9300));

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

	// 排序规则
	public Comparator<People> getComparator() {
		Comparator<People> comparator = new Comparator<People>() {
			public int compare(People s1, People s2) {
				// 先排年龄
				if (s1.getLikeValue() > s2.getLikeValue()) {
					return -1;
				} else if (s1.getLikeValue() < s2.getLikeValue()) {
					return 1;
				}
				return 0;
			}
		};
		return comparator;
	}
}
