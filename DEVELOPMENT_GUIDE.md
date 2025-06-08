# STARDUST Development Guide

This guide provides information and examples on how to extend the STARDUST framework, such as adding custom fault localizers or integrating new datasets for experiments.

## Extensibility Points

STARDUST is designed with extensibility in mind, primarily in these areas:

1.  **Fault Localizers:** Implementing new Spectrum-Based Fault Localization (SBFL) algorithms or variations.
2.  **Spectra Providers:** Adding support for different trace data formats or sources beyond Cobertura.

## Adding Custom Fault Localizers

To add a new fault localization algorithm, you need to implement the `IFaultLocalizer<T>` interface found in the `fk.stardust.localizer` package. The type parameter `T` represents the type of component being localized (e.g., `String` for line numbers, or a custom class for more complex component representations).

The core method to implement is:

```java
Ranking<T> localize(Spectra<T> spectra);
```

This method takes a `Spectra<T>` object (containing all trace information) as input and should return a `Ranking<T>` object, which is essentially a list of components sorted by their calculated suspiciousness.

### Steps to Implement a Custom Localizer:

1.  **Create a new Java class** in a suitable package (e.g., `fk.stardust.localizer.custom`).
2.  **Implement the `IFaultLocalizer<T>` interface.**
3.  **Implement the `localize` method:**
    *   Iterate through the nodes (components) in the `spectra.getNodes()`.
    *   For each node, access its execution statistics:
        *   `node.getEF()`: Number of failing traces executing the component.
        *   `node.getEP()`: Number of passing traces executing the component.
        *   `node.getNF()`: Number of failing traces *not* executing the component.
        *   `node.getNP()`: Number of passing traces *not* executing the component.
    *   You can also get total numbers of failing and passing traces from `spectra.getFailingTracesCount()` and `spectra.getPassingTracesCount()`.
    *   Calculate the suspiciousness score based on your algorithm's formula.
    *   Store these scores.
4.  **Construct and return a `Ranking<T>` object.** The `Ranking` class typically takes a list of `RankedElement<T>` objects, where each `RankedElement` pairs a component with its score.

### Example: Simple Custom Localizer

Let's say we want to create a very simple localizer that ranks components based purely on the number of failing traces they are part of.

```java
package fk.stardust.localizer.custom;

import fk.stardust.localizer.IFaultLocalizer;
import fk.stardust.traces.INode;
import fk.stardust.traces.Spectra;
import fk.stardust.traces.Ranking;
import fk.stardust.traces.RankedElement;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SimpleFailCountLocalizer<T> implements IFaultLocalizer<T> {

    @Override
    public Ranking<T> localize(Spectra<T> spectra) {
        List<RankedElement<T>> rankedElements = new ArrayList<>();

        for (INode<T> node : spectra.getNodes()) {
            // Our suspiciousness score is just the count of failing traces covering this node
            double suspiciousness = node.getEF();
            rankedElements.add(new RankedElement<>(node.getIdentifier(), suspiciousness));
        }

        // Sort elements by suspiciousness in descending order
        // Note: The Ranking constructor might also handle sorting based on how it's implemented,
        // but explicit sorting here is safer.
        Collections.sort(rankedElements, Comparator.comparingDouble(RankedElement<T>::getScore).reversed());

        return new Ranking<>(rankedElements);
    }

    @Override
    public String getName() {
        return "SimpleFailCountLocalizer";
    }
}
```

### Using the Custom Localizer:

Once implemented, you can use your custom localizer just like the built-in ones:

```java
// Assuming 'programSpectra' is a loaded Spectra<String> object
IFaultLocalizer<String> customLocalizer = new SimpleFailCountLocalizer<>();
Ranking<String> ranking = customLocalizer.localize(programSpectra);
ranking.save("custom-ranking.txt");
```

## Integrating Different Data Sets

STARDUST experiments often rely on datasets of buggy programs and their tests. The framework was notably used with the [iBugs bug data set](https://www.st.cs.uni-saarland.de/ibugs/). To use different datasets, you'll primarily need to ensure your data can be converted into the `Spectra<T>` format that STARDUST expects.

This usually involves:

1.  **Understanding Your Data:**
    *   How are faulty components identified (e.g., line numbers, method names)? This will determine your type `T`.
    *   How is test execution and coverage information stored?
    *   How are tests marked as passing or failing?

2.  **Creating a Custom `ISpectraProvider<T>` (if necessary):**
    *   If your dataset's trace/coverage format is not Cobertura XML, you'll need to implement the `ISpectraProvider<T>` interface (from `fk.stardust.provider`).
    *   The main method is `Spectra<T> loadSpectra();`.
    *   Inside this method, you'll parse your dataset's specific file formats, identify unique program components (`T`), and for each component, count `EF, EP, NF, NP` based on the test outcomes and coverage.
    *   You'll then populate and return a `Spectra<T>` object.

### Example: Conceptual Steps for a Custom `ISpectraProvider`

Suppose your dataset provides coverage in a simple CSV format like:
`test_id,component_id,outcome` (e.g., `test1,com.example.MyClass:45,FAIL`)

```java
package fk.stardust.provider.custom;

import fk.stardust.provider.ISpectraProvider;
import fk.stardust.traces.Spectra;
import fk.stardust.traces.RawTraceCollector; // Helper class

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class CustomCsvProvider implements ISpectraProvider<String> {

    private String csvFilePath;

    public CustomCsvProvider(String csvFilePath) {
        this.csvFilePath = csvFilePath;
    }

    @Override
    public Spectra<String> loadSpectra() {
        RawTraceCollector<String> collector = new RawTraceCollector<>();
        Set<String> allComponents = new HashSet<>();
        Map<String, Boolean> testOutcomes = new HashMap<>(); // testId -> isFailing

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            reader.readLine(); // Skip header
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String testId = parts[0];
                String componentId = parts[1];
                boolean isFailing = "FAIL".equalsIgnoreCase(parts[2]);

                allComponents.add(componentId);
                testOutcomes.put(testId, isFailing);
                collector.addCoverage(testId, componentId);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error reading CSV spectra file: " + csvFilePath, e);
        }

        // Add traces to the collector
        for (Map.Entry<String, Boolean> entry : testOutcomes.entrySet()) {
            collector.setTraceOutcome(entry.getKey(), entry.getValue());
        }

        return collector.toSpectra();
    }

    @Override
    public String getName() {
        return "CustomCsvProvider";
    }
}
```

**Using the Custom Provider:**

```java
ISpectraProvider<String> customProvider = new CustomCsvProvider("path/to/your/data.csv");
Spectra<String> spectra = customProvider.loadSpectra();

IFaultLocalizer<String> tarantula = new Tarantula<>(); // Or any other localizer
Ranking<String> ranking = tarantula.localize(spectra);
ranking.save("ranking-from-custom-data.txt");
```

### Considerations for New Datasets:

*   **Component Granularity:** Decide at what level you want to localize faults (lines, methods, classes). This will influence how you define components (`T`) and process your dataset. STARDUST examples primarily use `String` for line numbers (e.g., `"com.example.MyClass:123"`).
*   **Mapping to `Spectra`:** The key is to correctly populate the `Spectra` object with nodes representing your components and accurately associate them with passing and failing traces. The `RawTraceCollector` utility class can simplify this.
*   **Experiment Harness:** You might also need to adapt parts of the `fk.stardust.evaluation` package if your dataset has a different structure for defining bug instances, actual fault locations (for evaluation), etc., than iBugs.

By implementing these interfaces, you can significantly extend STARDUST's capabilities to work with new algorithms and diverse sources of software execution data.
