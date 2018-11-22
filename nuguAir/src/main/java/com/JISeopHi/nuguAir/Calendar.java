package com.JISeopHi.nuguAir;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Calendar{
	
	SimpleDateFormat mSimpleDateFormat_year = new SimpleDateFormat("yyyy");
	SimpleDateFormat mSimpleDateFormat_month = new SimpleDateFormat("MM");
	
	int nowYear;
	int nowMonth;
	
	String out_year;
	String in_year;
	
	// 현재 년도, 월 구하기
	public void getNowDate() {
		
		Date currentTime = new Date ();
		System.out.println(currentTime);
		this.nowYear = Integer.parseInt(mSimpleDateFormat_year.format ( currentTime ));
		this.nowMonth = Integer.parseInt(mSimpleDateFormat_month.format ( currentTime ));
    }
	
	// 나가는 날
	public void differDate(int requestMonth) {
				
		//요청한 달이 현재 달 보다 큰 경우 : 현재 년도로 설정.
		//혹은 현재 달인경우 : 현재 년도로 설정.
		if(nowMonth <= requestMonth) {
			
			out_year = Integer.toString(nowYear);
			
		}
		//요청한 달이 현재 년도보다 작을 경우 : 내년으로 설정.
		else {
			out_year = Integer.toString(nowYear+1);
		}
	   
	}
	// 들어오는 날
	public void indifferDate(int requestMonth) {
		
		//요청한 달이 현재 달 보다 큰 경우 : 현재 년도로 설정.
		//혹은 현재 달인경우 : 현재 년도로 설정.
		if(nowMonth <= requestMonth) {
			
			in_year = Integer.toString(nowYear);
			
		}
		//요청한 달이 현재 년도보다 작을 경우 : 내년으로 설정.
		else {
			in_year = Integer.toString(nowYear+1);
		}
	   
	}

	public String getOut_year() {
		return out_year;
	}

	public String getIn_year() {
		return in_year;
	}
	
}