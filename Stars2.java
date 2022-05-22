import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.w3c.dom.NodeList;

import java.awt.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Stars2 extends JPanel {
    // global variables
    static int startIndex;
    static int endIndex;
    static double distance;

    // take coordinates from list and turn them into nodes
    static class Node {
        double x, y;
        List<Node> nodesWithinDistance;
        // double distanceToGoal;
        double h = 0; // distance to goal
        double g = 0; // cost from start to node
        double f = 0; // main cost

        public Node(double coordX, double coordY) {
            x = coordX;
            y = coordY;
        }

        Comparator<Node> nodeToNode = new Comparator<>() {
            @Override
            public int compare(Node node1, Node node2) {
                return (int) node1.h - (int) node2.h;
            }
        };

        PriorityQueue<Node> nodeQueue = new PriorityQueue<>(nodeToNode);
    }

    static class Edge {
        Node _beginning;
        Node _end;
        double _distanceToNode;

        public Edge(Node beginning, Node end, double distanceToNode) {
            _beginning = beginning;
            _end = end;
            _distanceToNode = distanceToNode;
        }
    }

    public static void main(String[] args) {
        // read from entered arguments
        String fileName = args[0];
        startIndex = Integer.parseInt(args[1]);
        endIndex = Integer.parseInt(args[2]);
        distance = Double.parseDouble(args[3]);

        // read star coordinates from file
        List<String> coordinates = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                // System.out.println(line);
                coordinates.add(line);
            }
            br.close();
        } catch (Exception ex) {
            System.out.println("Error occured while reading file: " + ex);
        }

        List<Node> nodesList = new ArrayList<>();

        for (String line : coordinates) {
            String[] xy = line.split(",");
            double x = Double.parseDouble(xy[0]);
            double y = Double.parseDouble(xy[1]);
            Node node = new Node(x, y);
            // add to nodes list
            nodesList.add(node);

        }

        Node startNode = nodesList.get(startIndex);
        Node goalNode = nodesList.get(endIndex);

        // order list to make navigating nodes easier
        Collections.sort(coordinates);
        // once nodes are created,
        // create list for each node that holds all nodes within distance
        for (Node n : nodesList) {
            NodesWithinDistance(n, nodesList);
        }

        System.out.println(startNode.nodesWithinDistance.size());
        // get start node using node contents

        // ********************** A* IMPLEMENTATION
        // Step one: Add edges/paths to priority queue

        // create priority queue comparing distances
        Comparator<Edge> EdgeComparator = new Comparator<>() {
            @Override
            public int compare(Edge edge1, Edge edge2) {
                return (int) edge1._distanceToNode - (int) edge2._distanceToNode;
            }
        };

        Comparator<Node> nodeComparator = new Comparator<Node>() {
            @Override
            public int compare(Node node1, Node node2) {
                if (node1.h < node2.h) {
                    return -1;
                } else if (node1.h > node2.h) {
                    return 1;
                }
                return 0;
            }
        };

        PriorityQueue<Edge> queue = new PriorityQueue<>(EdgeComparator);
        PriorityQueue<Node> frontier = new PriorityQueue<>(nodeComparator);
        Stack<Node> stack = new Stack<>();

        startNode.h = getDistance(goalNode, startNode);
        startNode.f = startNode.h + startNode.g;
        frontier.add(startNode);

        boolean routeFound = false;
        ArrayList<Node> avoidNodes = new ArrayList<Node>();
        ArrayList<Node> beenNodes = new ArrayList<Node>();
        double tempH, tempG, tempF;
        Node lastN = null;

        while (!frontier.isEmpty() && !routeFound) {
            Node n = frontier.poll();
            frontier.clear();
            if (nodesList.indexOf(n) == endIndex) {
                System.out.println("Route found");
                stack.add(n);
                routeFound = true;
            }
            for (Node checkingNode : n.nodesWithinDistance) {
                tempH = getDistance(goalNode, checkingNode);
                tempG = n.g + getDistance(n, checkingNode);
                tempF = tempH + tempG;
                Node bestNode = null;
                double bestDistance = -1;

                if (avoidNodes.contains(checkingNode)) {
                    continue;
                } else if (frontier.contains(checkingNode)) {
                    checkingNode.h = tempH;
                    checkingNode.g = tempG;
                    checkingNode.f = tempF;
                } else if (!stack.contains(checkingNode)) {
                    checkingNode.h = tempH;
                    checkingNode.g = tempG;
                    checkingNode.f = tempF;
                    frontier.add(checkingNode);
                }

                if (lastN != null && stack.contains(checkingNode) && !beenNodes.contains(checkingNode)) {
                    if (checkingNode.g + getDistance(checkingNode, n) < lastN.g + getDistance(lastN, n)) {
                        while (stack.pop() != checkingNode) {
                            continue;
                        }
                        beenNodes.add(checkingNode);
                        stack.push(checkingNode);
                    }
                }

                /*
                 * Node bestNode = null;
                 * double bestDistance = -1;
                 * for(Node neigh: checkingNode.nodesWithinDistance) {
                 * if(stack.contains(neigh)) {
                 * if(neigh.g + getDistance(neigh, checkingNode) < tempG) {
                 * bestNode = neigh;
                 * }
                 * System.out.println("Better Route");
                 * }
                 * }
                 * if(bestNode != null) {
                 * while(stack.peek() != bestNode) {
                 * stack.pop();
                 * }
                 * }
                 */
            }

            /*
             * for(Node testNode: frontier) {
             * System.out.println(testNode.x + " : " + testNode.y + " : " + testNode.f);
             * }
             */
            // System.out.println(stack.size());
            if (frontier.size() == 0) {
                frontier.add(stack.pop());
                avoidNodes.add(n);
            } else if (stack.isEmpty()) {
                stack.add(n);
            } else if (!stack.contains(n)) {
                Node stackNode = stack.peek();
                if (stackNode.h > n.f) {
                    stack.pop();
                    stack.add(n);
                } else {
                    stack.add(n);
                }
            }

            lastN = n;
            // System.out.println("-------");
        }

        int nodeSize = 10;
        JFrame frame = new JFrame();
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                for (Node n : nodesList) {
                    int x = (int) Math.round(n.x * 8);
                    int y = (int) Math.round(n.y * 8);
                    g2.setColor(Color.BLUE);
                    if (nodesList.indexOf(n) == startIndex)
                        g2.setColor(Color.GREEN);
                    else if (nodesList.indexOf(n) == endIndex)
                        g2.setColor(Color.RED);
                    g2.fillOval(x, y, nodeSize, nodeSize);

                }

                Node prev = startNode;
                for (Node n : stack) {
                    int x1 = (int) Math.round(prev.x * 8) + (nodeSize / 2);
                    int y1 = (int) Math.round(prev.y * 8) + (nodeSize / 2);
                    int x2 = (int) Math.round(n.x * 8) + (nodeSize / 2);
                    int y2 = (int) Math.round(n.y * 8) + (nodeSize / 2);
                    g2.drawLine(x1, y1, x2, y2);
                    prev = n;
                }

            }
        };
        // Displaying chart
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setTitle("Space Explorer");
        frame.setVisible(true);

        /*
         * // get distance to goal from start
         * startNode.distanceToGoal = getDistance(goalNode, startNode);
         * // add each neighbour depending on distance
         * for (Node n : startNode.nodesWithinDistance) {
         * double dist = getDistance(n, startNode);
         * // create new edge between these two nodes
         * Edge edge = new Edge(startNode, n, dist);
         * // Add edge to queue
         * queue.add(edge);
         * }
         * Node currentNode = startNode;
         * // Step 2: Now that we have the first set of edges
         * // peek at node at end of first edge
         * while (currentNode != goalNode) {
         * Edge edge = queue.peek();
         * // add the node's distance to goal to edge distance
         * Node node = edge._end;
         * node.distanceToGoal = getDistance(node, goalNode);
         * double totalDist = edge._distanceToNode + node.distanceToGoal;
         * // if total distance is smaller than start distance to goal
         * // move to this node
         * if (totalDist <= startNode.distanceToGoal) {
         * currentNode = node;
         * }
         * // if total distance is larger
         * // peek at next edge in queue... maybe
         * else {
         * 
         * }
         * }
         */

    }

    public static double getDistance(Node node1, Node node2) {
        // caclulate distance to neighbour node
        return Math.sqrt(Math.pow(node1.x - node2.x, 2) + Math.pow(node1.y - node2.y, 2));
    }

    private static void NodesWithinDistance(Node node, List<Node> nodes) {
        // nodes are ordered so traversing is easier
        // create list to store close nodes
        List<Node> closeNodes = new ArrayList<Node>();

        for (Node n : nodes) {
            double distanceBetween = getDistance(n, node);
            if (distanceBetween <= distance) {
                closeNodes.add(n);
            }
        }

        node.nodesWithinDistance = closeNodes;
    }
}