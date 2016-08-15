package callback;

/**
 * Created by zhoukai1 on 2016/8/15.
 */
public class Main {
    public static void main(String[] args) {
        int a = 20;
        int b = 20;
        Stu stu = new Stu();
        stu.setName("zhoukai");
        stu.callHelp(a, b);
    }
}
