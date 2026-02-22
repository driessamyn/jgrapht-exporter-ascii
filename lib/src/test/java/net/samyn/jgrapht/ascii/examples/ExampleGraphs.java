package net.samyn.jgrapht.ascii.examples;

import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;

public class ExampleGraphs {
  public static DefaultDirectedGraph<String, DefaultEdge> singleVertexDag() {
    var graph = directedGraph();
    graph.addVertex("A");
    return graph;
  }

  public static DefaultDirectedGraph<String, DefaultEdge> twoVerticesWithEdgeDag() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addEdge("A", "B");
    return graph;
  }

  public static DefaultDirectedGraph<String, DefaultEdge> diamondDag() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "B");
    graph.addEdge("A", "C");
    graph.addEdge("B", "D");
    graph.addEdge("C", "D");
    return graph;
  }

  public static DefaultDirectedGraph<String, DefaultEdge> widerLabelsDag() {
    var graph = directedGraph();
    graph.addVertex("Start");
    graph.addVertex("End");
    graph.addEdge("Start", "End");
    return graph;
  }

  public static DefaultDirectedGraph<String, DefaultEdge> differentLengthDag() {
    var graph = directedGraph();
    graph.addVertex("X");
    graph.addVertex("Y");
    graph.addEdge("X", "Y");
    return graph;
  }

  public static DefaultDirectedGraph<String, DefaultEdge> multipleRootsDag() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "C");
    graph.addEdge("B", "C");
    return graph;
  }

  public static DefaultDirectedGraph<String, DefaultEdge> deepChainDag() {
    var graph = directedGraph();
    graph.addVertex("1");
    graph.addVertex("2");
    graph.addVertex("3");
    graph.addVertex("4");
    graph.addVertex("5");
    graph.addEdge("1", "2");
    graph.addEdge("2", "3");
    graph.addEdge("3", "4");
    graph.addEdge("4", "5");
    return graph;
  }

  public static DefaultDirectedGraph<String, DefaultEdge> skipLayerDag() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B");
    graph.addEdge("B", "C");
    graph.addEdge("A", "C");
    return graph;
  }

  public static DefaultDirectedGraph<String, DefaultEdge> wideDag() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "B");
    graph.addEdge("A", "C");
    graph.addEdge("A", "D");
    return graph;
  }

  public static DefaultDirectedGraph<String, DefaultEdge> multipleSourcesDag() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addVertex("D");
    graph.addEdge("A", "D");
    graph.addEdge("B", "D");
    graph.addEdge("C", "D");
    return graph;
  }

  public static DefaultDirectedGraph<String, DefaultEdge> isolatedVerticesDag() {
    var graph = directedGraph();
    graph.addVertex("X");
    graph.addVertex("Y");
    return graph;
  }

  public static DefaultDirectedGraph<String, DefaultEdge> threeVerticesLinearDag() {
    var graph = directedGraph();
    graph.addVertex("A");
    graph.addVertex("B");
    graph.addVertex("C");
    graph.addEdge("A", "B");
    graph.addEdge("B", "C");
    return graph;
  }

  public static DefaultDirectedGraph<String, DefaultEdge> filmProductionDag() {
    var graph = directedGraph();

    // Nodes
    graph.addVertex("Script");
    graph.addVertex("Concept");
    graph.addVertex("Casting");
    graph.addVertex("Budget");
    graph.addVertex("Storyboards");
    graph.addVertex("VFX Previs");
    graph.addVertex("Schedule");
    graph.addVertex("Shoot");
    graph.addVertex("On-set VFX");
    graph.addVertex("Motion Capture");
    graph.addVertex("Rough Cut");
    graph.addVertex("CGI Rendering");
    graph.addVertex("Sound Design");
    graph.addVertex("Musical Score");
    graph.addVertex("Final Edit");
    graph.addVertex("Color Grading");
    graph.addVertex("Trailer Campaign");
    graph.addVertex("Merchandising");
    graph.addVertex("Theatrical Release");
    graph.addVertex("Streaming Release");
    graph.addVertex("Blu-ray Release");

    // Edges
    graph.addEdge("Script", "Storyboards");
    graph.addEdge("Script", "VFX Previs");
    graph.addEdge("Concept", "Storyboards");
    graph.addEdge("Concept", "VFX Previs");
    graph.addEdge("Casting", "Schedule");
    graph.addEdge("Budget", "Schedule");
    graph.addEdge("Script", "Schedule");

    graph.addEdge("Schedule", "Shoot");
    graph.addEdge("Storyboards", "Shoot");
    graph.addEdge("Casting", "Shoot");

    graph.addEdge("Casting", "Motion Capture");
    graph.addEdge("VFX Previs", "Motion Capture");

    graph.addEdge("Shoot", "On-set VFX");
    graph.addEdge("Shoot", "Rough Cut");

    graph.addEdge("On-set VFX", "CGI Rendering");
    graph.addEdge("Motion Capture", "CGI Rendering");
    graph.addEdge("Rough Cut", "CGI Rendering");

    graph.addEdge("Rough Cut", "Sound Design");
    graph.addEdge("Rough Cut", "Musical Score");

    graph.addEdge("CGI Rendering", "Final Edit");
    graph.addEdge("Sound Design", "Final Edit");
    graph.addEdge("Musical Score", "Final Edit");
    graph.addEdge("Rough Cut", "Final Edit"); // Layer skipping

    graph.addEdge("Final Edit", "Color Grading");

    graph.addEdge("Rough Cut", "Trailer Campaign"); // Layer skipping
    graph.addEdge("Concept", "Merchandising"); // Multi-layer skip

    graph.addEdge("Color Grading", "Theatrical Release");
    graph.addEdge("Theatrical Release", "Streaming Release");
    graph.addEdge("Streaming Release", "Blu-ray Release");

    return graph;
  }

  private static DefaultDirectedGraph<String, DefaultEdge> directedGraph() {
    return new DefaultDirectedGraph<>(DefaultEdge.class);
  }
}
