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

	// clazzes�洢�����Ѽ��صġ�����ref���࣬key������޶�����value������
	private Map<String, Class<?>> clazzes;

	private Map<String, Object> refClass;

	// beans�洢���м��ص�beans�࣬key��id��value������
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
		// ��������ref��
		loadRefClasses();
		// ��������beans��
		loadBeanClasses();
	}

	/**
	 * ����property��㣬Ѱ��ref�ಢ����
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
	 * ����һ���࣬����ӵ�clazzes��
	 * 
	 * @param className
	 *            ����޶���
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
	 * ��������bean��
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
		// ��һ��ѭ����Ѱ�Ҳ�������ʹ�����ö����object
		outLoop: for (Element element : elementList) {
			// ��ȡid�����޶���
			String className = element.getAttributeValue("class");
			Class<?> clazz = Class.forName(className);

			// ��ȡ����
			List<Element> propertyList = element.getChildren();
			Class<?>[] classSet = new Class<?>[propertyList.size()];
			Object[] objectSet = new Object[propertyList.size()];
			// List<Class<?>> classList = new ArrayList<Class<?>>();
			// List<Object> objectList = new ArrayList<Object>();

			// Ѱ�� @autowire
			List<Class<?>> list = AnnoUtil.getPackageController("test", Autowired.class);
//			System.out.println(list);

			int i = 0;
			// ���������õ�object
			for (Element element2 : propertyList) {
				String refStr = element2.getAttributeValue("ref");
				if (refStr != null) {
					// ��������ö��󣬽����´�ѭ��
					continue outLoop;
				} else {
					// ������ΪString���
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
						// Ѱ�ҹ�����
						Constructor<?> con = clazz.getConstructor(classSet);
						con.setAccessible(true);
						Object classObj = con.newInstance(objectSet);
						refClass.put(className, classObj);
					}
				}
			}
		}

		// �ڶ���ѭ��
		outLoop1: for (Element element : elementList) {
			// ��ȡid�����޶���
			String classId = element.getAttributeValue("id");
			String className = element.getAttributeValue("class");
			Class<?> clazz = Class.forName(className);

			// ��ȡ����
			List<Element> propertyList = element.getChildren();
			Class<?>[] classSet = new Class<?>[propertyList.size()];
			Object[] objectSet = new Object[propertyList.size()];

			int i = 0;
			// ���������õ�object
			for (Element element2 : propertyList) {
				String refStr = element2.getAttributeValue("ref");
				if (refStr != null) {
					// ��������ö���Ѱ�Ҷ�Ӧ�����ö���
					Object refObj = refClass.get(refStr);
					// ���
					classSet[i] = Class.forName(refStr);
					objectSet[i] = refObj;
					i++;
					// classList.add(Class.forName(refStr));
					// objectList.add(refObj);
				} else {
					// ���򣬽����´�ѭ��
					continue outLoop1;
				}
			}

			// Class<?>[] classSet = (Class<?>[]) classList.toArray();
			// Class<?>[] objectSet = (Class<?>[]) objectList.toArray();
			// Ѱ�ҹ���������������
			Constructor<?> con = clazz.getConstructor(classSet);
			con.setAccessible(true);
			Object classObj = con.newInstance(objectSet);

			// ��ӵ�beans
			beans.put(classId, classObj);
		}
	}

	/**
	 * ��ȡxml�ļ������elementList
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
