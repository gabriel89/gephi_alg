package org.gephi.plugins.example.startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.importer.api.Container;
import org.gephi.io.importer.api.ImportController;
import org.gephi.io.importer.spi.FileImporter;
import org.gephi.io.processor.plugin.DefaultProcessor;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2;
import org.gephi.layout.plugin.forceAtlas2.ForceAtlas2Builder;
import org.gephi.partition.api.NodePartition;
import org.gephi.partition.impl.PartitionFactory;
import org.gephi.partition.plugin.NodeColorTransformer;
import org.gephi.preview.api.PreviewController;
import org.gephi.preview.api.PreviewModel;
import org.gephi.preview.api.PreviewProperties;
import org.gephi.preview.api.PreviewProperty;
import org.gephi.project.api.ProjectController;
import org.gephi.project.api.Workspace;
import org.gephi.project.api.WorkspaceListener;
import org.gephi.statistics.plugin.Modularity;
import org.gephi.utils.progress.Progress;
import org.gephi.utils.progress.ProgressTicket;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 * Global workspace listener example.
 *
 * @author Mathieu Bastian
 */
@ServiceProvider(service = WorkspaceListener.class)
public class WhenWorkspaceSelected implements WorkspaceListener {

    private ProgressTicket progress;

    @Override
    public void initialize(Workspace wrkspc) {
    }

    @Override
    public void select(Workspace wrkspc) {

//        Progress.start(progress, 10);
//        testExport2();
//        Progress.finish(progress);
    }

    @Override
    public void unselect(Workspace wrkspc) {
    }

    @Override
    public void close(Workspace wrkspc) {
    }

    @Override
    public void disable() {
    }   
}
