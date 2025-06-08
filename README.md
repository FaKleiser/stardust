# STARDUST

STARDUST is a framework for spectrum-based fault localization (SBFL). 
The framework was used to conduct experiments with SBFL during my bachelor thesis.


## Requirements

This project requires Java 17 or newer to build and run.


## Installation

STARDUST uses [maven](https://maven.apache.org/) as its build tool.
The `pom.xml` file contains all dependencies you need in order to run the framework.

### Using the Published Artifact

Successful builds on the `main` or `master` branch publish a JAR artifact named `stardust-jar`. You can download this artifact by following these steps:

1.  Navigate to the **Actions** tab of the GitHub repository.
2.  Select the **Java CI with Maven** workflow from the list of workflows.
3.  Choose a successful run (usually marked with a green checkmark).
4.  Scroll down to the **Artifacts** section at the bottom of the run page.
5.  Download the `stardust-jar` artifact. This will typically be a ZIP file containing `stardust-0.0.1.jar`.

Once downloaded and unzipped, you can run specific functionalities using a command similar to this:

```bash
java -cp stardust-0.0.1.jar fk.stardust.evaluation.sbfl.CreateRankings [args]
```
(Replace `[args]` with any arguments required by the chosen main class.)


## Build Status

![Build Status](https://github.com/FaKeller/stardust/actions/workflows/ci.yml/badge.svg)

This project uses GitHub Actions to automatically run tests and build the project upon commits and pull requests, ensuring code quality and integration.


## Theoretical Background

Spectrum-based fault localization (SBFL) can be used to locate faults in a very generic system model.
In general, all systems that consist of multiple components and provide the ability to track the involvement
of single components in passing or failing executions of the system, are capable of leveraging SBFL to diagnose
a faulty component in case of an error. The input for SBFL is a set of executions with each execution being
either classified as passing execution, i.e. no error occurred, or as a failing execution, i.e. an error occurred.

For each execution SBFL needs to know which components are involved or not involved in
the specific execution. A single execution with its involvement data is generally referred to
as a passing/failing trace and a set of traces is used as input for SBFL.

## Framework Architecture

Brief description of the relevant Java namespaces:

- **traces**: Contains classes to represent the program traces in an object-oriented manner.
- **provider**: Loads actual traces from some storage. Currently, only [Cobertura](http://cobertura.github.io/cobertura/) trace files are supported.
- **localizer**: Implementation of different strategies to localize the faulty component.
- **evaluation**: A set of classes that were used to run experiments on the [iBugs bug data set](https://www.st.cs.uni-saarland.de/ibugs/).


## Usage

A simplified usage of the framework looks as follows:

```java
ISpectraProvider<String> provider = new CoberturaProvider();
IFaultLocalizer<String> tarantula = new Tarantula();
Ranking<String> ranking = tarantula.localize(provider.loadSpectra());
ranking.save("resulting-ranking.txt");
```

## Contributing

STARDUST is an open source project released under the [MIT license](https://github.com/FaKeller/stardust/blob/master/LICENSE).
If you'd like to contribute, please submit a pull request :-)