package gloria.gloriaOptimizer.graph.PruningEngine;


import gloria.gloriaOptimizer.graph.OptimizerType;
import gloria.gloriaOptimizer.graph.pool.Pool;

import gloria.gloriaOptimizer.graph.pool.Pool_MultiPred;
import gloria.gloriaOptimizer.graph.node.Node;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;


public class PruneEngine {

    public static Pool SEQPrune(Pool S_pool){


        if ((!S_pool.isEndPool())&&S_pool.isPrunable()&&S_pool.getType()== OptimizerType.NORMAL){
            transitionPrune_SEQ(S_pool);
            nodePrune_SEQ(S_pool);
        }


        return S_pool;
    }

    public static Pool KleenePrune(Pool K_pool){
        if (!K_pool.isEndPool()&&K_pool.isPrunable()&&K_pool.getType()== OptimizerType.NORMAL){
            transitionPrune_Kleene(K_pool);
            nodePrune_Kleene(K_pool);
        }
        return K_pool;
    }

    public static Pool transitionPrune_Kleene(Pool K_pool){
        if (K_pool instanceof Pool_MultiPred){
            return K_pool;
        }

        HashMap<String, Node> newNodeHash = new HashMap<>();
        for (Node node: K_pool.getNodeHash().values()){
            node.searchMIT_CostExpr();
            newNodeHash.put(node.toString(), node);
        }
        K_pool.setNodeHash(newNodeHash);
        return K_pool;
    }



    public static Pool transitionPrune_SEQ(Pool pool){
        if (pool instanceof Pool_MultiPred){
            return pool;
        }

        HashMap<String, Node> newNodeHash = new HashMap<>();
        for (Node node: pool.getNodeHash().values()){
            node.searchMIT_CostValue();
            newNodeHash.put(node.toString(), node);
        }
        pool.setNodeHash(newNodeHash);
        return pool;
    }

    public static Pool nodePrune_Kleene(Pool pool){

        //sort nodes by partitions
        HashMap<String, ArrayList<Node>> partitionToNodes = sortNodesByPartition(pool);

        for (ArrayList<Node> nodesOfP: partitionToNodes.values()){
            nodePruneForSamePartition_Kleene(nodesOfP);

            for (Node n: nodesOfP){
                n.setExistingCost(n.getExistingCostExpr().getADouble());
                pool.getNodeHash().put(n.toString(), n);
            }
        }

        return pool;
    }

    /**
     * for the same s-set partition, if one node carries more snapshots than the other,
     * it's pruned
     * *
     * @param pool the input cluster
     * @return the output cluster
     */
    public static Pool nodePrune_SEQ(Pool pool){

        //sort nodes by partitions
       HashMap<String, ArrayList<Node>> partitionToNodes = sortNodesByPartition(pool);

        for (ArrayList<Node> nodesOfP: partitionToNodes.values()){
            nodePruneForSamePartition_SEQ(nodesOfP);

            for (Node n: nodesOfP){
                pool.getNodeHash().put(n.toString(), n);
            }
        }


        return pool;
    }



    private static ArrayList<Node> nodePruneForSamePartition_SEQ(ArrayList<Node> nodes){

        //sort the nodes by descending existing costs
        Collections.sort(nodes,Collections.reverseOrder());

        //list of number of snapshots
        ArrayList<Integer> snapshotNums = new ArrayList<>();
        for (Node node: nodes){
            snapshotNums.add(node.getSnapshotNum());
        }

        //compare nodes
        for (int i =0; i<nodes.size(); i++){
            for (int j = i+1;j<nodes.size(); j++){
                if (snapshotNums.get(i)>snapshotNums.get(j)){
                    nodes.get(i).recursiveDelete();
                    snapshotNums.remove(i);
                    nodes.remove(i);
                    i--;
                    break;
                }
            }
        }
        return nodes;
    }

    private static ArrayList<Node> nodePruneForSamePartition_Kleene(ArrayList<Node> nodes){

        //sort the nodes by descending existing costs
        for (int i = 0; i<nodes.size(); i++){

            for (int j = i+1; j<nodes.size(); j++){
                if (nodes.get(i).getExistingCostExpr().isGreater(nodes.get(j).getExistingCostExpr())&&
                nodes.get(i).getSnapshotNum()>nodes.get(j).getSnapshotNum()){
                    nodes.get(i).recursiveDelete();
                    nodes.remove(i);
                    i--;
                    break;
                }

                if (nodes.get(j).getExistingCostExpr().isGreater(nodes.get(i).getExistingCostExpr())&&
                        nodes.get(j).getSnapshotNum()>nodes.get(i).getSnapshotNum()){
                    nodes.get(j).recursiveDelete();
                    nodes.remove(j);
                    j--;
                }
            }
        }

        return nodes;
    }

    private static HashMap<String, ArrayList<Node>> sortNodesByPartition(Pool pool){

        HashMap<String, ArrayList<Node>> partitionToNodes = new HashMap<>();

        for (Node node: pool.getNodeHash().values()){
            String partition = node.partitionString();
            if (partitionToNodes.containsKey(partition)){
                partitionToNodes.get(partition).add(node);
            }else {
                ArrayList<Node> nodesOfPartition = new ArrayList<>();
                nodesOfPartition.add(node);
                partitionToNodes.put(partition, nodesOfPartition);
            }
        }

        return partitionToNodes;
    }



}
