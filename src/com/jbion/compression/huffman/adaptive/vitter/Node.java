package com.jbion.compression.huffman.adaptive.vitter;

public class Node {

    private Node leaderParent;
    private Node leaderRightChild;
    private int weight;
    
    public Node() {
        weight = 0;
    }

    void incrementWeight() {
        weight++;
    }
    
    int getWeight() {
        return weight;
    }

    public Node getLeaderParent() {
        return leaderParent;
    }

    public void setLeaderParent(Node leaderParent) {
        this.leaderParent = leaderParent;
    }

    public Node getLeaderRightChild() {
        return leaderRightChild;
    }

    public void setLeaderRightChild(Node leaderRightChild) {
        this.leaderRightChild = leaderRightChild;
    }

    boolean isZeroNode() {
        return false;
    }

    boolean isLeaf() {
        return false;
    }

    public void setZeroChild(Node node) {
        // TODO Auto-generated method stub
        
    }

    public void setRightChild(Node node) {
        // TODO Auto-generated method stub
        
    }
}
