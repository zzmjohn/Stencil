package stencil.modules;

import stencil.util.collections.ArrayUtil;

public class Dates {
	private static final String[] months = new String[12];
	static {
		months[0] = "January";
		months[1] = "February";
		months[2] = "March";
		months[3] = "April";
		months[4] = "May";
		months[5] = "Jun";
		months[6] = "July";
		months[7] = "August";
		months[8] = "September";
		months[9] = "October";
		months[10] = "November";
		months[11] = "December";
	}
	
	public static int monthNum(String month) {
		month = nice(month);
		int idx =ArrayUtil.indexOf(month, months); 
		if (idx <0) {
			idx = ArrayUtil.indexOf(fullMonth(month), months);
		}
		if (idx <0) {throw new RuntimeException("Input does not correspond to a month: " + month);}
		return  idx + 1;
	}
	
	public static String num2Month(int month) {return months[month-1];}
	public static String fullMonth(String abr) {
		abr = nice(abr);
		for (String month: months) {
			if (month.startsWith(abr)) {return month;}
		}
		throw new RuntimeException("Input does not correspond to an abbreviation:" + abr);
	}

	private static final String nice(String s) {
		s = s.trim().toLowerCase();
		if (s.length() >0) {s = s.substring(0,1).toUpperCase() + s.substring(1);}
		return s;
	}
}