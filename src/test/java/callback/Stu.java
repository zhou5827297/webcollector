package callback;

/**
 * Created by zhoukai1 on 2016/8/15.
 */
public class Stu {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public class doHomeWork implements DoJob {

        @Override
        public void fillResult(int a, int b, int result) {
            System.out.println(name + "求助小红计算" + a + "+" + b + "=" + result);
        }
    }

    public void callHelp(int a, int b) {
        new SupperCal().add(a, b, new doHomeWork());
    }
}
