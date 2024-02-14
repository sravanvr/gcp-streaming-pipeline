package io.github.streamingpipeline.utils;

/***
 * 
 * @author SravanVedala
 *
 */
public enum SeverityLevel {
    WARNING(1),
    ERROR(2),
    FATAL(3);

    private int value;
    private SeverityLevel(int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }
    public String toString() {
        return Integer.toString(this.value);
    }
}
