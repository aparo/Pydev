package org.python.pydev.django_templates.editor;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.python.pydev.django_templates.completions.templates.TemplateHelper;
import org.python.pydev.editor.actions.PyBackspace;
import org.python.pydev.plugin.PydevPlugin;
import org.python.pydev.plugin.preferences.PydevPrefs;
import org.python.pydev.ui.ColorAndStyleCache;
import org.python.pydev.utils.ICallback;

public class DjEditor {

    private IPropertyChangeListener prefChangeListener;

    public char[] getPairMatchingCharacters(char[] orig) {
        char[] modified = new char[orig.length + 2];
        System.arraycopy(orig, 0, modified, 0, orig.length);
        modified[orig.length] = '%';
        modified[orig.length + 1] = '%';
        return modified;
    }

    
    public void registerPrefChangeListener(final ICallback getISourceViewer) {
        this.prefChangeListener = createPrefChangeListener(getISourceViewer);
        getChainedPrefStore().addPropertyChangeListener(prefChangeListener);
        TemplateHelper.getTemplatesPreferenceStore().addPropertyChangeListener(prefChangeListener);
    }
    
    
    private IPropertyChangeListener createPrefChangeListener(final ICallback getISourceViewer) {
        return new IPropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent event) {
                String property = event.getProperty();
                if (ColorAndStyleCache.isColorOrStyleProperty(property) || TemplateHelper.CUSTOM_TEMPLATES_DJ_KEY.equals(property)) {
                    try {
                        ((ISourceViewer)getISourceViewer.call(null)).invalidateTextPresentation();
                    } catch (Exception e) {
                        PydevPlugin.log(e);
                    }
                }
            }
        };
    }


    public IPreferenceStore getChainedPrefStore() {
        return PydevPrefs.getChainedPrefStore();
    }


    public void dispose() {
        if(prefChangeListener != null){
            getChainedPrefStore().removePropertyChangeListener(prefChangeListener);
            prefChangeListener = null;
        }
    }


    public void onCreateSourceViewer(ISourceViewer viewer) {
        if(viewer instanceof TextViewer){
            TextViewer textViewer = (TextViewer) viewer;
            ((TextViewer) viewer).appendVerifyKeyListener(PyBackspace.createVerifyKeyListener(textViewer, null));
        }
    }
}
