package callback;

/**
 * Created by zhoukai1 on 2016/8/15.
 */
public class SupperCal {
    public void add(int a, int b, DoJob job) {
        int result = a + b;
        job.fillResult(a, b, result);
    }
}
