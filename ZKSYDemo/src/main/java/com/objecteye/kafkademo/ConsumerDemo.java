package com.objecteye.kafkademo;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import com.google.gson.JsonParser;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ConsumerDemo {
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

	// json����������
	JsonParser parser = new JsonParser();

	public static void main(String[] args) throws Exception {
		// ���������ļ�����
		SAXReader reader = new SAXReader();
		// src/main/resources/config.xml    /root/config.xml
		Document doc = reader.read("/root/config.xml");
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

		// 创建client
		Settings settings = Settings.builder().put("cluster.name", cluster_name).build();
		Client client = new PreBuiltTransportClient(settings)
				.addTransportAddress(new TransportAddress(InetAddress.getByName(ip), Integer.parseInt("9300")));

		Properties props = new Properties();
		props.put("bootstrap.servers", bootstrap_servers);
		props.put("group.id", group_id);
		props.put("enable.auto.commit", enable_auto_commit);
		props.put("auto.offset.reset", auto_offset_reset);
		props.put("auto.commit.interval.ms", auto_commit_interval_ms);
		props.put("key.deserializer", key_deserializer);
		props.put("value.deserializer", value_deserializer);
		KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
		consumer.subscribe(Collections.singletonList(topicName));

		int i = 1;
		while (true) {
			ConsumerRecords<String, String> records = consumer.poll(100);
			for (ConsumerRecord<String, String> record : records) {
				// 打印获取的信息
				 System.out.printf("offset = %d, key = %s, value = %s%n",record.offset(), record.key(), record.value());

				// 创建es索引
				Map<String, Object> source1 = new HashMap<String, Object>();
				source1.put("offset", record.offset());
				source1.put("key", record.key());
				// 将json包装成字符串
				// 获取value字段的值
				String value = record.value();
				int start = value.indexOf("[");
				int end = value.indexOf("]");
				String cv = value.substring(start, end+1);
//				System.out.println("**********************\n"+newArray+"\n**************************");
				source1.put("charac_value", cv);
				
				JSONObject jso = JSONObject.fromObject(value);  //出现问题的地方TODO				
//				Object cv = jso.get("charac_value");//此处容易发生精度丢失，所以注释
//				source1.put("charac_value", cv);
				Object di = jso.get("device_id");
				source1.put("device_id", di);
				Object pt = jso.get("picture_time");
				source1.put("picture_time", pt);
				Object pu = jso.get("picture_url");
				source1.put("picture_url", pu);
//				 System.out.println("=====================================\n"+cv+"\n"+di+"\n"+pt+"\n"+pu);

				// 创建索引
				IndexRequestBuilder indexRequestBuilder1 = client.prepareIndex(indexName, typeName, i + "")
						.setSource(source1);
				IndexResponse indexResponse1 = indexRequestBuilder1.get();

				i++;
				System.out.println("消费了"+i+"条数据！");
			}
		}
	}
}
