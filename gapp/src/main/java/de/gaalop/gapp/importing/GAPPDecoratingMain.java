package de.gaalop.gapp.importing;

import de.gaalop.OptimizationException;
import de.gaalop.cfg.ControlFlowGraph;
import de.gaalop.tba.Plugin;
import de.gaalop.tba.UseAlgebra;
import de.gaalop.tba.cfgImport.BaseVectorChecker;
import de.gaalop.tba.cfgImport.CFGImporterFacade;

/**
 * Facade class to decorate the ControlFlowGraph in a GAPP ControlFlowGraph
 * @author Christian Steinmetz
 */
public class GAPPDecoratingMain {

    /**
     * Decorates a given ControlFlowGraph in a GAPP ControlFlowGraph
     * @param graph The ControlFlowGraph
     * @return The same graph object (which is now decorated with GAPP instructions)
     * @throws OptimizationException
     */
    public ControlFlowGraph decorateGraph(UseAlgebra usedAlgebra, ControlFlowGraph graph) throws OptimizationException {

        if (!usedAlgebra.isN3()) {
            BaseVectorChecker checker = new BaseVectorChecker(usedAlgebra.getAlgebra().getBase());
            graph.accept(checker);
        }

        Plugin plugin = new Plugin();
        plugin.setOptInserting(false);
        CFGImporterFacade facade = new CFGImporterFacade(plugin);
        facade.importGraph(graph);

        // import now the graph in GAPP
        GAPPDecorator vCFG = new GAPPDecorator();
        graph.accept(vCFG);

        return graph;
    }

}
