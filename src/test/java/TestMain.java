import java.util.ArrayList;
import java.util.List;

public class TestMain {

    public static void main(String args[]) {
        List<String> list = new ArrayList<>();
        list.add("A");
        list.add("B");
        list.remove("A");
        System.out.println(list.get(0));
    }
}