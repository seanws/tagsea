/*******************************************************************************
 * Copyright (c) 2006-2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Research
 *******************************************************************************/
package com.ibm.research.tours.fx;

public interface IHighlightable 
{
	public IHighlightEffect getHighlightEffect();
	public void setHighlightEffect(IHighlightEffect highlightEffect);
	public IHighlightEffect[] getSupportedHighlightEffects();
}
