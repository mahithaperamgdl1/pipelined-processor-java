package processor.memorysystem;

import processor.Clock;

public class CacheLine {
    int data;
    int tag;
    boolean valid;
    long lru_value;

    public CacheLine() {
        this.tag = -1;
        this.data = -1;
        this.valid = false;
        this.lru_value = 0;
    }

    public boolean isValid() {
        return this.valid;
    }

    /**
     * Marks this line as the most recently used one in its set.
     */
    public void touch() {
        this.lru_value = Clock.getCurrentTime();
    }

    public long getLruValue() {
        return this.lru_value;
    }

    /**
     * Installs a new tag and value into this line, replacing whatever was here.
     */
    public void fill(int newTag, int newData) {
        this.tag = newTag;
        this.data = newData;
        this.valid = true;
        touch();
    }

    public void setData(int newData) {
        this.data = newData;
        touch();
    }

    public int getData() {
        touch();
        return this.data;
    }

    public void setTag(int newTag) {
        this.tag = newTag;
    }

    public int getTag() {
        return this.tag;
    }
}
