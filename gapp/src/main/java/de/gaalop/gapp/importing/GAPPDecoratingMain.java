package de.gaalop.gapp.importing;

import de.gaalop.OptimizationException;
import de.gaalop.cfg.ControlFlowGraph;
import de.gaalop.dfg.MultivectorComponent;
import de.gaalop.dfg.Variable;
import de.gaalop.gapp.GAPP;
import de.gaalop.gapp.Selector;
import de.gaalop.gapp.Selectorset;
import de.gaalop.gapp.Variableset;
import de.gaalop.gapp.instructionSet.GAPPAssignMv;
import de.gaalop.gapp.instructionSet.GAPPResetMv;
import de.gaalop.gapp.variables.GAPPMultivector;
import de.gaalop.gapp.variables.GAPPScalarVariable;
import de.gaalop.tba.Plugin;
import de.gaalop.tba.UseAlgebra;
import de.gaalop.tba.cfgImport.BaseVectorChecker;
import de.gaalop.tba.cfgImport.CFGImporterFacade;
import java.util.HashMap;
import java.util.LinkedList;

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
        facade.setUsedAlgebra(usedAlgebra);
        facade.importGraph(graph);

        GAPP gappStart = new GAPP();
        assignInputVariables(graph, gappStart);

        // import now the graph in GAPP
        GAPPDecorator vCFG = new GAPPDecorator(gappStart);
        graph.accept(vCFG);

        return graph;
    }

    private void assignInputVariables(ControlFlowGraph graph, GAPP gappStart) {
        LinkedList<Variable> toDo = new LinkedList<Variable>(graph.getInputVariables());

        GAPPMultivector inputsMv = new GAPPMultivector("inputsMv");
        gappStart.addInstruction(new GAPPResetMv(inputsMv));

        HashMap<Variable,MultivectorComponent> map = new HashMap<Variable, MultivectorComponent>();

        Selectorset selSet = new Selectorset();
        Variableset varSet = new Variableset();
        int slotNo = 0;
        for (Variable var: toDo) {
            selSet.add(new Selector(slotNo, (byte) 1));
            varSet.add(new GAPPScalarVariable(var.getName()));
            map.put(var, new MultivectorComponent("inputsMv", slotNo));
            slotNo++;
        }
   
        gappStart.addInstruction(new GAPPAssignMv(inputsMv, selSet, varSet));

        
        while (!toDo.isEmpty()) {
            Variable curVar = toDo.removeFirst();
            ReplaceVisitor replaceVisitor = new ReplaceVisitor(curVar, map.get(curVar));
            graph.accept(replaceVisitor);
        }
    }





}
