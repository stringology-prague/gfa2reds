/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gfa2reds;

import java.io.*;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.util.LinkedList;

/**
 *
 * @author prochazkap
 */
public class Gfa2reds {
    
    public static Path processPaths(HashMap<String,Node> nodes, Node start) {
        Path p = new Path(start,null);
        LinkedList<Path> activePaths = new LinkedList<Path>();
        activePaths.add(p);
        int counter = 0;
        int activePathsSize;
        
        while(true) {
            
            String id1;
            id1 = activePaths.get(0).headNode.id;
            
            if(counter % 10 == 0)
                System.out.println("Iteration: " + counter);
            
            if(activePaths.size() > 3)
                System.out.println("Queue size: " + activePaths.size());
            
            counter++;
            
            
            /*MERGE step
            - Algorithm maitains a tree of paths.
            - The leaves of the tree are actually active paths in the graph.
            - The paths that are equivalent (they came to the same point in the graph) are merged.
            - A path stores its corresponding REDS in sequence attribute. When path are merged the corresponding REDS are also merged.....
            */
            Path p1, p2;
            activePathsSize = activePaths.size();
            if(activePathsSize > 1) {
                for(int i=0;i<activePathsSize-1;i++) {
                    p1=activePaths.get(i);
                    for(int j=i+1;j<activePathsSize;j++) {
                        p2 = activePaths.get(j);
                        if(!p2.remove && p1.headNode.isPathEqual(p2.headNode)) {
                            p1.merge(p2);
                            p2.remove = true;
                            if(p2.parent != null)
                                p2.parent.childrenNum--;
                        }                      
                    }
                    if(p1.merged) {
                        p1.sequence = "{" + p1.sequence + "}";
                        p1.merged = false;
                    }
                    
                    //The paths that are the only child of their parent were merged before and need to be removed from the tree (and from activePathts).
                    if(p1.parent != null && p1.parent.childrenNum == 1) {
                        p1.parent.sequence = p1.parent.sequence + p1.sequence;
                        p1.parent.setHeadNode(p1.headNode);
                        p1.parent.childrenNum--;
                        activePaths.add(p1.parent);
                        p1.remove = true;                        
                    }
                }
                
                for(int i=0;i<activePaths.size();i++) {
                    if(activePaths.get(i).remove) {
                        activePaths.get(i).remove = false;
                        activePaths.remove(i);            
                        i--;
                    }
                }
            }
            
            //MOVE step
            
            //1. Set reachable children from their parents.
            for(int i=0;i<activePaths.size();i++) {
                Node n = activePaths.get(i).headNode;
                for(int j=0;j<n.children.size();j++)
                    n.children.get(j).parentCounter++;
            }
            //2. Expand reachable children nodes (i.e. nodes whose all parents were already reached).
            activePathsSize = activePaths.size();
            for(int i=0;i<activePathsSize;i++) {
                p = activePaths.get(i);
                Node n = p.headNode;
                
                if(p.childrenExpanded.length != n.children.size())
                    System.out.println("ERROR!");
                
                for(int j=0;j<n.children.size();j++) {
                    //Child was not expanded yet and all its parents were reached.
                    if(!p.childrenExpanded[j] && n.children.get(j).parentNum == n.children.get(j).parentCounter || n.children.get(j).reachAttemptsCounter > 10)
                    {
                        //New active path for new child when more than one child. 
                        if(n.childrenNum > 1) {
                            Path newPath = new Path(n.children.get(j),p);
                            activePaths.add(newPath);
                            p.childrenExpanded[j] = true;
                            p.expandedChildren++;
                        }
                        else {
                            p.setHeadNode(n.children.get(0));
                            p.sequence = p.sequence + n.children.get(0).sequence;
                        }
                    }
                    else
                        n.children.get(j).reachAttemptsCounter++;
                }
                if(p.expandedChildren == n.childrenNum && n.childrenNum > 1)
                    p.remove = true;
            }
            //3. Reset the parent counters;
            for(int i=0;i<activePaths.size();i++) {
                Node n = activePaths.get(i).headNode;
                for(int j=0;j<n.children.size();j++)
                    n.children.get(j).parentCounter = 0;
            }
            
            for(int i=0;i<activePaths.size();i++) {
                if(activePaths.get(i).remove) {
                    activePaths.get(i).remove = false;
                    activePaths.remove(i);
                    i--;
                }
            }
            
            if(activePaths.size() == 1 && activePaths.get(0).headNode.children.size() == 0)
                break;
            
            System.gc();
        }      
        
        return p;
    } 
    
    public static Node parseFileToGraph(String filename, HashMap<String,Node> nodes) {
        BufferedReader reader;
        StringTokenizer st;
        Node start = null;
        
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while(line != null) {
                String[] tokens = line.split("\t");
                
                if(tokens[0].charAt(0) == 'S') {
                    Node n = new Node(tokens[1],tokens[2]);
                    nodes.put(tokens[1], n);
                    
                    if(nodes.size() == 1)
                        start = n;
                }             
                                
                line = reader.readLine();
            }
            
            reader.close();
            reader = new BufferedReader(new FileReader(filename));
            line = reader.readLine();
            while(line != null) {
                String[] tokens = line.split("\t");
                                
                if(tokens[0].charAt(0) == 'L') {
                    if(nodes.containsKey(tokens[1]) && nodes.containsKey(tokens[3]) && (tokens[1].compareTo(tokens[3]) != 0))
                    {
                        nodes.get(tokens[1]).addChild(nodes.get(tokens[3]));
                        nodes.get(tokens[3]).addParent(nodes.get(tokens[1]));
                    }
                }
                
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return start;
    }
    
    /*TODO
    1. Jak odstranit cykly? Které?
    2. Jak zpracovat CIGAR strings. Pro Human Pangenome jsou nenulové...
    
    */
    public static void removeCycles(HashMap<String,Node> nodes, Node start) {
        LinkedList<Node> l1;
        LinkedList<Node> l2 = new LinkedList<Node>();
                
        l2.add(start);
        
        while(true) {
            l1 = l2;
            l2 = new LinkedList<Node>();
            
            for(int i=0;i<l2.size();i++) {
                Node n1 = l2.get(i);
                n1.bfs_visited = true;
                
                for(int j=0;j<n1.children.size();j++) {
                    Node n2 = n1.children.get(j);
                    if(n2.bfs_visited) {
                        n1.children.remove(j); 
                        j--;
                    }
                }
            }
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        HashMap<String,Node> nodes = new HashMap<String,Node>();
        Node start;
        start = parseFileToGraph("./yeast.gfa", nodes);
        
        Path reds = processPaths(nodes, start);
    }
}