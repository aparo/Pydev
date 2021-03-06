package com.python.pydev.refactoring.search;

import java.util.HashSet;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.IDocument;
import org.eclipse.search.ui.ISearchResult;
import org.python.pydev.core.REF;
import org.python.pydev.core.Tuple;
import org.python.pydev.core.docutils.PySelection;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.editor.refactoring.RefactoringRequest;
import org.python.pydev.parser.visitors.scope.ASTEntry;
import org.python.pydev.plugin.PydevPlugin;

import com.python.pydev.refactoring.IPyRefactoring2;
import com.python.pydev.refactoring.actions.PyFindAllOccurrences;
import com.python.pydev.refactoring.refactorer.search.AbstractPythonSearchQuery;
import com.python.pydev.refactoring.wizards.rename.AbstractRenameRefactorProcess;
import com.python.pydev.ui.search.FileMatch;
import com.python.pydev.ui.search.LineElement;

public class FindOccurrencesSearchQuery extends AbstractPythonSearchQuery{

    private IPyRefactoring2 pyRefactoring;
    private RefactoringRequest req;
    private FindOccurrencesSearchResult findOccurrencesSearchResult;

    public FindOccurrencesSearchQuery(IPyRefactoring2 r, RefactoringRequest req) {
        super(req.initialName);
        this.pyRefactoring = r;
        this.req = req;
    }

    public ISearchResult getSearchResult() {
        if(findOccurrencesSearchResult == null){
            findOccurrencesSearchResult = new FindOccurrencesSearchResult(this);
        }
        return findOccurrencesSearchResult;
    }

    public IStatus run(IProgressMonitor monitor) throws OperationCanceledException {
        try {
            req.pushMonitor(monitor);
            Map<Tuple<String, IFile>, HashSet<ASTEntry>> occurrences;
            occurrences = pyRefactoring.findAllOccurrences(req);
            if(occurrences == null){
                return Status.OK_STATUS;
            }
            int length = req.initialName.length();
            
            
            HashSet<Integer> foundOffsets = new HashSet<Integer>();
            for (Map.Entry<Tuple<String, IFile>, HashSet<ASTEntry>> o : occurrences.entrySet()) {
                
                foundOffsets.clear();
                IFile file = o.getKey().o2;
                IDocument doc = REF.getDocFromResource(file);
                
                for(ASTEntry entry:o.getValue()){
                    int offset = AbstractRenameRefactorProcess.getOffset(doc, entry);
                    if(!foundOffsets.contains(offset)){
                        foundOffsets.add(offset);
                        if(PyFindAllOccurrences.DEBUG_FIND_REFERENCES){
                            System.out.println("Adding match:"+file);
                        }
                        PySelection ps = new PySelection(doc, offset);
                        int lineNumber = ps.getLineOfOffset();
                        String lineContents = ps.getLine(lineNumber);
                        int lineStartOffset = ps.getLineOffset(lineNumber);
                        
                        LineElement element = new LineElement(file, lineNumber, lineStartOffset, lineContents);
                        findOccurrencesSearchResult.addMatch(new FileMatch(file, offset, length, element));
                    }
                }
            }
        } catch (CoreException e) {
            PydevPlugin.log(e);
        }finally{
            req.popMonitor();
        }
        return Status.OK_STATUS;
    }
    

    public String getResultLabel(int nMatches) {
        String searchString= getSearchString();
        if (searchString.length() > 0) {
            // text search
            if (isScopeAllFileTypes()) {
                // search all file extensions
                if (nMatches == 1) {
                    return StringUtils.format("%s - 1 match in %s", searchString, getDescription() );
                }
                return StringUtils.format("%s - %s matches in %s", searchString, new Integer(nMatches), getDescription() ); 
            }
            // search selected file extensions
            if (nMatches == 1) {
                return StringUtils.format("%s - 1 match in %s", searchString, getDescription() );
            }
            return StringUtils.format("%s - %s matches in %s", searchString, new Integer(nMatches), getDescription() );
        }
        throw new RuntimeException("Unexpected condition when finding: "+searchString);
    }

    private String getDescription() {
        return "'"+req.pyEdit.getProject().getName()+"' and related projects";
    }

}
