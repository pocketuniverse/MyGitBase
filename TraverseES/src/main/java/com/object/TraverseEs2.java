package com.object;

import java.io.File;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
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
import com.entity.Picture;
import com.example.FFDThread;
import com.sy.utils.JNIDispatcher;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class TraverseEs2 {

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
	// 获取特征值
	private double[] source_cv = null;
	// 对传入图片特征码的提取
	// public static String imagePath = null;
	private static double[] input_cv = null;

	// 相似度调用
	private double like;

	// String转double[]的总耗时
	private long string2double;
	// jni总耗时
	private long start_total_jni;
	// 比对装入集合总用时
	private long start_total_like;

	public static void main(String[] args) {

		TraverseEs2 tres = new TraverseEs2();
		tres.init();
		List<Picture> list = tres.tra_es();

		System.out.println("共找到" + list.size() + "条符合要求的图片");
		System.out.println("string转double共用时：" + tres.string2double + "毫秒");

	}

	/**
	 * 获取Transport client
	 */
	public void init() {
		SAXReader reader = new SAXReader();
		Document doc;
		try {
			doc = reader.read("src/main/resources/esconfig.xml");

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
	public List<Picture> tra_es() {
		// 创建list数组，存放符合要求的people对象
		List<Picture> list = new ArrayList<Picture>();

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
		long start_string = System.currentTimeMillis();
		do {

			// 读取结果集数据
			for (SearchHit hit : scrollResp.getHits().getHits()) {
				String source = hit.getSourceAsString();
				// System.out.println("==========================\n"+source+"\n====================");

				jso = JSONObject.fromObject(source);
				// 获取剩余字段的值
				offset = jso.get("offset");
				cv = jso.get("charac_value");
				di = jso.get("device_id");
				pt = jso.get("picture_time");
				pu = jso.get("picture_url");
				key = jso.get("key");

				Picture picture = new Picture(pu.toString(), cv.toString());
				list.add(picture);

				System.out.println("第" + i + "次打印信息");
				i++;

			}
			string2double += System.currentTimeMillis() - start_string;
			// 将scorllId循环传递
			scrollResp = client.prepareSearchScroll(scrollResp.getScrollId()).setScroll(TimeValue.timeValueMinutes(10))
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
