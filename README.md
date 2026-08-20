# Five-Stage Pipelined RISC-V Processor Simulator

A cycle-accurate simulator for a five-stage pipelined RISC-V processor, written in Java, with
data-hazard detection, stall-based hazard resolution, a two-way set associative cache with LRU
replacement, and an event-driven memory system.

Built for the Computer Architecture lab at IIT Dharwad.

## Pipeline

Five stages, connected by explicit latch objects that carry state between them each cycle:

```
IF  ──IF_OF──▶  OF  ──OF_EX──▶  EX  ──EX_MA──▶  MA  ──MA_RW──▶  RW
 ▲                ▲               │
 └────EX_IF───────┴──IF_Enable────┘
```

| Stage | Class | Responsibility |
|-------|-------|----------------|
| **IF** | `InstructionFetch` | Fetch instruction at PC from memory |
| **OF** | `OperandFetch` | Decode, read register operands, detect data hazards |
| **EX** | `Execute` | ALU operations, branch resolution and redirect |
| **MA** | `MemoryAccess` | Load/store against the memory system |
| **RW** | `RegisterWrite` | Write results back to the register file |

`EX_IF` carries branch redirects back to fetch; `IF_EnableLatch` gates whether the fetch stage
advances, which is how stalls are held in place.

## Hazard handling

Data hazards are detected in the **OF** stage: before issuing, it checks whether either source
operand is still in flight in a later stage (`EX_MA`, `MA_RW`). If so, the instruction is held and
the fetch stage is frozen through `IF_EnableLatch` until the dependency clears.

The simulator counts both, and reports them at the end of a run:

- the number of times OF stalled because of a data hazard
- the total number of cycles lost to those stalls

## Cache

Separate L1 instruction and data caches sit in front of main memory, each **N-way set associative
with LRU replacement** and configured from `config.xml` (currently two-way).

Main memory is word addressed and a line holds a single word, so an address is already a line
address and carries no block offset. It splits into two fields:

```
tag = address / numberOfSets          setIndex = address % numberOfSets
```

A lookup selects one set and compares the tag against every way in it. On a miss the victim is an
invalid way if the set has one, and otherwise the least recently used way **of that set** — every
hit and every fill stamps the line with the current clock value to keep that ordering. Writes are
write-through with write-allocate: a store that misses fetches the line first, then retries.

The practical difference from a direct-mapped cache: two addresses that map to the same set no
longer evict each other, so conflict misses between them disappear.

## Memory system

Memory access is event-driven rather than instantaneous. `EventQueue` schedules
`MemoryReadEvent` / `MemoryWriteEvent` against `MainMemory`, which replies with a
`MemoryResponseEvent` after a configurable latency — so the pipeline experiences realistic
multi-cycle memory delay rather than a one-cycle idealisation.

## Build and run

```bash
javac -d out $(find src -name "*.java")
java -cp out main.Main <config.xml> <program.obj>
```

Simulation parameters live in `src/configuration/config.xml`. Statistics are written out at the end
of each run.

## Tests

`CacheTest` covers the address decomposition, the coexistence of addresses that would collide in a
direct-mapped cache, LRU victim selection within a set, independence between sets, and set-count
sizing including the fully associative edge case.

```bash
javac -d out $(find src test -name "*.java")
java -cp out processor.memorysystem.CacheTest
```

```
20 passed, 0 failed
```

## Layout

```
src/
  main/            entry point
  generic/         simulator loop, event queue, instruction/operand model, statistics
  processor/
    pipeline/      the five stages and the latches between them
    memorysystem/  set associative cache and main memory
  configuration/   config parsing and simulation parameters
test/
  processor/memorysystem/CacheTest.java
```

## Notes

Coursework. The lab scaffolding — configuration parsing, the instruction/operand model and the
event-queue framework — was provided with the assignment; the pipeline stages, inter-stage latches,
hazard detection, stalling logic and the cache are my implementation.

The cache was originally submitted as direct-mapped and was rewritten afterwards as a set
associative cache with proper tag extraction and per-set LRU replacement.
