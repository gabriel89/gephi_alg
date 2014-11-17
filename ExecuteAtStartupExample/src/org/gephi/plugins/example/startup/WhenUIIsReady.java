package org.gephi.plugins.example.startup;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.desktop.project.api.ProjectControllerUI;
import org.gephi.graph.api.Graph;
import org.gephi.graph.api.GraphController;
import org.gephi.graph.api.GraphModel;
import org.gephi.io.exporter.preview.PNGExporter;
import org.gephi.io.generator.plugin.CellularGraph;
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
import org.gephi.statistics.plugin.Modularity;
import org.gephi.utils.progress.Progress;
import org.openide.modules.ModuleInstall;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;
import org.gephi.utils.progress.ProgressTicket;

/**
 * Simple 'installer' class called at startup. <p> It uses the
 * <code>invokeWhenUIReady()</code> method, which is called when the UI is
 * loaded so any UI related thing like a login dialog would work. <p> Make sure
 * to register this class in the 'manifest.mf' file in the module. For instance
 * in this manifest we have:
 * <pre>
 * OpenIDE-Module-Install: org/gephi/plugins/example/startup/WhenUIIsReady.class
 * </pre>
 *
 * @author Mathieu Bastian
 */
public class WhenUIIsReady extends ModuleInstall {

    private ProgressTicket progress;

    @Override
    public void restored() {
        WindowManager.getDefault().invokeWhenUIReady(new Runnable() {
            public void run() {
                //Do something
                System.out.println("WhenUIIsReady called");

                Progress.start(progress, 10);
                //processGMLtoPNG();
                Progress.finish(progress);
            }
        });
    }

    /**
     * Reads apnee.gml from the data folder of this plugin, applies force-atlas2
     * layout, runs modularity and colors nodes, then exports the png of the
     * graph as apnee_timestamp.png
     */
    public void processGMLtoPNG() {
        // settings
        String dirSource = "data\\";
        String dirDest = "C:\\Users\\Alexander\\Documents\\";
        String file = "apnee";
        String inExtension = ".gml";
        String outEtension = ".png";

        // get workspace
        ProjectController projectController = Lookup.getDefault().lookup(ProjectController.class);
        ProjectControllerUI projectControllerUI = Lookup.getDefault().lookup(ProjectControllerUI.class);
        if (projectController.getCurrentProject() == null) {
            projectControllerUI.newProject();
        }
        Workspace workspace = projectController.getCurrentWorkspace();

//        ProjectController pc = Lookup.getDefault().lookup(ProjectController.class);
//        pc.newProject();        
//        Workspace workspace = pc.getCurrentWorkspace();

        // import sample file
        String sample = dirSource + file + inExtension;
        final InputStream stream;
        try {
            stream = new FileInputStream(new File(sample));
            //stream.reset();

            ImportController importController = Lookup.getDefault().lookup(ImportController.class);
            FileImporter fileImporter = importController.getFileImporter(inExtension);
            Container container = importController.importFile(stream, fileImporter);
            importController.process(container, new DefaultProcessor(), workspace);
            Progress.progress(progress, 2);
            //WhenUIIsReady.class.getResourceAsStream(sample);
        } catch (Exception e) {
            return;
        }


        // Get current graph
        GraphController graphController = Lookup.getDefault().lookup(GraphController.class);
        GraphModel graphModel = graphController.getModel();
        Graph graph = graphModel.getGraph();

        // run forceatlas2
        ForceAtlas2Builder builder = new ForceAtlas2Builder();
        ForceAtlas2 atlas2 = builder.buildLayout();
        atlas2.setGraphModel(graphModel);
        atlas2.setAdjustSizes(true);
        atlas2.setOutboundAttractionDistribution(true);

        atlas2.initAlgo();
        for (int i = 0; i < 200; ++i) {
            if (atlas2.canAlgo()) {
                atlas2.goAlgo();
            }
        }
        atlas2.endAlgo();
        Progress.progress(progress, 6);

        // run modularity 
        Modularity modularity = new Modularity();
        modularity.setRandom(true);
        modularity.setUseWeight(false);
        modularity.setResolution(1.0);
        modularity.setProgressTicket(progress);
        AttributeModel attributeModel = Lookup.getDefault().lookup(AttributeController.class).getModel();
        modularity.execute(graphModel, attributeModel);
        modularity.getModularity();

        // color: partition by modularity class
        AttributeTable nodeTable = attributeModel.getNodeTable();
        AttributeTable graphTable = attributeModel.getGraphTable();
        AttributeColumn atrColumn = nodeTable.getColumn(Modularity.MODULARITY_CLASS);
        NodeColorTransformer transformer = new NodeColorTransformer();
        NodePartition partition = PartitionFactory.createNodePartition(atrColumn);
        PartitionFactory.buildNodePartition(partition, graph);
        transformer.randomizeColors(partition);
        transformer.transform(partition);
        Progress.progress(progress, 8);

        // create preview
        PreviewModel model = Lookup.getDefault().lookup(PreviewController.class).getModel();

        PreviewProperties props = model.getProperties();
        props.putValue(PreviewProperty.SHOW_NODE_LABELS, false);
        props.putValue(PreviewProperty.EDGE_CURVED, false);
        props.putValue(PreviewProperty.EDGE_OPACITY, 15f);
        props.putValue(PreviewProperty.EDGE_THICKNESS, 0.5);

        // export as png                        
        PNGExporter pngExporter = new PNGExporter();
        pngExporter.setWorkspace(workspace);
        try {
            File filePNG = new File(dirDest + file + "_" + new Date().getTime() + outEtension);
            System.out.println(filePNG.getAbsolutePath());
            FileOutputStream fos2 = new FileOutputStream(filePNG);
            pngExporter.setOutputStream(fos2);
            pngExporter.execute();
        } catch (Exception ex) {
            ex.printStackTrace();
            Exceptions.printStackTrace(ex);
        }
        Progress.progress(progress, 10);
    }
}
