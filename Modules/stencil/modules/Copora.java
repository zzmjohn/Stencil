package stencil.modules;

import java.util.Arrays;

import stencil.module.operator.util.AbstractOperator;
import stencil.module.util.BasicModule;
import stencil.module.util.ModuleData;
import stencil.module.util.OperatorData;

public class Copora extends BasicModule {
	/**A list of stop words for text analysis.
	 * List taken from http://www.textfixer.com/resources/common-english-words.txt.
	 * 
	 * */
	public static class StopWords extends AbstractOperator {
		String[] words = new String[]{"a","able","about","across","after","all","almost","also","am","among","an","and","any","are","as","at","be","because","been","but","by","can","cannot","could","dear","did","do","does","either","else","ever","every","for","from","get","got","had","has","have","he","her","hers","him","his","how","however","i","if","in","into","is","it","its","just","least","let","like","likely","may","me","might","most","must","my","neither","no","nor","not","of","off","often","on","only","or","other","our","own","rather","said","say","says","she","should","since","so","some","than","that","the","their","them","then","there","these","they","this","tis","to","too","twas","us","wants","was","we","were","what","when","where","which","while","who","whom","why","will","with","would","yet","you","your"};
		
		public StopWords(OperatorData opData) {super(opData);}		
		public boolean query(String word) {return (Arrays.binarySearch(words, word.toLowerCase()) >= 0);} 		
	}


	public Copora(ModuleData md) {super(md);}
}