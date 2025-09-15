# Nitrite JMH Benchmarks

This repository packages and executes Java Microbenchmark Harness (JMH) suites for different Nitrite versions.

## Benchmark runner

Use the `benchmark.sh` helper to build the project and execute the benchmarks.

### Usage

```bash
./benchmark.sh [-m modules] [-j "JMH args"]
```

- `-m` lets you choose a comma-separated list of modules to benchmark (`v3`, `v4`). The default runs both suites.
- `-j` forwards additional command line options directly to the underlying JMH runner.

The script stores JSON reports under the `reports/` directory.

### Examples

Run every benchmark suite:

```bash
./benchmark.sh
```

Run only the Nitrite v4 benchmarks:

```bash
./benchmark.sh -m v4
```

Forward additional JMH options:

```bash
./benchmark.sh -m v3 -j "-wi 5 -i 10 -prof stack"
```

