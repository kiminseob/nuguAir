package com.JISeopHi.nuguAir;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.JISeopHi.*;


public class NuguAir{
	
	//NUGU에서 요청한 값이 전부 들어있는지 여부 확인
	private static final int True = 1;
	private static final int False = -1;
	    
	//NUGU 변수
	public String origin_point;
	public String destination_point;
    public String origin_month;
    public String origin_day;
    
   
    //NUGU에서 요청한 request Parsing
	public int NUGU_requestParsing(JSONObject body) throws ParseException {
		//json parser
		JSONParser jsonParser = new JSONParser();
		try {
			//request parsing
			JSONObject bodyObj = (JSONObject) jsonParser.parse(body.toString());
			JSONObject actionObj = (JSONObject) jsonParser.parse(bodyObj.get("action").toString());
			
	        JSONObject parametersObj = (JSONObject) jsonParser.parse(actionObj.get("parameters").toString());
	        JSONObject origin_pointObj = (JSONObject) jsonParser.parse(parametersObj.get("origin_point").toString());
	        JSONObject destination_pointObj = (JSONObject) jsonParser.parse(parametersObj.get("destination_point").toString());
	        JSONObject monthObj = (JSONObject) jsonParser.parse(parametersObj.get("month").toString());
	        JSONObject dayObj = (JSONObject) jsonParser.parse(parametersObj.get("day").toString());
	        
	        System.out.println(actionObj);
	        System.out.println("NUGU가 요청한 정보입니다:"+origin_pointObj.get("value")+","+destination_pointObj.get("value")+","+monthObj.get("value")+","+dayObj.get("value"));
	      	        
	        this.origin_point = origin_pointObj.get("value").toString();
	        this.destination_point = destination_pointObj.get("value").toString();
	        this.origin_month = monthObj.get("value").toString();
	        this.origin_day = dayObj.get("value").toString();
	        
	        month_zeroMapping(origin_month);
	        day_zeroMapping(origin_day);
	            
		}catch(Exception e) {
			
			System.out.println("누구한테서 정보가 전부 안왔어요");
		    return False;
		}
		return True;
	   
	}
	//response to NUGU
	@SuppressWarnings("unchecked")
	public JSONObject NUGU_response(SkyScanner sky) {
			
		JSONObject key = new JSONObject();
		JSONObject response =  new JSONObject();
		
		key.put("destination", sky.getArr_PlaceName());
		key.put("origin", sky.getDep_PlaceName());
		key.put("origin_month", origin_month);
		key.put("origin_day", origin_day);
		key.put("total_min_price", sky.getTotal_min_price());
		key.put("carrierName", sky.getCarrierName());
		key.put("via_inform", sky.getVia_inform());
		
		response.put("resultCode", "OK");
		response.put("version", "2.0");
		response.put("output", key);

		return response;
	}
	
	// **   월 앞에 0 매핑 ** //
	public void month_zeroMapping(String month) {
		
		int month_integer = Integer.parseInt(month);
		if(month_integer<10) {
			this.origin_month = "0"+month;
		}

	}
	// **   일 앞에 0 매핑 ** //
	public void day_zeroMapping(String day) {
		
		int day_integer = Integer.parseInt(day);
		if(day_integer<10) {
			this.origin_day = "0"+ day; 
		}
		
			
	}

	public String getOrigin_point() {
		return origin_point;
	}

	public String getDestination_point() {
		return destination_point;
	}

	public String getOrigin_month() {
		return origin_month;
	}

	public String getOrigin_day() {
		return origin_day;
	}

}