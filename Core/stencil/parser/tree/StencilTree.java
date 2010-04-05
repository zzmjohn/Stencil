/* Copyright (c) 2006-2008 Indiana University Research and Technology Corporation.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice, this
 *  list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright notice,
 *  this list of conditions and the following disclaimer in the documentation
 *  and/or other materials provided with the distribution.
 *
 * - Neither the Indiana University nor the names of its contributors may be used
 *  to endorse or promote products derived from this software without specific
 *  prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package stencil.parser.tree;

import java.lang.reflect.*;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.CommonTree;

import stencil.parser.string.StencilParser;


public class StencilTree extends CommonTree {
	public StencilTree(Token token) {super(token);}

	/**Gets the string name of the type given.
	 * Note: This uses a relatively slow method for lookup.
	 * @param type Type to be investigated
	 * @return The name associated with the type integer
	 */
	protected static String typeName(int type) {
		return StencilParser.tokenNames[type];
	}
	
	protected CommonTree findChild(int type, String content) {
		for (int i=0; children != null && i < children.size(); i++) {
			CommonTree t = (CommonTree) children.get(i);
			if (t.getType() == type 
				&& (content == null || content.equals(t.getText()))) {
				return t;
			}
		}
		return null;
	}
	
	public static boolean verifyType(Tree tree, int type) {return tree.getType() == type;}
	
	public Tree dupNode() {
		try {
			Constructor c = this.getClass().getConstructor(Token.class);
			return (Tree) c.newInstance(this.getToken());
		} catch (Exception e) {
			throw new Error(String.format("Error reflectively duplicating node to node of same type (%1$s).", this.getClass().getName()), e);
		}
	}

	public StencilTree getParent() {return (StencilTree) super.getParent();}
}
