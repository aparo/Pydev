package org.python.pydev.django_templates.editor;

import java.util.ArrayList;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.python.pydev.core.docutils.StringUtils;
import org.python.pydev.django_templates.IDjConstants;
import org.python.pydev.django_templates.completions.templates.DjContextType;
import org.python.pydev.django_templates.completions.templates.TemplateHelper;
import org.python.pydev.editor.PyCodeScanner;
import org.python.pydev.plugin.preferences.PydevPrefs;
import org.python.pydev.ui.ColorAndStyleCache;

import com.aptana.editor.common.IPartitioningConfiguration;
import com.aptana.editor.common.ISourceViewerConfiguration;
import com.aptana.editor.common.text.rules.ISubPartitionScanner;
import com.aptana.editor.common.text.rules.SubPartitionScanner;
import com.aptana.theme.IThemeManager;
import com.aptana.theme.ThemePlugin;

/**
 * @author Fabio Zadrozny
 */
public abstract class DjSourceConfiguration implements IPartitioningConfiguration, ISourceViewerConfiguration {

    protected static final String SOURCE_DJ = "source.dj";
    protected static final String STRING_QUOTED_DOUBLE_DJ = "string.quoted.double.dj";
    protected static final String STRING_QUOTED_SINGLE_DJ = "string.quoted.single.dj";
    protected static final String COMMENT_DJ = "comment.dj";

    public final static String PREFIX = "__dj_";
    public final static String DEFAULT = "__dj" + IDocument.DEFAULT_CONTENT_TYPE;
    public final static String STRING_SINGLE = PREFIX + "string_single";
    public final static String STRING_DOUBLE = PREFIX + "string_double";
    public final static String COMMENT = PREFIX + "comment";

    public static final String[] CONTENT_TYPES = new String[] { DEFAULT, STRING_SINGLE, STRING_DOUBLE, COMMENT };

    protected static final String[][] TOP_CONTENT_TYPES = new String[][] { { IDjConstants.CONTENT_TYPE_DJANGO_HTML } };

    protected IPredicateRule[] partitioningRules = new IPredicateRule[] {
            new SingleLineRule("\"", "\"", new Token(STRING_DOUBLE), '\\'),
            new SingleLineRule("\'", "\'", new Token(STRING_SINGLE), '\\'), 
            new SingleLineRule("{#", "#}", new Token(COMMENT)) 
    };

    protected PyCodeScanner codeScanner;
    protected RuleBasedScanner singleQuotedStringScanner;
    protected RuleBasedScanner doubleQuotedStringScanner;
    protected RuleBasedScanner commentScanner;
    private String contentType;
    
    public DjSourceConfiguration(String contentType){
        this.contentType = contentType;
    }


    /**
     * @see com.aptana.editor.common.IPartitioningConfiguration#getContentTypes()
     */
    public String[] getContentTypes() {
        return CONTENT_TYPES;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.aptana.editor.common.ITopContentTypesProvider#getTopContentTypes()
     */
    public String[][] getTopContentTypes() {
        return TOP_CONTENT_TYPES;
    }

    /**
     * @see com.aptana.editor.common.IPartitioningConfiguration#getPartitioningRules()
     */
    public IPredicateRule[] getPartitioningRules() {
        return partitioningRules;
    }

    /**
     * @see com.aptana.editor.common.IPartitioningConfiguration#createSubPartitionScanner()
     */
    public ISubPartitionScanner createSubPartitionScanner() {
        return new SubPartitionScanner(partitioningRules, CONTENT_TYPES, new Token(DEFAULT));
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.aptana.editor.common.IPartitioningConfiguration#
     * getDocumentDefaultContentType()
     */
    public String getDocumentContentType(String contentType) {
        if (contentType.startsWith(PREFIX)) {
            return this.contentType;
        }
        return null;
    }

    /**
     * @see com.aptana.editor.common.ISourceViewerConfiguration#setupPresentationReconciler(org.eclipse.jface.text.presentation.PresentationReconciler,
     *      org.eclipse.jface.text.source.ISourceViewer)
     */
    public void setupPresentationReconciler(PresentationReconciler reconciler, ISourceViewer sourceViewer) {
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(getCodeScanner());
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

        reconciler.setDamager(dr, DEFAULT);
        reconciler.setRepairer(dr, DEFAULT);

        dr = new DefaultDamagerRepairer(getSingleQuotedStringScanner());
        reconciler.setDamager(dr, DjSourceConfiguration.STRING_SINGLE);
        reconciler.setRepairer(dr, DjSourceConfiguration.STRING_SINGLE);

        dr = new DefaultDamagerRepairer(getDoubleQuotedStringScanner());
        reconciler.setDamager(dr, DjSourceConfiguration.STRING_DOUBLE);
        reconciler.setRepairer(dr, DjSourceConfiguration.STRING_DOUBLE);
        
        dr = new DefaultDamagerRepairer(getCommentScanner());
        reconciler.setDamager(dr, DjSourceConfiguration.COMMENT);
        reconciler.setRepairer(dr, DjSourceConfiguration.COMMENT);
    }

    public PyCodeScanner getCodeScanner() {
        if (codeScanner == null) {
            IPreferenceStore store = TemplateHelper.getTemplatesPreferenceStore();
            codeScanner = new PyCodeScanner(getColorCache(), getKeywordsFromTemplates());
            store.addPropertyChangeListener(new IPropertyChangeListener() {
                
                public void propertyChange(PropertyChangeEvent event) {
                    if(TemplateHelper.CUSTOM_TEMPLATES_DJ_KEY.equals(event.getProperty())){
                        updateCodeScannerKeywords();
                    }
                }
            });
        }
        return codeScanner;
    }



    protected void updateCodeScannerKeywords() {
        if(this.codeScanner != null){
            String[] keywords = getKeywordsFromTemplates();
            this.codeScanner.setKeywords(keywords);
        }
    }

    public String[] getKeywordsFromTemplates() {
        Template[] templates = TemplateHelper.getTemplateStore().
            getTemplates(DjContextType.DJ_TAGS_COMPLETIONS_CONTEXT_TYPE);
        ArrayList<String> templateNames = new ArrayList<String>();
        for (Template template : templates) {
            String name = template.getName();
            if(StringUtils.containsWhitespace(name)){
                continue;
            }
            templateNames.add(name);
        }
        String[] keywords = templateNames.toArray(new String[templateNames.size()]);
        return keywords;
    }

    protected ITokenScanner getSingleQuotedStringScanner() {
        if (singleQuotedStringScanner == null) {
            singleQuotedStringScanner = new RuleBasedScanner();
            singleQuotedStringScanner.setDefaultReturnToken(getToken(STRING_QUOTED_SINGLE_DJ));
        }
        return singleQuotedStringScanner;
    }

    
    protected ITokenScanner getCommentScanner() {
        if (commentScanner == null) {
            commentScanner = new RuleBasedScanner();
            commentScanner.setDefaultReturnToken(getToken(COMMENT_DJ));
        }
        return commentScanner;
    }
    
    protected ITokenScanner getDoubleQuotedStringScanner() {
        if (doubleQuotedStringScanner == null) {
            doubleQuotedStringScanner = new RuleBasedScanner();
            doubleQuotedStringScanner.setDefaultReturnToken(getToken(STRING_QUOTED_DOUBLE_DJ));
        }
        return doubleQuotedStringScanner;
    }

    protected IToken getToken(String tokenName) {
        return getThemeManager().getToken(tokenName);
    }

    protected IThemeManager getThemeManager() {
        return ThemePlugin.getDefault().getThemeManager();
    }

    protected ColorAndStyleCache colorCache;

    public ColorAndStyleCache getColorCache() {
        if (colorCache == null) {
            IPreferenceStore prefStore = PydevPrefs.getChainedPrefStore();
            colorCache = new ColorAndStyleCache(prefStore);
            prefStore.addPropertyChangeListener(new IPropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent event) {
                    String property = event.getProperty();
                    if (ColorAndStyleCache.isColorOrStyleProperty(property)) {
                        colorCache.reloadNamedColor(property);
                        updateSyntaxColorAndStyle();
                    }
                }
            });
        }
        return colorCache;
    }

    public void updateSyntaxColorAndStyle() {
        this.getCodeScanner().updateColors();
    }
}
