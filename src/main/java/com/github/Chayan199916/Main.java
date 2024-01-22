package com.github.Chayan199916;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;
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
        String fileName = "Sample5.java";
        // Specify the output file name
        String outputFileName = "Sample5.json";

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

        // Example: Find the first if statement
        IfStmt ifStmt = null;
        List<IfStmt> ifStmts = cu.findAll(IfStmt.class);
        if (ifStmts.isEmpty()) {
            System.out.println("No 'if' statements found in the code.");
        } else {
            ifStmt = ifStmts.get(0);
        }

        // Example: Find the first while statement
        WhileStmt whileStmt = null;
        List<WhileStmt> whileStmts = cu.findAll(WhileStmt.class);
        if (whileStmts.isEmpty()) {
            System.out.println("No 'while' statements found in the code.");
        } else {
            whileStmt = whileStmts.get(0);
        }

        // Create CFG nodes and edges for if statement
        if (ifStmt != null) {
            boundedRegions++;
            Map<String, List<String>> adjacencyList = createCFG(ifStmt);

            // Process CFG and output results (similar to your original code)
            processCFG(adjacencyList, targetDirectory, "If_" + outputFileName, "if", "end");
        }
        boundedRegions = 0;
        numberOfNodes = 0;
        numberOfEdges = 0;
        // Create CFG nodes and edges for while statement
        if (whileStmt != null) {
            boundedRegions++;
            Map<String, List<String>> adjacencyListWhile = createCFG(whileStmt);

            // Process CFG and output results (similar to your original code)
            processCFG(adjacencyListWhile, targetDirectory, "While_" + outputFileName, "while", "end");
        }
    }

    private static void processCFG(Map<String, List<String>> adjacencyList, String targetDirectory,
            String outputFileName, String start, String end) {
        // # of Bounded regions = # of predicates in the CFG
        System.out.println("Number of bounded regions: " + boundedRegions);
        System.out.println("Number of predicates: " + boundedRegions);
        System.out.println("Number of nodes: " + numberOfNodes);
        System.out.println("Number of edges: " + numberOfEdges);
        int cyclomaticComplexity = boundedRegions + 1;
        System.out.println("Cyclomatic complexity: " + cyclomaticComplexity);
        System.out.println("So we need at most " + cyclomaticComplexity + " independent paths to cover the CFG");

        List<List<String>> independentPaths = findMaximalIndependentPaths(adjacencyList, start, end);
        System.out.println("Maximal Independent Paths:");
        for (List<String> path : independentPaths) {
            System.out.println(path);
        }

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

        // Add nodes and edges for statements within each block
        Statement thenStmt = ifStmt.getThenStmt();
        if (thenStmt instanceof BlockStmt) {
            BlockStmt thenBlockStmt = (BlockStmt) thenStmt;
            addNodesAndEdges(adjacencyList, thenBlockStmt.getStatements(), false);
            adjacencyList.get("then").add(thenBlockStmt.getStatements().get(0).toString());
            numberOfEdges++;
        } else if (thenStmt instanceof ExpressionStmt) {
            ExpressionStmt expressionStmt = (ExpressionStmt) thenStmt;
            Expression expression = expressionStmt.getExpression();
            addNode(adjacencyList, expression.toString());
            adjacencyList.get("then").add(expression.toString());
            numberOfEdges++;
            adjacencyList.get(expression.toString()).add("end");
            numberOfEdges++;
        }

        Optional<Statement> optionalElseStmt = ifStmt.getElseStmt();
        optionalElseStmt.ifPresent(statement -> {
            /**
             * if-else part is there;
             * no need to have if -> end relation
             */
            addNode(adjacencyList, "else");
            adjacencyList.get("if").remove(adjacencyList.get("if").size() - 1);
            numberOfEdges--;
            adjacencyList.get("if").add("else");
            numberOfEdges++;

            Statement elseStmt = optionalElseStmt.get();
            if (elseStmt instanceof BlockStmt) {
                BlockStmt elseBlockStmt = (BlockStmt) elseStmt;
                addNodesAndEdges(adjacencyList, elseBlockStmt.getStatements(), false);
                adjacencyList.get("else").add(elseBlockStmt.getStatements().get(0).toString());
                numberOfEdges++;
            } else if (elseStmt instanceof ExpressionStmt) {
                ExpressionStmt expressionStmt = (ExpressionStmt) elseStmt;
                Expression expression = expressionStmt.getExpression();
                addNode(adjacencyList, expression.toString());
                adjacencyList.get("else").add(expression.toString());
                adjacencyList.get(expression.toString()).add("end");
                numberOfEdges++;
            }
        });

        return adjacencyList;
    }

    private static Map<String, List<String>> createCFG(WhileStmt whileStmt) {
        Map<String, List<String>> adjacencyList = new HashMap<>();

        // Create nodes for the while statement and its branches
        addNode(adjacencyList, "while");
        addNode(adjacencyList, "body");
        addNode(adjacencyList, "end");

        // Connect edges based on control flow
        adjacencyList.get("while").add("body");
        numberOfEdges++;
        adjacencyList.get("while").add("end");
        numberOfEdges++;

        // Add nodes and edges for statements within the loop body
        Statement loopBody = whileStmt.getBody();
        if (loopBody instanceof BlockStmt) {
            BlockStmt blockStmt = (BlockStmt) loopBody;
            addNodesAndEdges(adjacencyList, blockStmt.getStatements(), true);
            adjacencyList.get("body").add(blockStmt.getStatements().get(0).toString());
            numberOfEdges++;
        } else if (loopBody instanceof ExpressionStmt) {
            ExpressionStmt expressionStmt = (ExpressionStmt) loopBody;
            Expression expression = expressionStmt.getExpression();
            addNode(adjacencyList, expression.toString());
            adjacencyList.get("body").add(expression.toString());
            adjacencyList.get(expression.toString()).add("while");
            numberOfEdges++;
        }

        return adjacencyList;
    }

    private static void addNodesAndEdges(Map<String, List<String>> adjacencyList, List<Statement> statements,
            boolean isLoop) {
        for (int i = 0; i < statements.size(); i++) { // Statement stmt : statements
            addNode(adjacencyList, statements.get(i).toString());
            if (i == statements.size() - 1) {
                String terminal = isLoop ? "while" : "end";
                adjacencyList.get(statements.get(i).toString()).add(terminal);
                numberOfEdges++;
            } else {
                adjacencyList.get(statements.get(i).toString()).add(statements.get(i + 1).toString());
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
