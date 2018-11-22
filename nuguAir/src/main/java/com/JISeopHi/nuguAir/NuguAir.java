package com.JISeopHi.nuguAir;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import com.JISeopHi.*;


public class NuguAir{
	
	//NUGU에서 요청한 값이 전부 들어있는지 여부 확인
	private static final int True = 1;
	private static final int False = -1;
	
	//편도타입, 왕복타입
	private static final String trip_type1 = "one-way";
	private static final String trip_type2 = "return";
	
	//NUGU 변수 (편도)
	public String origin_point;
	public String destination_point;
    public String origin_month;
    public String origin_day;
    
    //NUGU 변수 (왕복)
    public String round_origin_point;
	public String round_destination_point;
    public String out_month;
    public String out_day; 
    public String in_month;
    public String in_day;
   
    //편도일때
	public int NUGU_requestParsing_oneway(JSONObject body) throws ParseException {
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
	           
		}catch(Exception e) {
			
			System.out.println("누구한테서 정보가 전부 안왔어요");
		    return False;
		}
		return True;
	   
	}
	//왕복일때
	public int NUGU_requestParsing_round(JSONObject body) throws ParseException {
		//json parser
		JSONParser jsonParser = new JSONParser();
		try {
			//request parsing
			JSONObject bodyObj = (JSONObject) jsonParser.parse(body.toString());
			JSONObject actionObj = (JSONObject) jsonParser.parse(bodyObj.get("action").toString());
			
	        JSONObject parametersObj = (JSONObject) jsonParser.parse(actionObj.get("parameters").toString());
	        JSONObject round_origin_pointObj = (JSONObject) jsonParser.parse(parametersObj.get("round_origin_point").toString());
	        JSONObject round_destination_pointObj = (JSONObject) jsonParser.parse(parametersObj.get("round_destination_point").toString());
	        JSONObject out_monthObj = (JSONObject) jsonParser.parse(parametersObj.get("out_month").toString());
	        JSONObject out_dayObj = (JSONObject) jsonParser.parse(parametersObj.get("out_day").toString());
	        JSONObject in_monthObj = (JSONObject) jsonParser.parse(parametersObj.get("in_month").toString());
	        JSONObject in_dayObj = (JSONObject) jsonParser.parse(parametersObj.get("in_day").toString());
	        
	                
	        this.round_origin_point = round_origin_pointObj.get("value").toString();
	        this.round_destination_point = round_destination_pointObj.get("value").toString();
	        this.out_month = out_monthObj.get("value").toString();
	        this.out_day = out_dayObj.get("value").toString();
	        this.in_month = in_monthObj.get("value").toString();
	        this.in_day = in_dayObj.get("value").toString();
	        
	        
	        System.out.println(actionObj);
	        System.out.println("NUGU가 요청한 정보입니다:"+round_origin_point+","+round_destination_point+","+out_month+","+out_day+","+in_month+","+in_day);
	      	
	            
		}catch(Exception e) {
			
			System.out.println("누구한테서 정보가 전부 안왔어요");
		    return False;
		}
		return True;
	   
	}
	
	//리뷰일때
	public int NUGU_requestParsing_review(JSONObject body) throws ParseException {
		//json parser
		JSONParser jsonParser = new JSONParser();
		try {
			//request parsing
			JSONObject bodyObj = (JSONObject) jsonParser.parse(body.toString());
			JSONObject actionObj = (JSONObject) jsonParser.parse(bodyObj.get("action").toString());
			
	        JSONObject parametersObj = (JSONObject) jsonParser.parse(actionObj.get("parameters").toString());
	        JSONObject review_origin_pointObj = (JSONObject) jsonParser.parse(parametersObj.get("review_origin_point").toString());
	        JSONObject review_destination_pointObj = (JSONObject) jsonParser.parse(parametersObj.get("review_destination_point").toString());
	        JSONObject review_monthObj = (JSONObject) jsonParser.parse(parametersObj.get("review_month").toString());
	       	        
	        System.out.println(actionObj);
	        System.out.println("NUGU가 요청한 정보입니다:"+review_origin_pointObj.get("value")+","+review_destination_pointObj.get("value")+","+review_monthObj.get("value"));
	      	        
	        this.origin_point = review_origin_pointObj.get("value").toString();
	        this.destination_point = review_destination_pointObj.get("value").toString();
	        this.origin_month = review_monthObj.get("value").toString();
	            
		}catch(Exception e) {
			
			System.out.println("누구한테서 정보가 전부 안왔어요");
		    return False;
		}
		return True;
	   
	}
		
		
	//response to NUGU  (편도일때)
	@SuppressWarnings("unchecked")
	public JSONObject NUGU_response_oneway(SkyScanner sky) {
			
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
	//response to NUGU  (왕복일때)
	@SuppressWarnings("unchecked")
	public JSONObject NUGU_response_round(SkyScanner sky) {
		
		JSONObject key = new JSONObject();
		JSONObject response =  new JSONObject();	
		
		key.put("destination", sky.getArr_PlaceName());
		key.put("origin", sky.getDep_PlaceName());
		key.put("origin_month", origin_month);
		key.put("review_total_min_price", sky.getTotal_min_price());
		key.put("carrierName", sky.getCarrierName());
		key.put("via_inform", sky.getVia_inform());
		
		response.put("resultCode", "OK");
		response.put("version", "2.0");
		response.put("output", key);

		return response;
	}
	//response to NUGU  (리뷰일때)
	@SuppressWarnings("unchecked")
	public JSONObject NUGU_response_review(SkyScanner sky) {
		
		JSONObject key = new JSONObject();
		JSONObject response =  new JSONObject();
		
		key.put("R_review_destination_point", sky.getArr_PlaceName());
		key.put("R_review_origin_point", sky.getDep_PlaceName());
		key.put("R_review_month", origin_month);
		key.put("R_review_total_min_price", sky.getReview_total_min_price());
		key.put("R_review_total_max_price", sky.getReview_total_max_price());
		key.put("R_review_total_average_price", sky.getReview_total_average_price());
		key.put("R_review_total_min_day", sky.getReview_total_min_day());
		key.put("R_review_total_max_day", sky.getReview_total_max_day());
		
		response.put("resultCode", "OK");
		response.put("version", "2.0");
		response.put("output", key);

		return response;
	
	}
	//예외 상황 응답 case1
	public JSONObject NUGU_response_exception() {
			
		JSONObject key = new JSONObject();
		JSONObject response =  new JSONObject();
		
		key.put("date_fail", "날짜가 맞지 않네요.");

		
		response.put("resultCode", "date_fail");
		response.put("version", "2.0");
		response.put("output", key);

		return response;
	}
	// **   월 앞에 0 매핑 ** //
	public void month_zeroMapping(String month, String out, String in, String type) {
		
		//편도
		if(type.equals(trip_type1)) { 
			int month_integer = Integer.parseInt(month);
			if(month_integer<10) {
				//편도
				this.origin_month = "0"+month;
			}
		}
		//왕복
		else {
			int in_integer = Integer.parseInt(in);
			int out_integer = Integer.parseInt(out);
			if(in_integer<10) {
				
				this.in_month = "0"+in;
				System.out.println("in month"+in_month);
			}
			if(out_integer<10) {
				
				this.out_month = "0"+out;
				System.out.println("out month"+out_month);
			}
			
		}

	}
	// **   일 앞에 0 매핑 ** //
	public void day_zeroMapping(String day, String out, String in, String type) {
		
		//편도
		if(type.equals(trip_type1)) { 
			int day_integer = Integer.parseInt(day);
			if(day_integer<10) {
				this.origin_day = "0"+ day; 
			}
		}
		//왕복
		else {
			int in_integer = Integer.parseInt(in);
			int out_integer = Integer.parseInt(out);
			if(in_integer<10) {
				
				this.in_day = "0"+in;
				System.out.println("in day"+in_day);
			}
			if(out_integer<10) {
				
				this.out_day = "0"+out;
				System.out.println("out day"+out_day);
			}
		}
			
	}
	
	//편도 
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
	
	public void setOrigin_point(String origin_point) {
		this.origin_point = origin_point;
	}
	public void setDestination_point(String destination_point) {
		this.destination_point = destination_point;
	}
	public void setOrigin_month(String origin_month) {
		this.origin_month = origin_month;
	}
	public void setOrigin_day(String origin_day) {
		this.origin_day = origin_day;
	}
	//왕복
	public String getRound_origin_point() {
		return round_origin_point;
	}
	public String getRound_destination_point() {
		return round_destination_point;
	}
	public String getOut_month() {
		return out_month;
	}
	public String getOut_day() {
		return out_day;
	}
	public String getIn_month() {
		return in_month;
	}
	public String getIn_day() {
		return in_day;
	}
	
}