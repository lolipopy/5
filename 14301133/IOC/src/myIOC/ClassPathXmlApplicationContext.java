package myIOC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import annotation.Autowired;


public class ClassPathXmlApplicationContext implements ApplicationContext {

	private List<Element> elementList;

	// clazzes存储所有已加载的、用于ref的类，key是类的限定名，value是类类
	private Map<String, Class<?>> clazzes;

	private Map<String, Object> refClass;

	// beans存储所有加载的beans类，key是id，value是类类
	private Map<String, Object> beans;

	public ClassPathXmlApplicationContext(String[] xmlLocation) {
		String location = xmlLocation[0];
		clazzes = new HashMap<String, Class<?>>();
		refClass = new HashMap<String, Object>();
		beans = new HashMap<String, Object>();
		try {
			readXmlFile(location);
			getClassFromXML();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getClassFromXML() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		// 载入所有ref类
		loadRefClasses();
		// 载入所有beans类
		loadBeanClasses();
	}

	/**
	 * 遍历property结点，寻找ref类并载入
	 * 
	 * @throws ClassNotFoundException
	 */
	private void loadRefClasses() throws ClassNotFoundException {
		for (Element element : elementList) {
			List<Element> propertyList = element.getChildren();
			for (Element element2 : propertyList) {
				String refStr = element2.getAttributeValue("ref");
				if (refStr != null)
					loadOneClass(refStr);
			}
		}
	}

	/**
	 * 载入一个类，并添加到clazzes中
	 * 
	 * @param className
	 *            类的限定名
	 * @throws ClassNotFoundException
	 */
	private void loadOneClass(String className) throws ClassNotFoundException {
		Class<?> clazz = Class.forName(className);
		if (clazzes.containsValue(clazz))
			return;
		else
			clazzes.put(className, clazz);
	}

	/**
	 * 载入所有bean类
	 * 
	 * @throws ClassNotFoundException
	 * @throws IllegalAccessException
	 * @throws InstantiationException
	 * @throws SecurityException
	 * @throws NoSuchMethodException
	 * @throws InvocationTargetException
	 * @throws IllegalArgumentException
	 */
	private void loadBeanClasses() throws ClassNotFoundException, InstantiationException, IllegalAccessException,
			NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
		// 第一次循环，寻找并创建不使用引用对象的object
		outLoop: for (Element element : elementList) {
			// 获取id和类限定名
			String className = element.getAttributeValue("class");
			Class<?> clazz = Class.forName(className);

			// 读取属性
			List<Element> propertyList = element.getChildren();
			Class<?>[] classSet = new Class<?>[propertyList.size()];
			Object[] objectSet = new Object[propertyList.size()];
			// List<Class<?>> classList = new ArrayList<Class<?>>();
			// List<Object> objectList = new ArrayList<Object>();

			// 寻找 @autowire
			List<Class<?>> list = AnnoUtil.getPackageController("test", Autowired.class);
//			System.out.println(list);

			int i = 0;
			// 创建被引用的object
			for (Element element2 : propertyList) {
				String refStr = element2.getAttributeValue("ref");
				if (refStr != null) {
					// 如果是引用对象，进行下次循环
					continue outLoop;
				} else {
					// 否则，作为String添加
					String value = element2.getAttributeValue("value");
					if (value != null) {
						classSet[i] = String.class;
						objectSet[i] = value;
						i++;
						// classList.add(String.class);
						// objectList.add(value);
						// Class<?>[] classSet = (Class<?>[])
						// classList.toArray();
						// Class<?>[] objectSet = (Class<?>[])
						// objectList.toArray();
						// 寻找构造器
						Constructor<?> con = clazz.getConstructor(classSet);
						con.setAccessible(true);
						Object classObj = con.newInstance(objectSet);
						refClass.put(className, classObj);
					}
				}
			}
		}

		// 第二次循环
		outLoop1: for (Element element : elementList) {
			// 获取id和类限定名
			String classId = element.getAttributeValue("id");
			String className = element.getAttributeValue("class");
			Class<?> clazz = Class.forName(className);

			// 读取属性
			List<Element> propertyList = element.getChildren();
			Class<?>[] classSet = new Class<?>[propertyList.size()];
			Object[] objectSet = new Object[propertyList.size()];

			int i = 0;
			// 创建被引用的object
			for (Element element2 : propertyList) {
				String refStr = element2.getAttributeValue("ref");
				if (refStr != null) {
					// 如果是引用对象，寻找对应的引用对象
					Object refObj = refClass.get(refStr);
					// 添加
					classSet[i] = Class.forName(refStr);
					objectSet[i] = refObj;
					i++;
					// classList.add(Class.forName(refStr));
					// objectList.add(refObj);
				} else {
					// 否则，进行下次循环
					continue outLoop1;
				}
			}

			// Class<?>[] classSet = (Class<?>[]) classList.toArray();
			// Class<?>[] objectSet = (Class<?>[]) objectList.toArray();
			// 寻找构造器，创建对象
			Constructor<?> con = clazz.getConstructor(classSet);
			con.setAccessible(true);
			Object classObj = con.newInstance(objectSet);

			// 添加到beans
			beans.put(classId, classObj);
		}
	}

	/**
	 * 读取xml文件，获得elementList
	 * 
	 * @param location
	 * @throws FileNotFoundException
	 * @throws JDOMException
	 * @throws IOException
	 */
	private void readXmlFile(String location) throws FileNotFoundException, JDOMException, IOException {
		@SuppressWarnings("deprecation")
		SAXBuilder builder = new SAXBuilder(false);
		Document document = builder.build(new FileInputStream(new File(location)));
		Element root = document.getRootElement();
		elementList = root.getChildren("bean");
	}

	@Override
	public Object getBean(String bean) {
		return beans.get(bean);
	}

}
