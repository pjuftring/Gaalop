package de.gaalop.tba.cfgImport.optimization.maxima;

import de.gaalop.api.cfg.AssignmentNodeCollector;
import de.gaalop.cfg.AssignmentNode;
import de.gaalop.cfg.ControlFlowGraph;
import de.gaalop.cfg.EmptyControlFlowVisitor;
import de.gaalop.cfg.FindStoreOutputNodes;
import de.gaalop.cfg.StoreResultNode;
import de.gaalop.dfg.Expression;
import de.gaalop.tba.cfgImport.optimization.maxima.parser.MaximaLexer;
import de.gaalop.tba.cfgImport.optimization.maxima.parser.MaximaParser;
import de.gaalop.tba.cfgImport.optimization.maxima.parser.MaximaTransformer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.CommonTreeNodeStream;

/**
 * Defines a facade class for transforming a graph with maxima
 * @author christian
 */
public class MaximaOptimizer {

    private MaximaConnection connection;

    public MaximaOptimizer(MaximaConnection connection) {
        this.connection = connection;
    }

    public void transformGraph(ControlFlowGraph graph) throws RecognitionException {
        MaximaInput input = new MaximaInput();
        input.add("display2d:false;"); // very important!
        fillMaximaInput(graph,input);
        input.add("quit();"); // very important!

        MaximaOutput output = connection.optimizeWithMaxima(input);

        //connect in and output
        LinkedList<MaximaInOut> connected = new LinkedList<MaximaInOut>();
        groupMaximaInAndOutputs(connected, output);

        connected.removeFirst(); // remove batch
        connected.removeFirst(); // remove display2d
        connected.removeLast(); // remove quit()

        ListIterator<AssignmentNode> listIterator = assignmentNodeCollector.getAssignmentNodes().listIterator();
        for (MaximaInOut io: connected) {
            Expression exp = getExpressionFromMaximaOutput(io.getOutput());
            listIterator.next().setValue(exp);
        }

    }

    public static Expression getExpressionFromMaximaOutput(String maximaOut) throws RecognitionException {
        ANTLRStringStream inputStream = new ANTLRStringStream(maximaOut);
        MaximaLexer lexer = new MaximaLexer(inputStream);
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        MaximaParser parser = new MaximaParser(tokenStream);
        MaximaParser.program_return parserResult = parser.program();

        CommonTreeNodeStream treeNodeStream = new CommonTreeNodeStream(parserResult.getTree());
        MaximaTransformer transformer = new MaximaTransformer(treeNodeStream);

        return transformer.expression();
    }

    private void groupMaximaInAndOutputs(LinkedList<MaximaInOut> connected, MaximaOutput output) {

        MaximaInOut curIO = new MaximaInOut(null, null);

        boolean lastWasInput = false;

        int curInput = -1;
        READIN:
        for (String o: output) {
             if (o.startsWith("(%i")) {
                 int indexRBracket = o.indexOf(')');
                 curIO = new MaximaInOut(o.substring(indexRBracket+1).trim(),null);
                 connected.add(curIO);
                 curInput = Integer.parseInt(o.substring(3, indexRBracket));
                 lastWasInput = true;
            } else
                 if (o.startsWith("(%o")) {
                     int indexRBracket = o.indexOf(')');
                     if (curInput == Integer.parseInt(o.substring(3, indexRBracket)))
                        connected.getLast().setOutput(o.substring(indexRBracket+1).trim());
                     else {
                         //ups.
                         if (Integer.parseInt(o.substring(3, indexRBracket)) < curInput) {
                             System.err.println("Error in associating maxima input to output: "+o+" expected: "+curInput+" actual: "+Integer.parseInt(o.substring(3, indexRBracket)));
                         }
                         break READIN;
                     }
                     lastWasInput = false;
                } else {
                    if (lastWasInput) {
                        curIO.setInput(curIO.getInput()+o.trim());
                    } else {
                        curIO.setOutput(curIO.getOutput()+o.trim());
                    }
                }
        }
    }

    private AssignmentNodeCollector assignmentNodeCollector;

    private void fillMaximaInput(ControlFlowGraph graph, MaximaInput input) {
        assignmentNodeCollector = new AssignmentNodeCollector();
        graph.accept(assignmentNodeCollector);

        FindStoreOutputNodes f = new FindStoreOutputNodes();

        HashSet<String> storeNodeVariables = new HashSet<String>();

        for (StoreResultNode curNode: f.getNodes())
            storeNodeVariables.add(curNode.getValue().getName());
        
        for (AssignmentNode node: assignmentNodeCollector.getAssignmentNodes()) {
            //use store result nodes for marking to evaluate immediately
            DFGToMaximaCode dfg = new DFGToMaximaCode();
            node.getVariable().accept(dfg);
            String variable = "";
            
            if (!storeNodeVariables.contains(node.getVariable().getName())) {
                variable = dfg.getResultString()+"::";
            }

            dfg = new DFGToMaximaCode();
            node.getValue().accept(dfg);
            String value = dfg.getResultString()+";";

            

            input.add(variable+value);
        }

    }

    

}