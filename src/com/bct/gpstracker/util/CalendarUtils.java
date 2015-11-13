package com.bct.gpstracker.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

public class CalendarUtils {

	private int weeks = 0;// 用来全局控制 上一周，本周，下一周的周数变化
	private int MaxDate; // 一月最大天数
	private int MaxYear; // 一年最大天数

//	public static void main(String[] args) {
//		CalendarUtils tt = new CalendarUtils();
//		System.out.println("获取当天日期:" + tt.getNowTime("yyyy-MM-dd"));
//		System.out.println("获取本周一日期:" + tt.getMondayOFWeek());
//		System.out.println("获取本周日的日期~:" + tt.getCurrentWeekday());
//		System.out.println("获取上周一日期:" + tt.getPreviousWeekday());
//		System.out.println("获取上周日日期:" + tt.getPreviousWeekSunday());
//		System.out.println("获取下周一日期:" + tt.getNextMonday());
//		System.out.println("获取下周日日期:" + tt.getNextSunday());
//		System.out.println("获得相应周的周六的日期:" + tt.getNowTime("yyyy-MM-dd"));
//		System.out.println("获取本月第一天日期:" + tt.getFirstDayOfMonth());
//		System.out.println("获取本月最后一天日期:" + tt.getDefaultDay());
//		System.out.println("获取上月第一天日期:" + tt.getPreviousMonthFirst());
//		System.out.println("获取上月最后一天的日期:" + tt.getPreviousMonthEnd());
//		System.out.println("获取下月第一天日期:" + tt.getNextMonthFirst());
//		System.out.println("获取下月最后一天日期:" + tt.getNextMonthEnd());
//		System.out.println("获取本年的第一天日期:" + tt.getCurrentYearFirst());
//		System.out.println("获取本年最后一天日期:" + tt.getCurrentYearEnd());
//		System.out.println("获取去年的第一天日期:" + tt.getPreviousYearFirst());
//		System.out.println("获取去年的最后一天日期:" + tt.getPreviousYearEnd());
//		System.out.println("获取明年第一天日期:" + tt.getNextYearFirst());
//		System.out.println("获取明年最后一天日期:" + tt.getNextYearEnd());
//		System.out.println("获取本季度第一天:" + tt.getThisSeasonFirstTime(11));
//		System.out.println("获取本季度最后一天:" + tt.getThisSeasonFinallyTime(11));
//		System.out.println("获取两个日期之间间隔天数2008-12-1~2008-9.29:"
//				+ CalendarUtils.getTwoDay("2008-12-1", "2008-9-29"));
//		System.out.println("获取当前月的第几周：" + tt.getWeekOfMonth());
//		System.out.println("获取当前年份：" + tt.getYear());
//		System.out.println("获取当前月份：" + tt.getMonth());
//		System.out.println("获取今天在本年的第几天：" + tt.getDayOfYear());
//		System.out.println("获得今天在本月的第几天(获得当前日)：" + tt.getDayOfMonth());
//		System.out.println("获得今天在本周的第几天：" + tt.getDayOfWeek());
		// System.out.println("获得半年后的日期："+
		// tt.convertDateToString(tt.getTimeYearNext()));
//	}

	/**
	 * 获取当前年份
	 * @return
	 */
	public static int getYear() {
		return Calendar.getInstance().get(Calendar.YEAR);
	}

	/**
	 * 获取当前月份
	 * @return
	 */
	public static int getMonth() {
		return Calendar.getInstance().get(Calendar.MONTH) + 1;
	}

	/**
	 * 获取今天在本年的第几天
	 * @return
	 */
	public static int getDayOfYear() {
		return Calendar.getInstance().get(Calendar.DAY_OF_YEAR);
	}

	/**
	 * 获得今天在本月的第几天(获得当前日)
	 * @return
	 */
	public static int getDayOfMonth() {
		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	}

	/**
	 * 获得今天在本周的第几天
	 * @return
	 */
	public static int getDayOfWeek() {
		return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * "获取当前月的第几周
	 * @return
	 */
	public static int getWeekOfMonth() {
		return Calendar.getInstance().get(Calendar.DAY_OF_WEEK_IN_MONTH);
	}
	
	public static int getWeekOfYear(){
		return Calendar.getInstance().get(Calendar.WEEK_OF_YEAR);
	}

	/**
	 * 获得半年后的日期
	 * @return
	 */
	public static Date getTimeYearNext() {
		Calendar.getInstance().add(Calendar.DAY_OF_YEAR, 183);
		return Calendar.getInstance().getTime();
	}
	/**
	 * 转换日期为yyyy-MM-dd格式的字符串
	 */
	public static String convertDateToString(Date dateTime) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		return df.format(dateTime);
	}

	public static String getTwoDay(String sj1, String sj2) {
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
		long day = 0;
		try {
			java.util.Date date = myFormatter.parse(sj1);
			java.util.Date mydate = myFormatter.parse(sj2);
			day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		} catch (Exception e) {
			return "";
		}
		return day + "";
	}
	
	public static String getWeek(String sdate) {
		// 再转换为时间
		Date date = CalendarUtils.strToDate(sdate);
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		// int hour=c.get(Calendar.DAY_OF_WEEK);
		// hour中存的就是星期几了，其范围 1~7
		// 1=星期日 7=星期六，其他类推
		return new SimpleDateFormat("EEEE").format(c.getTime());
	}

	public static Date strToDate(String strDate) {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		ParsePosition pos = new ParsePosition(0);
		Date strtodate = formatter.parse(strDate, pos);
		return strtodate;
	}

	public static long getDays(String date1, String date2) {
		if (date1 == null || date1.equals(""))
			return 0;
		if (date2 == null || date2.equals(""))
			return 0;
		// 转换为标准时间
		SimpleDateFormat myFormatter = new SimpleDateFormat("yyyy-MM-dd");
		java.util.Date date = null;
		java.util.Date mydate = null;
		try {
			date = myFormatter.parse(date1);
			mydate = myFormatter.parse(date2);
		} catch (Exception e) {
		}
		long day = (date.getTime() - mydate.getTime()) / (24 * 60 * 60 * 1000);
		return day;
	}

	public String getDefaultDay() {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar lastDate = Calendar.getInstance();
		lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
		lastDate.add(Calendar.MONTH, 1);// 加一个月，变为下月的1号
		lastDate.add(Calendar.DATE, -1);// 减去一天，变为当月最后一天

		str = sdf.format(lastDate.getTime());
		return str;
	}
	
	public String getPreviousMonthFirst() {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar lastDate = Calendar.getInstance();
		lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
		lastDate.add(Calendar.MONTH, -1);// 减一个月，变为下月的1号
		// lastDate.add(Calendar.DATE,-1);//减去一天，变为当月最后一天
		str = sdf.format(lastDate.getTime());
		return str;
	}

	public String getFirstDayOfMonth() {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Calendar lastDate = Calendar.getInstance();
		lastDate.set(Calendar.DATE, 1);// 设为当前月的1号
		str = sdf.format(lastDate.getTime());
		return str;
	}

	public String getCurrentWeekday() {
		weeks = 0;
		int mondayPlus = this.getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 6);
		Date monday = currentDate.getTime();

		DateFormat df = DateFormat.getDateInstance();
		String preMonday = df.format(monday);
		return preMonday;
	}

	/**
	 * 获取当天日期
	 * @param dateformat
	 * @return
	 */
	public String getNowTime(String dateformat) {
		Date now = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);// 可以方便地修改日期格式
		String hehe = dateFormat.format(now);
		return hehe;
	}
	
	/**
	 * 获取今天是这一周的第几天
	 */
	private static int getMondayPlus() {
		Calendar cd = Calendar.getInstance();
		// 获得今天是一周的第几天，星期日是第一天，星期二是第二天......
		int dayOfWeek = cd.get(Calendar.DAY_OF_WEEK) - 1; // 因为按中国礼拜一作为第一天所以这里减1
		if (dayOfWeek == 1) {
			return 0;
		} else {
			return 1 - dayOfWeek;
		}
	}
	
	/**
	 * 返回指定年度的所有周。List中包含的是String[2]对象<br>
	 * string[0]本周的开始日期,string[1]是本周的结束日期。<br>
	 * 日期的格式为yyyy-MM-dd。<br>
	 * 每年的第一个周(目前为1月1号为第一周)，必须包含星期一且是完整的七天。<br>
	 * 例如：2009年的第一个周开始日期为2009-01-05，结束日期为2009-01-11。 <br>
	 * 星期一在哪一年，那么包含这个星期的周就是哪一年的周。<br>
	 * 例如：2008-12-29是星期一，2009-01-04是星期日，哪么这个周就是2008年度的最后一个周。<br>
	 * @param year 格式 yyyy  ，必须大于1900年度 小于9999年 
	 * @return 
	 */
	public static List<String> getWeeksByYear(final int year){
		if(year<1900 || year >9999){
			throw new NullPointerException("年度必须大于等于1900年小于等于9999年");
		}
		//实现思路，首先计算当年有多少个周，然后找到每个周的开始日期和结束日期
//		Calendar calendar = new GregorianCalendar();
//		// 在具有默认语言环境的默认时区内使用当前时间构造一个默认的 GregorianCalendar。
//		calendar.setFirstDayOfWeek(Calendar.MONDAY); //设置每周的第一天为星期一
//		calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY); //每周从周一开始		
//      上面两句代码配合，才能实现，每年度的第一个周，是包含第一个星期一的那个周。		
//		calendar.setMinimalDaysInFirstWeek(7);  //设置每周最少为7天
//		calendar.set(Calendar.YEAR, year); // 设置年度为指定的年
//		//首先计算当年有多少个周,每年都至少有52个周，个别年度有53个周
		int weeks = getWeekNumByYear(year);
//		System.out.println(year+"共有"+weeks+"个周");
		List<String> result = new ArrayList<String>(weeks);
		String tempStr = new String();
		for(int i=1;i<=weeks;i++){
			tempStr = year+"年第"+i+"周 "+getYearWeekFirstDay(year,i)+"-"+getYearWeekEndDay (year,i);
//			tempWeek[0] = year+"年";
//			tempWeek[1] = i+"周";
//			tempWeek[2] = getYearWeekFirstDay(year,i);
//			tempWeek[3] = getYearWeekEndDay (year,i);
//或者使用下面的代码，不过发现效率更低			
//			tempWeek[0] = getDateAdd(firstWeekDay,(i-1)*7+0);
//			tempWeek[1] = getDateAdd(firstWeekDay,(i-1)*7+6);			
			result.add(tempStr);
//			System.out.println(i+"="+tempWeek[0]+"_"+tempWeek[1]);
		}
		return result;
	}

	/**
	 * 计算指定年度共有多少个周。
	 * @param year 格式 yyyy  ，必须大于1900年度 小于9999年 
	 * @return 
	 */
	public static int getWeekNumByYear(final int year){
		if(year<1900 || year >9999){
			throw new NullPointerException("年度必须大于等于1900年小于等于9999年");
		}
		int result = 52;//每年至少有52个周 ，最多有53个周。
		String date = getYearWeekFirstDay(year,53);
		if(date.substring(0, 4).equals(year+"")){ //判断年度是否相符，如果相符说明有53个周。
			result = 53;
		}
		return result;
	}
	
	public static String getDateFormat(Date dateTime) {
		SimpleDateFormat df = new SimpleDateFormat("MM/dd");
		return df.format(dateTime);
	}
	
    /**
     * 计算某年某周的开始日期
     * @param yearNum 格式 yyyy  ，必须大于1900年度 小于9999年 
     * @param weekNum 1到52或者53
     * @return 日期，格式为MM/dd
     */
    public static String getYearWeekFirstDay(int yearNum,int weekNum)  {
		if(yearNum<1900 || yearNum >9999){
			throw new NullPointerException("年度必须大于等于1900年小于等于9999年");
		}
	     Calendar cal = Calendar.getInstance();
	     cal.setFirstDayOfWeek(Calendar.MONDAY); //设置每周的第一天为星期一
	     cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);//每周从周一开始
//	     上面两句代码配合，才能实现，每年度的第一个周，是包含第一个星期一的那个周。
	     cal.setMinimalDaysInFirstWeek(1);  //设置每周最少为7天     
	     cal.set(Calendar.YEAR, yearNum);
	     cal.set(Calendar.WEEK_OF_YEAR, weekNum);
	     //分别取得当前日期的年、月、日
	     return getDateFormat(cal.getTime());
    }
    /**
     * 计算某年某周的结束日期
     * @param yearNum 格式 yyyy  ，必须大于1900年度 小于9999年 
     * @param weekNum 1到52或者53
     * @return 日期，格式为yyyy-MM-dd
     */
    public static String getYearWeekEndDay(int yearNum,int weekNum)  {
		if(yearNum<1900 || yearNum >9999){
			throw new NullPointerException("年度必须大于等于1900年小于等于9999年");
		}    	
	     Calendar cal = Calendar.getInstance();
	     cal.setFirstDayOfWeek(Calendar.SUNDAY); //设置每周的第一天为星期一
	     cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);//每周从周一开始
//	     上面两句代码配合，才能实现，每年度的第一个周，是包含第一个星期一的那个周。	     
	     cal.setMinimalDaysInFirstWeek(7);  //设置每周最少为7天         
	     cal.set(Calendar.YEAR, yearNum);
	     cal.set(Calendar.WEEK_OF_YEAR, weekNum);
	     return getDateFormat(cal.getTime());
    }
	/**
	 * 获取本周一日期
	 * @return
	 */
	public String getMondayOFWeek() {
		weeks = 0;
		int mondayPlus = this.getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus);
		Date monday = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preMonday = df.format(monday);
		return preMonday;
	}

	public String getSaturday() {
		int mondayPlus = this.getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks + 6);
		Date monday = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preMonday = df.format(monday);
		return preMonday;
	}

	public String getPreviousWeekSunday() {
		weeks = 0;
		weeks--;
		int mondayPlus = this.getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + weeks);
		Date monday = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preMonday = df.format(monday);
		return preMonday;
	}

	public String getPreviousWeekday() {
		weeks--;
		int mondayPlus = this.getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 * weeks);
		Date monday = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preMonday = df.format(monday);
		return preMonday;
	}

	public String getNextMonday() {
		weeks++;
		int mondayPlus = this.getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7);
		Date monday = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preMonday = df.format(monday);
		return preMonday;
	}

	public String getNextSunday() {

		int mondayPlus = this.getMondayPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, mondayPlus + 7 + 6);
		Date monday = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preMonday = df.format(monday);
		return preMonday;
	}

	private int getMonthPlus() {
		Calendar cd = Calendar.getInstance();
		int monthOfNumber = cd.get(Calendar.DAY_OF_MONTH);
		cd.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		cd.roll(Calendar.DATE, -1);// 日期回滚一天，也就是最后一天
		MaxDate = cd.get(Calendar.DATE);
		if (monthOfNumber == 1) {
			return -MaxDate;
		} else {
			return 1 - monthOfNumber;
		}
	}

	public String getPreviousMonthEnd() {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar lastDate = Calendar.getInstance();
		lastDate.add(Calendar.MONTH, -1);// 减一个月
		lastDate.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		lastDate.roll(Calendar.DATE, -1);// 日期回滚一天，也就是本月最后一天
		str = sdf.format(lastDate.getTime());
		return str;
	}

	public String getNextMonthFirst() {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar lastDate = Calendar.getInstance();
		lastDate.add(Calendar.MONTH, 1);// 减一个月
		lastDate.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		str = sdf.format(lastDate.getTime());
		return str;
	}

	public String getNextMonthEnd() {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar lastDate = Calendar.getInstance();
		lastDate.add(Calendar.MONTH, 1);// 加一个月
		lastDate.set(Calendar.DATE, 1);// 把日期设置为当月第一天
		lastDate.roll(Calendar.DATE, -1);// 日期回滚一天，也就是本月最后一天
		str = sdf.format(lastDate.getTime());
		return str;
	}

	public String getNextYearEnd() {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar lastDate = Calendar.getInstance();
		lastDate.add(Calendar.YEAR, 1);// 加一个年
		lastDate.set(Calendar.DAY_OF_YEAR, 1);
		lastDate.roll(Calendar.DAY_OF_YEAR, -1);
		str = sdf.format(lastDate.getTime());
		return str;
	}

	public String getNextYearFirst() {
		String str = "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar lastDate = Calendar.getInstance();
		lastDate.add(Calendar.YEAR, 1);// 加一个年
		lastDate.set(Calendar.DAY_OF_YEAR, 1);
		str = sdf.format(lastDate.getTime());
		return str;

	}

	private int getMaxYear() {
		Calendar cd = Calendar.getInstance();
		cd.set(Calendar.DAY_OF_YEAR, 1);// 把日期设为当年第一天
		cd.roll(Calendar.DAY_OF_YEAR, -1);// 把日期回滚一天。
		int MaxYear = cd.get(Calendar.DAY_OF_YEAR);
		return MaxYear;
	}

	private int getYearPlus() {
		Calendar cd = Calendar.getInstance();
		int yearOfNumber = cd.get(Calendar.DAY_OF_YEAR);// 获得当天是一年中的第几天
		cd.set(Calendar.DAY_OF_YEAR, 1);// 把日期设为当年第一天
		cd.roll(Calendar.DAY_OF_YEAR, -1);// 把日期回滚一天。
		int MaxYear = cd.get(Calendar.DAY_OF_YEAR);
		if (yearOfNumber == 1) {
			return -MaxYear;
		} else {
			return 1 - yearOfNumber;
		}
	}

	public String getCurrentYearFirst() {
		int yearPlus = this.getYearPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, yearPlus);
		Date yearDay = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preYearDay = df.format(yearDay);
		return preYearDay;
	}

	// 获得本年最后一天的日期 *
	public String getCurrentYearEnd() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		return years + "-12-31";
	}

	// 获得上年第一天的日期 *
	public String getPreviousYearFirst() {
		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		int years_value = Integer.parseInt(years);
		years_value--;
		return years_value + "-1-1";
	}

	// 获得上年最后一天的日期
	public String getPreviousYearEnd() {
		weeks--;
		int yearPlus = this.getYearPlus();
		GregorianCalendar currentDate = new GregorianCalendar();
		currentDate.add(GregorianCalendar.DATE, yearPlus + MaxYear * weeks
				+ (MaxYear - 1));
		Date yearDay = currentDate.getTime();
		DateFormat df = DateFormat.getDateInstance();
		String preYearDay = df.format(yearDay);
		return preYearDay;
	}

	public String getThisSeasonFirstTime(int month) {
		int array[][] = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 }, { 10, 11, 12 } };
		int season = 1;
		if (month >= 1 && month <= 3) {
			season = 1;
		}
		if (month >= 4 && month <= 6) {
			season = 2;
		}
		if (month >= 7 && month <= 9) {
			season = 3;
		}
		if (month >= 10 && month <= 12) {
			season = 4;
		}
		int start_month = array[season - 1][0];
		int end_month = array[season - 1][2];

		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		int years_value = Integer.parseInt(years);

		int start_days = 1;// years+"-"+String.valueOf(start_month)+"-1";//getLastDayOfMonth(years_value,start_month);
		int end_days = getLastDayOfMonth(years_value, end_month);
		String seasonDate = years_value + "-" + start_month + "-" + start_days;
		return seasonDate;

	}

	public String getThisSeasonFinallyTime(int month) {
		int array[][] = { { 1, 2, 3 }, { 4, 5, 6 }, { 7, 8, 9 }, { 10, 11, 12 } };
		int season = 1;
		if (month >= 1 && month <= 3) {
			season = 1;
		}
		if (month >= 4 && month <= 6) {
			season = 2;
		}
		if (month >= 7 && month <= 9) {
			season = 3;
		}
		if (month >= 10 && month <= 12) {
			season = 4;
		}
		int start_month = array[season - 1][0];
		int end_month = array[season - 1][2];

		Date date = new Date();
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy");// 可以方便地修改日期格式
		String years = dateFormat.format(date);
		int years_value = Integer.parseInt(years);

		int start_days = 1;// years+"-"+String.valueOf(start_month)+"-1";//getLastDayOfMonth(years_value,start_month);
		int end_days = getLastDayOfMonth(years_value, end_month);
		String seasonDate = years_value + "-" + end_month + "-" + end_days;
		return seasonDate;

	}

	private int getLastDayOfMonth(int year, int month) {
		if (month == 1 || month == 3 || month == 5 || month == 7 || month == 8 || month == 10 || month == 12) {
			return 31;
		}
		if (month == 4 || month == 6 || month == 9 || month == 11) {
			return 30;
		}
		if (month == 2) {
			if (isLeapYear(year)) {
				return 29;
			} else {
				return 28;
			}
		}
		return 0;
	}

	public boolean isLeapYear(int year) {
		return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0);
	}

	public boolean isLeapYear2(int year) {
		return new GregorianCalendar().isLeapYear(year);
	}
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    // 获得当前月--开始日期
    public static String getMinMonthDate(String date) {   
             Calendar calendar = Calendar.getInstance();   
              try {
                 calendar.setTime(dateFormat.parse(date));
                 calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH)); 
                 return dateFormat.format(calendar.getTime());
               } catch (java.text.ParseException e) {
               e.printStackTrace();
              }
            return null;

    }
    // 获得当前月--结束日期
    public static String getMaxMonthDate(String date){   
         Calendar calendar = Calendar.getInstance();   
         try {
                calendar.setTime(dateFormat.parse(date));
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                return dateFormat.format(calendar.getTime());
         }  catch (java.text.ParseException e) {
                e.printStackTrace();
          }
        return null;
    }
    // 获得当前周- 周一的日期
    public static String getCurrentMonday() {
        int mondayPlus = getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus);
        return dateFormat.format(currentDate.getTime());
//        Date monday = currentDate.getTime();
//        DateFormat df = DateFormat.getDateInstance();
//        String preMonday = df.format(monday);
//        return preMonday;
    }
    
    // 获得当前周- 周日  的日期
    public static String getPreviousSunday() {
        int mondayPlus = getMondayPlus();
        GregorianCalendar currentDate = new GregorianCalendar();
        currentDate.add(GregorianCalendar.DATE, mondayPlus +6);
//        Date monday = currentDate.getTime();
//        DateFormat df = DateFormat.getDateInstance();
//        String preMonday = df.format(monday);
//        return preMonday;
        return dateFormat.format(currentDate.getTime());
    }
    
    /**
    * @Title: dateToString 
    * @Description: TODO(date类型转换为String类型) 
    * @param @param data Date类型的时间
    * @param @param formatType formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    * @return String    返回类型 
     */
  	public static String dateToString(Date date, String formatType) {
  		return new SimpleDateFormat(formatType).format(date);
  	}
  	/**
  	* @Title: longToString 
  	* @Description: TODO(long类型转换为String类型) 
  	* @param @param currentTime currentTime要转换的long类型的时间
  	* @param @param formatType formatType要转换的string类型的时间格式
  	* @return String    返回类型 
  	 */
  	public static String longToString(long currentTime, String formatType) throws ParseException{
	  	Date date = longToDate(currentTime, formatType); // long类型转成Date类型
	  	String strTime = dateToString(date, formatType); // date类型转成String
	  	return strTime;
  	}
  	/**
  	* @Title: stringToDate 
  	* @Description: TODO(string类型转换为date类型) 
  	* @param @param strTime strTime要转换的string类型的时间
  	* @param @param formatType formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒，
  	* @return Date    返回类型 
  	* strTime的时间格式必须要与formatType的时间格式相同
  	 */
  	public static Date stringToDate(String strTime, String formatType) throws ParseException{
	  	SimpleDateFormat formatter = new SimpleDateFormat(formatType);
	  	Date date = null;
	  	date = formatter.parse(strTime);
	  	return date;
  	}
  	/**
  	* @Title: longToDate 
  	* @Description: TODO(long转换为Date类型) 
  	* @param @param currentTime currentTime要转换的long类型的时间
  	* @param @param formatType formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
  	* @return Date    返回类型 
  	 */
  	public static Date longToDate(long currentTime, String formatType) throws ParseException{
	  	Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
	  	String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
	  	Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
	  	return date;
  	}
  	/**
  	* @Title: stringToLong 
  	* @Description: TODO(string类型转换为long类型) 
  	* @param @param strTime strTime要转换的String类型的时间
  	* @param @param formatType formatType时间格式
  	* @return long    返回类型 
  	* strTime的时间格式和formatType的时间格式必须相同
  	 */
  	public static long stringToLong(String strTime, String formatType) throws ParseException {
	  	Date date = stringToDate(strTime, formatType); // String类型转成date类型
	  	if (date == null) {
	  		return 0;
	  	} else {
		  	long currentTime = dateToLong(date); // date类型转成long类型
		  	return currentTime;
	  	}
  	}
  	
  	/**
  	* @Title: dateToLong 
  	* @Description: TODO(date类型转换为long类型) 
  	* @param @param date date要转换的date类型的时间
  	* @return long    返回类型 
  	 */
  	public static long dateToLong(Date date) {
  		return date.getTime();
  	}
}