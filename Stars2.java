import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.w3c.dom.NodeList;

import java.awt.*;
import javax.swing.*;

import java.util.ArrayList;
import java.util.Collections;

public class Stars2 extends JPanel {
    // global variables
    static int startIndex;
    static int endIndex;
    static double distance;

    // take coordinates from list and turn them into nodes
    static class Node {
        double x, y;
        List<Node> nodesWithinDistance;

        public Node(double coordX, double coordY) {
            x = coordX;
            y = coordY;
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

        // order list to make navigating nodes easier
        Collections.sort(coordinates);
        // once nodes are created,
        // create list for each node that holds all nodes within distance
        for (Node n : nodesList) {
            NodesWithinDistance(n, nodesList);
        }

        System.out.println(startNode.nodesWithinDistance.size());
        // get start node using node contents

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
                    g2.fillOval(x, y, nodeSize, nodeSize);

                }

                // draw lines to all nodes within distance of startnode
                for (Node n : startNode.nodesWithinDistance) {
                    int x1 = (int) startNode.x * 8;
                    int y1 = (int) startNode.y * 8;
                    int x2 = (int) n.x * 8;
                    int y2 = (int) n.y * 8;
                    g2.drawLine(x1, y1, x2, y2);
                }

            }
        };
        // Displaying chart
        frame.add(panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);
        frame.setTitle("Space Explorer");
        frame.setVisible(true);

        // ****************************************** A* IMPLEMENTATION
        // ****************************************
    }

    private static void NodesWithinDistance(Node node, List<Node> nodes) {
        // nodes are ordered so traversing is easier
        // create list to store close nodes
        List<Node> closeNodes = new ArrayList<Node>();
        // find index of current node
        int index = nodes.indexOf(node);
        int tempIndex = index;
        // System.out.println("Index: " + index);
        // System.out.println(node.x + " " + node.y);

        // move below node index until they are out of range
        // or start of list is reached
        Boolean searching = true;
        Boolean lookingBelow = true;
        Boolean lookingAbove = false;

        while (searching == true) {
            if (lookingBelow == true) {
                if (tempIndex > 0) {
                    // System.out.println("Looking below for: " + tempIndex);
                    Node n = nodes.get(tempIndex - 1);
                    // get distance from current node to node n
                    double distanceX = n.x - node.x;
                    double distanceY = n.y - node.y;

                    double distanceBetween = Math.sqrt(distanceY * distanceY + distanceX * distanceX);
                    tempIndex--;
                    if (distanceBetween <= distance) {
                        closeNodes.add(n);
                    } else {
                        lookingBelow = false;
                        lookingAbove = true;
                        tempIndex = index;
                    }
                } else {
                    lookingBelow = false;
                    lookingAbove = true;
                    tempIndex = index;
                }
            } else if (lookingAbove == true) {
                if (tempIndex < nodes.size() - 1) {
                    // System.out.println("Looking above for: " + index);
                    Node n = nodes.get(tempIndex + 1);
                    // get distance from current node to node n
                    double distanceX = n.x - node.x;
                    double distanceY = n.y - node.y;

                    double distanceBetween = Math.sqrt(distanceY * distanceY + distanceX * distanceX);
                    tempIndex++;
                    if (distanceBetween <= distance) {
                        closeNodes.add(n);
                    } else {
                        searching = false;
                    }
                } else {
                    searching = false;
                }
            }
        }
        node.nodesWithinDistance = closeNodes;
    }
}
