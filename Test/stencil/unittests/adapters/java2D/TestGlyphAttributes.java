package stencil.unittests.adapters.java2D;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import stencil.adapters.java2D.data.Glyph2D;
import stencil.adapters.java2D.data.glyphs.*;
import stencil.tuple.prototype.TuplePrototypes;
import stencil.unittests.StencilTestCase;

//TODO: Add tests for failure cases
public class TestGlyphAttributes extends StencilTestCase {
	private void testAttributes(Glyph2D node, String...ignores) throws Exception {
		List<String> ignore = new ArrayList(Arrays.asList(ignores));
		ignore.add("ID");
		ignore.add("IMPLANTATION");
		
		for(String att : TuplePrototypes.getNames(node.getPrototype())) {
			if (ignore.contains(att)) {continue;} 
			Object value = node.get(att);
			assertTrue(String.format("Default value not returned for %1$s when expected (got %2$s)", att, value), node.isDefault(att, value));
		}
		
		for (String att: TuplePrototypes.getNames(node.getPrototype())) {
			assertTrue(String.format("Expected to find %1$s in glyph of type %2$s", att, node.get("IMPLANTATION")), node.getPrototype().contains(att));
		}
	}

	public void testText() throws Exception {
		testAttributes(new Text(null, "MyID"));
	}
	
	public void testShape() throws Exception {
		testAttributes(new Shape(null, "MyID"));
	}

	public void testImage() throws Exception {
		testAttributes(new Image(null, "MyID"), "HEIGHT", "WIDTH");
	}

	public void testLine() throws Exception {
		testAttributes(new Line(null, "MyID"), "X", "Y", "HEIGHT", "WIDTH");
	}
	
	public void testPolyLine() throws Exception {
		testAttributes(new Poly.PolyLine(null, "MyID"), "X", "Y", "XS", "YS");
	}
	
	public void testPoly() throws Exception {
		testAttributes(new Poly.Polygon(null, "MyID"), "X", "Y", "XS", "YS");
	}
	
	public void testPie() throws Exception {
		testAttributes(new Pie(null, "MyID"));
	}

}
