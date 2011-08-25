package stencil.interpreter.guide.samplers;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import stencil.interpreter.guide.SampleOperator;
import stencil.interpreter.guide.SampleSeed;
import stencil.interpreter.tree.Specializer;
import stencil.tuple.Tuple;
import stencil.types.Converter;

//TODO: Implement a date seed operator that can treat them as continuous (save the parse)
/**Sample  between two dates.*/
public class DateSampler implements SampleOperator {
	/**Should this sample in units of days, weeks, months or years?**/
	public static final String UNIT_KEY = "unit";
	
	/**How many units at a time?  Must be a positive whole number.*/
	public static final String STRIDE_KEY = "stride";
	
	/**How should inputs be parsed and outputs returned?
	 * Outputs may be modified, based on the selected unit.
	 */
	public static final String PARSE_KEY = "parse";
	private static final String DEFAULT_PARSE = "dd-MMM-yy";

	public static final String FORMAT_KEY = "format";

	
	private static enum Unit {
		DAY ("dd-MMM-yyyy"), 
		WEEK ("dd-MMM-yyyy"), 
		MONTH ("MMM-yyyy"), 
		YEAR ("yyyy"), 
		DECADE ("yyyy"), 
		CENTURY ("yyyy");		
	
		public final String format;
		private Unit(String format) {this.format = format;}
	}
	
	public List<Tuple> sample(SampleSeed seed, Specializer spec) {
		Unit unit = (Unit) Converter.convert(spec.get(UNIT_KEY, Unit.YEAR), Unit.class);
		String format = (String) Converter.convert(spec.get(PARSE_KEY, DEFAULT_PARSE), String.class);
		int stride = (Integer) Converter.convert(spec.get(STRIDE_KEY, 1), Integer.class);
		SimpleDateFormat formatter = new SimpleDateFormat(format);

		String outFormat = (String) Converter.convert(spec.get(FORMAT_KEY, unit.format), String.class);
		
		List<Date> dates = new ArrayList();
		
		seed = seed.getCategorical();
		for (Object v: seed) {
			try {
				if (v.getClass().isArray()) {v = Array.get(v, 0);}
				dates.add(formatter.parse(v.toString()));
			} catch (ParseException e) {throw new RuntimeException("Error parsing date `" + v + "' with format `" + format + "'", e);}
		}
		
		if (dates.size() < 1) {return new ArrayList();}
		
		Collections.sort(dates);
		

		Date start = dates.get(0);
		Date end = dates.get(dates.size()-1);
		
		switch(unit) {
			case DAY: dates = days(start, end, stride); break;
			case WEEK: dates = weeks(start, end, stride); break;
			case MONTH: dates = months(start, end, stride); break;
			case YEAR: dates = years(start, end, stride); break;
			case DECADE: dates = decades(start, end, stride); break;
			case CENTURY: dates = centuries(start, end, stride); break;
		}
		
		List<Tuple> sample = new ArrayList();
		formatter = new SimpleDateFormat(outFormat);
		for (Date d: dates) {
			String s = formatter.format(d);
			sample.add(Converter.toTuple(s));
		}
		return sample;		
	}
	
	public static List<Date> days(Date start, Date end, int stride) {
		return runOf(start, end, Calendar.DATE, stride);
	}

	public static List<Date> weeks(Date start, Date end, int stride) {
		start = find(start, Calendar.DATE, 1, WEEK_START);
		return runOf(start, end, Calendar.DATE, 7*stride);
	}
	
	public static List<Date> months(Date start, Date end, int stride) {
		return runOf(start, end, Calendar.MONTH, stride);		
	}
	
	public static List<Date> years(Date start, Date end, int stride) {
		return runOf(start, end, Calendar.YEAR, stride);				
	}
	
	public static List<Date> decades(Date start, Date end, int stride) {
		start = find(start, Calendar.DATE, 1, DECADE);
		return runOf(start, end, Calendar.YEAR, 10*stride);
	}
	
	public static List<Date> centuries(Date start, Date end, int stride) {
		start = find(start, Calendar.DATE, 1, CENTURY);
		return runOf(start, end, Calendar.YEAR, 100*stride);
	}
	
	private static Date find(Date start, int field, int amount, Predicate p) {
		Calendar c = Calendar.getInstance();
		c.setTime(start);
		while(!p.is(c)) {
			c.add(field, amount);
		}
		return c.getTime();
	}
	
	private static List<Date> runOf(Date start, Date end, int field, int amount) {
		Calendar cal = Calendar.getInstance();

		cal.setTime(end);
		cal.add(Calendar.DATE, 1);		//HACK: Makes the  ranges work  out the way I think they should; +1 to the day, so you get the last day/month but doesn't mess up weeks or years because it only adds on more unit
		end = cal.getTime();
		
		ArrayList dates = new ArrayList();
		Date now = start;
		while (now.before(end)) {
			dates.add(now);
			cal.setTime(now);
			cal.add(field, amount);
			now = cal.getTime();
		}
		return dates;				
	}
	
	private static interface Predicate {public boolean is(Calendar c);}

	private static final Predicate DECADE = new Predicate() {
		public boolean is(Calendar c) {
			int year=c.get(Calendar.YEAR);
			return year%10==0;
		}
	};
	
	private static final Predicate CENTURY  = new Predicate() {
		public boolean is(Calendar c) {
			int year=c.get(Calendar.YEAR);
			return year%100==0;
		}		
	};
	
	private static final Predicate WEEK_START = new Predicate() {
		public boolean is(Calendar c) {
			return c.get(Calendar.DAY_OF_WEEK) == c.getFirstDayOfWeek();
		}
	};
}

 	