// Bradley Aries - 1265367
// Taylor Herdman

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

import javax.swing.JFrame;
import javax.swing.JPanel;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Stars2 extends JPanel {
    // global variables
    static int startIndex;
    static int endIndex;
    static double distance;
    static String fileName;

    // take coordinates from list and turn them into nodes
    static class Node {
        double x, y;
        List<Node> nodesWithinDistance;
        // double distanceToGoal;
        double h = 0; // distance to goal
        double g = 0; // cost from start to node
        double f = 0; // main cost
        Node prev;

        public Node(double coordX, double coordY) {
            x = coordX;
            y = coordY;
        }
    }

    public static void main(String[] args) {
        // read from entered arguments
        try {
            fileName = args[0];
            startIndex = Integer.parseInt(args[1]);
            endIndex = Integer.parseInt(args[2]);
            distance = Double.parseDouble(args[3]);
        } catch (Exception ex) {
            System.out.println("Please enter: Stars <filename.csv> <start node> <goal node> <distance>");
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

        // ********************** A* IMPLEMENTATION //////////////////////////

        // create priority queue comparing distances

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
        Stack<Node> stack = new Stack<>();

        startNode.h = getDistance(goalNode, startNode);
        startNode.f = startNode.h + startNode.g;
        frontier.add(startNode);
        boolean routeFound = false;
        double tempH, tempG, tempF;
        Node lastN = null;

        try {
            while (!frontier.isEmpty() && !routeFound) {
                Node current = frontier.poll();
                if (lastN == current) {
                    current = frontier.poll();
                }
                lastN = current;
                if (nodesList.indexOf(current) == endIndex) {
                    System.out.println("Route found");
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
        } catch (Exception ex) {
            System.out.println("No possible route found");
            System.exit(0);
        }
        Node current = nodesList.get(endIndex);
        while (current != null && current != nodesList.get(startIndex)) {
            stack.add(current);
            current = current.prev;
        }
        stack.add(current);

        // overriding paintComponent methods
        // draw nodes on graph
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
                // draw lines between each node in route
                System.out.println(stack.size());
                if (stack.size() <= 2) {
                    System.out.println("No possible route found");
                    return;
                }
                Node prev = startNode;
                for (Node n : stack) {
                    System.out.println(n.x + " : " + n.y);
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
        };

        // Displaying chart
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setTitle("Space Explorer");
        frame.setVisible(true);
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