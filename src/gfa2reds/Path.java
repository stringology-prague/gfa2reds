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
    public int passive;
    public int depth;
    
    public Path(Node n, Path p, int d) {
        setHeadNode(n);
        this.sequence = n.sequence;
        this.childrenExpanded = new boolean[this.headNode.childrenNum];
        this.expandedChildren = 0;
        
        this.parent = p;
        if(parent != null)
            this.parent.childrenNum++;
        this.childrenNum = 0;
        this.remove = false; 
        this.passive = 0;
        this.depth = d;
    }
    
    public void merge(Path p) {
        
        if(this.sequence.compareTo(p.sequence) != 0)
            this.sequence = this.sequence + "," + p.sequence;
        
        merged = true;
    }
    
    public void setHeadNode(Node n) {
        this.headNode = n;
        this.childrenExpanded = new boolean[n.childrenNum];
        Arrays.fill(this.childrenExpanded, Boolean.FALSE);
        this.expandedChildren = 0;
    }
    
    public void commonSuffix() {
        String[] elements = this.sequence.split(",");
        int suffLen = 0;
        char c;
        
        boolean common = true;
        while(common) {
            if(suffLen == elements[0].length())
                break;
            else
                c = elements[0].charAt(elements[0].length()-suffLen-1);
            
            for(int i=1; i<elements.length; i++) {
                if(suffLen == elements[i].length() || c != elements[i].charAt(elements[i].length()-suffLen-1)) {
                    common = false;
                    break;
                }
            }
            if(common)
                suffLen++;
        }
                
        if(suffLen > 0) {
            this.sequence = "{";
            String commonSuff = elements[0].substring(elements[0].length()-suffLen,elements[0].length());
            
            for(int i=0; i<elements.length; i++) {
                this.sequence += elements[i].substring(0,elements[i].length()-suffLen);
                if(i != elements.length-1)
                    this.sequence += ",";
            }
            
            this.sequence += "}" + commonSuff;
        }
        else
            this.sequence = "{" + this.sequence + "}";
    }
}