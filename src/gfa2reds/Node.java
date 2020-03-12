/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gfa2reds;

import java.util.*;
/**
 *
 * @author prochazkap
 */
public class Node {
    public String id;
    public String sequence;
    public LinkedList<Node> children;
    public LinkedList<Node> parents;
    public int parentNum;
    public int childrenNum;
    public int parentCounter;
    public int reachAttemptsCounter;
    public int processed;
            
    public Node(String id, String seq) {
        this.id = id;
        this.sequence = seq;
        this.children = new LinkedList<Node>();
        this.parents = new LinkedList<Node>();
        this.parentNum = 0;
        this.childrenNum = 0;
        this.parentCounter = 0;
        this.reachAttemptsCounter = 0;
        this.processed = 0;
    }
    
    public void addChild(Node n) {
        this.children.add(n);
        this.childrenNum++;
    }
    
    public void addParent(Node n) {
        this.parents.add(n);
        this.parentNum++;
    }
    
    public boolean isPathEqual(Node n) {
        if(n.childrenNum != this.childrenNum)
            return false;
        
        for(int i=0;i<this.children.size();i++)
           if(this.children.get(i).id.compareTo(n.children.get(i).id) != 0)
               return false;
        
        return true;
    }
}