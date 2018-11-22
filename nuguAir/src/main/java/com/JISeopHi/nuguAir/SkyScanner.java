package com.JISeopHi.nuguAir;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class SkyScanner{
	
	//�պ��϶�
	private static final String trip_type2 = "return";
	public String inboundDate="";
	public String return_date="";
	
	
	//json parser
    public JSONParser jsonParser = new JSONParser();
	//skyScanner ��� �ڵ�
	public String arr_PlaceId;
	public String arr_PlaceName;
	public String arr_CountryId;
	public String arr_CityId;
	public String arr_CountryName;
	public String arr_CityName;
	public String[] arr_Location;
    
    //skyScanner ���� �ڵ�
	public String dep_PlaceId;
	public String dep_PlaceName;
	public String dep_CountryId;
	public String dep_CityId;
	public  String dep_CountryName;
	public String dep_CityName;
	public String[] dep_Location;
    
    //����to����, ����to����, ����to����, ����to���� type üũ
	public String arr_type;
	public String dep_type;
    //���� Ÿ���̸� airportId �߰�
	public String arr_airportId;
	public String dep_airportId;
    
    //Post ��û�� ���� Response
	public Document doc;
    
    //Post Response Parsing
	public String direct_min_price;          //���� ������
	public String one_stop_min_price;        //1�� ���� ������
	public String two_plus_stops_min_price;  //2�� �̻� ���� ������
	public String total_min_price;			  //��ü ������
	public String carriersId;                //���׻� id
	public String carrierName;               //���׻� �̸�
	public String via_inform;				  //���� ����
    
     
	//SkyScanner�� get��û�� ���� ���� �װ� ������ �޾ƿ´�.
	public void getRequest(String air_location, String where) throws IOException, ParseException {
		
        String location = URLEncoder.encode(air_location, "UTF-8");
                 
        String connGetUrl_location = "https://www.skyscanner.co.kr/g/autosuggest-flights/KR/ko-KR/"
        		+ location
        		+ "?IsDestination=false&enable_general_search_v2=true";
        
        Document doc_location = Jsoup.connect(connGetUrl_location)
        		.ignoreContentType(true)
        		.get();
    
        String str_location = doc_location.body().html().toString();
        JSONArray location_array = (JSONArray)jsonParser.parse(str_location);
        JSONObject locationObj = (JSONObject)jsonParser.parse(location_array.get(0).toString());
        String temp = locationObj.get("Location").toString();
        
        if(where.equals("����")) {
	        arr_PlaceId = locationObj.get("PlaceId").toString();
	        arr_PlaceName = locationObj.get("PlaceName").toString();
	        arr_CountryId = locationObj.get("CountryId").toString();
	        arr_CityId = locationObj.get("CityId").toString();
	        arr_CountryName = locationObj.get("CountryName").toString();
	        arr_CityName = locationObj.get("CityName").toString();
	        arr_Location = temp.split(",");
	    }
        else if(where.equals("���")) {
	        dep_PlaceId = locationObj.get("PlaceId").toString();
	        dep_PlaceName = locationObj.get("PlaceName").toString();
	        dep_CountryId = locationObj.get("CountryId").toString();
	        dep_CityId = locationObj.get("CityId").toString();
	        dep_CountryName = locationObj.get("CountryName").toString();
	        dep_CityName = locationObj.get("CityName").toString();
	        dep_Location = temp.split(",");
	    }
	}
	
	//SkyScanner�� post��û�� ���� �װ��� ������ ������ �޾ƿ´�.
	public void postRequest(String trip_type, String out_month, String out_day,String in_month, String in_day) throws IOException {
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
		
		//�պ��϶�
		if(trip_type.equals(trip_type2)) {
			inboundDate = "\"inboundDate\":\"2019-"+in_month+"-"+in_day+"\",";
			return_date = "\"return_date\":\"2019-"+in_month+"-"+in_day+"\",";
		}
		
		String payload = "{\"market\":\"KR\",\"currency\":\"KRW\",\"locale\":\"ko-KR\","
				+ "\"cabin_class\":\"economy\",\"prefer_directs\":true,\"trip_type\":\""+trip_type+"\","
				
				+ "\"legs\":[{"
				+ "\"origin\":\""+ dep_PlaceId+ "\","      //dep_PlaceId
				+ "\"destination\":\""+ arr_PlaceId+ "\"," //arr_PlaceId
				+ return_date
				+ "\"date\":\"2019-"+out_month+"-"+out_day+"\"}],"
				
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
				
				+ inboundDate
				+ "\"outboundDate\":\"2019-"+out_month+"-"+out_day+"\","
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
	//  **  ���� ���� ����  **     //
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

}