package com.JISeopHi.nuguAir;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;

import javax.sound.sampled.ReverbType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SkyScanner{
	
	//왕복일때
	private static final String trip_type2 = "return";
	public String inboundDate="";
	public String return_date="";
	
	
	//json parser
    public JSONParser jsonParser = new JSONParser();
	//skyScanner 출발 코드
	public String arr_PlaceId;
	public String arr_PlaceName;
	public String arr_CountryId;
	public String arr_CityId;
	public String arr_CountryName;
	public String arr_CityName;
	public String[] arr_Location;
    
    //skyScanner 도착 코드
	public String dep_PlaceId;
	public String dep_PlaceName;
	public String dep_CountryId;
	public String dep_CityId;
	public  String dep_CountryName;
	public String dep_CityName;
	public String[] dep_Location;
    
    //공항to공항, 공항to도시, 도시to도시, 도시to공항 type 체크
	public String arr_type;
	public String dep_type;
    //공항 타입이면 airportId 추가
	public String arr_airportId;
	public String dep_airportId;
    
    //Post 요청에 대한 Response
	public Document doc;
    
    //Post Response Parsing
	public String direct_min_price;          //직항 최저가
	public String one_stop_min_price;        //1번 경유 최저가
	public String two_plus_stops_min_price;  //2번 이상 경유 최저가
	public String total_min_price;			  //전체 최저가
	public String carriersId;                //공항사 id
	public String carrierName;               //공항사 이름
	public String via_inform;				  //경유 정보
    
	// 항공권 리뷰
	// 직항에 대한 한달간의 price 저장
	// 키 : 가격, 값 : 특정일
	Map<Integer,Integer> ReviewdirectPrice_hashMap;
	Map<Integer,Integer> ReviewindirectPrice_hashMap;
	//트리맵을 이욯한 키 sorting
	TreeMap<Integer, Integer> direct_tm;
	TreeMap<Integer, Integer> indirect_tm;
		
	//review 총 최저가, 최고가, 평균가
	public int review_total_min_price;
	public int review_total_max_price;
	public int review_total_average_price;
	//review 최저가, 최고가인 날짜
	public int review_total_min_day;
	public int review_total_max_day;
	
	//SkyScanner에 get요청을 통해 각종 항공 정보를 받아온다.
	public void getRequest(String air_location, String where) throws IOException, ParseException {
		
        String location = URLEncoder.encode(air_location, "UTF-8");
                 
        String connGetUrl_location = "https://www.skyscanner.co.kr/g/autosuggest-flights/KR/ko-KR/"
        		+ location
        		+ "?IsDestination=false&enable_general_search_v2=true";
        
        Document doc_location = Jsoup.connect(connGetUrl_location)
        		.ignoreContentType(true)
        		.timeout(3000)
        		.get();
    
        String str_location = doc_location.body().html().toString();
        JSONArray location_array = (JSONArray)jsonParser.parse(str_location);
        JSONObject locationObj = (JSONObject)jsonParser.parse(location_array.get(0).toString());
        String temp = locationObj.get("Location").toString();
        
        if(where.equals("도착")) {
	        arr_PlaceId = locationObj.get("PlaceId").toString();
	        arr_PlaceName = locationObj.get("PlaceName").toString();
	        arr_CountryId = locationObj.get("CountryId").toString();
	        arr_CityId = locationObj.get("CityId").toString();
	        arr_CountryName = locationObj.get("CountryName").toString();
	        arr_CityName = locationObj.get("CityName").toString();
	        arr_Location = temp.split(",");
	    }
        else if(where.equals("출발")) {
	        dep_PlaceId = locationObj.get("PlaceId").toString();
	        dep_PlaceName = locationObj.get("PlaceName").toString();
	        dep_CountryId = locationObj.get("CountryId").toString();
	        dep_CityId = locationObj.get("CityId").toString();
	        dep_CountryName = locationObj.get("CountryName").toString();
	        dep_CityName = locationObj.get("CityName").toString();
	        dep_Location = temp.split(",");
	    }
	}
	
	//SkyScanner에 post요청을 통해 항공권 최저가 정보를 받아온다.
	public void postRequest(String trip_type,String out_year,String in_year, String out_month, String out_day,String in_month, String in_day) throws IOException {
		//request post 요청
        System.out.println("스카이스캐너에 post 요청합니다.");
        System.out.println(out_year);
		String connUrl = "https://www.skyscanner.co.kr/g/conductor/v1/fps3/search/?"
				+ "geo_schema=skyscanner&carrier_schema=skyscanner&response_include="
				+ "stats";
		Map<String, String> header = new HashMap<String, String>();
		header.put("user-agent", "Chrome/70.0.3538.102");
		header.put("x-skyscanner-channelid", "website");
				
		// 세 글자이면 Airport 네 글자면  City
		int dep_PlaceId_len = dep_PlaceId.length();
		int arr_PlaceId_len = arr_PlaceId.length();
		
		// mapping case 4가지
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
		
		
		
		//왕복일때
		if(trip_type.equals(trip_type2)) {
			inboundDate = "\"inboundDate\":\""+in_year+"-"+in_month+"-"+in_day+"\",";
			return_date = "\"return_date\":\""+in_year+"-"+in_month+"-"+in_day+"\",";
		}
		
		String payload = "{\"market\":\"KR\",\"currency\":\"KRW\",\"locale\":\"ko-KR\","
				+ "\"cabin_class\":\"economy\",\"prefer_directs\":true,\"trip_type\":\""+trip_type+"\","
				
				+ "\"legs\":[{"
				+ "\"origin\":\""+ dep_PlaceId+ "\","      //dep_PlaceId
				+ "\"destination\":\""+ arr_PlaceId+ "\"," //arr_PlaceId
				+ return_date
				+ "\"date\":\""+out_year+"-"+out_month+"-"+out_day+"\"}],"
				
				+ "\"origin\":{"
				+ "\"id\":\""+ dep_PlaceId +"\","        // dep_PlaceId
				+ dep_airportId // dep_PlaceId 
				+ "\"name\":\""+ dep_PlaceName +"\","      // dep_PlaceName
				+ "\"cityId\":\"" + dep_CityId + "\","   // dep_CityId
				+ "\"cityName\":\""+ dep_CityName + "\","  // dep_CityName
				+ "\"countryId\":\"KR\","
				+ "\"type\":\""+dep_type+"\","  // 세 글자이면 Airport 네 글자면  City
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
				
				+ inboundDate
				+ "\"outboundDate\":\""+out_year+"-"+out_month+"-"+out_day+"\","
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
	
	// skyscanner에 특정 월의 캘린더 정보 요청
	public void getRequestToCalendar(String out_year, String month) throws IOException, ParseException {
		
		System.out.println(this.dep_PlaceId+ this.arr_PlaceId+month);
		String connUrl = "https://www.skyscanner.co.kr/g/browseservice/dataservices/browse/v3/mvweb/KR/KRW/ko-KR/calendar/"
				+ ""+this.dep_PlaceId+"/"+this.arr_PlaceId+"/"+out_year+"-"+month+"/"
				+ "?profile=minimalmonthviewgridv2"
				+ "&abvariant=FLUX_GDT2791_SendPriceTraceToMixpanel:b|rts_wta_shadowtraffic:b"
				+ "&apikey=c32d1a225f454c49a44ddec56ddc6910";
		System.out.println(connUrl);
		Document doc = Jsoup.connect(connUrl)
				.ignoreContentType(true)
				.header("user-agent", "Chrome/70.0.3538.102")
				.get();
		
		try {
			String docString = doc.body().html().toString();
			JSONObject jsonObj = (JSONObject) jsonParser.parse(docString);
			JSONObject priceGridsObj = (JSONObject)jsonParser.parse(jsonObj.get("PriceGrids").toString());
			JSONArray gridArray = (JSONArray)jsonParser.parse(priceGridsObj.get("Grid").toString());
			JSONArray grid = (JSONArray)gridArray.get(0);
		
		
			ReviewdirectPrice_hashMap = new HashMap<Integer,Integer>();
			ReviewindirectPrice_hashMap = new HashMap<Integer,Integer>();
			int cnt=0;
			for(int i=0; i<grid.size(); i++) {
				JSONObject gridObj = (JSONObject)grid.get(i);
				cnt++;
				try {
					//직항 price
					JSONObject directOutbound =  (JSONObject)gridObj.get("DirectOutbound");
					ReviewdirectPrice_hashMap.put(Integer.parseInt(directOutbound.get("Price").toString()),i+1);
				}catch(NullPointerException e){
					continue;
				}
				
				try {
					//경유하는 경우 price
					JSONObject indirectOutbound =  (JSONObject)gridObj.get("IndirectOutbound");
					ReviewindirectPrice_hashMap.put(Integer.parseInt(indirectOutbound.get("Price").toString()),i+1);
				}catch(NullPointerException e) {
					continue;
				}
			}
			treeMapSorting();
			find_review_minPrice();
			find_review_maxPrice();
			find_review_averagePrice();
		}catch(NullPointerException e) {
			System.out.println("리뷰에 대한 오류");
		}

	}
	
	//트리맵 오름차순 정렬
	public void treeMapSorting() {
		
		//트리맵을 활용한 키값 오름차순 정렬
		direct_tm = new TreeMap<Integer,Integer>(ReviewdirectPrice_hashMap);
		indirect_tm = new TreeMap<Integer,Integer>(ReviewindirectPrice_hashMap);
		
	}
	//트리맵을 이용해 오름차순 정렬된 가격 트리에서 첫번째 키를 가져와 가격을 비교해 최저가를 알아낸다.
	//항공권 리뷰 최저가
	public void find_review_minPrice() {
		
		try {
			int direct_min_price = direct_tm.firstKey();
			int indirect_min_price = indirect_tm.firstKey();
		
		
			System.out.println("최저가 비교: "+ direct_min_price+","+ indirect_min_price);
			if(direct_min_price < indirect_min_price) {
				this.review_total_min_price = direct_min_price;
				this.review_total_min_day = direct_tm.get(direct_min_price);
			}else {
				this.review_total_min_price = indirect_min_price;
				this.review_total_min_day = indirect_tm.get(indirect_min_price);
			}
			
			System.out.println("최저가: "+ review_total_min_price+", 일 : "+ review_total_min_day);
		}catch(NoSuchElementException e) {
			System.out.println("리뷰 최저가 없음");
		}
	}
	//항공권 리뷰 최고가
	public void find_review_maxPrice() {
		try {
			int direct_max_price = direct_tm.lastKey();
			int indirect_max_price = indirect_tm.lastKey();
			
			System.out.println("최고가 비교 : "+ direct_max_price+","+ indirect_max_price);
			if(direct_max_price > indirect_max_price) {
				this.review_total_max_price = direct_max_price;
				this.review_total_max_day = direct_tm.get(direct_max_price);
			}else {
				this.review_total_max_price = indirect_max_price;
				this.review_total_max_day = indirect_tm.get(indirect_max_price);
			}
			System.out.println("최고가: "+ review_total_max_price+", 일 : "+ review_total_max_day);
		}catch(NoSuchElementException e) {
			System.out.println("리뷰 최고가 없음");
		}
	}
	
	//항공권 리뷰 평균가
	public void find_review_averagePrice() {
		try {
			//트리맵 iterator
			Iterator<Integer> direct_iteratorKey;
			Iterator<Integer> indirect_iteratorKey;
			//iterator
			direct_iteratorKey = direct_tm.keySet().iterator();
			indirect_iteratorKey = indirect_tm.keySet().iterator();
			
			int temp_sum = 0;
			
			while(direct_iteratorKey.hasNext()) {
		
				Integer key = direct_iteratorKey.next();
				temp_sum+=key;
				System.out.println("direct key 정렬 : "+key+","+direct_tm.get(key));
		
			}
			while(indirect_iteratorKey.hasNext()) {
				
				Integer key = indirect_iteratorKey.next();
				temp_sum+=key;
				System.out.println("indirect key 정렬 : "+key+","+indirect_tm.get(key));
		
			}
			
			this.review_total_average_price = temp_sum/(direct_tm.size()+indirect_tm.size());
			System.out.println("평균 : "+ review_total_average_price);
		}catch(NoSuchElementException e) {
			System.out.println("리뷰 평균가 없음");
		}
	}
		
	
	// 직항 최저가 파싱
	public void direct_min_priceParsing(JSONObject stopsObj,JSONObject statsObj, JSONObject jsonObj) throws ParseException {
		
		try {
			// 직항 최저가
	        JSONObject directObj = (JSONObject)jsonParser.parse(stopsObj.get("direct").toString());
	        JSONObject directTotalObj = (JSONObject)jsonParser.parse(directObj.get("total").toString());
	        direct_min_price = directTotalObj.get("min_price").toString();
	        if(direct_min_price.equals(total_min_price)) {via_inform = "직항으로";}
	        
	        // carriers : 최저가의 항공사 이름을 찾기위한 dom
	        JSONObject carriersObj = (JSONObject)jsonParser.parse(statsObj.get("carriers").toString());
	        JSONArray single_carriersArray = (JSONArray)jsonParser.parse(carriersObj.get("single_carriers").toString());
	        for(int i=0 ; i<single_carriersArray.size() ; i++){
	            JSONObject tempObj = (JSONObject) single_carriersArray.get(i);
	            
	            if(tempObj.get("min_price").toString().equals(total_min_price)) {
	            	carriersId = tempObj.get("id").toString();
	            	break;
	            }
	           
	        }
	        System.out.println("항공 id : "+carriersId);
	        JSONArray rootCarriersArray = (JSONArray)jsonParser.parse(jsonObj.get("carriers").toString());
	        for(int i=0 ; i<rootCarriersArray.size() ; i++){
	            JSONObject tempObj = (JSONObject) rootCarriersArray.get(i);
	            if(tempObj.get("id").toString().equals(carriersId)) {
	            	carrierName = tempObj.get("name").toString();
	            	break;
	            }
	           
	        }
	        System.out.println("항공사 이름 : "+carrierName);
		}catch(Exception e){
			System.out.println("직항 최저가가 존재하지 않습니다.");
		}
	}
	//1번 경유 최저가 파싱
	public void one_stop_min_priceParsing(JSONObject stopsObj) throws ParseException {
		try {
			//1번 경유 최저가
	        JSONObject one_stopObj = (JSONObject)jsonParser.parse(stopsObj.get("one_stop").toString());
	        JSONObject one_stopTotalObj = (JSONObject)jsonParser.parse(one_stopObj.get("total").toString());
	        one_stop_min_price = one_stopTotalObj.get("min_price").toString();
	        if(one_stop_min_price.equals(total_min_price)) {via_inform = "1번 경유하여";}
	    }catch(Exception e){
			System.out.println("1번 경유 최저가가 존재하지 않습니다.");
		}
	}
	//2번 경유 이상 최저가 파싱
	public void two_plus_stops_min_priceParsing(JSONObject stopsObj) throws ParseException {
		try {
			//1번 경유 최저가
	        JSONObject two_plus_stopsObj = (JSONObject)jsonParser.parse(stopsObj.get("two_plus_stops").toString());
	        JSONObject two_plus_stopsTotalObj = (JSONObject)jsonParser.parse(two_plus_stopsObj.get("total").toString());
	        two_plus_stops_min_price = two_plus_stopsTotalObj.get("min_price").toString();
	        if(two_plus_stops_min_price.equals(total_min_price)) {via_inform = "2번 경유하여";}
	   	}catch(Exception e){
			System.out.println("2번 이상 경유 최저가가 존재하지 않습니다.");
		}
	}

	public void domParsing() throws ParseException {
		
		try {
			
			//response를 json객체로 변환
			String docString = doc.body().html().toString();
			JSONObject jsonObj = (JSONObject) jsonParser.parse(docString);
	        
	        // **   stats : 항공권 최저가 가격이 들어있는 dom   **   //
			JSONObject statsObj = (JSONObject)jsonParser.parse(jsonObj.get("stats").toString());
	        JSONObject itinerariesObj = (JSONObject)jsonParser.parse(statsObj.get("itineraries").toString());
	        JSONObject stopsObj = (JSONObject)jsonParser.parse(itinerariesObj.get("stops").toString());
	        
	        //전체 최저가 파싱
	        JSONObject total_min_priceObj = (JSONObject)jsonParser.parse(itinerariesObj.get("total").toString());
	        total_min_price = total_min_priceObj.get("min_price").toString();
	        
	        //직항,1번 경유,2번 이상 경유 최저가 파싱
	        direct_min_priceParsing(stopsObj , statsObj , jsonObj);   
	        one_stop_min_priceParsing(stopsObj);
	        two_plus_stops_min_priceParsing(stopsObj);
	        
	        System.out.println("직항최저가 : "+direct_min_price);
	        System.out.println("1번 경유 최저가 : "+one_stop_min_price);
	        System.out.println("2번 경유 최저가 : "+two_plus_stops_min_price);
	        System.out.println("전체 최저가 : "+total_min_price);
	     
	        
		}catch(NullPointerException e) {
			System.out.println("존재하는"+via_inform+" 항공권이 없네요. 경유지를 검색해드릴까요? (검색해줘라고 말해주세요)");
		}
		
        
      
	}
	//  **  공항 도시 매핑  **     //
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
    public String getArr_PlaceName() {
		return arr_PlaceName;
	}

	public String getDep_PlaceName() {
		return dep_PlaceName;
	}

	public String getTotal_min_price() {
		return total_min_price;
	}

	public String getCarrierName() {
		return carrierName;
	}

	public String getVia_inform() {
		return via_inform;
	}

	public int getReview_total_min_price() {
		return review_total_min_price;
	}

	public int getReview_total_max_price() {
		return review_total_max_price;
	}

	public int getReview_total_average_price() {
		return review_total_average_price;
	}

	public int getReview_total_min_day() {
		return review_total_min_day;
	}

	public int getReview_total_max_day() {
		return review_total_max_day;
	}

}