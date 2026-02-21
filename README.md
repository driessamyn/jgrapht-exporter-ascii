# jgrapht-exporter-ascii

A Java library that exports [JGraphT](https://jgrapht.org/) graphs to ASCII/Unicode text art.
Designed for visualizing DAGs (Directed Acyclic Graphs) in console output, logs, and documentation.

## Example Output

```
┌───────┐
│ Start │
└───┬───┘
    │
    v
┌───────┐
│  End  │
└───────┘
```

## Features

- Implements JGraphT's `GraphExporter<V,E>` interface
- Sugiyama-inspired layered layout algorithm for DAGs
- Unicode box-drawing characters (default) and plain ASCII fallback
- Orthogonal edge routing with arrow indicators
- Automatic label sizing and vertex spacing

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("net.samyn:jgrapht-exporter-ascii:<version>")
}
```

### Maven

```xml
<dependency>
    <groupId>net.samyn</groupId>
    <artifactId>jgrapht-exporter-ascii</artifactId>
    <version>VERSION</version>
</dependency>
```

## Usage

```java
import net.samyn.jgrapht.ascii.AsciiExporter;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;

var graph = new SimpleDirectedGraph<String, DefaultEdge>(DefaultEdge.class);
graph.addVertex("A");
graph.addVertex("B");
graph.addVertex("C");
graph.addEdge("A", "B");
graph.addEdge("A", "C");

var exporter = new AsciiExporter<String, DefaultEdge>();
exporter.exportGraph(graph, System.out);
```

## Technical Details

*   [Graph Layering Explained](LAYERING.md) - Learn how graph vertices are assigned to layers for visualization.

## Requirements

- Java 11+
- JGraphT 1.5.2+

## License

[Apache License 2.0](LICENSE)
