public class TestMain {

    public static void main(String args[]) {
        System.out.println(A.reKey());
    }
}

class A {

    public static String reKey() {
        StackTraceElement[] stackTraceElements = (new Throwable()).getStackTrace();
        String methodName = stackTraceElements[1].getMethodName();
        String className = stackTraceElements[1].getClassName();
        int line = stackTraceElements[1].getLineNumber();

        return className + methodName + line;
    }
}