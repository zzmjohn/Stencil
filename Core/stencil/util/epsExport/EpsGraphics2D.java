/*
 *  Copyright 2001-2006 Paul James Mutton, http://www.jibble.org/
 *  Copyright 2007 Arnaud Blouin 
 *
 *  This file is part of jlibeps.
 * 
 *  jlibeps is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  jlibeps is distributed without any warranty; without even the 
 *  implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR 
 *  PURPOSE. See the GNU General Public License for more details.<br>
 * 
 */

package stencil.util.epsExport;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.RenderableImage;
import java.io.*;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.text.CharacterIterator;
import java.util.Hashtable;
import java.util.Map;





/**
 * Taken whole-cloth from jlibeps, then modified slightly for Stencil.
 * 
 * 
 * EpsGraphics2D is suitable for creating high quality EPS graphics for use in
 * documents and papers, and can be used just like a standard Graphics2D object.
 * <p>
 * Many Java programs use Graphics2D to draw stuff on the screen, and while it
 * is easy to save the output as a png or jpeg file, it is a little harder to
 * export it as an EPS for including in a document or paper.
 * <p>
 * This class makes the whole process extremely easy, because you can use it as
 * if it's a Graphics2D object. The only difference is that all of the
 * implemented methods create EPS output, which means the diagrams you draw can
 * be resized without leading to any of the jagged edges you may see when
 * resizing pixel-based images, such as jpeg and png files.
 * <p>
 * Example usage:
 * <p>
 * 
 * <pre>
 * Graphics2D g = new EpsGraphics2D();
 * g.setColor(Color.black);
 * 
 * // Line thickness 2.
 * g.setStroke(new BasicStroke(2.0f));
 * 
 * // Draw a line.
 * g.drawLine(10, 10, 50, 10);
 * 
 * // Fill a rectangle in blue
 * g.setColor(Color.blue);
 * g.fillRect(10, 0, 20, 20);
 * 
 * // Get the EPS output.
 * String output = g.toString();
 * </pre>
 * 
 * <p>
 * You do not need to worry about the size of the canvas when drawing on a
 * EpsGraphics2D object. The bounding box of the EPS document will automatically
 * resize to accommodate new items that you draw.
 * <p>
 * Not all methods are implemented yet. Those that are not are clearly labelled.
 * <p>
 * Copyright 2001-2006 Paul James Mutton, http://www.jibble.org/<br>
 * Copyright 2007 Arnaud Blouin<br>
 * 08/09/07
 * @version 0.1
 */
public class EpsGraphics2D extends java.awt.Graphics2D
{

	public static final String	VERSION			= "0.1";

	public static final int		BLACK_AND_WHITE	= 1;

	public static final int		GRAYSCALE		= 2;

	public static final int		RGB				= 3;		// Default



	private Color						_color;

	private Color						_backgroundColor;

	private Paint						_paint;

	private Composite					_composite;

	private BasicStroke					_stroke;

	private Font						_font;

	private Shape						_clip;

	private AffineTransform				_clipTransform;

	private AffineTransform				_transform;

	private boolean						_accurateTextMode;

	private int							_colorDepth;

	private EpsDocument					_document;

	private static FontRenderContext	_fontRenderContext	= new FontRenderContext(null, false, true);
	
	


	/**
	 * Constructs a new EPS document that is initially empty and can be drawn on
	 * like a Graphics2D object. The EPS document is stored in memory.
	 * @since 0.1
	 */
	public EpsGraphics2D()
	{
		this("Untitled");
	}





	/**
	 * Constructs a new EPS document that is initially empty and can be drawn on
	 * like a Graphics2D object. The EPS document is stored in memory.
	 * @since 0.1
	 */
	public EpsGraphics2D(String title)
	{
		_document = new EpsDocument(title);
		_backgroundColor = Color.white;
		_clip = null;
		_transform = new AffineTransform();
		_clipTransform = new AffineTransform();
		_accurateTextMode = true;
		_colorDepth = EpsGraphics2D.RGB;
		setColor(Color.black);
		setPaint(Color.black);
		setComposite(AlphaComposite.getInstance(AlphaComposite.CLEAR));
		setFont(Font.decode(null));
		setStroke(new BasicStroke());
	}





	/**
	 * Constructs a new EPS document that is initially empty and can be drawn on
	 * like a Graphics2D object. The EPS document is written to the file as it
	 * goes, which reduces memory usage. The bounding box of the document is
	 * fixed and specified at construction time by minX,minY,maxX,maxY. The file
	 * is flushed and closed when the close() method is called.
	 * @since 0.1
	 */
	public EpsGraphics2D(String title, File file, int minX, int minY, int maxX, int maxY) throws IOException
	{
		this(title, new FileOutputStream(file), minX, minY, maxX, maxY);
	}





	/**
	 * Constructs a new EPS document that is initially empty and can be drawn on
	 * like a Graphics2D object. The EPS document is written to the output
	 * stream as it goes, which reduces memory usage. The bounding box of the
	 * document is fixed and specified at construction time by
	 * minX,minY,maxX,maxY. The output stream is flushed and closed when the close() method is called.
	 * @since 0.1
	 */
	public EpsGraphics2D(String title, OutputStream outputStream, int minX, int minY, int maxX, int maxY) throws IOException
	{
		this(title);
		_document = new EpsDocument(title, outputStream, minX, minY, maxX, maxY);
	}





	/**
	 * Constructs a new EpsGraphics2D instance that is a copy of the supplied
	 * argument and points at the same EpsDocument.
	 * @since 0.1
	 */
	protected EpsGraphics2D(EpsGraphics2D g)
	{
		_document = g._document;
		_backgroundColor = g._backgroundColor;
		_clip = g._clip;
		_clipTransform = (AffineTransform)g._clipTransform.clone();
		_transform = (AffineTransform)g._transform.clone();
		_color = g._color;
		_paint = g._paint;
		_composite = g._composite;
		_font = g._font;
		_stroke = g._stroke;
		_accurateTextMode = g._accurateTextMode;
		_colorDepth = g._colorDepth;
	}





	/**
	 * This method is called to indicate that a particular method is not
	 * supported yet. The stack trace is printed to the standard output.
	 * @since 0.1
	 */
	private void methodNotSupported()
	{
		EpsException e = new EpsException("Method not currently supported by jlibeps version " + VERSION);
		e.printStackTrace(System.err);
	}





	// ///////////// Specialist methods ///////////////////////

	/**
	 * Sets whether to use accurate text mode when rendering text in EPS. This
	 * is enabled (true) by default. When accurate text mode is used, all text
	 * will be rendered in EPS to appear exactly the same as it would do when
	 * drawn with a Graphics2D context. With accurate text mode enabled, it is
	 * not necessary for the EPS viewer to have the required font installed.
	 * <p>
	 * Turning off accurate text mode will require the EPS viewer to have the
	 * necessary fonts installed. If you are using a lot of text, you will find
	 * that this significantly reduces the file size of your EPS documents.
	 * AffineTransforms can only affect the starting point of text using this
	 * simpler text mode - all text will be horizontal.
	 * @since 0.1
	 */
	public void setAccurateTextMode(boolean b)
	{
		_accurateTextMode = b;

		if(!getAccurateTextMode())
			setFont(getFont());
	}





	/**
	 * Returns whether accurate text mode is being used.
	 * @since 0.1
	 */
	public boolean getAccurateTextMode()
	{
		return _accurateTextMode;
	}





	/**
	 * Sets the number of colours to use when drawing on the document. Can be
	 * either EpsGraphics2D.RGB (default) or EpsGraphics2D.GREYSCALE.
	 * @since 0.1
	 */
	public void setColorDepth(int c)
	{
		if(c == RGB || c == GRAYSCALE || c == BLACK_AND_WHITE)
			_colorDepth = c;
	}





	/**
	 * Returns the colour depth used for all drawing operations. This can be
	 * either EpsGraphics2D.RGB (default) or EpsGraphics2D.GREYSCALE.
	 * @since 0.1
	 */
	public int getColorDepth()
	{
		return _colorDepth;
	}





	/**
	 * Flushes the buffered contents of this EPS document to the underlying
	 * OutputStream it is being written to.
	 * @since 0.1
	 */
	public void flush() throws IOException
	{
		_document.flush();
	}





	/**
	 * Closes the EPS file being output to the underlying OutputStream. The
	 * OutputStream is automatically flushed before being closed. If you forget
	 * to do this, the file may be incomplete.
	 * @since 0.1
	 */
	public void close() throws IOException
	{
		flush();
		_document.close();
	}





	/**
	 * Appends a line to the EpsDocument.
	 * @since 0.1
	 */
	private void append(String line)
	{
		_document.append(this, line);
	}





	/**
	 * Returns the point after it has been transformed by the transformation.
	 * @since 0.1
	 */
	private Point2D transform(float x, float y)
	{
		Point2D result = new Point2D.Float(x, y);
		result = _transform.transform(result, result);
		result.setLocation(result.getX(), -result.getY());
		return result;
	}





	/**
	 * Appends the commands required to draw a shape on the EPS document.
	 * @since 0.1
	 */
	private void draw(Shape s, String action)
	{

		if(s != null)
		{
			if(!_transform.isIdentity())
				s = _transform.createTransformedShape(s);

			// Update the bounds.
			if(!action.equals("clip"))
			{
				Rectangle2D shapeBounds = s.getBounds2D();
				Rectangle2D visibleBounds = shapeBounds;
				
				if(_clip != null)
				{
					Rectangle2D clipBounds = _clip.getBounds2D();
					visibleBounds = shapeBounds.createIntersection(clipBounds);
				}
				
				float lineRadius = _stroke.getLineWidth() / 2;
				float minX = (float)visibleBounds.getMinX() - lineRadius;
				float minY = (float)visibleBounds.getMinY() - lineRadius;
				float maxX = (float)visibleBounds.getMaxX() + lineRadius;
				float maxY = (float)visibleBounds.getMaxY() + lineRadius;
				_document.updateBounds(minX, -minY);
				_document.updateBounds(maxX, -maxY);
			}

			append("newpath");
			int type = 0;
			float[] coords = new float[6];
			PathIterator it = s.getPathIterator(null);
			float x0 = 0;
			float y0 = 0;
			
			while(!it.isDone())
			{
				type = it.currentSegment(coords);
				float x1 = coords[0];
				float y1 = -coords[1];
				float x2 = coords[2];
				float y2 = -coords[3];
				float x3 = coords[4];
				float y3 = -coords[5];

				if(type == PathIterator.SEG_CLOSE)
				{
					append("closepath");
				}
				else
					if(type == PathIterator.SEG_CUBICTO)
					{
						append(x1 + " " + y1 + " " + x2 + " " + y2 + " " + x3 + " " + y3 + " curveto");
						x0 = x3;
						y0 = y3;
					}
					else
						if(type == PathIterator.SEG_LINETO)
						{
							append(x1 + " " + y1 + " lineto");
							x0 = x1;
							y0 = y1;
						}
						else
							if(type == PathIterator.SEG_MOVETO)
							{
								append(x1 + " " + y1 + " moveto");
								x0 = x1;
								y0 = y1;
							}
							else
								if(type == PathIterator.SEG_QUADTO)
								{
									// Convert the quad curve into a cubic.
									float _x1 = x0 + 2 / 3f * (x1 - x0);
									float _y1 = y0 + 2 / 3f * (y1 - y0);
									float _x2 = x1 + 1 / 3f * (x2 - x1);
									float _y2 = y1 + 1 / 3f * (y2 - y1);
									float _x3 = x2;
									float _y3 = y2;
									append(_x1 + " " + _y1 + " " + _x2 + " " + _y2 + " " + _x3 + " " + _y3 + " curveto");
									x0 = _x3;
									y0 = _y3;
								}
								else
									if(type == PathIterator.WIND_EVEN_ODD)
									{
										// Ignore.
									}
									else
										if(type == PathIterator.WIND_NON_ZERO)
										{
											// Ignore.
										}
				it.next();
			}
			append(action);
			append("newpath");
		}
	}





	/**
	 * Returns a hex string that always contains two characters.
	 * @since 0.1
	 */
	private String toHexString(int n)
	{
		String result = Integer.toString(n, 16);
		
		while(result.length() < 2)
			result = "0" + result;

		return result;
	}





	/////////////// Graphics2D methods ///////////////////////

	/**
	 * Draws a 3D rectangle outline. If it is raised, light appears to come from the top left.
	 * @since 0.1
	 */
	public void draw3DRect(int x, int y, int width, int height, boolean raised)
	{
		Color originalColor = getColor();
		Stroke originalStroke = getStroke();

		setStroke(new BasicStroke(1.0f));

		if(raised)
			setColor(originalColor.brighter());
		else
			setColor(originalColor.darker());

		drawLine(x, y, x + width, y);
		drawLine(x, y, x, y + height);

		if(raised)
			setColor(originalColor.darker());
		else
			setColor(originalColor.brighter());

		drawLine(x + width, y + height, x, y + height);
		drawLine(x + width, y + height, x + width, y);

		setColor(originalColor);
		setStroke(originalStroke);
	}





	/**
	 * Fills a 3D rectangle. If raised, it has bright fill and light appears to come from the top left.
	 * @since 0.1
	 */
	public void fill3DRect(int x, int y, int width, int height, boolean raised)
	{
		Color originalColor = getColor();

		if(raised)
			setColor(originalColor.brighter());
		else
			setColor(originalColor.darker());

		draw(new Rectangle(x, y, width, height), "fill");
		setColor(originalColor);
		draw3DRect(x, y, width, height, raised);
	}





	/**
	 * Draws a Shape on the EPS document.
	 * @since 0.1
	 */
	public void draw(Shape s)
	{
		draw(s, "stroke");
	}





	/**
	 * Draws an Image on the EPS document.
	 * @since 0.1
	 */
	public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs)
	{
		AffineTransform at = getTransform();
		transform(xform);
		boolean st = drawImage(img, 0, 0, obs);
		setTransform(at);
		return st;
	}





	/**
	 * Draws a BufferedImage on the EPS document.
	 * @since 0.1
	 */
	public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y)
	{
		BufferedImage img1 = op.filter(img, null);
		drawImage(img1, new AffineTransform(1f, 0f, 0f, 1f, x, y), null);
	}





	/**
	 * Draws a RenderedImage on the EPS document.
	 * @since 0.1
	 */
	public void drawRenderedImage(RenderedImage img, AffineTransform xform)
	{
		Hashtable properties = new Hashtable();
		String[] names = img.getPropertyNames();
		
		for(int i = 0; i < names.length; i++)
			properties.put(names[i], img.getProperty(names[i]));

		ColorModel cm = img.getColorModel();
		WritableRaster wr = img.copyData(null);
		BufferedImage img1 = new BufferedImage(cm, wr, cm.isAlphaPremultiplied(), properties);
		AffineTransform at = AffineTransform.getTranslateInstance(img.getMinX(), img.getMinY());
		at.preConcatenate(xform);
		drawImage(img1, at, null);
	}





	/**
	 * Draws a RenderableImage by invoking its createDefaultRendering method.
	 * @since 0.1
	 */
	public void drawRenderableImage(RenderableImage img, AffineTransform xform)
	{
		drawRenderedImage(img.createDefaultRendering(), xform);
	}





	/**
	 * Draws a string at (x,y).
	 * @since 0.1
	 */
	public void drawString(String str, int x, int y)
	{
		drawString(str, (float)x, (float)y);
	}





	/**
	 * Draws a string at (x,y).
	 * @since 0.1
	 */
	public void drawString(String s, float x, float y)
	{
		if(s != null && s.length() > 0)
		{
			AttributedString as = new AttributedString(s);
			as.addAttribute(TextAttribute.FONT, getFont());
			drawString(as.getIterator(), x, y);
		}
	}





	/**
	 * Draws the characters of an AttributedCharacterIterator, starting from (x,y).
	 * @since 0.1
	 */
	public void drawString(AttributedCharacterIterator iterator, int x, int y)
	{
		drawString(iterator, (float)x, (float)y);
	}





	/**
	 * Draws the characters of an AttributedCharacterIterator, starting from (x,y).
	 * @since 0.1
	 */
	public void drawString(AttributedCharacterIterator iterator, float x, float y)
	{
		if(getAccurateTextMode())
		{
			TextLayout layout = new TextLayout(iterator, getFontRenderContext());
			Shape shape = layout.getOutline(AffineTransform.getTranslateInstance(x, y));
			draw(shape, "fill");
		}
		else
		{
			append("newpath");
			Point2D location = transform(x, y);
			append(location.getX() + " " + location.getY() + " moveto");
			StringBuffer buffer = new StringBuffer();
			
			for(char ch = iterator.first(); ch != CharacterIterator.DONE; ch = iterator.next())
			{
				if(ch == '(' || ch == ')')
					buffer.append('\\');

				buffer.append(ch);
			}
			
			append("(" + buffer.toString() + ") show");
		}
	}





	/**
	 * Draws a GlyphVector at (x,y).
	 * @since 0.1
	 */
	public void drawGlyphVector(GlyphVector g, float x, float y)
	{
		Shape shape = g.getOutline(x, y);
		draw(shape, "fill");
	}





	/**
	 * Fills a Shape on the EPS document.
	 * @since 0.1
	 */
	public void fill(Shape s)
	{
		draw(s, "fill");
	}





	/**
	 * Checks whether or not the specified Shape intersects the specified Rectangle, which is in device space.
	 * @since 0.1
	 */
	public boolean hit(Rectangle rect, Shape s, boolean onStroke)
	{
		return s.intersects(rect);
	}





	/**
	 * Returns the device configuration associated with this EpsGraphics2D object.
	 * @since 0.1
	 */
	public GraphicsConfiguration getDeviceConfiguration()
	{
		GraphicsConfiguration gc = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] gds = ge.getScreenDevices();
		
		for(int i = 0; i < gds.length; i++)
		{
			GraphicsDevice gd = gds[i];
			GraphicsConfiguration[] gcs = gd.getConfigurations();
			
			if(gcs.length > 0)
				return gcs[0];
		}
		
		return gc;
	}





	/**
	 * Sets the Composite to be used by this EpsGraphics2D. EpsGraphics2D does not make use of these.
	 * @since 0.1
	 */
	public void setComposite(Composite comp)
	{
		_composite = comp;
	}





	/**
	 * Sets the Paint attribute for the EpsGraphics2D object. Only Paint objects of type Color are respected by EpsGraphics2D.
	 * @since 0.1
	 */
	public void setPaint(Paint paint)
	{
		_paint = paint;
		
		if(paint instanceof Color)
			setColor((Color)paint);
	}





	/**
	 * Sets the stroke. Only accepts BasicStroke objects (or subclasses of BasicStroke).
	 * @since 0.1
	 */
	public void setStroke(Stroke s)
	{
		if(s instanceof BasicStroke)
		{
			_stroke = (BasicStroke)s;

			append(_stroke.getLineWidth() + " setlinewidth");
			float miterLimit = _stroke.getMiterLimit();
			
			if(miterLimit < 1.0f)
				miterLimit = 1;

			append(miterLimit + " setmiterlimit");
			append(_stroke.getLineJoin() + " setlinejoin");
			append(_stroke.getEndCap() + " setlinecap");

			StringBuffer dashes = new StringBuffer();
			dashes.append("[ ");
			float[] dashArray = _stroke.getDashArray();
			
			if(dashArray != null)
				for(int i = 0; i < dashArray.length; i++)
					dashes.append((dashArray[i]) + " ");

			dashes.append("]");
			append(dashes.toString() + " 0 setdash");
		}
	}





	/**
	 * Sets a rendering hint. These are not used by EpsGraphics2D.
	 * @since 0.1
	 */
	public void setRenderingHint(RenderingHints.Key hintKey, Object hintValue)
	{
		// Do nothing.
	}





	/**
	 * Returns the value of a single preference for the rendering algorithms.
	 * Rendering hints are not used by EpsGraphics2D.
	 * @since 0.1
	 */
	public Object getRenderingHint(RenderingHints.Key hintKey)
	{
		return null;
	}





	/**
	 * Sets the rendering hints. These are ignored by EpsGraphics2D.
	 * @since 0.1
	 */
	public void setRenderingHints(Map hints)
	{
		// Do nothing.
	}





	/**
	 * Adds rendering hints. These are ignored by EpsGraphics2D.
	 * @since 0.1
	 */
	public void addRenderingHints(Map hints)
	{
		// Do nothing.
	}





	/**
	 * Returns the preferences for the rendering algorithms.
	 * @since 0.1
	 */
	public RenderingHints getRenderingHints()
	{
		return new RenderingHints(null);
	}





	/**
	 * Translates the origin of the EpsGraphics2D context to the point (x,y) in the current coordinate system.
	 * @since 0.1
	 */
	public void translate(int x, int y)
	{
		translate((double)x, (double)y);
	}





	/**
	 * Concatenates the current EpsGraphics2D Transformation with a translation transform.
	 * @since 0.1
	 */
	public void translate(double tx, double ty)
	{
		transform(AffineTransform.getTranslateInstance(tx, ty));
	}





	/**
	 * Concatenates the current EpsGraphics2D Transform with a rotation transform.
	 */
	public void rotate(double theta)
	{
		rotate(theta, 0, 0);
	}





	/**
	 * Concatenates the current EpsGraphics2D Transform with a translated rotation transform.
	 * @since 0.1
	 */
	public void rotate(double theta, double x, double y)
	{
		transform(AffineTransform.getRotateInstance(theta, x, y));
	}





	/**
	 * Concatenates the current EpsGraphics2D Transform with a scaling transformation.
	 * @since 0.1
	 */
	public void scale(double sx, double sy)
	{
		transform(AffineTransform.getScaleInstance(sx, sy));
	}





	/**
	 * Concatenates the current EpsGraphics2D Transform with a shearing  transform.
	 * @since 0.1
	 */
	public void shear(double shx, double shy)
	{
		transform(AffineTransform.getShearInstance(shx, shy));
	}





	/**
	 * Composes an AffineTransform object with the Transform in this
	 * EpsGraphics2D according to the rule last-specified-first-applied.
	 * @since 0.1
	 */
	public void transform(AffineTransform Tx)
	{
		_transform.concatenate(Tx);
		setTransform(getTransform());
	}





	/**
	 * Sets the AffineTransform to be used by this EpsGraphics2D.
	 * @since 0.1
	 */
	public void setTransform(AffineTransform Tx)
	{
		if(Tx == null)
			_transform = new AffineTransform();
		else
			_transform = new AffineTransform(Tx);
		
		// Need to update the stroke and font so they know the scale changed
		setStroke(getStroke());
		setFont(getFont());
	}





	/**
	 * Gets the AffineTransform used by this EpsGraphics2D.
	 * @since 0.1
	 */
	public AffineTransform getTransform()
	{
		return new AffineTransform(_transform);
	}





	/**
	 * Returns the current Paint of the EpsGraphics2D object.
	 * @since 0.1
	 */
	public Paint getPaint()
	{
		return _paint;
	}





	/**
	 * returns the current Composite of the EpsGraphics2D object.
	 * @since 0.1
	 */
	public Composite getComposite()
	{
		return _composite;
	}





	/**
	 * Sets the background colour to be used by the clearRect method.
	 * @since 0.1
	 */
	public void setBackground(Color color)
	{
		if(color == null)
			color = Color.black;
		
		_backgroundColor = color;
	}





	/**
	 * Gets the background colour that is used by the clearRect method.
	 * @since 0.1
	 */
	public Color getBackground()
	{
		return _backgroundColor;
	}





	/**
	 * Returns the Stroke currently used. Guaranteed to be an instance of BasicStroke.
	 * @since 0.1
	 */
	public Stroke getStroke()
	{
		return _stroke;
	}





	/**
	 * Intersects the current clip with the interior of the specified Shape and
	 * sets the clip to the resulting intersection.
	 * @since 0.1
	 */
	public void clip(Shape s)
	{
		if(_clip == null)
			setClip(s);
		else
		{
			Area area = new Area(_clip);
			area.intersect(new Area(s));
			setClip(area);
		}
	}





	/**
	 * Returns the FontRenderContext.
	 * @since 0.1
	 */
	public FontRenderContext getFontRenderContext()
	{
		return _fontRenderContext;
	}





	/////////////// Graphics methods ///////////////////////

	/**
	 * Returns a new Graphics object that is identical to this EpsGraphics2D.
	 * @since 0.1
	 */
	public Graphics create()
	{
		return new EpsGraphics2D(this);
	}





	/**
	 * Returns an EpsGraphics2D object based on this Graphics object, but with a
	 * new translation and clip area.
	 * @since 0.1
	 */
	public Graphics create(int x, int y, int width, int height)
	{
		Graphics g = create();
		g.translate(x, y);
		g.clipRect(0, 0, width, height);
		
		return g;
	}





	/**
	 * Returns the current Color. This will be a default value (black) until it
	 * is changed using the setColor method.
	 * @since 0.1
	 */
	public Color getColor()
	{
		return _color;
	}





	/**
	 * Sets the Color to be used when drawing all future shapes, text, etc.
	 * @since 0.1
	 */
	public void setColor(Color c)
	{
		if(c == null)
			c = Color.black;

		_color = c;

		if(getColorDepth() == BLACK_AND_WHITE)
		{
			float value = 0;
			
			if(c.getRed() + c.getGreen() + c.getBlue() > 255 * 1.5 - 1)
				value = 1;

			append(value + " setgray");
		}
		else
			if(getColorDepth() == GRAYSCALE)
			{
				float value = ((c.getRed() + c.getGreen() + c.getBlue()) / (3 * 255f));
				append(value + " setgray");
			}
			else
				append((c.getRed() / 255f) + " " + (c.getGreen() / 255f) + " " + (c.getBlue() / 255f) + " setrgbcolor");
	}





	/**
	 * Sets the paint mode of this EpsGraphics2D object to overwrite the
	 * destination EpsDocument with the current colour.
	 * @since 0.1
	 */
	public void setPaintMode()
	{
		// Do nothing - paint mode is the only method supported anyway.
	}





	/**
	 * <b><i><font color="red">Not implemented</font></i></b> - performs no action.
	 * @since 0.1
	 */
	public void setXORMode(Color c1)
	{
		methodNotSupported();
	}





	/**
	 * Returns the Font currently being used.
	 * @since 0.1
	 */
	public Font getFont()
	{
		return _font;
	}





	/**
	 * Sets the Font to be used in future text.
	 * @since 0.1
	 */
	public void setFont(Font font)
	{
		if(font == null)
			font = Font.decode(null);
		
		_font = font;
		
		if(!getAccurateTextMode())
			append("/" + _font.getPSName() + " findfont " + _font.getSize() + " scalefont setfont");
	}





	/**
	 * Gets the font metrics of the current font.
	 * @since 0.1
	 */
	public FontMetrics getFontMetrics()
	{
		return getFontMetrics(getFont());
	}





	/**
	 * Gets the font metrics for the specified font.
	 * @since 0.1
	 */
	public FontMetrics getFontMetrics(Font f)
	{
		BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		
		return g.getFontMetrics(f);
	}





	/**
	 * Returns the bounding rectangle of the current clipping area.
	 * @since 0.1
	 */
	public Rectangle getClipBounds()
	{
		if(_clip == null)
			return null;

		return getClip().getBounds();
	}





	/**
	 * Intersects the current clip with the specified rectangle.
	 * @since 0.1
	 */
	public void clipRect(int x, int y, int width, int height)
	{
		clip(new Rectangle(x, y, width, height));
	}





	/**
	 * Sets the current clip to the rectangle specified by the given coordinates.
	 * @since 0.1
	 */
	public void setClip(int x, int y, int width, int height)
	{
		setClip(new Rectangle(x, y, width, height));
	}





	/**
	 * Gets the current clipping area.
	 * @since 0.1
	 */
	public Shape getClip()
	{
		if(_clip == null)
			return null;
		
		try
		{
			AffineTransform t = _transform.createInverse();
			t.concatenate(_clipTransform);
			
			return t.createTransformedShape(_clip);
			
		}catch(Exception e)
		{
			throw new EpsException("Unable to get inverse of matrix: " + _transform);
		}
	}





	/**
	 * Sets the current clipping area to an arbitrary clip shape.
	 * @since 0.1
	 */
	public void setClip(Shape clip)
	{
		if(clip != null)
		{
			if(_document.isClipSet())
			{
				append("grestore");
				append("gsave");
			}
			else
			{
				_document.setClipSet(true);
				append("gsave");
			}
			
			draw(clip, "clip");
			_clip = clip;
			_clipTransform = (AffineTransform)_transform.clone();
		}
		else
		{
			if(_document.isClipSet())
			{
				append("grestore");
				_document.setClipSet(false);
			}
			
			_clip = null;
		}
	}





	/**
	 * <b><i><font color="red">Not implemented</font></i></b> - performs no action.
	 * @since 0.1
	 */
	public void copyArea(int x, int y, int width, int height, int dx, int dy)
	{
		methodNotSupported();
	}





	/**
	 * Draws a straight line from (x1,y1) to (x2,y2).
	 * @since 0.1
	 */
	public void drawLine(int x1, int y1, int x2, int y2)
	{
		Shape shape = new Line2D.Float(x1, y1, x2, y2);
		draw(shape);
	}





	/**
	 * Fills a rectangle with top-left corner placed at (x,y).
	 * @since 0.1
	 */
	public void fillRect(int x, int y, int width, int height)
	{
		Shape shape = new Rectangle(x, y, width, height);
		draw(shape, "fill");
	}





	/**
	 * Draws a rectangle with top-left corner placed at (x,y).
	 * @since 0.1
	 */
	public void drawRect(int x, int y, int width, int height)
	{
		Shape shape = new Rectangle(x, y, width, height);
		draw(shape);
	}





	/**
	 * Clears a rectangle with top-left corner placed at (x,y) using the current background color.
	 * @since 0.1
	 */
	public void clearRect(int x, int y, int width, int height)
	{
		Color originalColor = getColor();

		setColor(getBackground());
		Shape shape = new Rectangle(x, y, width, height);
		draw(shape, "fill");

		setColor(originalColor);
	}





	/**
	 * Draws a rounded rectangle.
	 * @since 0.1
	 */
	public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
	{
		Shape shape = new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight);
		draw(shape);
	}





	/**
	 * Fills a rounded rectangle.
	 * @since 0.1
	 */
	public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight)
	{
		Shape shape = new RoundRectangle2D.Float(x, y, width, height, arcWidth, arcHeight);
		draw(shape, "fill");
	}





	/**
	 * Draws an oval.
	 * @since 0.1
	 */
	public void drawOval(int x, int y, int width, int height)
	{
		Shape shape = new Ellipse2D.Float(x, y, width, height);
		draw(shape);
	}





	/**
	 * Fills an oval.
	 * @since 0.1
	 */
	public void fillOval(int x, int y, int width, int height)
	{
		Shape shape = new Ellipse2D.Float(x, y, width, height);
		draw(shape, "fill");
	}





	/**
	 * Draws an arc.
	 * @since 0.1
	 */
	public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle)
	{
		Shape shape = new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN);
		draw(shape);
	}





	/**
	 * Fills an arc.
	 * @since 0.1
	 */
	public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle)
	{
		Shape shape = new Arc2D.Float(x, y, width, height, startAngle, arcAngle, Arc2D.PIE);
		draw(shape, "fill");
	}





	/**
	 * Draws a polyline.
	 * @since 0.1
	 */
	public void drawPolyline(int[] xPoints, int[] yPoints, int nPoints)
	{
		if(nPoints > 0)
		{
			GeneralPath path = new GeneralPath();
			path.moveTo(xPoints[0], yPoints[0]);
			
			for(int i = 1; i < nPoints; i++)
				path.lineTo(xPoints[i], yPoints[i]);
			
			draw(path);
		}
	}





	/**
	 * Draws a polygon made with the specified points.
	 * @since 0.1
	 */
	public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints)
	{
		Shape shape = new Polygon(xPoints, yPoints, nPoints);
		draw(shape);
	}





	/**
	 * Draws a polygon.
	 * @since 0.1
	 */
	public void drawPolygon(Polygon p)
	{
		draw(p);
	}





	/**
	 * Fills a polygon made with the specified points.
	 * @since 0.1
	 */
	public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints)
	{
		Shape shape = new Polygon(xPoints, yPoints, nPoints);
		draw(shape, "fill");
	}





	/**
	 * Fills a polygon.
	 * @since 0.1
	 */
	public void fillPolygon(Polygon p)
	{
		draw(p, "fill");
	}





	/**
	 * Draws the specified characters, starting from (x,y).
	 * @since 0.1
	 */
	public void drawChars(char[] data, int offset, int length, int x, int y)
	{
		String string = new String(data, offset, length);
		drawString(string, x, y);
	}





	/**
	 * Draws the specified bytes, starting from (x,y).
	 * @since 0.1
	 */
	public void drawBytes(byte[] data, int offset, int length, int x, int y)
	{
		String string = new String(data, offset, length);
		drawString(string, x, y);
	}





	/**
	 * Draws an image.
	 * @since 0.1
	 */
	public boolean drawImage(Image img, int x, int y, ImageObserver observer)
	{
		return drawImage(img, x, y, Color.white, observer);
	}





	/**
	 * Draws an image.
	 * @since 0.1
	 */
	public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer)
	{
		return drawImage(img, x, y, width, height, Color.white, observer);
	}





	/**
	 * Draws an image.
	 * @since 0.1
	 */
	public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer)
	{
		int width = img.getWidth(null);
		int height = img.getHeight(null);
		return drawImage(img, x, y, width, height, bgcolor, observer);
	}





	/**
	 * Draws an image.
	 * @since 0.1
	 */
	public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer)
	{
		return drawImage(img, x, y, x + width, y + height, 0, 0, width, height, bgcolor, observer);
	}





	/**
	 * Draws an image.
	 * @since 0.1
	 */
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, ImageObserver observer)
	{
		return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, Color.white, observer);
	}





	/**
	 * Draws an image.
	 * @since 0.1
	 */
	public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2, 
								Color bgcolor, ImageObserver observer)
	{
		if(dx1 >= dx2)
			throw new IllegalArgumentException("dx1 >= dx2");

		if(sx1 >= sx2)
			throw new IllegalArgumentException("sx1 >= sx2");

		if(dy1 >= dy2)
			throw new IllegalArgumentException("dy1 >= dy2");

		if(sy1 >= sy2)
			throw new IllegalArgumentException("sy1 >= sy2");

		append("gsave");

		int width = sx2 - sx1;
		int height = sy2 - sy1;
		int destWidth = dx2 - dx1;
		int destHeight = dy2 - dy1;

		int[] pixels = new int[width * height];
		PixelGrabber pg = new PixelGrabber(img, sx1, sy1, sx2 - sx1, sy2 - sy1, pixels, 0, width);
	
		try
		{
			pg.grabPixels();
		}catch(InterruptedException e)
		{
			return false;
		}

		AffineTransform matrix = new AffineTransform(_transform);
		matrix.translate(dx1, dy1);
		matrix.scale(destWidth / (double)width, destHeight / (double)height);
		double[] m = new double[6];
		
		try
		{
			matrix = matrix.createInverse();
		}catch(Exception e)
		{
			throw new EpsException("Unable to get inverse of matrix: " + matrix);
		}
		
		matrix.scale(1, -1);
		matrix.getMatrix(m);
		String bitsPerSample = "8";
		// Not using proper imagemask function yet
		// if (getColorDepth() == BLACK_AND_WHITE) {
		// bitsPerSample = "true";
		// }
		append(width + " " + height + " " + bitsPerSample + " [" + m[0] + " " + m[1] + " " + m[2] + " " + m[3] + " " + m[4] + " " + m[5] + "]");
		// Fill the background to update the bounding box.
		Color oldColor = getColor();
		setColor(getBackground());
		fillRect(dx1, dy1, destWidth, destHeight);
		setColor(oldColor);

		if(getColorDepth() == BLACK_AND_WHITE)
		{
			// Should really use imagemask.
			append("{currentfile " + width + " string readhexstring pop} bind");
			append("image");
		}
		else
			if(getColorDepth() == GRAYSCALE)
			{
				append("{currentfile " + width + " string readhexstring pop} bind");
				append("image");
			}
			else
			{
				append("{currentfile 3 " + width + " mul string readhexstring pop} bind");
				append("false 3 colorimage");
			}

		System.err.println(getColorDepth());

		StringBuffer line = new StringBuffer();
		
		for(int y = 0; y < height; y++)
		{
			for(int x = 0; x < width; x++)
			{
				Color color = new Color(pixels[x + width * y]);

				if(getColorDepth() == BLACK_AND_WHITE)
					if(color.getRed() + color.getGreen() + color.getBlue() > 255 * 1.5 - 1)
						line.append("ff");
					else
						line.append("00");
				else
					if(getColorDepth() == GRAYSCALE)
						line.append(toHexString((color.getRed() + color.getGreen() + color.getBlue()) / 3));
					else
						line.append(toHexString(color.getRed()) + toHexString(color.getGreen()) + toHexString(color.getBlue()));

				if(line.length() > 64)
				{
					append(line.toString());
					line = new StringBuffer();
				}
			}
		}
		
		if(line.length() > 0)
			append(line.toString());

		append("grestore");

		return true;
	}





	/**
	 * Disposes of all resources used by this EpsGraphics2D object. If this is
	 * the only remaining EpsGraphics2D instance pointing at a EpsDocument
	 * object, then the EpsDocument object shall become eligible for garbage collection.
	 * @since 0.1
	 */
	public void dispose()
	{
		_document = null;
	}





	/**
	 * Finalises the object.
	 * @since 0.1
	 */
	public void finish()
	{
		super.finalize();
	}





	/**
	 * Returns the entire contents of the EPS document, complete with headers
	 * and bounding box. The returned String is suitable for being written
	 * directly to disk as an EPS file.
	 * @since 0.1
	 */
	public String toString()
	{
		StringWriter writer = new StringWriter();
		
		try
		{
			_document.write(writer);
			_document.flush();
			_document.close();
		}catch(IOException e)
		{
			throw new EpsException(e.toString());
		}
		
		return writer.toString();
	}





	/**
	 * Returns true if the specified rectangular area might intersect the current clipping area.
	 * @since 0.1
	 */
	public boolean hitClip(int x, int y, int width, int height)
	{
		if(_clip == null)
			return true;

		return hit(new Rectangle(x, y, width, height), _clip, true);
	}





	/**
	 * Returns the bounding rectangle of the current clipping area.
	 * @since 0.1
	 */
	public Rectangle getClipBounds(Rectangle r)
	{
		if(_clip == null)
			return r;

		Rectangle rect = getClipBounds();
		r.setLocation((int)rect.getX(), (int)rect.getY());
		r.setSize((int)rect.getWidth(), (int)rect.getHeight());
		
		return r;
	}
}
