package org.python.pydev.core.docutils;

import java.util.Iterator;

import junit.framework.TestCase;

import org.eclipse.jface.text.Document;

public class PyImportsHandlingTest extends TestCase {

    public static void main(String[] args) {
        try {
            PyImportsHandlingTest test = new PyImportsHandlingTest();
            test.setUp();
            test.testPyImportHandling3();
            test.tearDown();
            junit.textui.TestRunner.run(PyImportsHandlingTest.class);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    public void testPyImportHandling() throws Exception {
        Document doc = new Document("from xxx import yyy");
        PyImportsHandling importsHandling = new PyImportsHandling(doc);
         Iterator<ImportHandle> it = importsHandling.iterator();
        assertTrue(it.hasNext());
        ImportHandle next = it.next();
        assertEquals("from xxx import yyy", next.importFound);
        assertEquals(0, next.startFoundLine);
        assertEquals(0, next.endFoundLine);
        assertFalse(it.hasNext());
    }        
        
    public void testPyImportHandling2() throws Exception {
        
        Document doc = new Document("from xxx import yyy\nfrom y import (a, \nb,\nc)");
        PyImportsHandling importsHandling = new PyImportsHandling(doc);
        Iterator<ImportHandle> it = importsHandling.iterator();
        assertTrue(it.hasNext());
        ImportHandle next = it.next();
        assertEquals("from xxx import yyy", next.importFound);
        
        assertEquals(0, next.startFoundLine);
        assertEquals(0, next.endFoundLine);
        assertTrue(it.hasNext());
        next = it.next();
        
        assertEquals("from y import (a, \nb,\nc)", next.importFound);
        assertEquals(1, next.startFoundLine);
        assertEquals(3, next.endFoundLine);
        
        assertTrue(!it.hasNext());
    }
    
    public void testPyImportHandling3() throws Exception {
        
        Document doc = new Document("from ...a.b import b\nfrom xxx.bbb \\\n    import yyy\n");
        PyImportsHandling importsHandling = new PyImportsHandling(doc);
        Iterator<ImportHandle> it = importsHandling.iterator();
        assertTrue(it.hasNext());
        ImportHandle next = it.next();
        
        assertEquals("from ...a.b import b", next.importFound);
        assertEquals(0, next.startFoundLine);
        assertEquals(0, next.endFoundLine);
        
        next = it.next();
        assertEquals("from xxx.bbb \\\n    import yyy", next.importFound);
        assertEquals(1, next.startFoundLine);
        assertEquals(2, next.endFoundLine);
        
        assertTrue(!it.hasNext());
        
    }

}
