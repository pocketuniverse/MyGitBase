package com.objecteye.kafkademo;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.elasticsearch.common.settings.Settings;

import com.google.gson.JsonParser;

import net.sf.json.JSONObject;

/**
 * 该文档同ConsumerDemo1一样，区别与在消费时没有对特征值进行String化
 * 
 * @author zksy
 *
 */
public class ConsumerDemo3 {
	private static String ip;
	private static String bootstrap_servers;
	private static String group_id;
	private static String enable_auto_commit;
	private static String auto_offset_reset;
	private static String auto_commit_interval_ms;
	private static String key_deserializer;
	private static String value_deserializer;
	private static String indexName;
	private static String typeName;
	private static String cluster_name;
	private static String topicName;
	private static String configUrl;
	private static KafkaConsumer<String, String> consumer;

	// json����������
	JsonParser parser = new JsonParser();

	public static void main(String[] args) throws Exception {

		ConsumerDemo3 c3 = new ConsumerDemo3();
		c3.consumer();

	}

	public void consumer() {

		int count = 1;
		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(100);
			for (ConsumerRecord<String, String> record : records) {
				// 判断record.value()是否符合转换json
				if (isJsonObject(record.value())) {
					String source = record.value();
					JSONObject jsonObj = JSONObject.fromObject(source);
					jsonObj.element("offset", record.offset());
					jsonObj.element("key", record.key());

					// TODO此处已经获得json对象jsonObj，可以直接使用

					count++;
					System.out.println("消费了" + count + "条数据！");
				}
			}
		}
	}

	static {
		try {
			// 创建SAXReader对象
			SAXReader reader = new SAXReader();
			// src/main/resources/config.xml /root/config.xml
			Document doc = reader.read("src/main/resources/config.xml");
			Element root = doc.getRootElement();

			List<Element> param = root.elements();
			for (Element element : param) {
				if (element.attributeValue("key").equals("es_ip")) {
					ip = element.getText();
				}
				if (element.attributeValue("key").equals("bootstrap.servers")) {
					bootstrap_servers = element.getText();
				}
				if (element.attributeValue("key").equals("group.id")) {
					group_id = element.getText();
				}
				if (element.attributeValue("key").equals("enable.auto.commit")) {
					enable_auto_commit = element.getText();
				}
				if (element.attributeValue("key").equals("auto.offset.reset")) {
					auto_offset_reset = element.getText();
				}
				if (element.attributeValue("key").equals("auto.commit.interval.ms")) {
					auto_commit_interval_ms = element.getText();
				}
				if (element.attributeValue("key").equals("key.deserializer")) {
					key_deserializer = element.getText();
				}
				if (element.attributeValue("key").equals("value.deserializer")) {
					value_deserializer = element.getText();
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

				if (element.attributeValue("key").equals("topicName")) {
					topicName = element.getText();
				}
				if (element.attributeValue("key").equals("configUrl")) {
					configUrl = element.getText();
				}

			}

			Settings settings = Settings.builder().put("cluster.name", cluster_name).build();

			Properties props = new Properties();
			props.put("bootstrap.servers", bootstrap_servers);
			props.put("group.id", group_id);
			props.put("enable.auto.commit", enable_auto_commit);
			props.put("auto.offset.reset", auto_offset_reset);
			props.put("auto.commit.interval.ms", auto_commit_interval_ms);
			props.put("key.deserializer", key_deserializer);
			props.put("value.deserializer", value_deserializer);
			consumer = new KafkaConsumer<String, String>(props);
			consumer.subscribe(Collections.singletonList(topicName));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 判断传入的String是否符合json格式
	public static boolean isJsonObject(String content) {
		// 此处应该注意，不要使用StringUtils.isEmpty(),因为当content为"
		// "空格字符串时，JSONObject.parseObject可以解析成功，
		// 实际上，这是没有什么意义的。所以content应该是非空白字符串且不为空，判断是否是JSON数组也是相同的情况。
		if (StringUtils.isBlank(content))
			return false;
		try {
			JSONObject jsonStr = JSONObject.fromObject(content);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
