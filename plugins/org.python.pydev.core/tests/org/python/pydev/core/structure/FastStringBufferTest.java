package org.python.pydev.core.structure;

import junit.framework.TestCase;

public class FastStringBufferTest extends TestCase{

    private static final int ITERATIONS = 10000;
    private static final int OUTER_ITERATIONS = 50;

    public void testFastString1() throws Exception {
        
        FastStringBuffer fastString = new FastStringBuffer(2);
        fastString.append("bbb");
        assertEquals("bbb", fastString.toString());
        fastString.append("ccc");
        assertEquals("bbbccc", fastString.toString());
        fastString.clear();
        assertEquals("", fastString.toString());
        fastString.append("abc");
        assertEquals("abc", fastString.toString());
        fastString.reverse();
        assertEquals("cba", fastString.toString());
        
        fastString.clear();
        fastString.append("aaa");
        FastStringBuffer other = new FastStringBuffer(3);
        other.append("bbcccdddddddddddddddddddddddddddddd");
        fastString.append(other);
        assertEquals("aaabbcccdddddddddddddddddddddddddddddd", fastString.toString());
        fastString.insert(1, "22");
        assertEquals("a22aabbcccdddddddddddddddddddddddddddddd", fastString.toString());
        fastString.append('$');
        assertEquals("a22aabbcccdddddddddddddddddddddddddddddd$", fastString.toString());
        fastString.insert(1, ".");
        assertEquals("a.22aabbcccdddddddddddddddddddddddddddddd$", fastString.toString());
        fastString.replace(0,1, "xxx");
        assertEquals("xxx.22aabbcccdddddddddddddddddddddddddddddd$", fastString.toString());
        fastString.delete(0,1);
        assertEquals("xx.22aabbcccdddddddddddddddddddddddddddddd$", fastString.toString());
        
        char[] charArray = fastString.toString().toCharArray();
        char[] charArray2 = fastString.toCharArray();
        assertEquals(charArray.length, charArray2.length);
        for (int i = 0; i < charArray2.length; i++) {
            assertEquals(charArray[i], charArray2[i]);
            
        }
    }
    
    public void testReverseIterating() {
        FastStringBuffer fastStringBuffer = new FastStringBuffer("abc", 0);
        FastStringBuffer fastStringBuffer2 = new FastStringBuffer("", 3);
        for (Character c : fastStringBuffer.reverseIterator()) {
            fastStringBuffer2.append(c);
        }
        assertEquals("cba", fastStringBuffer2.toString());
    }
    
    public void testEndsWith() throws Exception {
        FastStringBuffer fastStringBuffer = new FastStringBuffer("abc", 0);
        assertTrue(fastStringBuffer.endsWith("c"));
        assertTrue(fastStringBuffer.endsWith("bc"));
        assertTrue(fastStringBuffer.endsWith("abc"));
        assertTrue(!fastStringBuffer.endsWith("aabc"));
        
        assertTrue(fastStringBuffer.startsWith("abc"));
        assertTrue(fastStringBuffer.startsWith("a"));
        assertTrue(fastStringBuffer.startsWith("ab"));
        assertTrue(!fastStringBuffer.startsWith("abcd"));
        
        fastStringBuffer.setCharAt(0, 'h');
        assertTrue(fastStringBuffer.startsWith("hb"));
    }
    
    public void testReplace() throws Exception {
        assertEquals("def", new FastStringBuffer("abcdefabc", 0).replaceAll("abc", "").toString());
        assertEquals("xyzdef", new FastStringBuffer("abcdef", 0).replaceAll("abc", "xyz").toString());
        assertEquals("xyzdefxyz", new FastStringBuffer("abcdefabc", 0).replaceAll("abc", "xyz").toString());
        assertEquals("aaa", new FastStringBuffer("abcabcabc", 0).replaceAll("abc", "a").toString());
        assertEquals("xyzxyzxyz", new FastStringBuffer("aaa", 0).replaceAll("a", "xyz").toString());
        assertEquals("", new FastStringBuffer("aaa", 0).replaceAll("a", "").toString());
        assertEquals("ba", new FastStringBuffer("aaa", 0).replaceAll("aa", "b").toString());
    }
    
    
    public void testAppendN() throws Exception {
		FastStringBuffer buf = new FastStringBuffer(0);
		assertEquals("   ", buf.appendN(' ', 3).toString());
		assertEquals("   ", buf.clear().appendN(" ", 3).toString());
		assertEquals("   aaa", buf.appendN('a', 3).toString());
		assertEquals("   aaabbbbbb", buf.appendN("bb", 3).toString());
	}
    
    public void testStartsWith() throws Exception {
        FastStringBuffer buf = new FastStringBuffer(0);
        assertFalse(buf.startsWith('"'));
        buf.append("a");
        assertFalse(buf.startsWith('"'));
        assertTrue(buf.startsWith('a'));
        buf.deleteFirst();
        assertFalse(buf.startsWith('"'));
        assertFalse(buf.startsWith('a'));
    }
    
    public void testEndsWithChar() throws Exception {
        FastStringBuffer buf = new FastStringBuffer(0);
        assertFalse(buf.endsWith('"'));
        buf.append("a");
        assertFalse(buf.endsWith('"'));
        assertTrue(buf.endsWith('a'));
        buf.deleteFirst();
        assertFalse(buf.endsWith('"'));
        assertFalse(buf.endsWith('a'));
        buf.append("ab");
        assertTrue(buf.endsWith('b'));
        assertFalse(buf.endsWith('a'));
    }
    
    public void testCountNewLines() throws Exception {
        FastStringBuffer buf = new FastStringBuffer(0);
        assertEquals(0, buf.countNewLines());
        buf.append('\r');
        assertEquals(1, buf.countNewLines());
        buf.append('\n');
        assertEquals(1, buf.countNewLines());
        buf.append('\n');
        assertEquals(2, buf.countNewLines());
    }
    
    
//    public void testFastString() throws Exception {
//        
//        long total=0;
//        FastStringBuffer fastString = new FastStringBuffer(50);
//        for(int j=0;j<OUTER_ITERATIONS;j++){
//            final long start = System.nanoTime();
//            
//            
//            fastString.clear();
//            for(int i=0;i<ITERATIONS;i++){
//                fastString.append("test").append("bar").append("foo").append("foo").append("foo").append("foo");
//            }
//            
//            final long end = System.nanoTime();
//            long delta=(end-start)/1000000;
//            total+=delta;
////            System.out.println("Fast: " + delta);
//        }        
//        System.out.println("Fast Total:"+total);
//    }
//    
//    public void testStringBuffer() throws Exception {
//        
//        long total=0;
//        StringBuffer fastString = new StringBuffer(50);
//        for(int j=0;j<OUTER_ITERATIONS;j++){
//            final long start = System.nanoTime();
//            
//            
//            fastString.setLength(0);
//            for(int i=0;i<ITERATIONS;i++){
//                fastString.append("test").append("bar").append("foo").append("foo").append("foo").append("foo");
//            }
//            
//            final long end = System.nanoTime();
//            long delta=(end-start)/1000000;
//            total+=delta;
////            System.out.println("Buffer: " + delta);
//        }   
//        System.out.println("Buffer Total:"+total);
//    }
        
}
