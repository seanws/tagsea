/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Research
 *******************************************************************************/
package com.ibm.research.tagging.core.ui.controls;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.internal.misc.StringMatcher;

public class NQueryTablePatternFilter extends AbstractPatternFilter 
{    
	/**
	 * The string pattern matcher used for this pattern filter.  
	 */
    private Vector<StringMatcher> matchers;

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerFilter#filter(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object[])
     */
    public final Object[] filter(Viewer viewer, Object parent, Object[] elements) 
    {     
    	if (matchers == null) 
			return elements;

        return super.filter(viewer, parent, elements);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
     */
    public final boolean select(Viewer viewer, Object parentElement, Object element) 
    {
        return isElementVisible(viewer, element);
    }
   	
    /**
     * The pattern string for which this filter should select 
     * elements in the viewer.
     * 
     * @param patternString
     */
    public void setPattern(String patternString) 
    {
        if (patternString == null || patternString.equals("")) { //$NON-NLS-1$
        	matchers = null;
		}
        else 
		{			
			// Split on whitespace
			String[] words = patternString.split("[ \t]");
		
			for(int i = 0 ; i < words.length; i++)
			{
				if(!words[i].endsWith("*"))
					words[i] = words[i] + "*";
			}
			
			if(words.length > 0)
			{
				matchers = new Vector<StringMatcher>();
				
				for(String word : words)
				{
					matchers.add(new StringMatcher(word, true, false));
				}
			}
		}
    }

    /**
     * Answers whether the given String matches the pattern.
     * 
     * @param string the String to test
     * 
     * @return whether the string matches the pattern
     */
    private boolean match(String string) 
    {
    	if (matchers == null) 
    	{
			return true;
    	}
    	
    	for(StringMatcher matcher : matchers)
    	{
    		if(matcher.match(string))
    			return true;
    	}
    	
        return false;
    }
    
    /**
     * Answers whether the given element is a valid selection in 
     * the filtered tree.  For example, if a tree has items that 
     * are categorized, the category itself may  not be a valid 
     * selection since it is used merely to organize the elements.
     * 
     * @param element
     * @return true if this element is eligible for automatic selection
     */
    public boolean isElementSelectable(Object element){
    	return element != null;
    }
    
    /**
     * Answers whether the given element in the given viewer matches
     * the filter pattern.  This is a default implementation that will 
     * show a leaf element in the tree based on whether the provided  
     * filter text matches the text of the given element's text, or that 
     * of it's children (if the element has any).  
     * 
     * Subclasses may override this method.
     * 
     * @param viewer the tree viewer in which the element resides
     * @param element the element in the tree to check for a match
     * 
     * @return true if the element matches the filter pattern
     */
    public boolean isElementVisible(Viewer viewer, Object element){
    	return isLeafMatch(viewer, element);
    }
   
    /**
     * Check if the current (leaf) element is a match with the filter text.  
     * The default behavior checks that the label of the element is a match. 
     * 
     * Subclasses should override this method.
     * 
     * @param viewer the viewer that contains the element
     * @param element the tree element to check
     * @return true if the given element's label matches the filter text
     */
    public boolean isLeafMatch(Viewer viewer, Object element)
    {
    	int nColumns = 1;
    	if ( viewer.getControl() instanceof Table )
    	{
    		 nColumns = ((Table) viewer.getControl()).getColumnCount();
    		 if ( nColumns<1 )
    			 nColumns = 1;
    	}
    			
        String labelText = "";
        for (int col=0; col<nColumns; col++)
        	labelText += ((ITableLabelProvider) ((StructuredViewer) viewer).getLabelProvider()).getColumnText(element,col) + " ";
        
        if(labelText == null) {
			return false;
		}
        return wordMatches(labelText);  
    }
    
    /**
     * Take the given filter text and break it down into words using a 
     * BreakIterator.  
     * 
     * @param text
     * @return an array of words
     */
    private String[] getWords(String text)
    {
    	List words = new ArrayList();
		// Break the text up into words, separating based on whitespace and
		// common punctuation.
		// Previously used String.split(..., "\\W"), where "\W" is a regular
		// expression (see the Javadoc for class Pattern).
		// Need to avoid both String.split and regular expressions, in order to
		// compile against JCL Foundation (bug 80053).
		// Also need to do this in an NL-sensitive way. The use of BreakIterator
		// was suggested in bug 90579.
		BreakIterator iter = BreakIterator.getWordInstance();
		iter.setText(text);
		int i = iter.first();
		while (i != java.text.BreakIterator.DONE && i < text.length()) {
			int j = iter.following(i);
			if (j == java.text.BreakIterator.DONE) {
				j = text.length();
			}
			// match the word
			if (Character.isLetterOrDigit(text.charAt(i))) 
			{
				String word = text.substring(i, j);
				words.add(word);
			}
			
			i = j;
		}
		return (String[]) words.toArray(new String[words.size()]);
    }
    
	/**
	 * Return whether or not if any of the words in text satisfy the
	 * match critera.
	 * 
	 * @param text the text to match
	 * @return boolean <code>true</code> if one of the words in text 
	 * 					satisifes the match criteria.
	 */
	protected boolean wordMatches(String text) {
		if (text == null) {
			return false;
		}

		//If the whole text matches we are all set
		if(match(text)) 
		{
			return true;
		}
		
		// Otherwise check if any of the words of the text matches
		String[] words = getWords(text);
		
		for (int i = 0; i < words.length; i++) 
		{	
			String word = words[i];
			
			if (match(word)) 
			{
				return true;
			}
		}

		return false;
	}    
}
