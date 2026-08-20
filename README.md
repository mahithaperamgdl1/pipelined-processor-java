# Five-Stage Pipelined Processor Simulator

A cycle-accurate simulator for a five-stage pipelined processor, written in Java, with data-hazard
detection, stall-based hazard resolution, and an event-driven memory system.

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

## Memory system

Memory access is event-driven rather than instantaneous. `EventQueue` schedules
`MemoryReadEvent` / `MemoryWriteEvent` against `MainMemory`, which replies with a
`MemoryResponseEvent` after a configurable latency — so the pipeline experiences realistic
multi-cycle memory delay rather than a one-cycle idealisation.

## Build and run

```bash
cd src
javac -d ../out $(find . -name "*.java")
java -cp ../out main.Main <config.xml> <program.obj>
```

Simulation parameters live in `configuration/config.xml`. Statistics are written out at the end of
each run.

## Layout

```
src/
  main/            entry point
  generic/         simulator loop, event queue, instruction/operand model, statistics
  processor/
    pipeline/      the five stages and the latches between them
    memorysystem/  main memory
  configuration/   config parsing and simulation parameters
```

## Notes

Coursework. The lab scaffolding — configuration parsing, the instruction/operand model and the
event-queue framework — was provided with the assignment; the pipeline stages, inter-stage latches,
hazard detection and stalling logic are my implementation.
