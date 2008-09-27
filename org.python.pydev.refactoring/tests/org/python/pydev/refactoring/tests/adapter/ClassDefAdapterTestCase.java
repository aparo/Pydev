/* 
 * Copyright (C) 2006, 2007  Dennis Hunziker, Ueli Kistler 
 */

package org.python.pydev.refactoring.tests.adapter;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.python.pydev.refactoring.ast.adapters.IClassDefAdapter;
import org.python.pydev.refactoring.ast.adapters.ModuleAdapter;
import org.python.pydev.refactoring.tests.CompletionEnvironmentSetupHelper;
import org.python.pydev.refactoring.tests.core.AbstractIOTestCase;

public class ClassDefAdapterTestCase extends AbstractIOTestCase {

    private CompletionEnvironmentSetupHelper setupHelper;

    public ClassDefAdapterTestCase(String name) {
        super(name);
    }

    @Override
    public void runTest() throws Throwable {
        setupHelper = new CompletionEnvironmentSetupHelper();
        setupHelper.setupEnv();
        try{

            StringBuffer buffer = new StringBuffer();
            ModuleAdapter module = setupHelper.createModuleAdapter(this);
            List<IClassDefAdapter> classes = module.getClasses();
            assertTrue(classes.size() > 0);
    
            for (IClassDefAdapter adapter : classes) {
                printBaseClass(buffer, adapter);
                
                //sort them as the order is not actually guaranteed!
                IClassDefAdapter[] baseClasses = adapter.getBaseClasses().toArray(new IClassDefAdapter[0]);
                Arrays.sort(baseClasses, new Comparator<IClassDefAdapter>(){

                    public int compare(IClassDefAdapter o1, IClassDefAdapter o2) {
                        return o1.getName().compareTo(o2.getName());
                    }}
                );
                
                for (IClassDefAdapter base : baseClasses) {
                    buffer.append("## " + adapter.getName());
                    printBaseDefClass(buffer, base);
                }
    
            }
    
            this.setTestGenerated(buffer.toString().trim());
            assertEquals(getExpected(), getGenerated());
        }finally{
            setupHelper.tearDownEnv();
        }
    }

    private void printBaseDefClass(StringBuffer buffer, IClassDefAdapter base) {
        buffer.append(" Base: " + base.getName());
        buffer.append("\n");

    }

    private void printBaseClass(StringBuffer buffer, IClassDefAdapter adapter) {
        buffer.append("# " + adapter.getName());
        for (String name : adapter.getBaseClassNames()) {
            buffer.append(" " + name);
        }
        buffer.append("\n");
    }

    @Override
    public String getExpected() {
        return getResult();
    }

}
