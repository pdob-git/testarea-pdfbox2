package pl.pdob.pdftables.solution.boxfinderinternal;

public class RangeOperator {


    private RangeOperator() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Constant used for cases where corners of tables doesn't fit exactly
     */
    private static final int ALLOWANCE = 1;
    /**
     * Calculated if number is between lowerBound and upperBound including border values
     * Method for doubles.
     * @param number checked number
     * @param lowerBound min value
     * @param upperBound max value
     * @return True if in range, False if outside range
     */
    public static boolean isInClosedRange(Double number, Double lowerBound, Double upperBound) {
        return (lowerBound - ALLOWANCE <= number && number <= upperBound + ALLOWANCE);
    }


    /**
     * Calculated if number is between lowerBound and upperBound including border values
     * Method for floats.
     * @param number checked number
     * @param lowerBound min value
     * @param upperBound max value
     * @return True if in range, False if outside range
     */
    public static boolean isInClosedRange(Float number, Float lowerBound, Float upperBound) {
        return (lowerBound - ALLOWANCE <= number && number <= upperBound + ALLOWANCE);
    }

}
