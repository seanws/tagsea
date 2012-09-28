/**
 * <copyright>
 * </copyright>
 *
 * $Id: TaskNavigateEventValidator.java,v 1.1 2007/07/30 21:12:09 delmyers Exp $
 */
package net.sourceforge.tagsea.logging.validation;


/**
 * A sample validator interface for {@link net.sourceforge.tagsea.logging.TaskNavigateEvent}.
 * This doesn't really do anything, and it's not a real EMF artifact.
 * It was generated by the org.eclipse.emf.examples.generator.validator plug-in to illustrate how EMF's code generator can be extended.
 * This can be disabled with -vmargs -Dorg.eclipse.emf.examples.generator.validator=false.
 */
public interface TaskNavigateEventValidator {
	boolean validate();

	boolean validateDescription(String value);
	boolean validateLine(int value);
	boolean validateResource(String value);
}
