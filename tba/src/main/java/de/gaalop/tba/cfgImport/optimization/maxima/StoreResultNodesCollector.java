package de.gaalop.tba.cfgImport.optimization.maxima;

import de.gaalop.cfg.EmptyControlFlowVisitor;
import de.gaalop.cfg.StoreResultNode;
import de.gaalop.dfg.Variable;
import java.util.LinkedList;

/**
 * Collects all StoreResultNodes in a Control Flow Graph
 * @author Christian Steinmetz
 */
public class StoreResultNodesCollector extends EmptyControlFlowVisitor {

    private LinkedList<Variable> variables;

    public StoreResultNodesCollector() {
        variables = new LinkedList<Variable>();
    }

    public LinkedList<Variable> getVariables() {
        return variables;
    }

    @Override
    public void visit(StoreResultNode node) {
        variables.add(node.getValue());
        super.visit(node);
    }

    public boolean containsStoreResultVariableName(String name) {
        for (Variable var: variables)
            if (var.getName().equals(name))
                return true;

        return false;
    }

}
