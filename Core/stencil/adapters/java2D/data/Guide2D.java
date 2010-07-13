package stencil.adapters.java2D.data;

import stencil.display.DisplayGuide;
import stencil.parser.tree.Guide;

/**Conforming to this interface is required for guides used as automatic generation targets.
 * Additionally, if a custom default specializer is desired, a public static field DEFAULT_ARGUMENTS
 * should be defined (otherwise, a zero-argument list will be used).
 */
public abstract class Guide2D implements DisplayGuide, Renderable {
	protected final String attribute;
	protected Guide2D(Guide def) {
		attribute = def.getSelector().toStringTree();
	}
	
	/**What is this guide for?*/
	public String getAttribute() {return attribute;}
}
