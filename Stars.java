// Bradley Aries - 1265367
// Taylor Herdman - 1539767

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;
import java.util.ArrayList;
import java.util.Comparator;

import javax.swing.JFrame;
import javax.swing.JPanel;
import java.awt.*;

public class Stars extends JPanel {
    // global variables
    static int startIndex;
    static int endIndex;
    static double distance;
    static String fileName;
    static boolean routeFound;

    // Node class to hold required parameters
    static class Node {
        double x, y; // x and y coordinates
        List<Node> nodesWithinDistance;
        double h = 0; // distance to goal
        double g = 0; // cost from start to node
        double f = 0; // main cost
        Node prev; // what node comes before it in the path

        public Node(double coordX, double coordY) {
            x = coordX;
            y = coordY;
        }
    }

    public static void main(String[] args) {
        // read from given arguments
        try {
            fileName = args[0];
            startIndex = Integer.parseInt(args[1]);
            endIndex = Integer.parseInt(args[2]);
            distance = Double.parseDouble(args[3]);
        } catch (Exception e) {
            System.err.println("Please enter: Stars <filename.csv> <start node> <goal node> <distance>");
            System.exit(0);
        }

        // read star coordinates from file
        List<String> coordinates = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                coordinates.add(line);
            }
            br.close();
        } catch (Exception e) {
            System.err.println("Error occured while reading file: " + e);
            System.exit(0);
        }

        
        if(endIndex < 0 || endIndex > coordinates.size() || startIndex < 0 || startIndex > coordinates.size() || distance <= 0) {
            System.err.println("Enter indexes of nodes that are within the graph and a distance greater than 0");
            System.exit(0);
        }

        // create list of nodes from the list of coordinates
        List<Node> nodesList = new ArrayList<>();
        for (String line : coordinates) {
            String[] xy = line.split(",");
            double x = Double.parseDouble(xy[0]);
            double y = Double.parseDouble(xy[1]);
            Node node = new Node(x, y);
            nodesList.add(node);
        }

        // set the start node and goal node
        Node startNode = nodesList.get(startIndex);
        Node goalNode = nodesList.get(endIndex);

        // create list for each node that holds all nodes within specified distance
        for (Node n : nodesList) {
            NodesWithinDistance(n, nodesList);
        }

        // create priority queue comparing f value of nodes
        Comparator<Node> nodeComparator = new Comparator<Node>() {
            @Override
            public int compare(Node node1, Node node2) {
                if (node1.f < node2.f) {
                    return -1;
                } else if (node1.f > node2.f) {
                    return 1;
                }
                return 0;
            }
        };
        PriorityQueue<Node> frontier = new PriorityQueue<>(nodeComparator);

        routeFound = false;
        double tempH, tempG, tempF;
        Node lastN = null;

         // ********************** A* IMPLEMENTATION //////////////////////////
        startNode.h = getDistance(goalNode, startNode);
        startNode.f = startNode.h + startNode.g;
        frontier.add(startNode);

        while (!frontier.isEmpty() && !routeFound) {
            Node current = frontier.poll();
            if (lastN == current) {
                current = frontier.poll();
            }
            lastN = current;
            if (current == goalNode) {
                routeFound = true;
            }
            for (Node neighbour : current.nodesWithinDistance) {
                tempH = getDistance(goalNode, neighbour);
                tempG = current.g + getDistance(current, neighbour);
                tempF = tempH + tempG;

                if (tempG < neighbour.g || neighbour.g == 0d) {
                    neighbour.prev = current;
                    neighbour.g = tempG;
                    neighbour.h = tempH;
                    neighbour.f = tempF;
                    if (!frontier.contains(neighbour)) {
                        frontier.add(neighbour);
                    }
                }
            }
        }

        Stack<Node> stack = new Stack<>();
        // if route was found, create a stack of the path by backtracking through nodes
        if (routeFound) {
            System.out.println("Route Found");
            Node current = goalNode;
            while (current != null && current != startNode) {
                stack.add(current);
                current = current.prev;
            }
            stack.add(current);
        }
        // if not route was found, indicate that fact
        else {
            System.out.println("No Route Found");
        }

        int nodeSize = 10;
        JFrame frame = new JFrame();
        JPanel panel = new JPanel() {
            // overriding paintComponent methods to display the graph
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                // draw each Node on graph
                // starting node is green, goal node is red
                for (Node n : nodesList) {
                    int x = (int) Math.round(n.x * 8);
                    int y = (int) Math.round(n.y * 8);
                    g2.setColor(Color.BLUE);
                    if (n == startNode)
                        g2.setColor(Color.GREEN);
                    else if (n == goalNode)
                        g2.setColor(Color.RED);
                    g2.fillOval(x, y, nodeSize, nodeSize);

                }
                // draw lines between each node in route if a route was found
                if(routeFound) {
                    Node prev = startNode;
                    for (Node n : stack) {
                        if (prev != startNode) {
                            int x1 = (int) Math.round(n.x * 8) + (nodeSize / 2);
                            int y1 = (int) Math.round(n.y * 8) + (nodeSize / 2);
                            int x2 = (int) Math.round(prev.x * 8) + (nodeSize / 2);
                            int y2 = (int) Math.round(prev.y * 8) + (nodeSize / 2);
                            g2.drawLine(x1, y1, x2, y2);
                        }
                        prev = n;
                    }
                }
            }
        };

        // set up and display chart
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 1000);
        frame.setTitle("Space Explorer");
        frame.setVisible(true);
    }

    public static double getDistance(Node node1, Node node2) {
        // calculate distance to neighbour node
        return Math.sqrt(Math.pow(node1.x - node2.x, 2) + Math.pow(node1.y - node2.y, 2));
    }

    private static void NodesWithinDistance(Node node, List<Node> nodes) {
        // create list to store close nodes
        List<Node> closeNodes = new ArrayList<Node>();

        // loop through all nodes and if the distance is less than distance entered
        // add it to a list of nodes
        for (Node n : nodes) {
            double distanceBetween = getDistance(n, node);
            if (distanceBetween <= distance && distanceBetween != 0d) {
                closeNodes.add(n);
            }
        }

        node.nodesWithinDistance = closeNodes;
    }
}