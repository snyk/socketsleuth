public class JSONRPCMethodItem {
    private String methodName;
    private String request;
    private String response;

    public JSONRPCMethodItem(String methodName, String request, String response) {
        this.methodName = methodName;
        this.request = request;
        this.response = response;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getRequest() {
        return request;
    }

    public String getResponse() {
        return response;
    }

    // Override the toString method to display the method name in the JList
    @Override
    public String toString() {
        return methodName;
    }
}