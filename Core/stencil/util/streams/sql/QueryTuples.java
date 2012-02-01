package stencil.util.streams.sql;

import java.sql.*;

import stencil.interpreter.tree.Specializer;
import stencil.module.util.ann.Description;
import stencil.module.util.ann.Stream;
import stencil.tuple.SourcedTuple;
import stencil.tuple.instances.ArrayTuple;
import stencil.tuple.prototype.TuplePrototype;
import stencil.tuple.stream.TupleStream;
import stencil.types.Converter;

/**Converts a query and connect string to a stream of tuples.
 * Connection will always be verified as having the correct number of columns,
 * but no other meta-data validation is performed.
 */

@Description("Database query results, in tuple form.")
@Stream(name="SQL", spec="[query: \"\", connect:\"\", driver:\"\", queue:50]")
public class QueryTuples implements TupleStream {
	public static final String QUERY = "query";
	public static final String DRIVER = "driver";
	public static final String CONNECT = "connect";
	
	protected Connection connection;
	protected Statement statement;
	

	protected final String name;
	protected final String query;
	protected final int columnCount;
	protected ResultSet results;

	public QueryTuples(String name, TuplePrototype proto, Specializer spec) throws Exception {
		this(name, 
			proto.size(),
			Converter.toString(spec.get(QUERY)),
			Converter.toString(spec.get(CONNECT)),
			Converter.toString(spec.get(DRIVER)));
	}

	public QueryTuples(String name, int columnCount, String driver, String connect, String query) throws Exception {
		this.name = name;
		this.columnCount = columnCount;

		connection = DriverManager.connect(driver, connect);
		statement = connection.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
		this.query = query;

		reset();

		if (results.getMetaData().getColumnCount() != columnCount) {throw new RuntimeException("Query does not return the quantity of columns specified.");}
	}

	/**Closes connection.
	 * Connection cannot be reset after this, it must be recreated.
	 **/
	public void stop() {
		try {
			results.close();
			statement.close();
			connection.close();
		} catch (Exception e) {}
	}

	public SourcedTuple next() {
		Object[] values = new Object[columnCount];

		try {results.next();}
		catch (Exception e) {throw new RuntimeException("Error advancing to next row.", e);}

		for (int i=1; i<= columnCount; i++) {
			try {values[i-1] = results.getString(i);}//r
			catch (Exception e) {throw new RuntimeException(String.format("Error retrieving value %1$d for tuples.", i),e);}
		}
		return new SourcedTuple.Wrapper(name, new ArrayTuple(values));
	}

	public void reset() throws Exception {
		results = statement.executeQuery(query);
	}

	public boolean hasNext() {
		try {return !results.isLast();}
		catch (Exception e) {throw new RuntimeException(e);}
	}


	public void remove() {throw new UnsupportedOperationException("Remove not supported on Query TupleStream.");}
}
