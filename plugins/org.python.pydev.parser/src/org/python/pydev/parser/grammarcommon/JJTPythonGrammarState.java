package org.python.pydev.parser.grammarcommon;

import java.lang.reflect.Constructor;

import org.python.pydev.core.structure.FastStack;
import org.python.pydev.core.structure.FastStringBuffer;
import org.python.pydev.parser.PyParser;
import org.python.pydev.parser.jython.Node;
import org.python.pydev.parser.jython.ParseException;
import org.python.pydev.parser.jython.SimpleNode;
import org.python.pydev.parser.jython.Token;

public final class JJTPythonGrammarState implements IJJTPythonGrammarState{


    private final boolean DEBUG = false;
    private int debugLevel = 0;
    
    
    protected final FastStack<SimpleNode> nodes;
    protected final IntStack marks;
    protected final IntStack lines;
    protected final IntStack columns;

    protected int sp; // number of nodes on stack
    protected int mk; // current mark
    protected boolean node_created;

    public final ITreeBuilder builder;
    private final AbstractPythonGrammar grammar;

    public JJTPythonGrammarState(Class<?> treeBuilderClass, AbstractPythonGrammar grammar) {
        this.grammar = grammar;
        nodes = new FastStack<SimpleNode>();
        marks = new IntStack();
        lines = new IntStack();
        columns = new IntStack();
        sp = 0;
        mk = 0;
        
        try {
            Constructor<?> constructor = treeBuilderClass.getConstructor(JJTPythonGrammarState.class);
            this.builder = (ITreeBuilder) constructor.newInstance(new Object[]{this});
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }
    
    public AbstractPythonGrammar getGrammar() {
        return grammar;
    }
    
    public final SimpleNode getLastOpened() {
        return this.builder.getLastOpened();
    }

    /* Determines whether the current node was actually closed and
       pushed.  This should only be called in the final user action of a
       node scope.  */
    public boolean nodeCreated() {
        return node_created;
    }

    /* Call this to reinitialize the node stack.  It is called
    automatically by the parser's ReInit() method. */
    public void reset() {
        nodes.removeAllElements();
        marks.removeAllElements();
        sp = 0;
        mk = 0;
    }

    /* Returns the root node of the AST.  It only makes sense to call
       this after a successful parse. */
    public Node rootNode() {
        return nodes.getFirst();
    }

    /* Pushes a node on to the stack. */
    private void pushNode(Node n, SimpleNode created, int line, int col) {
        nodes.push(created);
        
		if(created.beginLine == 0)
		    created.beginLine = line;
		
		if(created.beginColumn == 0)
		    created.beginColumn = col;
		

        ++sp;
    }

    /**
     * Note that popNode doesn't pop lines and columns. This is because those are
     * always controlled by the openNodeScope/closeNodeScope or openNodeScope/clearNodeScope.
     * 
     * (so, in this case, clearNodeScope will be called before the popNode).
     */
    public SimpleNode popNode() {
        if (--sp < mk) {
            clearMark();
        }
        return nodes.pop();
    }
    
    
    /* Returns the node currently on the top of the stack. */
    public SimpleNode peekNode() {
        return nodes.peek();
    }
    
    /* Returns the node currently on the top of the stack. */
    public SimpleNode peekNode(int i) {
        return nodes.peek(i);
    }

    /* Returns the number of children on the stack in the current node
       scope. */
    public int nodeArity() {
        return sp - mk;
    }

    /**
     * Called when some exception is raised after the open (so, the proper close won't be called).
     * E.g.: the with construct will end up calling a clear as it's based on a treated exception 
     * in the python 2.5 grammar.
     * 
     * Note that the popNode may still be called after this method.
     */
    public void clearNodeScope(Node n) {
    	if (DEBUG) {
    		debugLevel -= 1;
    		System.out.println(new FastStringBuffer().appendN(' ', debugLevel*4)+""+debugLevel+" clearing scope:" + n);
    	}
    	
        while (sp > mk) {
            popNode();
        }
        lines.pop();
        columns.pop();
        
        clearMark();
    }


    /**
     * Open a new scope (which may result in a new SimpleNode if the close is properly called later on).
     */
    public void openNodeScope(Node n) {
    	Token t = this.grammar.getToken(1);

        if (DEBUG) {
        	System.out.println(new FastStringBuffer().appendN(' ', debugLevel*4)+""+debugLevel+" opening scope:" + n+"tok: "+t+" line: "+t.beginLine);
        	debugLevel += 1;
        }
        lines.push(t.beginLine);
		columns.push(t.beginColumn);

    	
        marks.push(mk);
        mk = sp;
    }

    
    
    /* A definite node is constructed from a specified number of
       children.  That number of nodes are popped from the stack and
       made the children of the definite node.  Then the definite node
       is pushed on to the stack. */
    public void closeNodeScope(final Node n, int num) throws ParseException {
    	if (DEBUG) {
    		debugLevel -= 1;
    		System.out.print(new FastStringBuffer().appendN(' ', debugLevel*4)+""+debugLevel+" closing scope:" + n);
    	}
    	int line = lines.pop();
    	int col = columns.pop();
    	if (DEBUG) {
    		System.out.println(" line: "+line);
    	}

        SimpleNode sn = (SimpleNode) n;
        clearMark();
        SimpleNode newNode = null;
        try {
            newNode = builder.closeNode(sn, num);
            if (ITreeBuilder.DEBUG_TREE_BUILDER) {
                System.out.println("Created node: " + newNode);
            }
        } catch (ParseException exc) {
            throw exc;
        } catch (Exception exc) {
            exc.printStackTrace();
            throw new ParseException("Internal error:" + exc);
        }
        if (newNode == null) {
            throw new ParseException("Internal AST builder error");
        }
    	
        pushNode(n, newNode, line, col);
        node_created = true;
    }



    /* A conditional node is constructed if its condition is true.  All
    the nodes that have been pushed since the node was opened are
    made children of the the conditional node, which is then pushed
    on to the stack.  If the condition is false the node is not
    constructed and they are left on the stack. */
    public void closeNodeScope(final Node n, boolean condition) throws ParseException {
    	if (DEBUG) {
    		debugLevel -= 1;
    		System.out.print(new FastStringBuffer().appendN(' ', debugLevel*4)+""+debugLevel+" closing scope:" + n);
    	}
    	int line = lines.pop();
    	int col = columns.pop();
    	if (DEBUG) {
    		System.out.println(" line: "+line);
    	}
    	
        SimpleNode sn = (SimpleNode) n;
        if (condition) {
            SimpleNode newNode = null;
            try {
                newNode = builder.closeNode(sn, nodeArity());
                if (ITreeBuilder.DEBUG_TREE_BUILDER) {
                    System.out.println("Created node: " + newNode);
                }
            } catch (ParseException exc) {
                throw exc;
                
            } catch (ClassCastException exc) {
                if(PyParser.DEBUG_SHOW_PARSE_ERRORS){
                    exc.printStackTrace();
                }
                throw new ParseException("Internal error:" + exc, sn);
                
            } catch (Exception exc) {
                if(PyParser.DEBUG_SHOW_PARSE_ERRORS){
                    exc.printStackTrace();
                }
                throw new ParseException("Internal error:" + exc, sn);
            }
            if (newNode == null) {
                throw new ParseException("Internal AST builder error when closing node:" + sn);
            }
            clearMark();


            pushNode(n, newNode, line, col);
            node_created = true;
        } else {
            clearMark();
            node_created = false;
        }
    }

    private void clearMark() {
        if (marks.size() > 0) {
            mk = marks.pop();
        } else {
            mk = 0;
        }
    }

    /**
     * @return true if the last scope found has the same indent or a lower indent from the scope just before it.
     * false is returned if the last scope has a higher indent than the scope before it.
     */
    public boolean lastIsNewScope() {
        int size = this.columns.size();
        if(size > 1){
            return this.columns.elementAt(size-1) <= this.columns.elementAt(size-2);
        }
        return true;
    }


}


/**
 * IntStack implementation. During all the tests, it didn't have it's size raised,
 * so, 50 is probably a good overall size... (max on python lib was 40)
 */
class IntStack {
    int[] stack;
    int sp = 0;

    public IntStack() {
        stack = new int[50];
    }


    public void removeAllElements() {
        sp = 0;
    }

    public int size() {
        return sp;
    }

    public int elementAt(int idx) {
        return stack[idx];
    }

    public void push(int val) {
        if (sp >= stack.length) {
            int[] newstack = new int[sp*2];
            System.arraycopy(stack, 0, newstack, 0, sp);
            stack = newstack;
        }
        stack[sp++] = val;
    }

    public int pop() {
        return stack[--sp];
    }
}
