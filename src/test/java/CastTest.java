/**
 * @author HHH
 * @date 2017/11/16
 */
public class CastTest {

    public static void main(String[] args) {
        // 1:test 向上转型
        Person p = new Student();
        p.setName("helingyu1");
        // p.doTest(); // ps：无法通过编译，在执行向上转型时，子类会丢失所有除与父类共有的成员方法

        // 2:test 向下转型
        Person p0 = new Person();
        p0.setName("helingyu");
        //Student s = (Student) p0; // 可以通过编译
        //System.out.println(s.getName()); // 但是会抛出异常 ClassCastException: Person cannot be cast to Student。（此为情况1，p0指向的是父类）

        Student s0 = (Student) p;
        System.out.println(s0.getName()); // 此时不会报错，因为此时p引用指向的真实类是子类Student

    }
}

class Person{
    private String name;

    private int age;

    private String sex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }
}

class Student extends Person{
    public String doTest(){
        return "hello world";
    }
}
