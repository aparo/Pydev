package org.python.pydev.editor.codecompletion;

import org.eclipse.jface.text.Document;

import junit.framework.TestCase;

public class PyContextInformationValidatorTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testIt() throws Exception {
        PyContextInformationValidator validator = new PyContextInformationValidator();
        assertEquals(1, validator.getCurrentParameter(new Document("m1(a,b)\n"), 3, 5, ",", "", true));
        assertEquals(1, validator.getCurrentParameter(new Document("m1('',b)\n"), 3, 6, ",", "", true));
        assertEquals(1, validator.getCurrentParameter(new Document("m1('''(''',b)\n"), 3, 11, ",", "", true));
    }

}
