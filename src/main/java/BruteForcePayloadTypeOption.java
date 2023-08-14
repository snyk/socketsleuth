public enum BruteForcePayloadTypeOption {
    SIMPLE_LIST("Simple list"),
    NUMBERS("Numbers");

    private final String option;

    BruteForcePayloadTypeOption(String option) {
        this.option = option;
    }

    @Override
    public String toString() {
        return option;
    }
}
