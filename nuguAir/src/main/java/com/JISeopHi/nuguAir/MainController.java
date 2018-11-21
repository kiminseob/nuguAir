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
	//private static final String round="왕복";
	
	@RequestMapping(value = "/oneway", method = RequestMethod.POST, headers = "Accept=application/json;Authorization:03E4DA94-C21B-4D3E-89FF-408788FA0943",produces="application/json;charset=UTF-8")
	@ResponseBody
	public String TicketPrice(@RequestBody JSONObject body) throws ParseException, IOException {
		
		System.out.println("NUGU에게서 요청이 왔습니다.");
		
		NuguAir nuguAir = new NuguAir();
		SkyScanner sky = new SkyScanner();
		
		//Parsing request from NUGU
        if(nuguAir.NUGU_requestParsing(body)==-1) {
        	return "";
        };
        //get Parameters
        sky.getRequest(nuguAir.getOrigin_point(),oneway_origin);
        sky.getRequest(nuguAir.getDestination_point(),oneway_destination);
        //post Response
        sky.postRequest(nuguAir.getOrigin_month(), nuguAir.getOrigin_day());
        //json Parsing
        sky.domParsing();
        //response to NUTU
        JSONObject response = nuguAir.NUGU_response(sky);
	    System.out.println("최종 응답 : "+response);
		return response.toJSONString();
	}
	
}
