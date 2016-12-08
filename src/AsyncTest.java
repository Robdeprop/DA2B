import java.util.Date;
import java.util.Random;

public class AsyncTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		System.out.println("Starting");
		T1 t1 = new T1();
		new Thread(t1).start();
	}

}


interface Callback {
    void callback(); // would be in any signature
}