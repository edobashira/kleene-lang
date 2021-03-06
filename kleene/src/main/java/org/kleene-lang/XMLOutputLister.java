
//	XMLOutputLister.java
//
//	The Kleene Programming Language

//   Copyright 2006-2012 SAP AG

//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

//   Author: ken.beesley@sap.com (Kenneth R. Beesley)

// Class that knows how to output Strings, via BufferedWriter, found in
// an Fst.  The challenge is that the Fst is in the C++ universe and is
// being searched by a native (C++) function.  That native function will
// be passed an object of type XMLOutputLister and will call back to
// methods of this object.
//
// This may seem roundabout, but it lets Java handle the Unicode I/O.
// And the symmap (needed to map ints to String) is on the Java side.

import java.util.Stack ;
import java.util.Iterator ;
import java.io.BufferedWriter ; 

//import org.apache.commons.lang.StringEscapeUtils ;

public class XMLOutputLister implements StringLister {

	private SymMap symmap ;
	private Stack<Integer> intStack ;
	private BufferedWriter bwriter ;
	String outputElmtName ;
	String weightAttrName ;

	// Constructor
	public XMLOutputLister(SymMap sm, BufferedWriter bw, String oen, String wan) {
		symmap = sm ;
		bwriter = bw ;
		intStack = new Stack<Integer>() ;
		outputElmtName = oen ;
		weightAttrName = wan ;
	}

	// callback functions, called by the native C++ function finding 
	// strings in the Fst
	// output strings are built up integer by integer (codepoint by 
	// codepoint); the arcs
	// on the Fst just have integer labels
	
	public void push(int i) {
		intStack.push(new Integer(i)) ;
	}

	public void pop() {
		intStack.pop() ;
	}

	public void emit(float w) {
		// get String from intStack (basically a list of label integers)
		StringBuilder sb = new StringBuilder() ;
		// iterate through the integers, convert to StringBuffer (UTF-16)
		for (Iterator<Integer> iter = intStack.iterator(); iter.hasNext() ; ) {
			sb.append(symmap.getsym(iter.next().intValue())) ;
		}

		try {
			// StringEscapeUtils.escapeXml(str) escapes the five special
			//  XML characters, but also escapes all characters beyond
			//  the ASCII range
			bwriter.write("      <" + outputElmtName + " " + 
					weightAttrName + "=\"" + w + "\">" + 
					EscapeXML.escapeXML(sb.toString()) + 
					"</" + outputElmtName + ">") ;
			bwriter.newLine() ;
		} catch (Exception e) {
			// KRB:  what to do here?
			e.printStackTrace() ;
		}
	}
}

