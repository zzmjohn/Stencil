package stencil.types.geometry;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Array;

import stencil.tuple.Tuple;
import stencil.types.Converter;
import stencil.types.TypeWrapper;
import stencil.util.ConversionException;

/**Wrap the basic geometric units in tuples.  
 * Can handle: Point2D and Rectangle2D
 */

public class GeometryWrapper implements TypeWrapper {
	private static Class[] ACCEPTS = {Point2D.Double.class, Rectangle2D.Double.class, Point2D.Float.class, Rectangle2D.Float.class, Point2D.class, Rectangle2D.class, Point.class, Rectangle.class};
	
	@Override
	public Class[] appliesTo() {return ACCEPTS;}

	@Override
	public Object convert(Object v, Class c) {
		if (v.getClass().isArray()) {
			int length = Array.getLength(v);
			if (Rectangle2D.class.equals(c) && length == 4) {
				double x = Converter.toDouble(Array.get(v, 0));
				double y = Converter.toDouble(Array.get(v, 0));
				double w = Converter.toDouble(Array.get(v, 0));
				double h = Converter.toDouble(Array.get(v, 0));
				return new Rectangle2D.Double(x,y,w,h);
			} else if (Point2D.class.equals(c) && length ==2) {
				double x = Converter.toDouble(Array.get(v, 0));
				double y = Converter.toDouble(Array.get(v, 0));
				return new Point2D.Double(x,y);
			}
		} else if (c.equals(String.class)) {
			return v.toString();
		} else if (c.equals(Rectangle.class) && v instanceof String) {
			/*Take a comma separated list of integers and make a rectangle out of it.*/
			try {
				String[] parts = ((String) v).split("\\s*,\\s*");
				int[] dims = new int[4];
				for (int i=0; i<dims.length;i++) {
					dims[i] = Integer.parseInt(parts[i]);
				}
				return new Rectangle(dims[0], dims[1], dims[2], dims[3]);
			} catch (Exception e) {throw new ConversionException(v,c,e);}
		} else if (c.equals(Point2D.class) && v instanceof PointTuple) {
			return ((PointTuple) v).basis();
		} else if (c.equals(Rectangle2D.class) && v instanceof RectangleTuple) {
			return ((RectangleTuple) v).basis();
		}
		throw new ConversionException(v,c);
	}

	@Override
	public Tuple toTuple(Object o) {
		if (o instanceof RectangleTuple || o instanceof PointTuple) {return (Tuple) o;}
		if (o instanceof Point2D) {return new PointTuple((Point2D) o);}
		if (o instanceof Rectangle2D) {return new RectangleTuple((Rectangle2D) o);}
		throw new RuntimeException("Error wrapping: " + o.toString());
	}

}
