package org.python.pydev.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.python.pydev.core.structure.FastStringBuffer;
import org.python.pydev.editor.codecompletion.revisited.PythonPathHelper;

/**
 * Helper class for finding out about python files below some source folder.
 * 
 * @author Fabio
 */
public class PyFileListing {
    
    /**
     * Information about a python file found (the actual file and the way it was resolved as a python module)
     */
    public static final class PyFileInfo {

        private final String relPath;

        private final File file;

        public PyFileInfo(File file, String relPath) {
            this.file = file;
            this.relPath = relPath;
        }

        /** File object. */
        public File getFile() {
            return file;
        }

        /** Returns fully qualified name of module. */
        public String getModuleName() {
            return relPath;
        }
    }

    /**
     * Returns the directories and python files in a list.
     * 
     * @param file
     * @param addSubFolders
     *            indicates if sub-folders should be added
     * @return tuple with files in pos 0 and folders in pos 1
     */
    @SuppressWarnings("unchecked")
    private static PyFileListing getPyFilesBelow(File file, FileFilter filter, IProgressMonitor monitor, boolean addSubFolders, 
            int level, boolean checkHasInit, String currModuleRep) {
        

        if (monitor == null) {
            monitor = new NullProgressMonitor();
        }
        
        PyFileListing ret = new PyFileListing();
    
        if (file != null && file.exists()) {
            //only check files that actually exist
    
            if (file.isDirectory()) {
                if(level != 0){
                    FastStringBuffer newModuleRep = new FastStringBuffer(currModuleRep, 128); 
                    if(newModuleRep.length() != 0){
                        newModuleRep.append(".");
                    }
                    newModuleRep.append(file.getName());
                    currModuleRep = newModuleRep.toString();
                }
                
                File[] files = null;
    
                if (filter != null) {
                    files = file.listFiles(filter);
                } else {
                    files = file.listFiles();
                }
    
                boolean hasInit = false;
    
                List<File> foldersLater = new LinkedList<File>();
                
                for (int i = 0; i < files.length; i++) {
                    File file2 = files[i];
                    
                    if(file2.isFile()){
                        ret.addPyFileInfo(new PyFileInfo(file2, currModuleRep));

                        monitor.worked(1);
                        monitor.setTaskName("Found:" + file2.toString());
                        
                        if (checkHasInit && hasInit == false){
                            //only check if it has __init__ if really needed
                            if(PythonPathHelper.isValidInitFile(file2.getName())){
                                hasInit = true;
                            }
                        }
                        
                    }else{
                        foldersLater.add(file2);
                    }
                }
                
                if(!checkHasInit || hasInit || level == 0){
                    ret.foldersFound.add(file);
    
                    for (Iterator iter = foldersLater.iterator(); iter.hasNext();) {
                        File file2 = (File) iter.next();
                        if(file2.isDirectory() && addSubFolders){
                            
                            ret.extendWith(getPyFilesBelow(file2, filter, monitor, addSubFolders, level+1, 
                                    checkHasInit, currModuleRep));
                            
                            monitor.worked(1);
                        }
                    }
                }
    
                
            } else if (file.isFile()) {
                ret.addPyFileInfo(new PyFileInfo(file, currModuleRep));
                
            } else{
                throw new RuntimeException("Not dir nor file... what is it?");
            }
        }
        
        return ret;
    }

    private static PyFileListing getPyFilesBelow(File file, FileFilter filter, IProgressMonitor monitor, boolean addSubFolders, boolean checkHasInit) {
        return getPyFilesBelow(file, filter, monitor, addSubFolders, 0, checkHasInit, "");
    }

    public static PyFileListing getPyFilesBelow(File file, FileFilter filter, IProgressMonitor monitor, boolean checkHasInit) {
        return getPyFilesBelow(file, filter, monitor, true, checkHasInit);
    }

    /**
     * @param includeDirs determines if we can include subdirectories
     * @return a file filter only for python files (and other dirs if specified)
     */
    public static FileFilter getPyFilesFileFilter(final boolean includeDirs) {
        return new FileFilter() {
    
            public boolean accept(File pathname) {
                if (includeDirs){
                    if(pathname.isDirectory()){
                        return true;
                    }
                    if(PythonPathHelper.isValidSourceFile(pathname.toString())){
                        return true;
                    }
                    return false;
                }else{
                    if(pathname.isDirectory()){
                        return false;
                    }
                    if(PythonPathHelper.isValidSourceFile(pathname.toString())){
                        return true;
                    }
                    return false;
                }
            }
    
        };
    }

    /**
     * Returns the directories and python files in a list.
     * 
     * @param file
     * @return tuple with files in pos 0 and folders in pos 1
     */
    public static PyFileListing getPyFilesBelow(File file, IProgressMonitor monitor, final boolean includeDirs, boolean checkHasInit) {
        FileFilter filter = getPyFilesFileFilter(includeDirs);
        return getPyFilesBelow(file, filter, monitor, true, checkHasInit);
    }

    /**
     * @return All the IFiles below the current folder that are python files (does not check if it has an __init__ path)
     */
    public static List<IFile> getAllIFilesBelow(IFolder member) {
        final ArrayList<IFile> ret = new ArrayList<IFile>();
        try {
            member.accept(new IResourceVisitor(){
    
                public boolean visit(IResource resource) {
                    if(resource instanceof IFile){
                        ret.add((IFile) resource);
                        return false; //has no members
                    }
                    return true;
                }
                
            });
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
        return ret;
    }

    /**
     * The files we found as being valid for the given filter
     */
    private final List<PyFileInfo> pyFileInfos = new ArrayList<PyFileInfo>();
    
    /**
     * The folders we found as being valid for the given filter
     */
    private List<File> foldersFound = new ArrayList<File>();
    
    public PyFileListing() {
    }
    
    public Collection<PyFileInfo> getFoundPyFileInfos() {
      return pyFileInfos;
    }
    
    public Collection<File> getFoundFolders() {
      return foldersFound;
    }
    
    private void addPyFileInfo(PyFileInfo info) {
      pyFileInfos.add(info);
    }
    
    /**
     * Add the info from the passed listing to this one.
     */
    private void extendWith(PyFileListing other) {
        pyFileInfos.addAll(other.pyFileInfos);
        foldersFound.addAll(other.foldersFound);
    }
}
