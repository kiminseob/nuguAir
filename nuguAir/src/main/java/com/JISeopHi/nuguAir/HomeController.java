package com.JISeopHi.nuguAir;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

import javax.print.Doc;
import javax.servlet.http.HttpServletRequest;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.JISeopHi.nuguAir.HomeController;

/**
 * Handles requests for the application home page.
 */
@Controller
public class HomeController {
	
	private static final Logger logger = LoggerFactory.getLogger(HomeController.class);
	
	public static final int True = 1;
	public static final int False = -1;
	//NUGU ����
	String origin;
    String destination;
    String origin_month;
    String origin_day;
	
	//skyScanner ��� �ڵ�
	String arr_PlaceId;
    String arr_PlaceName;
    String arr_CountryId;
    String arr_CityId;
    String arr_CountryName;
    String arr_CityName;
    String[] arr_Location;
    
    //skyScanner ���� �ڵ�
    String dep_PlaceId;
    String dep_PlaceName;
    String dep_CountryId;
    String dep_CityId;
    String dep_CountryName;
    String dep_CityName;
    String[] dep_Location;
    
    //����to����, ����to����, ����to����, ����to���� type üũ
    String arr_type;
    String dep_type;
    //���� Ÿ���̸� airportId �߰�
    String arr_airportId;
    String dep_airportId;
    
    //Post ��û�� ���� Response
    Document doc;
    
    //Post Response Parsing
    String direct_min_price;          //���� ������
    String one_stop_min_price;        //1�� ���� ������
    String two_plus_stops_min_price;  //2�� �̻� ���� ������
    String total_min_price;			  //��ü ������
    String carriersId;                //���׻� id
    String carrierName;               //���׻� �̸�
    String via_inform;				  //���� ����
    
    //json parser
    JSONParser jsonParser = new JSONParser();
    
	@RequestMapping(value = "/TicketFormat", method = RequestMethod.POST, headers = "Accept=application/json;Authorization:03E4DA94-C21B-4D3E-89FF-408788FA0943",produces="application/json;charset=UTF-8")
	@ResponseBody
	public String TicketFormat(@RequestBody JSONObject body) throws ParseException, IOException {
		
		System.out.println("NUGU���Լ� ��û�� �Խ��ϴ�.");
		
		//Parsing request from NUGU
        if(NUGU_requestParsing(body)==-1) {
        	return "";
        };
        //get Parameters
        getRequest();
        //post Response
        postRequest();
        //json Parsing
        domParsing();
        //response to NUTU
        JSONObject response = NUGU_response();
        	
		return response.toJSONString();
	}
	
	public int NUGU_requestParsing(JSONObject body) throws ParseException {
		
		try {
			//request parsing
			JSONObject bodyObj = (JSONObject) jsonParser.parse(body.toString());
			JSONObject actionObj = (JSONObject) jsonParser.parse(bodyObj.get("action").toString());
			System.out.println(actionObj);
	        JSONObject parametersObj = (JSONObject) jsonParser.parse(actionObj.get("parameters").toString());
	        JSONObject departmentObj = (JSONObject) jsonParser.parse(parametersObj.get("department").toString());
	        JSONObject arrivalObj = (JSONObject) jsonParser.parse(parametersObj.get("arrival").toString());
	        JSONObject monthObj = (JSONObject) jsonParser.parse(parametersObj.get("month").toString());
	        JSONObject dayObj = (JSONObject) jsonParser.parse(parametersObj.get("day").toString());
	        
	        System.out.println("NUGU�� ��û�� �����Դϴ�:"+departmentObj.get("value")+","+arrivalObj.get("value")+","+monthObj.get("value")+","+dayObj.get("value"));
	      	        
	        origin = departmentObj.get("value").toString();
	        destination = arrivalObj.get("value").toString();
	        origin_month = monthObj.get("value").toString();
	        origin_day = dayObj.get("value").toString();
	        
	        month_zeroMapping(origin_month);
	        day_zeroMapping(origin_day);
	            
		}catch(Exception e) {
			
			System.out.println("�������׼� ������ ���� �ȿԾ��");
			origin="";
		    destination="";
		    origin_month="";
		    origin_day="";
		    
		    return False;
		}
		return True;
       
	}
	
	public JSONObject NUGU_response() {
		//response to NUGU
		JSONObject key = new JSONObject();
		JSONObject response =  new JSONObject();
		
		key.put("destination", arr_PlaceName);
		key.put("origin", dep_PlaceName);
		key.put("origin_month", origin_month);
		key.put("origin_day", origin_day);
		key.put("total_min_price", total_min_price);
		key.put("carrierName", carrierName);
		key.put("via_inform", via_inform);
		
		response.put("resultCode", "OK");
		response.put("version", "2.0");
		response.put("output", key);
		
		//NUGU ����
		origin="";
	    destination="";
	    origin_month="";
	    origin_day="";
		
		//skyScanner ��� �ڵ�
		arr_PlaceId="";
	    arr_PlaceName="";
	    arr_CountryId="";
	    arr_CityId="";
	    arr_CountryName="";
	    arr_CityName="";
	    arr_Location= new String[2];
	    
	    //skyScanner ���� �ڵ�
	    dep_PlaceId="";
	    dep_PlaceName="";
	    dep_CountryId="";
	    dep_CityId="";
	    dep_CountryName="";
	    dep_CityName="";
	    dep_Location= new String[2];
	    
	    //����to����, ����to����, ����to����, ����to���� type üũ
	    arr_type="";
	    dep_type="";
	    //���� Ÿ���̸� airportId �߰�
	    arr_airportId="";
	    dep_airportId="";
	    
	    //Post ��û�� ���� Response
	    doc= new Document("");
	    
	    //Post Response Parsing
	    direct_min_price="";          //���� ������
	    one_stop_min_price="";        //1�� ���� ������
	    two_plus_stops_min_price="";  //2�� �̻� ���� ������
	    total_min_price="";			  //��ü ������
	    carriersId="";                //���׻� id
	    carrierName="";               //���׻� �̸�
	    via_inform="";				  //���� ��
	    
		return response;
	}
	
	public void getRequest() throws IOException, ParseException {
		
        String department = URLEncoder.encode(origin, "UTF-8");
        String arrival = URLEncoder.encode(destination, "UTF-8");
           
        String connGetUrl_dep = "https://www.skyscanner.co.kr/g/autosuggest-flights/KR/ko-KR/"
        		+ department
        		+ "?IsDestination=false&enable_general_search_v2=true";
        String connGetUrl_arr = "https://www.skyscanner.co.kr/g/autosuggest-flights/KR/ko-KR/"
        		+ arrival
        		+ "?IsDestination=false&enable_general_search_v2=true";
        
        Document doc_dep = Jsoup.connect(connGetUrl_dep)
        		.ignoreContentType(true)
        		.get();
        Document doc_arr = Jsoup.connect(connGetUrl_arr)
        		.ignoreContentType(true)
        		.get();
        
        String str_dep = doc_dep.body().html().toString();
        String str_arr = doc_arr.body().html().toString();
        
        JSONArray arr_array = (JSONArray)jsonParser.parse(str_arr);
        JSONArray dep_array = (JSONArray)jsonParser.parse(str_dep);
        
        JSONObject arrObj = (JSONObject)jsonParser.parse(arr_array.get(0).toString());
        JSONObject depObj = (JSONObject)jsonParser.parse(dep_array.get(0).toString());
        
        arr_PlaceId = arrObj.get("PlaceId").toString();
        arr_PlaceName = arrObj.get("PlaceName").toString();
        arr_CountryId = arrObj.get("CountryId").toString();
        arr_CityId = arrObj.get("CityId").toString();
        arr_CountryName = arrObj.get("CountryName").toString();
        arr_CityName = arrObj.get("CityName").toString();
        String temp = arrObj.get("Location").toString();
        arr_Location = temp.split(",");
        
        
        dep_PlaceId = depObj.get("PlaceId").toString();
        dep_PlaceName = depObj.get("PlaceName").toString();
        dep_CountryId = depObj.get("CountryId").toString();
        dep_CityId = depObj.get("CityId").toString();
        dep_CountryName = depObj.get("CountryName").toString();
        dep_CityName = depObj.get("CityName").toString();
        String temp2 = depObj.get("Location").toString();
        dep_Location = temp2.split(",");
	}
	
	//    **  ���� ���� ����  **     //
	public void airToAir_mapping() {
	    
	    dep_type = "Airport";
	    arr_type = "Airport";
	    
	    dep_airportId = "\"airportId\":\""+ dep_PlaceId+"\",";
	    arr_airportId = "\"airportId\":\""+ arr_PlaceId+"\",";
	    
	}
	public void airToCity_mapping() {
		
		dep_type = "Airport";
	    arr_type = "City";
	    
	    dep_airportId = "\"airportId\":\""+ dep_PlaceId+"\",";
	    arr_airportId = "";
	    
	}
	public void cityToAir_mapping() {
		
		dep_type = "City";
	    arr_type = "Airport";
	    
	    dep_airportId = "";
	    arr_airportId = "\"airportId\":\""+ arr_PlaceId+"\",";
	    
	}
	public void cityToCity_mapping() {
		
		dep_type = "City";
	    arr_type = "City";
	    
	    dep_airportId = "";
	    arr_airportId = "";
	    
	}
	
	// **   �� �տ� 0 ���� ** //
	public void month_zeroMapping(String month) {
		
		int month_integer = Integer.parseInt(month);
		if(month_integer<10) {
			this.origin_month = "0"+month;
		}

	}
	// **   �� �տ� 0 ���� ** //
	public void day_zeroMapping(String day) {
		
		int day_integer = Integer.parseInt(day);
		if(day_integer<10) {
			this.origin_day = "0"+ day; 
		}
		
			
	}
	
	public void postRequest() throws IOException {
		//request post ��û
        System.out.println("��ī�̽�ĳ�ʿ� post ��û�մϴ�.");
		String connUrl = "https://www.skyscanner.co.kr/g/conductor/v1/fps3/search/?"
				+ "geo_schema=skyscanner&carrier_schema=skyscanner&response_include="
				+ "stats";
		Map<String, String> header = new HashMap<String, String>();
		header.put("user-agent", "Chrome/70.0.3538.102");
		header.put("x-skyscanner-channelid", "website");
				
		// �� �����̸� Airport �� ���ڸ�  City
		int dep_PlaceId_len = dep_PlaceId.length();
		int arr_PlaceId_len = arr_PlaceId.length();
		
		// mapping case 4����
		//air to air
		if(dep_PlaceId_len==3 && arr_PlaceId_len==3){
			airToAir_mapping();
		}
		//air to city
		else if(dep_PlaceId_len==3 && arr_PlaceId_len!=3) {
			airToCity_mapping();
		}
		//city to air
		else if(dep_PlaceId_len!=3 && arr_PlaceId_len==3) {
			cityToAir_mapping();
		}
		//city to city
		else {
			cityToCity_mapping();
		}
		
		
		String payload = "{\"market\":\"KR\",\"currency\":\"KRW\",\"locale\":\"ko-KR\","
				+ "\"cabin_class\":\"economy\",\"prefer_directs\":true,\"trip_type\":\"one-way\","
				
				+ "\"legs\":[{"
				+ "\"origin\":\""+ dep_PlaceId+ "\","      //dep_PlaceId
				+ "\"destination\":\""+ arr_PlaceId+ "\"," //arr_PlaceId
				+ "\"date\":\"2018-"+origin_month+"-"+origin_day+"\"}],"
				
				+ "\"origin\":{"
				+ "\"id\":\""+ dep_PlaceId +"\","        // dep_PlaceId
				+ dep_airportId // dep_PlaceId 
				+ "\"name\":\""+ dep_PlaceName +"\","      // dep_PlaceName
				+ "\"cityId\":\"" + dep_CityId + "\","   // dep_CityId
				+ "\"cityName\":\""+ dep_CityName + "\","  // dep_CityName
				+ "\"countryId\":\"KR\","
				+ "\"type\":\""+dep_type+"\","  // �� �����̸� Airport �� ���ڸ�  City
				+ "\"centroidCoordinates\":["+dep_Location[1]+","+dep_Location[0]+"]},"  // dep_Location_swap (change)
				
				+ "\"destination\":{"
				+ "\"id\":\""+ arr_PlaceId+ "\","       // arr_PlaceId
				+ arr_airportId
				+ "\"name\":\""+arr_PlaceName+ "\","      // arr_PlaceName
				+ "\"cityId\":\"" + arr_CityId + "\","   // arr_CityId
				+ "\"cityName\":\""+ arr_CityName + "\","  // arr_CityName
				+ "\"countryId\":\""+ arr_CountryId + "\","  // arr_CountryId
				+ "\"type\":\""+arr_type+"\","    
				+ "\"centroidCoordinates\":["+arr_Location[1]+","+arr_Location[0]+"]}," //arr_Location_swap
				
				+ "\"outboundDate\":\"2018-"+origin_month+"-"+origin_day+"\","
				+ "\"adults\":1,"
				+ "\"child_ages\":[],"
				
				+ "\"options\":{"
				+ "\"include_unpriced_itineraries\":true,"
				+ "\"include_mixed_booking_options\":true}}";
		
		doc = Jsoup.connect(connUrl)
				.requestBody(payload)
				.headers(header)
				.ignoreContentType(true)
				.post();
		
		
	}
	//  ***      ������ �Ľ�       ***   //
	// ���� ������ �Ľ�
	public void direct_min_priceParsing(JSONObject stopsObj,JSONObject statsObj, JSONObject jsonObj) throws ParseException {
		
		try {
			// ���� ������
	        JSONObject directObj = (JSONObject)jsonParser.parse(stopsObj.get("direct").toString());
	        JSONObject directTotalObj = (JSONObject)jsonParser.parse(directObj.get("total").toString());
	        direct_min_price = directTotalObj.get("min_price").toString();
	        if(direct_min_price.equals(total_min_price)) {via_inform = "��������";}
	        
	        // carriers : �������� �װ��� �̸��� ã������ dom
	        JSONObject carriersObj = (JSONObject)jsonParser.parse(statsObj.get("carriers").toString());
	        JSONArray single_carriersArray = (JSONArray)jsonParser.parse(carriersObj.get("single_carriers").toString());
	        for(int i=0 ; i<single_carriersArray.size() ; i++){
	            JSONObject tempObj = (JSONObject) single_carriersArray.get(i);
	            
	            if(tempObj.get("min_price").toString().equals(total_min_price)) {
	            	carriersId = tempObj.get("id").toString();
	            	break;
	            }
	           
	        }
	        JSONArray rootCarriersArray = (JSONArray)jsonParser.parse(jsonObj.get("carriers").toString());
	        for(int i=0 ; i<rootCarriersArray.size() ; i++){
	            JSONObject tempObj = (JSONObject) rootCarriersArray.get(i);
	            if(tempObj.get("id").toString().equals(carriersId)) {
	            	carrierName = tempObj.get("name").toString();
	            	break;
	            }
	           
	        }
		}catch(Exception e){
			System.out.println("���� �������� �������� �ʽ��ϴ�.");
		}
	}
	//1�� ���� ������ �Ľ�
	public void one_stop_min_priceParsing(JSONObject stopsObj) throws ParseException {
		try {
			//1�� ���� ������
	        JSONObject one_stopObj = (JSONObject)jsonParser.parse(stopsObj.get("one_stop").toString());
	        JSONObject one_stopTotalObj = (JSONObject)jsonParser.parse(one_stopObj.get("total").toString());
	        one_stop_min_price = one_stopTotalObj.get("min_price").toString();
	        if(one_stop_min_price.equals(total_min_price)) {via_inform = "1�� �����Ͽ�";}
	        carrierName="";               //���׻� �̸�
		}catch(Exception e){
			System.out.println("1�� ���� �������� �������� �ʽ��ϴ�.");
		}
	}
	//2�� ���� �̻� ������ �Ľ�
	public void two_plus_stops_min_priceParsing(JSONObject stopsObj) throws ParseException {
		try {
			//1�� ���� ������
	        JSONObject two_plus_stopsObj = (JSONObject)jsonParser.parse(stopsObj.get("two_plus_stops").toString());
	        JSONObject two_plus_stopsTotalObj = (JSONObject)jsonParser.parse(two_plus_stopsObj.get("total").toString());
	        two_plus_stops_min_price = two_plus_stopsTotalObj.get("min_price").toString();
	        if(two_plus_stops_min_price.equals(total_min_price)) {via_inform = "2�� �����Ͽ�";}
	        carrierName="";               //���׻� �̸�
		}catch(Exception e){
			System.out.println("2�� �̻� ���� �������� �������� �ʽ��ϴ�.");
		}
	}

	public void domParsing() throws ParseException {
		
		try {
			
			//response�� json��ü�� ��ȯ
			String docString = doc.body().html().toString();
			JSONObject jsonObj = (JSONObject) jsonParser.parse(docString);
	        
	        // **   stats : �װ��� ������ ������ ����ִ� dom   **   //
			JSONObject statsObj = (JSONObject)jsonParser.parse(jsonObj.get("stats").toString());
	        JSONObject itinerariesObj = (JSONObject)jsonParser.parse(statsObj.get("itineraries").toString());
	        JSONObject stopsObj = (JSONObject)jsonParser.parse(itinerariesObj.get("stops").toString());
	        
	        //��ü ������ �Ľ�
	        JSONObject total_min_priceObj = (JSONObject)jsonParser.parse(itinerariesObj.get("total").toString());
	        total_min_price = total_min_priceObj.get("min_price").toString();
	        
	        //����,1�� ����,2�� �̻� ���� ������ �Ľ�
	        direct_min_priceParsing(stopsObj , statsObj , jsonObj);   
	        one_stop_min_priceParsing(stopsObj);
	        two_plus_stops_min_priceParsing(stopsObj);
	        
	        System.out.println("���������� : "+direct_min_price);
	        System.out.println("1�� ���� ������ : "+one_stop_min_price);
	        System.out.println("2�� ���� ������ : "+two_plus_stops_min_price);
	        System.out.println("��ü ������ : "+total_min_price);
	        
	       
      
	        
		}catch(NullPointerException e) {
			System.out.println("�����ϴ�"+via_inform+" �װ����� ���׿�. �������� �˻��ص帱���? (�˻������� �����ּ���)");
		}
		
        
      
	}
	
	@RequestMapping(value = "/keyboard", method = RequestMethod.GET, produces="text/plain;charset=UTF-8")
	@ResponseBody
	public String keyboard() throws IOException, ParseException {
		
		JSONParser jsonParser = new JSONParser();
		String origin = "����";
        String destination = "����";
        String origin_month = "12";
        String origin_day = "10";
       
        
        String department = URLEncoder.encode("����", "UTF-8");
        String arrival = URLEncoder.encode("����", "UTF-8");
        
        String connGetUrl_dep = "https://www.skyscanner.co.kr/g/autosuggest-flights/KR/ko-KR/"
        		+ department
        		+ "?IsDestination=false&enable_general_search_v2=true";
        String connGetUrl_arr = "https://www.skyscanner.co.kr/g/autosuggest-flights/KR/ko-KR/"
        		+ arrival
        		+ "?IsDestination=false&enable_general_search_v2=true";
        
        Document doc_dep = Jsoup.connect(connGetUrl_dep)
        		.ignoreContentType(true)
        		.get();
        Document doc_arr = Jsoup.connect(connGetUrl_arr)
        		.ignoreContentType(true)
        		.get();
        
        String str_dep = doc_dep.body().html().toString();
        String str_arr = doc_arr.body().html().toString();
        
        JSONArray arr_array = (JSONArray)jsonParser.parse(str_arr);
        JSONArray dep_array = (JSONArray)jsonParser.parse(str_dep);
        
        JSONObject arrObj = (JSONObject)jsonParser.parse(arr_array.get(0).toString());
        JSONObject depObj = (JSONObject)jsonParser.parse(dep_array.get(0).toString());
        
        String arr_PlaceId = arrObj.get("PlaceId").toString();
        String arr_PlaceName = arrObj.get("PlaceName").toString();
        String arr_CountryId = arrObj.get("CountryId").toString();
        String arr_CityId = arrObj.get("CityId").toString();
        String arr_CountryName = arrObj.get("CountryName").toString();
        String arr_CityName = arrObj.get("CityName").toString();
        String arr_Location = arrObj.get("Location").toString();
        String[] arr_Location_split = arr_Location.split(",");
        
        
        String dep_PlaceId = depObj.get("PlaceId").toString();
        String dep_PlaceName = depObj.get("PlaceName").toString();
        String dep_CountryId = depObj.get("CountryId").toString();
        String dep_CityId = depObj.get("CityId").toString();
        String dep_CountryName = depObj.get("CountryName").toString();
        String dep_CityName = depObj.get("CityName").toString();
        String dep_Location = depObj.get("Location").toString();
        String[] dep_Location_split = dep_Location.split(",");
       
        
        System.out.println(arr_PlaceId);
        System.out.println(arr_PlaceName);
        System.out.println(arr_CountryId);
        System.out.println(arr_CityId);
        System.out.println(arr_CountryName);
        System.out.println(arr_CityName);
        System.out.println(arr_Location_split[1]+","+arr_Location_split[0]);
        
        System.out.println(dep_PlaceId);
        System.out.println(dep_PlaceName);
        System.out.println(dep_CountryId);
        System.out.println(dep_CityId);
        System.out.println(dep_CountryName);
        System.out.println(dep_CityName);
        System.out.println(dep_Location_split[1]+","+dep_Location_split[0]);
        
        
        //request post ��û
        System.out.println("request ��û");
		String connUrl = "https://www.skyscanner.co.kr/g/conductor/v1/fps3/search/?"
				+ "geo_schema=skyscanner&carrier_schema=skyscanner&response_include="
				+ "stats";
		Map<String, String> header = new HashMap<String, String>();
		header.put("user-agent", "Chrome/70.0.3538.102");
		header.put("x-skyscanner-channelid", "website");
		String payload = "{\"market\":\"KR\",\"currency\":\"KRW\",\"locale\":\"ko-KR\","
				+ "\"cabin_class\":\"economy\",\"prefer_directs\":true,\"trip_type\":\"one-way\","
				
				+ "\"legs\":[{"
				+ "\"origin\":\""+ dep_PlaceId+ "\","      //dep_PlaceId
				+ "\"destination\":\""+ arr_PlaceId+ "\"," //arr_PlaceId
				+ "\"date\":\"2018-"+origin_month+"-"+origin_day+"\"}],"
				
				+ "\"origin\":{"
				+ "\"id\":\""+ dep_PlaceId +"\","        // dep_PlaceId
				+ "\"airportId\":\""+ dep_PlaceId+"\"," // dep_PlaceId 
				+ "\"name\":\""+ dep_PlaceName +"\","      // dep_PlaceName
				+ "\"cityId\":\"" + dep_CityId + "\","   // dep_CityId
				+ "\"cityName\":\""+ dep_CityName + "\","  // dep_CityName
				+ "\"countryId\":\"KR\","
				+ "\"type\":\"Airport\","  // �� �����̸� Airport �� ���ڸ�  City
				+ "\"centroidCoordinates\":["+dep_Location_split[1]+","+dep_Location_split[0]+"]},"  // dep_Location_swap (change)
				
				+ "\"destination\":{"
				+ "\"id\":\""+ arr_PlaceId+ "\","       // arr_PlaceId
				+ "\"name\":\""+arr_PlaceName+ "\","      // arr_PlaceName
				+ "\"cityId\":\"" + arr_CityId + "\","   // arr_CityId
				+ "\"cityName\":\""+ arr_CityName + "\","  // arr_CityName
				+ "\"countryId\":\""+ arr_CountryId + "\","  // arr_CountryId
				+ "\"type\":\"City\","    
				+ "\"centroidCoordinates\":["+arr_Location_split[1]+","+arr_Location_split[0]+"]}," //arr_Location_swap
				
				+ "\"outboundDate\":\"2018-"+origin_month+"-"+origin_day+"\","
				+ "\"adults\":1,"
				+ "\"child_ages\":[],"
				
				+ "\"options\":{"
				+ "\"include_unpriced_itineraries\":true,"
				+ "\"include_mixed_booking_options\":true}}";
		
		Document doc = Jsoup.connect(connUrl)
				.requestBody(payload)
				.headers(header)
				.ignoreContentType(true)
				.post();
		
		//response�� json��ü�� ��ȯ
		String docString = doc.body().html().toString();
		JSONObject jsonObj = (JSONObject) jsonParser.parse(docString);
        
		//���� �װ��� �˻�
		try {
	        // stats : �װ��� ������ ������ ����ִ� dom
	        String direct_min_price = "";
	        JSONObject statsObj = (JSONObject)jsonParser.parse(jsonObj.get("stats").toString());
	        JSONObject itinerariesObj = (JSONObject)jsonParser.parse(statsObj.get("itineraries").toString());
	        JSONObject stopsObj = (JSONObject)jsonParser.parse(itinerariesObj.get("stops").toString());
	        JSONObject directObj = (JSONObject)jsonParser.parse(stopsObj.get("direct").toString());
	        JSONObject totalObj = (JSONObject)jsonParser.parse(directObj.get("total").toString());
	        direct_min_price = totalObj.get("direct_min_price").toString();
	        System.out.println(direct_min_price);
	        
	        // carriers : �������� �װ��� �̸��� ã������ dom
	        String carriersId="";
	        String carrierName="";
	        JSONObject carriersObj = (JSONObject)jsonParser.parse(statsObj.get("carriers").toString());
	        JSONArray single_carriersArray = (JSONArray)jsonParser.parse(carriersObj.get("single_carriers").toString());
	        for(int i=0 ; i<single_carriersArray.size() ; i++){
	            JSONObject tempObj = (JSONObject) single_carriersArray.get(i);
	            if(tempObj.get("direct_min_price").toString().equals(direct_min_price)) {
	            	carriersId = tempObj.get("id").toString();
	            	break;
	            }
	           
	        }
	        JSONArray rootCarriersArray = (JSONArray)jsonParser.parse(jsonObj.get("carriers").toString());
	        for(int i=0 ; i<rootCarriersArray.size() ; i++){
	            JSONObject tempObj = (JSONObject) rootCarriersArray.get(i);
	            if(tempObj.get("id").toString().equals(carriersId)) {
	            	carrierName = tempObj.get("name").toString();
	            	break;
	            }
	           
	        }
	        System.out.println(carrierName);
	        
	        //itineraries : ������ �װ����� ���, ���� �ð��� �˱����� dom
	        JSONArray rootItinerariesArray = (JSONArray)jsonParser.parse(jsonObj.get("itineraries").toString());
	        for(int i=0 ; i<rootItinerariesArray.size() ; i++){
	            JSONObject tempObj = (JSONObject) rootCarriersArray.get(i);
	            
	            if(tempObj.get("id").toString().equals(carriersId)) {
	            	carrierName = tempObj.get("name").toString();
	            	break;
	            }
	           
	        }
		}
		catch(NullPointerException e){
			
		}finally {
			System.out.println("�����ϴ� ���� �װ����� ���׿�. �������� �˻��ص帱���? (�˻������� �����ּ���)");
		}
      
		return docString;

	}
	
	
}
