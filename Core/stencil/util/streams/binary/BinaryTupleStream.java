package stencil.util.streams.binary;

import stencil.explore.model.Model;
import stencil.explore.model.sources.StreamSource;
import stencil.explore.util.StencilIO;
import stencil.tuple.SourcedTuple;
import stencil.tuple.Tuple;
import stencil.tuple.instances.ArrayTuple;
import stencil.tuple.stream.TupleStream;
import stencil.types.Converter;
import stencil.util.streams.QueuedStream;

import java.nio.*;
import java.nio.channels.*;
import java.io.*;

/**Takes stream source and constructs a file caching the stream values as binary.
 * Loading format is thus:
 *  
 *  File Prefix---
 *  int: # of fields per line (n)
 *  Char+: Field encoding marker; i/d/s for int,double, string; one per field 
 *  
 *  Entries ---
 *  value   -OR- int data
 *      If it is a fixed-lenght value, the value itself is present (the types reveal this case)
 *      For variable length data, the int indicates how many MORE bytes to read for the field 
 *  
 *  Since the header encodes how many fields there are per tuple, that many points are read and constructed into a tuple.
 **/
public class BinaryTupleStream {
	public static final int INT_BYTES = 4;
	public static final int DOUBLE_BYTES = 8;

	/**Means of producing a file that can be read by the Reader**/
	public static final class Writer {
		private final TupleStream source;
		
		public Writer(TupleStream source) {
			this.source = source;
		}
		
		public static byte[] makeHeader(char[] types) {
			assert types != null;
			assert types.length != 0;
			for (char c: types) {
				if (c != 's' && c== 'i' && c == 'd') {throw new IllegalArgumentException("Invalid type marker; only i,s,d allowed, found  '" + c + "'");}
			}
						
			byte[] size = intBytes(types.length);
			byte[] encoding = charBytes(types);	
			byte[] rslt = new byte[encoding.length+size.length] ;
			
			System.arraycopy(size, 0, rslt, 0, size.length);
			System.arraycopy(encoding, 0, rslt, size.length, encoding.length);
			return rslt;
		}
		
		private static byte[] charBytes(char... c) {return String.valueOf(c).getBytes();}
		private static byte[] intBytes(int i ){return ByteBuffer.allocate(INT_BYTES).putInt(i).array();}
		private static byte[] doubleBytes(double d) {return ByteBuffer.allocate(DOUBLE_BYTES).putDouble(d).array();}
		
		
		/**Get a byte array of a single data value
		 * **/
		public static byte[] asBinary(Object value, char type) {
			byte[] bytes;
			
			switch (type) {
				case 's' :
					String v = Converter.toString(value);
					bytes = charBytes(v.toCharArray());
					ByteBuffer buff = ByteBuffer.allocate(INT_BYTES + bytes.length);
					buff.put(intBytes(bytes.length));
					buff.put(bytes);
					return buff.array();
				case 'i' :
					return intBytes(Converter.toInteger(value));
				case 'd' :
					return doubleBytes(Converter.toDouble(value));
				default: throw new IllegalArgumentException("Unknown type: " + type);
			}			
		}
		
		public static byte[] asBinary(Tuple t, char[] types) {
			byte[][] entries= new byte[t.size()][];
			int total=0;
			for (int i=0;i<t.size();i++) {
				entries[i] = asBinary(t.get(i), types[i]);
				total += entries[i].length;
			}
			
			int offset=0;
			byte[] full = new byte[total];
			for (byte[] entry: entries) { 
				System.arraycopy(entry, 0, full, offset, entry.length);
				offset += entry.length;
			}
			return full;
		}

		/**Write all of the tuples in the stream to the file.
		 * @return the number of tuples written
		 * **/
		public void writeStream(String filename, char[] types) throws Exception {
			FileOutputStream file = new FileOutputStream(filename);
			try {
				byte[] header = makeHeader(types); 
				file.write(header);

				while(source.hasNext()) {
					SourcedTuple sourced = source.next();
					if (sourced == null) {continue;}
					Tuple t = sourced.getValues();
					for (int i=0;i<t.size();i++) {
						byte[] entry = asBinary(t.get(i), types[i]);
						file.write(entry);						
					}
				}
			} finally {file.close();}
		}
	}
	
	
	/**Stream source that can read a FastStream written by the above included Writer**/
	public static final class Reader implements TupleStream, QueuedStream.Queable {
		/**File channel contents are loaded from**/
		private final ByteChannel input;
		private final ByteBuffer mainBuffer;
		
		/**Name of the stream**/
		private final String name;
		
		/**Number of value fields per tuple**/
		private final int tupleSize;
		private final char[] types;
		
		public Reader(String streamName, String sourcefile) throws Exception {
			FileChannel input = new FileInputStream(sourcefile).getChannel();
			this.input = input;
			long size = input.size();
			mainBuffer = input.map(FileChannel.MapMode.READ_ONLY, 0, size);
			
			this.name = streamName;
			tupleSize = mainBuffer.getInt();
			types = new char[tupleSize];
			
			for (int i=0; i<tupleSize; i++) {
				types[i] = (char) mainBuffer.get();
			}
		}

		
		@Override
		public boolean hasNext() {
			return input != null
					&& input.isOpen()
					&& mainBuffer.hasRemaining();
		}

		
		@Override
		public SourcedTuple next() {
			try {
				Object[] values = new Object[tupleSize];
				for (int i=0; i<tupleSize; i++) {
					switch (types[i]) {
						case 'i' :
							values[i] = mainBuffer.getInt();
							break;
						case 'd' :
							values[i] = mainBuffer.getDouble();
							break;
						case 's' : 
							int size = mainBuffer.getInt();
							byte[] binary = new byte[size];
							mainBuffer.get(binary);
							values[i] = new String(binary);
							break;
						default: throw new IllegalArgumentException("Could not unpack item of type '" + types[i] + "'");
					}					
				}
				return new SourcedTuple.Wrapper(name, new ArrayTuple(values));
			} catch (Exception e) {
				throw new RuntimeException("Error reading line from file.", e);
			}
		}

		public void close() throws Exception {input.close();}
		@Override
		public void remove() {throw new UnsupportedOperationException();}
	
	}
	
	/**Intentionally left empty, use Reader or Writer instances instead.**/
	private BinaryTupleStream() {}
	
	
	/**Indicate a stencil an a stream, will load up the stream
	 * and create a new file of it that can be loaded as a FastStream.
	 * 
	 * The stream indicated must be finite
	 * TODO: Disentangle from the Explore application Model class
	 * @param args: stencil file, stream to load, file to save in
	 */
	public static void main(String[] args) throws Exception {
		String stencilFile = args[0];
		String targetStream = args[1];
		String targetFile = args[2];
		String types = args[3];

		Model model = new Model();
		StencilIO.load(stencilFile, model);
		StreamSource ss = model.getSourcesMap().get(targetStream);
		TupleStream stream = ss.getStream(model);
		
		Writer w = new Writer(stream);
		w.writeStream(targetFile, types.toCharArray());
	}
}
