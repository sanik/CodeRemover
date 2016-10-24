package ru.redserver.coderemover;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import static ru.redserver.coderemover.CodeRemover.DEEP_LOG;
import static ru.redserver.coderemover.CodeRemover.LOG;

/**
 * Ищет аннотации и создаёт
 * @author Nuclear
 */
public class AnnotationProccessor {

	public static ClassChangeList processClass(CtClass clazz, boolean parent) throws ClassNotFoundException, NotFoundException, CannotCompileException {
		ClassChangeList classChangeList = new ClassChangeList();

		// Если в родительских классах {метод, поле} был удалён, проверяем, не перезаписан ли он в этом классе
		if(clazz.getSuperclass() != CodeRemover.CLASS_POOL.get("java.lang.Object")) {
			ClassChangeList superClassChange = processClass(clazz.getSuperclass(), true);

			if(parent) classChangeList.merge(superClassChange);

			// Ищем по родительским методам
			List<CtMethod> methods = Arrays.asList(clazz.getDeclaredMethods());
			for(String superMethod : superClassChange.getMethods()) {
				for(CtMethod method : methods) {
					if(superMethod.equals(method.getName())) {
						classChangeList.getMethods().add(superMethod);
					}
				}
			}

			// Ищем по родительским полям
//			List<CtField> fields = Arrays.asList(clazz.getDeclaredFields());
//			for(String superField : superClassChange.getFields()) {
//				for(CtField field : fields) {
//					if(superField.equals(field.getName())) {
//						classChangeList.getFields().add(superField);
//					}
//				}
//			}
		}

		// Проверяем класс
		Removable classAnnotation = (Removable)clazz.getAnnotation(Removable.class);
		if(classAnnotation != null) {
			if(classAnnotation.remove()) {
				// Помечаем класс на удаление, сразу возвращаем список изменений класса
				classChangeList.removeClass();
				return classChangeList;
			} else {
				// Удаляем аннотацию
				AnnotationsAttribute attr = (AnnotationsAttribute)clazz.getClassFile().getAttribute(AnnotationsAttribute.invisibleTag);
				attr.removeAnnotation(Removable.class.getName());
				clazz.getClassFile().addAttribute(attr);
			}
		}

		// Проверяем методы
		for(CtMethod method : clazz.getDeclaredMethods()) {
			Removable methodAnnotation = (Removable)method.getAnnotation(Removable.class);
			if(methodAnnotation != null) {
				if(methodAnnotation.remove()) {
					// Помечаем метод на удаление
					classChangeList.getMethods().add(method.getName());
				} else {
					// Удаляем аннотацию
					AnnotationsAttribute attr = (AnnotationsAttribute)method.getMethodInfo().getAttribute(AnnotationsAttribute.invisibleTag);
					attr.removeAnnotation(Removable.class.getName());
					method.getMethodInfo().addAttribute(attr);
				}
			}
		}

		// Проверяем поля
		for(CtField field : clazz.getDeclaredFields()) {
			Removable fieldAnnotation = (Removable)field.getAnnotation(Removable.class);
			if(fieldAnnotation != null) {
				if(fieldAnnotation.remove()) {
					// Помечаем поле на удаление
					classChangeList.getFields().add(field.getName());
				} else {
					// Удаляем аннотацию
					AnnotationsAttribute attr = (AnnotationsAttribute)field.getFieldInfo().getAttribute(AnnotationsAttribute.invisibleTag);
					attr.removeAnnotation(Removable.class.getName());
					field.getFieldInfo().addAttribute(attr);
				}
			}
		}

		return classChangeList;
	}

	public static CtClass applyChange(ClassChangeList changeList, CtClass clazz) throws CannotCompileException {
		// Удаляю методы из класса
		changeList.getMethods().forEach(method -> {
			try {
				if(DEEP_LOG)
					LOG.log(Level.INFO, "Удаление метода {0} в классе {1}.", new Object[]{method, clazz.getName()});
				clazz.removeMethod(clazz.getDeclaredMethod(method));
			} catch (NotFoundException ex) {
			}
		});
		// Удаляю поля из класса
		changeList.getFields().forEach(field -> {
			try {
				if(DEEP_LOG)
					LOG.log(Level.INFO, "Удаление поля {0} в классе {1}.", new Object[]{field, clazz.getName()});
				clazz.removeField(clazz.getDeclaredField(field));
			} catch (NotFoundException ex) {
			}
		});

		return clazz;
	}

}
