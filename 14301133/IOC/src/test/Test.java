package test;

import model.Boss;
import myIOC.ApplicationContext;
import myIOC.ClassPathXmlApplicationContext;

public class Test {

	public static void main(String[] args) {
		String[] locations = {"bean.xml"};
		ApplicationContext ctx = new ClassPathXmlApplicationContext(locations);
		Boss boss = (Boss) ctx.getBean("boss");
		System.out.println(boss.toString());
		System.out.println("∏…µ√∆Ø¡¡£°ªÔº∆£°");
		
	}

}
