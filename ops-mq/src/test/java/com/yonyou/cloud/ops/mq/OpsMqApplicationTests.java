package com.yonyou.cloud.ops.mq;

import java.util.HashMap;
import java.util.Map;

import org.elasticsearch.action.get.MultiGetRequestBuilder;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.pagehelper.Page;
import com.xiaoleilu.hutool.json.JSONObject;
import com.xiaoleilu.hutool.json.JSONUtil;
import com.xiaoleilu.hutool.util.MapUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import com.yonyou.cloud.common.beans.PageResultResponse;
import com.yonyou.cloud.ops.mq.dao.MqDataApi;
import com.yonyou.cloud.ops.mq.entity.MqData;
import com.yonyou.cloud.ops.mq.entity.MqProducer;
import com.yonyou.cloud.ops.mq.service.MqProducerService;

@RunWith(SpringRunner.class)
@SpringBootTest
public class OpsMqApplicationTests {
	@Autowired
	private TransportClient client;

	@Test
	public void contextLoads() {
//		MultiGetRequestBuilder builder = client.prepareMultiGet();
//		//builder.add("demo_mq", "track","");
//		builder.add("demo_mq", "track","*");
//		client.prepareMultiGet().
		SearchResponse searchResponse  = client.prepareSearch("demo_mq")
		.setTypes("track")
		.setQuery(QueryBuilders.matchAllQuery())
		.setSearchType(SearchType.QUERY_THEN_FETCH)  
        .setFrom(10).setSize(10)//分页  
        .addSort("@timestamp", SortOrder.DESC)//排序  
        .get();  
		
		SearchHits hits = searchResponse.getHits();  
        long total = hits.getTotalHits();  
        System.out.println(total);  
        SearchHit[] searchHits = hits.getHits();  
        for(SearchHit s : searchHits)  
        {  
        	
        	String properties = s.getSource().get("properties").toString();
        	System.out.println(properties);
        	System.out.println(fromStr(properties).get("column1"));
        }  
		
		
//		MultiGetResponse responses = builder.get();
//		System.out.println(responses.getResponses());
//		responses.forEach(response -> System.out.println(response.getResponse().getSource().get("properties")));
	}
	
	public Map<String,String> fromStr(String str){
		Map<String, String> strMap = new HashMap<String, String>();  
		String[] strs = str.split(",");
		for(String string : strs){
			String[] ss = string.trim().split("=");
			strMap.put(ss[0], ss[1]);
		}
		
		return strMap;
		
	}
	
	
	@Autowired
	MqDataApi mqDataApi;

	
	@Test
	public void test2(){
		PageResultResponse<MqData> page = mqDataApi.pageQueryMqData(1, 20, "demo_mq", "track", "properties.column1:9ef524e9*", "@timestamp", "desc");
		System.out.println(page.getData());
		
	}
	
	@Autowired
	MqProducerService mqProducerService;
	
	
	@Test
	public void test3() throws JsonProcessingException{
//		System.out.println("data===="+mqProducerService.selectOne("mq", "msgKey:cf2b37f5-7cf4-4391-8aeb-5eac8a17da84"));
//		System.out.println("data===="+mqProducerService.selectList("mq", "msgKey:cf2b37f5-7cf4-4391-8aeb-5eac8a17da84"));
		
		MqProducer pro = new MqProducer();
		pro.setData("666666");
		pro.setHost("10");
		pro.setMsg("msg");
		pro.setSuccess("false");
		pro.setMsgKey("bbbb");
		
//		mqProducerService.insert("mq", pro);
		
//		mqProducerService.update("mq",pro, "AV_JMnIaZG64QQzzpAPt");
		
//		System.out.println("data===="+mqProducerService.selectOne("mq", "_type:"));
		System.out.println("dataList===="+mqProducerService.selectList("mq", "msg:msg AND msgKey:bb*").size());
		
	}
}