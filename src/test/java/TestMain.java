import java.util.ArrayList;
import java.util.List;

public class TestMain {

    public static void main(String args[]) {
       NumberGenerator generator = new RandomNumberGenerator();
       Observer digitObserver = new DegitObserver();
       Observer graphObserver = new GraphObserver();
       generator.addObserver(digitObserver);    // RandomNumberGeneratorはdigitObserverを実行する必要がある
       generator.addObserver(graphObserver);
       generator.execute();
    }

    // 実行するクラスのインターフェース
    public interface Observer {
        public abstract void update(NumberGenerator generator);
    }

    // 観測されるクラスの抽象クラス
    public abstract class NumberGenerator {
        private ArrayList observers = new ArrayList();  // 実行するクラスの保管
        public void addObserver(Observer observer) {
            observers.add(observer);
        }
        public void deleteObserver(Observer observer) {
            observers.add(observer);
        }
        public void notifyObservers() { // 実行せよ
            Iterator iterator = observers.iterator();
            while (iterator.hasNext()) {
                Observer observer = (Observer)iterator.next();
                observer.update(this);
            }
        }

        public abstract  int getNumber();   // 実行するクラスで呼び出したいメソッド
        public abstract void execute(); // 実際に実行を呼ぶメソッド
    }

    // 実行内容１
    public class DegitObserver implements Observer {
        public void update(NumberGenerator generator) {
            System.out.println("DegitObserber" + generator.getNumber());
        }
    }

    // 実行内容２
    public class GraphObserver implements Observer {
        public void update(NumberGenerator generator) {
            System.out.println("GraphObserver: ");
            int count = generator.getNumber();
            for (int i = 0; i < count; i++) {
                System.out.println("*");
            }
            System.out.println("");
        }
    }

    // 観測されるクラス１
    public class RandomNumberGenerator extends NumberGenerator {
        private Random random = new Random();
        private int number;

        public int getNumber() {
            return number;
        }

        public void execute() {
            for (int i = 0; i < 20; i++) {
                number = random.nextInt(50);
                notifyObservers();  // executeが終わったら観測してくれ
            }
        }
    }
}