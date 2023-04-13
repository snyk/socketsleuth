public enum WSIntruderType {
    JSONRPCMETHOD("JSONRPC method discovery"),
    JSONRPCPARAM("JSONRPC param discovery"),
    SNIPER("Sniper");

    private final String value;

    WSIntruderType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return value;
    }
}
