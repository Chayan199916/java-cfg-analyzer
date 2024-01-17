package com.github.Chayan199916;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.expr.Expression;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Main {
    static int boundedRegions = 0;
    static int numberOfNodes = 0;
    static int numberOfEdges = 0;

    public static void main(String[] args) throws Exception {
        // Specify the file name in the resources folder
        String fileName = "Sample2.java";
        // Specify the output file name
        String outputFileName = "Sample2.json";

        // Get the target directory
        String targetDirectory = "target";

        // Load the file as an InputStream from the resources folder
        InputStream inputStream = Main.class.getClassLoader().getResourceAsStream(fileName);
        if (inputStream == null) {
            System.out.println("File not found: " + fileName);
            return;
        }

        // Parse the Java source file
        CompilationUnit cu = StaticJavaParser.parse(inputStream);

        IfStmt ifStmt = cu.findAll(IfStmt.class).get(0);

        // Create CFG nodes and edges
        if (ifStmt != null) {
            boundedRegions++;
            Map<String, List<String>> adjacencyList = createCFG(ifStmt);

            // # of Bounded regions = # of predicates in the CFG
            System.out.println("Number of bounded regions: " + boundedRegions);
            System.out.println("Number of predicates: " + boundedRegions);
            System.out.println("Number of nodes: " + numberOfNodes);
            System.out.println("Number of edges: " + numberOfEdges);
            int cyclomaticComplexity = boundedRegions + 1;
            System.out.println("Cyclomatic complexity: " + cyclomaticComplexity);
            System.out.println("So we need at most " + cyclomaticComplexity + " independant paths to cover the CFG");

            List<List<String>> independentPaths = findMaximalIndependentPaths(adjacencyList, "if", "end");
            System.out.println("Maximal Independent Paths:");
            for (List<String> path : independentPaths) {
                System.out.println(path);
            }
            // Entry point
            // String entryPoint = "if";

            // Calculate the number of distinct paths
            // int distinctPaths = calculateDistinctPaths(adjacencyList, entryPoint);
            // System.out.println("Number of distinct paths: " + distinctPaths);

            // Create the output directory if it doesn't exist
            Path outputPath = Paths.get(targetDirectory);
            if (!Files.exists(outputPath)) {
                try {
                    Files.createDirectories(outputPath);
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            // Specify the complete path for the output file in the target directory
            Path outputFile = outputPath.resolve(outputFileName);

            // Store CFG in JSON format
            Gson gson = new Gson();
            String json = gson.toJson(adjacencyList);
            System.out.println(json);
            // Write the output to the file
            try (FileWriter fileWriter = new FileWriter(outputFile.toFile())) {
                fileWriter.write(json);
                System.out.println("Output written to: " + outputFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Map<String, List<String>> createCFG(IfStmt ifStmt) {
        Map<String, List<String>> adjacencyList = new HashMap<>();

        // Create nodes for the if statement and its branches
        addNode(adjacencyList, "if");
        addNode(adjacencyList, "then");
        addNode(adjacencyList, "end");

        // Connect edges based on control flow
        adjacencyList.get("if").add("then");
        numberOfEdges++;
        adjacencyList.get("if").add("end"); // if else part not there
        numberOfEdges++;

        // adjacencyList.get("then").add("end");
        // adjacencyList.get("else").add("end");

        // Add nodes and edges for statements within each block
        Statement thenStmt = ifStmt.getThenStmt();
        if (thenStmt instanceof BlockStmt) {
            BlockStmt thenBlockStmt = (BlockStmt) thenStmt;
            // Access statements within the block using thenBlockStmt.getStatements()
            addNodesAndEdges(adjacencyList, thenBlockStmt.getStatements());
            for (Statement stmt : thenBlockStmt.getStatements()) {
                adjacencyList.get("then").add(stmt.toString());
                numberOfEdges++;
            }
        } else if (thenStmt instanceof ExpressionStmt) {
            ExpressionStmt expressionStmt = (ExpressionStmt) thenStmt;
            Expression expression = expressionStmt.getExpression();
            // Create a node for the expression statement
            addNode(adjacencyList, expression.toString());
            // Connect it to the subsequent node (usually "end")
            adjacencyList.get(expression.toString()).add("end");
            numberOfEdges++;
        }
        Optional<Statement> optionalElseStmt = ifStmt.getElseStmt();
        optionalElseStmt.ifPresent(statement -> {
            addNode(adjacencyList, "else");
            // if else part is present, then no edge between "if" and "end"
            adjacencyList.get("if").remove(adjacencyList.get("if").size() - 1);
            numberOfEdges--;
            adjacencyList.get("if").add("else");
            numberOfEdges++;
            // Do something with the Statement if it's present
            Statement elseStmt = optionalElseStmt.get(); // Unwrap the Optional
            if (elseStmt instanceof BlockStmt) {
                BlockStmt elseBlockStmt = (BlockStmt) elseStmt;
                // Access statements within the else block
                addNodesAndEdges(adjacencyList, elseBlockStmt.getStatements());
                for (Statement stmt : elseBlockStmt.getStatements()) {
                    adjacencyList.get("else").add(stmt.toString());
                    numberOfEdges++;
                }
            } else if (elseStmt instanceof ExpressionStmt) {
                ExpressionStmt expressionStmt = (ExpressionStmt) elseStmt;
                Expression expression = expressionStmt.getExpression();
                // Create a node for the expression statement
                addNode(adjacencyList, expression.toString());
                // Connect it to the "end" node
                adjacencyList.get(expression.toString()).add("end");
                numberOfEdges++;
            }
        });
        return adjacencyList;
    }

    private static void addNodesAndEdges(Map<String, List<String>> adjacencyList, List<Statement> statements) {
        for (Statement stmt : statements) {
            addNode(adjacencyList, stmt.toString());
            // Connect the last statement to the "end" node
            if (stmt == statements.get(statements.size() - 1)) {
                adjacencyList.get(stmt.toString()).add("end");
                numberOfEdges++;
            }
        }
    }

    private static void addNode(Map<String, List<String>> adjacencyList, String nodeId) {
        if (!adjacencyList.containsKey(nodeId)) {
            numberOfNodes++;
            adjacencyList.put(nodeId, new ArrayList<>());
        }
    }

    private static List<List<String>> findMaximalIndependentPaths(Map<String, List<String>> adjacencyList,
            String startNode, String endNode) {
        List<List<String>> independentPaths = new ArrayList<>();
        Set<String> visitedNodes = new HashSet<>();
        List<String> currentPath = new ArrayList<>();
        dfs(adjacencyList, startNode, endNode, visitedNodes, currentPath, independentPaths);
        return independentPaths;
    }

    private static void dfs(Map<String, List<String>> adjacencyList, String currentNode, String endNode,
            Set<String> visitedNodes, List<String> currentPath,
            List<List<String>> independentPaths) {
        visitedNodes.add(currentNode);
        currentPath.add(currentNode);

        if (currentNode.equals(endNode)) {
            independentPaths.add(new ArrayList<>(currentPath));
        } else {
            for (String nextNode : adjacencyList.get(currentNode)) {
                if (!visitedNodes.contains(nextNode)) {
                    dfs(adjacencyList, nextNode, endNode, visitedNodes, currentPath, independentPaths);
                }
            }
        }
        visitedNodes.remove(currentNode);
        currentPath.remove(currentPath.size() - 1);
    }
}
