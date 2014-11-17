package org.gephi.graph.api;

import java.util.Random;

/**
 * A social graph node.
 *
 * @author Alexandru Topirceanu
 */
public final class SNode {

    public static final String Opinion = "opinion";
    public static final String Sleep = "sleep";
    public static final String Tolerance = "tolerance";
    public static final String State = "state";
    public static final String Stubborn = "is stubborn?";
    public static final String TAG_SOCIALIZED = "socialized";
    
    public static final int StubbornSizeRatio = 3;
    public static final int ActiveNodeSizeRatio = 4;
    public static final float DefaultNodeSize = 5f;
    private static final int MinSleep = 1, MaxSleep = 50;
    
    private Random random;
    // encapsulate a graph node
    private Node node;

    public SNode(Node node) {
        this.node = node;       
        random = new Random();
    }

    public Node getNode() {
        return node;
    }

    // attributes column getters / setters
    ////////////////////////////////
    ///////////// SNode ////////////
    ////////////////////////////////
    public Object getValue(String id) {
        return node.getAttributes().getValue(id);
    }

    public String getValueAsString(String id) {
        Object obj = node.getAttributes().getValue(id);
        if (obj != null && obj instanceof String) {
            return (String) obj;
        } else {
            return null;
        }
    }

    public Integer getValueAsInteger(String id) {
        Object obj = node.getAttributes().getValue(id);
        if (obj != null && obj instanceof Integer) {
            return (Integer) obj;
        } else {
            return null;
        }
    }
    
    public Long getValueAsLong(String id) {
        Object obj = node.getAttributes().getValue(id);
        if (obj != null && obj instanceof Long) {
            return (Long) obj;
        } else {
            return null;
        }
    }

    public Float getValueAsFloat(String id) {
        Object obj = node.getAttributes().getValue(id);
        if (obj != null && obj instanceof Float) {
            return (Float) obj;
        } else {
            return null;
        }
    }
    
    public Double getValueAsDouble(String id) {
        Object obj = node.getAttributes().getValue(id);
        if (obj != null && obj instanceof Double) {
            return (Double) obj;
        } else {
            return null;
        }
    }

    public void setValue(String id, Object obj) {
        node.getAttributes().setValue(id, obj);
    }

    public void setValueAsFloat(String id, Float f) {
        if (f instanceof Float) {
            node.getAttributes().setValue(id, (Float) f);
        }
    }

    ////////// Model ////////////////
    public void setSleep() {
        node.getAttributes().setValue(Sleep, random.nextInt(MaxSleep - MinSleep + 1) + MinSleep);
    }

    public void setSleep(int sleep) {
        node.getAttributes().setValue(Sleep, sleep);
    }

    ////////// UI //////////    
    public void setLabel(String id) {
        node.getNodeData().setLabel(id);
    }

    private float getSize() {
        return node.getNodeData().getSize();
    }

    private void setSize(float newSize) {
        node.getNodeData().setSize(newSize);
    }

    public void resetSize() {
        setSize(DefaultNodeSize);
    }
    
    public void setStubbornSize()
    {
        setSize(DefaultNodeSize * StubbornSizeRatio);
    }
    
    public void setActiveSize()
    {
        setSize(DefaultNodeSize * ActiveNodeSizeRatio);
    }

    public void setColor(float r, float g, float b) {
        node.getNodeData().setColor(r, g, b);
    }
    
    public void setStubbornColor(float opinion)
    {                
        setColor(1-opinion, opinion, 128);
    }    
}
