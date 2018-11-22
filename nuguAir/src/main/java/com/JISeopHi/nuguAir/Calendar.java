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
	
	// ���� �⵵, �� ���ϱ�
	public void getNowDate() {
		
		Date currentTime = new Date ();
		System.out.println(currentTime);
		this.nowYear = Integer.parseInt(mSimpleDateFormat_year.format ( currentTime ));
		this.nowMonth = Integer.parseInt(mSimpleDateFormat_month.format ( currentTime ));
    }
	
	// ������ ��
	public void differDate(int requestMonth) {
				
		//��û�� ���� ���� �� ���� ū ��� : ���� �⵵�� ����.
		//Ȥ�� ���� ���ΰ�� : ���� �⵵�� ����.
		if(nowMonth <= requestMonth) {
			
			out_year = Integer.toString(nowYear);
			
		}
		//��û�� ���� ���� �⵵���� ���� ��� : �������� ����.
		else {
			out_year = Integer.toString(nowYear+1);
		}
	   
	}
	// ������ ��
	public void indifferDate(int requestMonth) {
		
		//��û�� ���� ���� �� ���� ū ��� : ���� �⵵�� ����.
		//Ȥ�� ���� ���ΰ�� : ���� �⵵�� ����.
		if(nowMonth <= requestMonth) {
			
			in_year = Integer.toString(nowYear);
			
		}
		//��û�� ���� ���� �⵵���� ���� ��� : �������� ����.
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