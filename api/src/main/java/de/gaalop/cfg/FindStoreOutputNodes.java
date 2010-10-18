package de.gaalop.cfg;

import java.util.HashSet;
import java.util.Set;

/**
 * This visitor collects all nodes that store a result.
 */
public class FindStoreOutputNodes implements ControlFlowVisitor {

  private Set<StoreResultNode> nodes = new HashSet<StoreResultNode>();

  public Set<StoreResultNode> getNodes() {
    return nodes;
  }

  @Override
  public void visit(StartNode node) {
    node.getSuccessor().accept(this);
  }

  @Override
  public void visit(AssignmentNode node) {
    node.getSuccessor().accept(this);
  }

  @Override
  public void visit(StoreResultNode node) {
    nodes.add(node);
    node.getSuccessor().accept(this);
  }

  @Override
  public void visit(EndNode node) {
  }

  @Override
  public void visit(IfThenElseNode node) {
    node.getPositive().accept(this);
    node.getNegative().accept(this);
    node.getSuccessor().accept(this);
  }
  
  @Override
  public void visit(BlockEndNode node) {
    // nothing to do
  }
}