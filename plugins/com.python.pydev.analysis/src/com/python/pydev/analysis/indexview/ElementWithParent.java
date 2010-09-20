package com.python.pydev.analysis.indexview;

public abstract class ElementWithParent implements ITreeElement{

    protected ITreeElement parent;

    public ElementWithParent(ITreeElement parent){
        this.parent = parent;
    }
    
    @Override
    public ITreeElement getParent() {
        return this.parent;
    }
}
