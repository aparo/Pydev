/**
 * Copyright (c) 2010 Aptana, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.python.pydev.django_templates.html.editor;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.swt.widgets.Composite;
import org.python.pydev.django_templates.IDjConstants;
import org.python.pydev.django_templates.editor.DjEditor;
import org.python.pydev.django_templates.html.outline.DjHTMLOutlineContentProvider;
import org.python.pydev.django_templates.html.outline.DjHTMLOutlineLabelProvider;
import org.python.pydev.utils.ICallback;

import com.aptana.editor.common.outline.CommonOutlinePage;
import com.aptana.editor.common.parsing.FileService;
import com.aptana.editor.html.HTMLEditor;
import com.aptana.editor.html.parsing.HTMLParseState;

/**
 * @author Fabio Zadrozny
 */
public class DjHTMLEditor extends HTMLEditor {

    private DjEditor djEditor;
    
    /*
     * (non-Javadoc)
     * 
     * @see com.aptana.editor.common.AbstractThemeableEditor#initializeEditor()
     */
    @Override
    protected void initializeEditor() {
        super.initializeEditor();
        djEditor = new DjEditor();
        this.djEditor.registerPrefChangeListener(new ICallback() {
            
            public Object call(Object args) throws Exception {
                return getISourceViewer();
            }
        });
        
        setSourceViewerConfiguration(new DjHTMLSourceViewerConfiguration(this.djEditor.getChainedPrefStore(), this));
        setDocumentProvider(new DjHTMLDocumentProvider());
    }
    
    @Override
    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
        ISourceViewer viewer = super.createSourceViewer(parent, ruler, styles);
        djEditor.onCreateSourceViewer(viewer);
        return viewer;
    }


    @Override
    public void dispose() {
        super.dispose();
        djEditor.dispose();
    }

    @Override
    protected FileService createFileService() {
        return new FileService(IDjConstants.LANGUAGE_DJANGO_TEMPLATES_HTML, new HTMLParseState());
    }

    @Override
    protected CommonOutlinePage createOutlinePage() {
        CommonOutlinePage outline = super.createOutlinePage();
        outline.setContentProvider(new DjHTMLOutlineContentProvider());
        outline.setLabelProvider(new DjHTMLOutlineLabelProvider(getFileService().getParseState()));

        return outline;
    }

    @Override
    protected char[] getPairMatchingCharacters() {
        return this.djEditor.getPairMatchingCharacters(super.getPairMatchingCharacters());
    }
}
