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
        Path p = new Path(start,null,0);
        LinkedList<Path> activePaths = new LinkedList<Path>();
        activePaths.add(p);
        int counter = 0;
        int activePathsSize;
        boolean run = true;
        
        while(run) {
            
            counter++;
            
            /*MERGE step
            - Algorithm maitains a tree of paths.
            - The leaves of the tree are actually active paths in the graph.
            - The paths that are equivalent (they came to the same point in the graph) are merged.
            - A path stores its corresponding REDS in sequence attribute. When path are merged the corresponding REDS are also merged.
            */
            Path p1, p2;
            activePathsSize = activePaths.size();
            if(activePathsSize > 1) {
                for(int i=0;i<activePathsSize;i++) {
                    p1=activePaths.get(i);
                    if(i < activePathsSize-1) {
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
                            if(p1.parent == null)
                                System.out.println("STOP");
                            
                            //Check possible common suffix of merged paths (their REDS).
                            if(p1.sequence.contains(","))
                                p1.commonSuffix();
                                
                            p1.merged = false;
                        }
                    }
                    //The paths that are the only child of their parent were merged before and need to be removed from the tree (and from activePaths).
                    if(p1.parent != null && p1.childrenNum == 0 && p1.parent.childrenNum == 1 && p1.parent.expandedChildren == p1.parent.headNode.childrenNum) {
                        p1.parent.sequence = p1.parent.sequence + p1.sequence;
                        p1.parent.setHeadNode(p1.headNode);
                        p1.parent.childrenNum--;
                        activePaths.add(p1.parent);
                        p1.remove = true;                        
                    }
                }
                
                //Remove all marked paths from activePaths.
                for(int i=0;i<activePaths.size();i++) {
                    if(activePaths.get(i).remove) {
                        activePaths.get(i).remove = false;
                        
                        //Stop main while cycle when there is only one path in activePaths.
                        if(activePaths.size() == 1) {
                            p = activePaths.get(i);
                            run = false;
                        }
                        
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
                
                boolean pPassive = true;
                for(int j=0;j<n.children.size();j++) {
                    if(n.children.get(j).processed < n.children.get(j).parentNum) { //Child was not processed yet (cycles in GFA)
                        //Child was not expanded yet and all its parents were reached.
                        if(!p.childrenExpanded[j] && n.children.get(j).parentNum == n.children.get(j).parentCounter || n.children.get(j).reachAttemptsCounter > 10)
                        {
                            //New active path for new child when more than one child. 
                            if(n.childrenNum > 1) {
                                Path newPath = new Path(n.children.get(j),p,p.depth+1);
                                activePaths.add(newPath);
                                p.childrenExpanded[j] = true;
                                p.expandedChildren++;
                            }
                            else {
                                p.setHeadNode(n.children.get(0));
                                p.sequence = p.sequence + n.children.get(0).sequence;
                            }
                            n.children.get(j).processed++;
                            pPassive = false;                            
                        }
                        else
                            n.children.get(j).reachAttemptsCounter++;
                    }
                    else { //Child was already processed before.
                        if(!p.childrenExpanded[j]) {
                            p.childrenExpanded[j] = true;
                            p.expandedChildren++;
                        }
                    }
                }
                
                //Remove paths that already expanded all their children.
                if(p.expandedChildren == n.childrenNum && n.childrenNum > 1)
                    p.remove = true;
                
                //Remove passive Paths, i.e. paths that did not expanded any child 100-times.
                if(pPassive)
                    p.passive++;
                else
                    p.passive=0;
                
                if(p.passive > 100)
                    p.remove = true;             
            }
            //3. Reset the parent counters;
            for(int i=0;i<activePaths.size();i++) {
                Node n = activePaths.get(i).headNode;
                for(int j=0;j<n.children.size();j++)
                    n.children.get(j).parentCounter = 0;
            }
            
            //Remove all marked paths from activePaths.
            for(int i=0;i<activePaths.size();i++) {
                if(activePaths.get(i).remove) {
                    activePaths.get(i).remove = false;
                    
                    //Stop main while cycle when there is only one path in activePaths.
                    if(activePaths.size() == 1) {
                            p = activePaths.get(i);
                            run = false;
                    }
                    
                    activePaths.remove(i);
                    i--;
                }
            }
            
            if(activePaths.size() == 1 && activePaths.get(0).headNode.children.size() == 0)
                break;
            
            System.gc();
        }      
        
        //Traverse bottom-up the path-tree and concatenate the corresponding REDS string.
        while(p.parent != null) {
            p.parent.sequence += p.sequence;
            p = p.parent;
        }
        
        return p;
    } 
    
    public static Node parseFileToGraph(String filename, HashMap<String,Node> nodes, String[] path, int minS, int maxS) {
        BufferedReader reader;
        StringTokenizer st;
        Node start = null;
        int sCounter = 1;
        
        try {
            reader = new BufferedReader(new FileReader(filename));
            String line = reader.readLine();
            while(line != null) {
                String[] tokens = line.split("\t");
                
                switch(tokens[0].charAt(0)) {
                    
                    case 'S':
                        if(minS <= sCounter && sCounter <= maxS) {
                            Node n = new Node(tokens[1],tokens[2]);
                            nodes.put(tokens[1], n);

                            if(nodes.size() == 1)
                                start = n;
                        }
                        
                        sCounter++;
                        break;
                    
                    case 'P':
                        
                        path = tokens[2].split(",");                        
                        break;
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
        //start = nodes.get(path[0]);        //Use for gfa containing one path
        
        return start;
    }
    
    public static void writeREDS(String filename, Path p) {        
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(filename));
            bw.write(p.sequence);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        int minS = new Integer(args[1]);
        int blockLength = new Integer(args[2]);
        int maxS = minS + blockLength;
                
        while(true)
        {
            HashMap<String,Node> nodes = new HashMap<String,Node>();
            String[] path = null;
            Node start;
            
            System.out.printf("Run: file = %s, minS = %d, maxS = %d\n", args[0], minS, maxS);
            
            start = parseFileToGraph(args[0], nodes, path, minS, maxS);
            
            if(nodes.size() == 0)
                break;
            
            Path reds = processPaths(nodes, start);
            String outputFileName = args[0] + "_" + minS + "_" + maxS + ".reds";
            writeREDS(outputFileName, reds);
            
            minS += blockLength;
            maxS += blockLength;
        }
    }        
}