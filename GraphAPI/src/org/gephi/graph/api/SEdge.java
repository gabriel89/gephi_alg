package org.gephi.graph.api;

import java.util.Random;

/**
 * A social graph node.
 *
 * @author Alexandru Topirceanu
 */
public final class SEdge {
        
    public static final String Trust = "trust";        
    public static final String TAG_SOCIALIZED = "socialized";
        
    private Random random;
    
    // encapsulate a graph edge
    private Edge edge;
    
    public SEdge(Edge edge)
    {
        this.edge = edge;
        random = new Random();
    }

    public Edge getEdge() {
        return edge;
    }   
    
    // attributes column getters / setters
    
    ////////////////////////////////
    ///////////// SEdge ////////////
    ////////////////////////////////
    
    public Object getValue(String id) {
        return edge.getAttributes().getValue(id);
    }

    public String getValueAsString(String id) {
        Object obj = edge.getAttributes().getValue(id);
        if (obj != null && obj instanceof String) {
            return (String) obj;
        } else {
            return null;
        }
    }

    public Integer getValueAsInteger(String id) {
        Object obj = edge.getAttributes().getValue(id);
        if (obj != null && obj instanceof Integer) {
            return (Integer) obj;
        } else {
            return null;
        }
    }

    public Float getValueAsFloat(String id) {
        Object obj = edge.getAttributes().getValue(id);
        if (obj != null && obj instanceof Float) {
            return (Float) obj;
        } else {
            return null;
        }
    }      

    public void setValue(String id, Object obj) {
        edge.getAttributes().setValue(id, obj);
    }

    public void setValueAsFloat(String id, Float f) {
        if (f instanceof Float) {
            edge.getAttributes().setValue(id, (Float) f);
        }
    }        
     
    ////////// UI //////////    
    
    public void setLabel(String id)
    {
        edge.getEdgeData().setLabel(id);
    }

    public void setSize(float newSize) {
        edge.getEdgeData().setSize(newSize);
    }

    public void setColor(float r, float g, float b) {
        edge.getEdgeData().setColor(r, g, b);
    }                    
}
