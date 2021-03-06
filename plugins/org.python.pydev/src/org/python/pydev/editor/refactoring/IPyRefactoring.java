/*
 * Created on May 5, 2005
 *
 * @author Fabio Zadrozny
 */
package org.python.pydev.editor.refactoring;


import org.python.pydev.editor.model.ItemPointer;

/**
 * @author Fabio Zadrozny
 */
public interface IPyRefactoring {

    /**
     * @return The name for the user that represents this refactoring engine.
     * 
     * This is useful for 'solving conflicts' if more than one refactoring engine provides the same action.
     */
    public String getName();


    /**
     * Rename something (class, method, local...)
     */
    public String rename(RefactoringRequest request);

    /**
     * Find where something is defined (many results because it may seem something is defined in several places)
     * @return an ItemPointer to some definition
     */
    public ItemPointer[] findDefinition(RefactoringRequest request) throws TooManyMatchesException;


}