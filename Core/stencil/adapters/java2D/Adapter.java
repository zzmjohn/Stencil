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
package stencil.adapters.java2D;

import java.awt.Color;
import stencil.adapters.java2D.data.Glyph2D;
import stencil.display.StencilPanel;
import stencil.parser.string.ParseStencil;
import stencil.parser.tree.Layer;
import stencil.parser.tree.Program;
import stencil.adapters.java2D.data.DisplayLayer;
import stencil.adapters.java2D.data.glyphs.Basic;
import stencil.adapters.java2D.util.ZoomPanHandler;

public final class Adapter implements stencil.adapters.Adapter<Glyph2D> {
	public static final Adapter INSTANCE = new Adapter();
	
	private boolean defaultMouse;
	
	public Panel generate(Program program) {
		Panel panel = new Panel(program);
		
		if (defaultMouse) {
			ZoomPanHandler zp = new ZoomPanHandler();
			panel.addMouseListener(zp);
			panel.addMouseMotionListener(zp);
		}
		return panel;
	}

	public Class getGuideClass(String name) {
		throw new UnsupportedOperationException("Not implemented");
	}

	public DisplayLayer makeLayer(Layer l) {
		return DisplayLayer.instance(l);
	}

	public void setDefaultMouse(boolean m) {this.defaultMouse = m;}
	
	public void setDebugColor(Color c) {
		Basic.DEBUG_COLOR = c;
	}

	public void setRenderQuality(String value) throws IllegalArgumentException {
		throw new UnsupportedOperationException("Not implemented");
	}

	public void finalize(StencilPanel panel) {/**No finalization required...yet**/}
	
	public Panel compile(String programSource) throws Exception {
		return generate(ParseStencil.parse(programSource, this));
	}
}
