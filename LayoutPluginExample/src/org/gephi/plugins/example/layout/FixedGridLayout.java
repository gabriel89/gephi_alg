package org.gephi.plugins.example.layout;

import java.util.ArrayList;
import java.util.List;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphModel;
import org.gephi.graph.api.Node;
import org.gephi.layout.spi.Layout;
import org.gephi.layout.spi.LayoutBuilder;
import org.gephi.layout.spi.LayoutProperty;

/**
 * Example of a layout algorithm which places all nodes in a fixed size grid.
 *
 * @author Alexandru Topirceanu
 */
public class FixedGridLayout implements Layout {

    //Architecture
    private final LayoutBuilder builder;
    private GraphModel graphModel;
    //Flags
    private boolean executing = false;
    //Properties
    private int areaSize;
    private float speed;

    public FixedGridLayout(FixedGridLayoutBuilder builder) {
        this.builder = builder;
    }

    @Override
    public void resetPropertiesValues() {
        areaSize = 1000;
        speed = 1f;
    }

    @Override
    public void initAlgo() {
        executing = true;
    }

    @Override
    public void goAlgo() {
        Graph graph = graphModel.getGraphVisible();
        graph.readLock();

        Node[] nodes = graph.getNodes().toArray();
        int w = 8, h = 6, n = nodes.length;
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w && (i * w + j) < n; j++) {
                nodes[w * i + j].getNodeData().setX(-areaSize / 2f + 1f * j / (w - 1) * areaSize);
                nodes[w * i + j].getNodeData().setY(-areaSize / 2f + 1f * i / (h - 1) * areaSize);//              
            }
        }
        nodes = null;

        graph.readUnlock();
    }

    @Override
    public void endAlgo() {
        executing = false;
    }

    @Override
    public boolean canAlgo() {
        return executing;
    }

    @Override
    public LayoutProperty[] getProperties() {
        List<LayoutProperty> properties = new ArrayList<LayoutProperty>();
        final String GRIDLAYOUT = "Fixed Grid Layout";

        try {
            properties.add(LayoutProperty.createProperty(
                    this, Integer.class,
                    "Area size",
                    GRIDLAYOUT,
                    "The area size",
                    "getAreaSize", "setAreaSize"));
            properties.add(LayoutProperty.createProperty(
                    this, Float.class,
                    "Speed",
                    GRIDLAYOUT,
                    "How fast are moving nodes",
                    "getSpeed", "setSpeed"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return properties.toArray(new LayoutProperty[0]);
    }

    @Override
    public LayoutBuilder getBuilder() {
        return builder;
    }

    @Override
    public void setGraphModel(GraphModel gm) {
        this.graphModel = gm;
    }

    public Float getSpeed() {
        return speed;
    }

    public void setSpeed(Float speed) {
        this.speed = speed;
    }

    public Integer getAreaSize() {
        return areaSize;
    }

    public void setAreaSize(Integer area) {
        this.areaSize = area;
    }
}
