package processor.memorysystem;

import configuration.Configuration;
import generic.Element;
import generic.Event;
import generic.MemoryReadEvent;
import generic.MemoryResponseEvent;
import generic.MemoryWriteEvent;
import generic.Simulator;
import processor.Clock;
import processor.Processor;
import processor.pipeline.MemoryAccess;

/**
 * An N-way set associative, write-through cache.
 *
 * Main memory here is word addressed, and a line holds a single word, so the
 * incoming address is already a line address and needs no block offset. The
 * address therefore splits into just two fields:
 *
 *     tag = address / numberOfSets        setIndex = address % numberOfSets
 *
 * A lookup selects one set and compares the tag against every way in it.
 * On a miss the victim is an invalid way if the set has one, otherwise the
 * least recently used way in that set.
 */
public class Cache implements Element {
    Processor containingProcessor;
    int cacheSize;
    int cacheLatency;
    int associativity;
    int numberOfLines;
    int numberOfSets;
    CacheLine[][] sets;

    int cacheMissAddress;
    Element cacheMissElement;
    boolean load_request = false;
    int StoreData;

    public Cache(Processor containingProcessor, int size, int associativity) {
        this.containingProcessor = containingProcessor;
        this.cacheSize = size;

        // Line size is 4B, and memory is word addressed, so one word per line.
        this.numberOfLines = (cacheSize / 4);

        // A cache can be no more associative than it has lines.
        this.associativity = Math.max(1, Math.min(associativity, numberOfLines));
        this.numberOfSets = numberOfLines / this.associativity;

        if (cacheSize == 16) {
            cacheLatency = 1;
        } else if (cacheSize == 128) {
            cacheLatency = 2;
        } else if (cacheSize == 512) {
            cacheLatency = 3;
        } else if (cacheSize == 1024) {
            cacheLatency = 4;
        }

        sets = new CacheLine[numberOfSets][this.associativity];
        for (int s = 0; s < numberOfSets; s++) {
            for (int w = 0; w < this.associativity; w++) {
                sets[s][w] = new CacheLine();
            }
        }
    }

    public int getCacheLatency() {
        return cacheLatency;
    }

    public int getAssociativity() {
        return associativity;
    }

    public int getNumberOfSets() {
        return numberOfSets;
    }

    // Package private rather than private so the unit tests can reach them.
    int setIndexOf(int address) {
        return Math.floorMod(address, numberOfSets);
    }

    int tagOf(int address) {
        return Math.floorDiv(address, numberOfSets);
    }

    /**
     * Returns the way in this address's set holding it, or null on a miss.
     */
    CacheLine lookup(int address) {
        CacheLine[] set = sets[setIndexOf(address)];
        int tag = tagOf(address);

        for (CacheLine line : set) {
            if (line.isValid() && line.getTag() == tag) {
                return line;
            }
        }
        return null;
    }

    /**
     * Picks the way to replace within a set: a free way if one exists,
     * otherwise the least recently used way.
     */
    CacheLine selectVictim(int setIndex) {
        CacheLine[] set = sets[setIndex];

        for (CacheLine line : set) {
            if (!line.isValid()) {
                return line;
            }
        }

        CacheLine victim = set[0];
        for (CacheLine line : set) {
            if (line.getLruValue() < victim.getLruValue()) {
                victim = line;
            }
        }
        return victim;
    }

    public void cacheRead(int address, Element requestingElement) {
        CacheLine line = lookup(address);

        if (line != null) {
            Simulator.getEventQueue().addEvent(
                    new MemoryResponseEvent(
                            Clock.getCurrentTime(),
                            this,
                            requestingElement,
                            line.getData()));
        } else {
            load_request = true;
            handleCacheMiss(address, requestingElement);
        }
    }

    public void cacheWrite(int address, int data, Element requestingElement) {
        CacheLine line = lookup(address);

        if (line != null) {
            line.setData(data);

            // Write-through: the store is also sent on to main memory.
            Simulator.getEventQueue().addEvent(
                    new MemoryWriteEvent(
                            Clock.getCurrentTime(),
                            this,
                            containingProcessor.getMainMemory(),
                            address,
                            data));

            ((MemoryAccess) requestingElement).MA_RW_Latch.setRW_enable(true);
            ((MemoryAccess) requestingElement).EX_MA_Latch.setMA_Busy(false);
        } else {
            // Write-allocate: fetch the line first, then retry the store.
            StoreData = data;
            load_request = false;
            handleCacheMiss(address, requestingElement);
        }
    }

    public void handleResponse(int data) {
        // Install the returned word, evicting the LRU way of that set if full.
        CacheLine victim = selectVictim(setIndexOf(cacheMissAddress));
        victim.fill(tagOf(cacheMissAddress), data);

        if (load_request) {
            Simulator.getEventQueue().addEvent(
                    new MemoryResponseEvent(
                            Clock.getCurrentTime(),
                            this,
                            cacheMissElement,
                            data));
        } else {
            // The line is present now, so this store will hit.
            cacheWrite(cacheMissAddress, StoreData, cacheMissElement);
        }
    }

    public void handleCacheMiss(int address, Element requestingElement) {
        Simulator.getEventQueue().addEvent(
                new MemoryReadEvent(
                        Clock.getCurrentTime() + Configuration.mainMemoryLatency,
                        this,
                        containingProcessor.getMainMemory(),
                        address));
        cacheMissAddress = address;
        cacheMissElement = requestingElement;
    }

    @Override
    public void handleEvent(Event e) {
        if (e.getEventType() == Event.EventType.MemoryResponse) {
            MemoryResponseEvent event = (MemoryResponseEvent) e;
            handleResponse(event.getValue());
        } else if (e.getEventType() == Event.EventType.MemoryWrite) {
            MemoryWriteEvent event = (MemoryWriteEvent) e;
            cacheWrite(event.getAddressToWriteTo(), event.getValue(), event.getRequestingElement());
        } else if (e.getEventType() == Event.EventType.MemoryRead) {
            MemoryReadEvent event = (MemoryReadEvent) e;
            cacheRead(event.getAddressToReadFrom(), event.getRequestingElement());
        }
    }
}
