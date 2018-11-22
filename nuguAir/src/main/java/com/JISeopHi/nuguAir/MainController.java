package com.JISeopHi.nuguAir;

import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.JISeopHi.nuguAir.MainController;

/**
 * Handles requests for the application home page.
 */
@Controller
public class MainController {
	
	private static final String oneway_origin="출발";
	private static final String oneway_destination="도착";
	private static final String trip_type1 = "one-way";
	private static final String trip_type2 = "return";
	//private static final String round="왕복";
	
	
	//편도 항공권 조회
	@RequestMapping(value = "/oneway", method = RequestMethod.POST, headers = "Accept=application/json;Authorization:03E4DA94-C21B-4D3E-89FF-408788FA0943",produces="application/json;charset=UTF-8")
	@ResponseBody
	public String TicketPrice(@RequestBody JSONObject body) throws ParseException, IOException {
		
		System.out.println("NUGU에게서 편도 항공권 요청이 왔습니다.");
		
		NuguAir nuguAir = new NuguAir();
		SkyScanner sky = new SkyScanner();
		Calendar calendar = new Calendar();
		
		//Parsing request from NUGU
        if(nuguAir.NUGU_requestParsing_oneway(body)==-1) {
        	return "";
        };
        
        //calculate date
        calendar.getNowDate();
        calendar.differDate(Integer.parseInt(nuguAir.getOrigin_month()));
        
        nuguAir.month_zeroMapping(nuguAir.getOrigin_month(),null,null,trip_type1);
        nuguAir.day_zeroMapping(nuguAir.getOrigin_day(),null,null,trip_type1);
        
        //get Parameters
        sky.getRequest(nuguAir.getOrigin_point(),oneway_origin);
        sky.getRequest(nuguAir.getDestination_point(),oneway_destination);
        
        //post Response
        sky.postRequest(trip_type1, calendar.getOut_year(),null,nuguAir.getOrigin_month(), nuguAir.getOrigin_day(),null,null);
        
        //json Parsing
        sky.domParsing();
        
        //response to NUTU
        JSONObject response = nuguAir.NUGU_response_oneway(sky);
	    System.out.println("최종 응답 : "+response);
	    
		return response.toJSONString();
	}
	
	//왕복 항공권 조회
	@RequestMapping(value = "/round", method = RequestMethod.POST, headers = "Accept=application/json;Authorization:03E4DA94-C21B-4D3E-89FF-408788FA0943",produces="application/json;charset=UTF-8")
	@ResponseBody
	public String roundTicketPrice(@RequestBody JSONObject body) throws ParseException, IOException {
		
		System.out.println("NUGU에게서 왕복 항공권 요청이 왔습니다.");
		
		NuguAir nuguAir = new NuguAir();
		SkyScanner sky = new SkyScanner();
		Calendar calendar = new Calendar();
		
		//Parsing request from NUGU
        if(nuguAir.NUGU_requestParsing_round(body)==-1) {
        	return "";
        };
        
        //calculate date
        calendar.getNowDate();
        calendar.differDate(Integer.parseInt(nuguAir.getOut_month()));
        calendar.indifferDate(Integer.parseInt(nuguAir.getIn_month()));
        
        nuguAir.month_zeroMapping(null,nuguAir.getOut_month(),nuguAir.getIn_month() ,trip_type2);
        nuguAir.day_zeroMapping(null,nuguAir.getOut_day(),nuguAir.getIn_day() ,trip_type2);
        
        //get Parameters
        sky.getRequest(nuguAir.getRound_origin_point(),oneway_origin);
        sky.getRequest(nuguAir.getRound_destination_point(),oneway_destination);
        
        //post Response
        sky.postRequest(trip_type2, calendar.getOut_year(),calendar.getIn_year(),nuguAir.getOut_month(),nuguAir.getOut_day(),nuguAir.getIn_month(),nuguAir.getIn_day());
        //json Parsing
        sky.domParsing();
        //response to NUTU
        JSONObject response = nuguAir.NUGU_response_round(sky);
	    System.out.println("최종 응답 : "+response);
		return response.toJSONString();
	}
	
	//항공권 리뷰 테스트
	//편도 항공권 리뷰
	@RequestMapping(value = "/review", method = RequestMethod.POST, headers = "Accept=application/json;Authorization:03E4DA94-C21B-4D3E-89FF-408788FA0943",produces="application/json;charset=UTF-8")
	@ResponseBody
	public String review(@RequestBody JSONObject body) throws ParseException, IOException {
	
		System.out.println("NUGU에게서 항공권 리뷰 요청이 왔습니다.");
		
		NuguAir nuguAir = new NuguAir();
		SkyScanner sky = new SkyScanner();
		Calendar calendar = new Calendar();
		
		//Parsing request from NUGU
        if(nuguAir.NUGU_requestParsing_review(body)==-1) {
        	return "";
        };

        //calculate date
        calendar.getNowDate();
        System.out.println("쓰바"+nuguAir.getOrigin_month());
        calendar.differDate(Integer.parseInt(nuguAir.getOrigin_month()));
        System.out.println("쓰바"+nuguAir.getOrigin_month());
        nuguAir.month_zeroMapping(nuguAir.getOrigin_month(),null,null,trip_type1);
        System.out.println("쓰바"+nuguAir.getOrigin_month());
        //get Parameters
        sky.getRequest(nuguAir.getOrigin_point(),oneway_origin);
        sky.getRequest(nuguAir.getDestination_point(),oneway_destination);
        
        sky.getRequestToCalendar(calendar.getOut_year(),nuguAir.getOrigin_month());
        
        //response to NUTU
        JSONObject response = nuguAir.NUGU_response_review(sky);
	    System.out.println("최종 응답 : "+response);
		return response.toJSONString();
	}
}
