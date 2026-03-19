# Benchmark Results (2026-03-19)

Executed with:

- `./gradlew :benchmark:jmh`
- JDK: Corretto 17.0.9
- JMH: 1.36
- Mode: `avgt` (`ms/op`)
- Warmup: 2 iterations
- Measurement: 5 iterations

Results (`benchmark/build/results/jmh/results.json`):

| Benchmark | Param | Score (ms/op) | Error |
| --- | --- | ---: | ---: |
| `EndToEndBenchmark.obfuscateAndRevealBatch` | `sampleSize=100` | 0.008 | +/- 0.001 |
| `EndToEndBenchmark.obfuscateAndRevealBatch` | `sampleSize=1000` | 0.096 | +/- 0.001 |
| `FileScanningBenchmark.locateAssets` | `totalFiles=100` | 3.152 | +/- 0.061 |
| `FileScanningBenchmark.locateAssets` | `totalFiles=1000` | 31.824 | +/- 0.680 |
| `FileScanningBenchmark.locateAssets` | `totalFiles=10000` | 382.744 | +/- 15.108 |
| `FileScanningBenchmark.locateResources` | `totalFiles=100` | 3.229 | +/- 0.266 |
| `FileScanningBenchmark.locateResources` | `totalFiles=1000` | 32.351 | +/- 0.571 |
| `FileScanningBenchmark.locateResources` | `totalFiles=10000` | 359.427 | +/- 16.350 |
| `XmlParsingBenchmark.parseStringsXml` | `stringCount=10` | 0.039 | +/- 0.001 |
| `XmlParsingBenchmark.parseStringsXml` | `stringCount=100` | 0.061 | +/- 0.002 |
| `XmlParsingBenchmark.parseStringsXml` | `stringCount=1000` | 0.279 | +/- 0.028 |

Notes:

- This is the first benchmark snapshot in this repo layout.
- A historical baseline from pre-refactor code is not available in this run, so this file serves as baseline for future regressions.
