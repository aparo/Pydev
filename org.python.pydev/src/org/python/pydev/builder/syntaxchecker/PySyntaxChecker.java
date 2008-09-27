package org.python.pydev.builder.syntaxchecker;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.python.pydev.builder.PyDevBuilderPrefPage;
import org.python.pydev.builder.PyDevBuilderVisitor;
import org.python.pydev.editor.codecompletion.revisited.modules.SourceModule;
import org.python.pydev.parser.PyParser;
import org.python.pydev.plugin.DebugSettings;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.nature.PythonNature;

/**
 * Whenever a given resource is changed, a syntax check is done, updating errors related to the syntax.
 * 
 * @author Fabio
 */
public class PySyntaxChecker extends PyDevBuilderVisitor{


    @Override
    public void visitChangedResource(IResource resource, IDocument document, IProgressMonitor monitor) {
        PythonNature nature = getPythonNature(resource);
        if(nature == null){
            return;
        }

        if(PyDevBuilderPrefPage.getAnalyzeOnlyActiveEditor()){
            if(DebugSettings.DEBUG_ANALYSIS_REQUESTS){
                System.out.println("PySyntaxChecker: PyDevBuilderPrefPage.getAnalyzeOnlyActiveEditor()");
            }
            return; //not analyzed with this builder... always from parser changes.
        }
        
        try {
            PyParser.deleteErrorMarkers(resource);
        } catch (CoreException e) {
            PydevPlugin.log(e);
        }
        
        if(DebugSettings.DEBUG_ANALYSIS_REQUESTS){
            System.out.println("PySyntaxChecker: Checking!");
        }
        
        SourceModule mod = getSourceModule(resource, document, nature);
        Throwable parseError = mod.parseError;
        
        if(parseError != null){
            try {
                PyParser.createParserErrorMarkers(parseError, resource, document);
            } catch (Exception e) {
                PydevPlugin.log(e);
            }
        }
        
    }

    @Override
    public void visitRemovedResource(IResource resource, IDocument document, IProgressMonitor monitor) {
        // Nothing needs to be done in this case
    }

}
