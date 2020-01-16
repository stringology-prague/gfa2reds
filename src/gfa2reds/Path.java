/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gfa2reds;

import java.util.*;
import java.util.HashMap;
/**
 *
 * @author prochazkap
 */
public class Path {
    public String sequence;
    public Node headNode;
    public Path parent;
    public boolean remove;
    public int childrenNum;
    public boolean merged;
    public boolean childrenExpanded[];    
    public int expandedChildren;
    
    public Path(Node n, Path p) {
        setHeadNode(n);
        this.sequence = n.sequence;
        this.childrenExpanded = new boolean[this.headNode.childrenNum];
        this.expandedChildren = 0;
        
        this.parent = p;
        if(parent != null)
            this.parent.childrenNum++;
        this.childrenNum = 0;
        this.remove = false;        
    }
    
    public void merge(Path p) {
        this.sequence = this.sequence + "," + p.sequence;
        merged = true;
    }
    
    public void setHeadNode(Node n) {
        this.headNode = n;
        this.childrenExpanded = new boolean[n.childrenNum];
        Arrays.fill(this.childrenExpanded, Boolean.FALSE);
        this.expandedChildren = 0;
    }
}