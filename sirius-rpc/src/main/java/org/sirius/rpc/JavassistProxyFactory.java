package org.sirius.rpc;

import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.sirius.rpc.consumer.invoker.Invoker;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;

/*
 * 生成如下格式的代理
 * 
 * public interface  A {

	public  Object get();
	public void set(String name);
	public void set(String name1,String name2);
}

 * public class A_proxy_0   implements A { 
 * 
     private final Invoker invoker;
     public static Method[] methods;
  
    public Invoker getInvoker()
    {
       return this.invoker;
    }
  
    public A_proxy_0(Invoker paramInvoker)
    {
       this.invoker = paramInvoker;
    }
  
    public Object get()
    {
       Object[] arrayOfObject = null;
       return (Object)this.invoker.invoke(methods[0], arrayOfObject);
    }
  
    public void set(String paramString)
    {
       Object[] arrayOfObject = { paramString };
       this.invoker.invoke(methods[1], arrayOfObject);
    }
  
    public void set(String paramString1, String paramString2)
    {
       Object[] arrayOfObject = { paramString1, paramString2 };
       this.invoker.invoke(methods[2], arrayOfObject);
    }
}

 */
public class JavassistProxyFactory implements ProxyFactory {

	private static final AtomicLong PROXY_CLASS_COUNTER = new AtomicLong(0);

	@Override
	public Object getProxy(Invoker invoker, Class clazz) throws Exception {
		if (clazz == null) {
			throw new RuntimeException("clazz must not be null");
		}

		if (!clazz.isInterface()) {
			throw new RuntimeException("clazz must not be an interface");
		}
		Method[] allMethods = clazz.getDeclaredMethods();
		ArrayList<Method> allPublicMethods = (ArrayList<Method>) Stream.of(allMethods)//
				.filter(m -> Modifier.isPublic(m.getModifiers()))//
				.filter(m -> !Modifier.isStatic(m.getModifiers()))//
				.collect(Collectors.toList());

		final String remoteClassName = clazz.getName() + "_proxy_"//
				+ PROXY_CLASS_COUNTER.getAndIncrement();

		// 创建类
		ClassPool pool = ClassPool.getDefault();
		CtClass remoteCtClass = pool.makeClass(remoteClassName);

		CtClass[] interfaces = { pool.getCtClass(clazz.getName()) };
		remoteCtClass.setInterfaces(interfaces);

		// 添加invoker字段
		CtField invokerField = new CtField(pool.get(Invoker.class.getName()), "invoker", remoteCtClass);
		invokerField.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
		remoteCtClass.addField(invokerField);

		// 添加method[]字段
		StringBuilder methodsFieldBuilder = new StringBuilder("public static java.lang.reflect.Method[] methods;\r\n");
		CtField methodsField = CtField.make(methodsFieldBuilder.toString(), remoteCtClass);
		remoteCtClass.addField(methodsField);

		// 添加get方法
		remoteCtClass.addMethod(CtNewMethod.getter("getInvoker", invokerField));

		// 添加有参的构造函数
		CtConstructor constructor1 = new CtConstructor(new CtClass[] { pool.get(Invoker.class.getName()) },
				remoteCtClass);
		constructor1.setBody("{$0.invoker = $1;}");
		remoteCtClass.addConstructor(constructor1);
		// 开始创建代理方法
		int n = 0;
		for (Method method : allPublicMethods) {
			StringBuilder methodBuilder = new StringBuilder();
			Class<?> returnType = method.getReturnType();
			String returnName = returnType.equals(Void.TYPE) ? "void" : returnType.getName();
			methodBuilder.append("public ").append(returnName + " ").append(method.getName()).append("(");
			// 构造参数列表(parm0,parm1....)
			int i = 0;
			Class<?>[] argTypes = method.getParameterTypes();
			if (argTypes.length != 0) {
				for (Class<?> argType : argTypes) {
					methodBuilder.append(argType.getName()).append(" ").append("parm" + i);
					if (i != method.getParameterCount() - 1) {
						methodBuilder.append(",");
					}
					i++;
				}
			}
			// 生成参数数组 Object[] parmArray ={parm0 ,parm1 ....}
			StringBuilder parmArrayBuilder = generateParmArray(method);
			methodBuilder.append(")")
					// 开始生成方法体
					.append("{\r\n").append(parmArrayBuilder.toString()).append(";\r\n");

			if (returnName != "void") {
				methodBuilder.append("return ").append("(").append(returnName).append(") ");
			}
			methodBuilder.append("this.invoker.invoke").append("(").append("methods[" + n + "],").append("parmArray")
					.append(");\r\n}");
			CtMethod m = CtNewMethod.make(methodBuilder.toString(), remoteCtClass);
			remoteCtClass.addMethod(m);
			n++;
		}
		Class<?> invokerClass = remoteCtClass.toClass();
		// 初始化method[];
		invokerClass.getField("methods").set(null, allPublicMethods.toArray(new Method[allPublicMethods.size()]));

		byte[] code = remoteCtClass.toBytecode();
		FileOutputStream fos = new FileOutputStream(remoteCtClass.getName() + ".class");
		fos.write(code);
		fos.close();
		return invokerClass.getConstructor(Invoker.class).newInstance(invoker);

	}

	private StringBuilder generateParmArray(Method method) {
		int length = method.getParameterCount();
		StringBuilder parmArrayBuilder = new StringBuilder();
		parmArrayBuilder.append("Object[] parmArray").append(" = ");
		if (length != 0) {
			parmArrayBuilder.append("{");
			for (int i = 0; i < length; i++) {
				parmArrayBuilder.append("parm" + i);
				if (i != length - 1) {
					parmArrayBuilder.append(",");
				} else {
					parmArrayBuilder.append("}");
				}
			}
		} else {
			parmArrayBuilder.append("null");
		}
		return parmArrayBuilder;
	}

}
