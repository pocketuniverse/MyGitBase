package com.esdemo;

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
import com.sy.utils.JNIDispatcher;

import net.sf.json.JSONObject;

public class ESSeleclt {

	// ES的客户端对象
	private static Client client;
	private static String ip; // ES服务器ip地址
	private static String indexName;
	private static String typeName;
	private static String cluster_name;
	private JSONObject jso;
	private Object offset;
	private String cv;
	private Object di;
	private Object pt;
	private Object pu;
	private Object key;
	private People peo;
	private static OutputStream out = null;
	private static File file = null;
	// 获取特征值
	private double[] source_cv = null;
	// 对传入图片特征码的提取
//	public static String imagePath = null;
	private static double[] input_cv = null;

	// 相似度调用
	private double like;

	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		
		ESSeleclt tres = new ESSeleclt();
		tres.init();
		List<People> list = tres.tra_es(args[0],Double.parseDouble(args[1]));
		//调用排序规则
		Comparator<People> comparator = tres.getComparator();
		list.sort(comparator);
		//遍历list集合
		Iterator<People> it = list.iterator();
		while (it.hasNext()) {
			People people = (People) it.next();

			// 获取要写出的数据
			String message = "相似度：" + people.getLikeValue() + "=====" + "地址：" + people.getPicture_url();
			System.out.println("**" + message);

		}
		System.out.println("共耗时："+(System.currentTimeMillis()-startTime)+"毫秒");
		System.out.println("共找到"+list.size()+"条符合要求的图片");
		
	}

	/**
	 * 获取Transport client
	 */
	public void init() {
		SAXReader reader = new SAXReader();
		Document doc;
		try {
			doc = reader.read("/root/esconfig.xml");

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


	// 遍历es
	public List<People> tra_es(String picPath,double likeValue) {
		//获取传入图片的特征码
		input_cv = new FFDThread().getFeaPath(JNIDispatcher.getInstance().getJNI(), picPath);
		// 判断两个double[]的相似度对象
		JNIInterface jni = JNIDispatcher.getInstance().getJNI();
		// 创建list数组，存放符合要求的people对象
		List<People> list = new ArrayList<People>();

		// 指定一个index和type
		SearchRequestBuilder search = client.prepareSearch(indexName).setTypes(typeName);
		// 使用原生排序优化性能
		search.addSort("_doc", SortOrder.ASC);
		// 设置每批读取的数据量(<=10000)
		search.setSize(1500);
		// 默认是查询所有
		search.setQuery(QueryBuilders.queryStringQuery("*:*"));
		// 设置 search context 维护1分钟的有效期
		search.setScroll(TimeValue.timeValueMinutes(1));

		// 获得首次的查询结果
		SearchResponse scrollResp = search.get();
		// 打印命中数量
		System.out.println("共寻址了" + scrollResp.getHits().getTotalHits()+"张图片");
		// ===================================
		
		do {

			// 读取结果集数据
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				String source = hit.getSourceAsString();
				
				int start = source.indexOf("[");
				int end = source.indexOf("]");
				String cv = source.substring(start+1, end);
				
				// 将string类型的特征值转换为double[]
				String[] split = cv.toString().split(",");
				double[] source_cv = new double[split.length];// 存放十万条数据中的每一个特征值

				for (int j = 0; j < split.length; j++) {
					source_cv[j] = Double.parseDouble(split[j]);
				}
				
				// 根据特征值返回相似度

				like = jni.FFSimilarity(input_cv, source_cv);
				// 打印每张图片的相似度
//				System.out.println(count+"============相似度为：" + like);
				if (like >= likeValue) {
					// 获取剩余字段的值
					JSONObject jso = JSONObject.fromObject(source);
					offset = jso.get("offset");
					di = jso.get("device_id");
					pt = jso.get("picture_time");
					pu = jso.get("picture_url");
					key = jso.get("key");
					
					if(key != null){
						peo = new People(offset.toString(), di.toString(), pu.toString(), cv, pt.toString(),key.toString(), like);
					}else{
						peo = new People(offset.toString(), di.toString(), pu.toString(), cv, pt.toString(),null, like);
					}
						
						
					list.add(peo);
				}				
			}
			

			// 将scorllId循环传递
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(TimeValue.timeValueMinutes(1))
					.execute().actionGet();

			// 当searchHits的数组为空的时候结束循环，至此数据全部读取完毕
		} while (scrollResp.getHits().getHits().length != 0);
		return list;
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
