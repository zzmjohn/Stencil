package stencil.modules.jung;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.util.IterativeContext;
import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import stencil.module.operator.StencilOperator;
import stencil.module.operator.util.BasicProject;
import stencil.module.util.OperatorData;
import stencil.parser.tree.Specializer;
import stencil.types.Converter;


/**Utility class for building JUNG graph operators.  This covers 
 * graph creation/storage, layout storage and position queries.
 */
public abstract class GraphOperator extends BasicProject implements StencilOperator {
	protected final DirectedGraph graph = new DirectedSparseGraph();
	protected Layout<Object, Object> layout;	
	
	protected GraphOperator(OperatorData opData) {super(opData);}

	/**Recalculate the layout.
	 * This method should call 'setLayout'.
	 */
	protected abstract void resetLayout();
	
	protected void setLayout(Layout layout) {this.layout = layout;}
	
	
	public boolean add(Object... values) {
		extendGraph(values);
		return true;	
	}
	
	public Point2D map(Object... values) {
		extendGraph(values);
		return query(values);
	}
	
	/**Add an entity. 
	 * Three arguments: Start, End, ID
	 * Two arguments: Start, End (synthesizes ID based on Start and End)
	 * 
	 * @return Always true...
	 */
	protected void extendGraph(Object... values) {
		Object start = values[0];
		Object end = values[1];

		if (start.equals(end)) {
        	graph.addVertex(values[0]);
        } else {
			Object id;
			if (values.length == 2) {id = String.format("%1$s <-> %2$s", start, end);}
			else {id = values[2];}
			
			graph.addEdge(id, start,end, EdgeType.DIRECTED);
        }
		stateID++;
		layout = null;
	}
	
	public Point2D query(Object id) {
		if (layout == null) {resetLayout();}
		return layout.transform(id);
	}
	
	
	/**Utility class for graph operators covering layouts that use a size factor.
	 * @author jcottam
	 *
	 */
	public static abstract class SizedOperator extends GraphOperator {
		private static final String WIDTH_KEY = "width";
		private static final String HEIGHT_KEY = "height";	
		protected final Dimension size;
		
		protected SizedOperator(OperatorData opData, Specializer spec) {
			super(opData);
			
			int width = Converter.toInteger(spec.get(WIDTH_KEY));
			int height = Converter.toInteger(spec.get(HEIGHT_KEY));
			size = new Dimension(width, height);
		}
	}
	
	
	/**Utility class for layout operators that use step-wise refinement.
	 * Supports retrieval of max-iterations, but may ignore it (depending on the layout's definition of 'done')
	 * 
	 * @author jcottam
	 *
	 */
	public static abstract class StepOperator extends SizedOperator {
		private static final String STEPS_KEY = "steps";
		private IterativeContext layout;
		protected final int maxIterations;
		
		protected StepOperator(OperatorData opData, Specializer spec) {
			super(opData, spec);
			maxIterations = Converter.toInteger(spec.get(STEPS_KEY));
		}
		
		public void setLayout(Layout layout) {
			this.layout = (IterativeContext) layout;
			
			super.setLayout((Layout) layout);
		}
		
		public Point2D query(String id) {
			if (super.layout == null) {resetLayout();}
			if (!layout.done()) {
				layout.step();
				stateID++;
			}
			return super.query(id);
		}
	}

}