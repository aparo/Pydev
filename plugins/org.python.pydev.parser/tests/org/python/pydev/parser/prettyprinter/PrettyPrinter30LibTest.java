/*
 * Created on Feb 27, 2006
 */
package org.python.pydev.parser.prettyprinter;

import java.io.File;

import org.python.pydev.core.IGrammarVersionProvider;
import org.python.pydev.core.TestDependent;
import org.python.pydev.parser.prettyprinterv2.PrettyPrinterPrefsV2;

public class PrettyPrinter30LibTest extends AbstractPrettyPrinterTestBase{


    private static boolean MAKE_COMPLETE_PARSE = true;


    public static void main(String[] args) {
        try {
//            DEBUG = true;
            junit.textui.TestRunner.run(PrettyPrinter30LibTest.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void setUp() throws Exception {
        super.setUp();
        prefs = new PrettyPrinterPrefsV2("\n", "    ", versionProvider);
        setDefaultVersion(IGrammarVersionProvider.GRAMMAR_PYTHON_VERSION_3_0);
    }

    public void testOnCompleteLib() throws Exception {
        parseAndPrettyPrintFile(new File("D:/bin/Python301/Lib/aifc"));
        File file = new File(TestDependent.PYTHON_30_LIB);
        if(MAKE_COMPLETE_PARSE){
            parseAndReparsePrettyPrintedFilesInDir(file);
        }else{
            System.out.println("COMPLETE LIB NOT PARSED!");
        }
    }
    
}
