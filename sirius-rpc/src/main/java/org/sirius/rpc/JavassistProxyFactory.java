package org.sirius.rpc;

import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;

public class JavassistProxyFactory implements ProxyFactory {

	@Override
	public <T> T getProxy(Invoker invoker, Class<T> clazz) throws Exception {
		if(clazz == null) {
			throw new RuntimeException("clazz must not be null");
		}
		
		if(!clazz.isInterface()) {
			throw new RuntimeException("clazz must not be an interface");
		}
		Method[] allMethods = clazz.getDeclaredMethods();
		List<Method> allPublicMethods = Stream
				.of(allMethods)//
				.filter(m -> Modifier.isPublic(m.getModifiers()))//
				.filter(m -> !Modifier.isStatic(m.getModifiers()))//
				.collect(Collectors.toList());
		
		final String remoteClassName = clazz.getName() + "_proxy_"//
				+ UUID.randomUUID().toString().replace("-", "");

		// 创建类
		ClassPool pool = ClassPool.getDefault();
		CtClass remoteCtClass = pool.makeClass(remoteClassName);

		CtClass[] interfaces = { pool.getCtClass(clazz.getName())};
		remoteCtClass.setInterfaces(interfaces);

		// 添加私有成员
		CtField invokerField = new CtField(pool.get(Invoker.class.getName()), "invoker", remoteCtClass);
		invokerField.setModifiers(Modifier.PRIVATE | Modifier.FINAL);
		remoteCtClass.addField(invokerField);

		// 添加get方法
		remoteCtClass.addMethod(CtNewMethod.getter("getInvoker", invokerField));

		// 添加有参的构造函数
		CtConstructor constructor1 = new CtConstructor(new CtClass[] { pool.get(Invoker.class.getName()) }, remoteCtClass);
		constructor1.setBody("{$0.invoker = $1;}");
		remoteCtClass.addConstructor(constructor1);
        for(Method method : allPublicMethods) {
        	
        	StringBuilder methodBuilder = new StringBuilder();
        	Class<?> returnType = method.getReturnType();
        	String returnName = returnType != null ? returnType.getName() :"void"; 
        	methodBuilder.append("public ")
                         .append(returnName + " ")
                         .append(method.getName())
                         .append("(");
        	int i = 0;
        	Class<?>[] argTypes =  method.getParameterTypes();
        	if(argTypes.length != 0) {
        		for(Class<?> argType : argTypes) {
            		methodBuilder.append(argType.getName())
            		             .append(" ")
            		             .append("parm" + i);
            		if(i != method.getParameterCount() - 1) {
            			methodBuilder.append(",");
            		} 
            		i++;
            	}
        	}
        	StringBuilder parmArrayBuilder = generateParmArray(method);
        	methodBuilder.append(")")
        	             .append("{\r\n")
        		         .append(parmArrayBuilder.toString())
	                     .append(";\r\n");
        	
        	if(returnName != "void") {
               methodBuilder.append("return ")
                            .append("(")
                            .append(returnName)
                            .append(") ");
        	}
        	methodBuilder.append("this.invoker.invoke")
        	             .append("(")
        	             .append("parmArray")
        	             .append(");\r\n}");
        	System.out.println(methodBuilder.toString());
            CtMethod m = CtNewMethod.make(methodBuilder.toString(), remoteCtClass);
            remoteCtClass.addMethod(m);
        }
        Class<?> invokerClass = remoteCtClass.toClass();
        byte[] code = remoteCtClass.toBytecode();
		FileOutputStream fos = new FileOutputStream(remoteCtClass.getName()+".class");  
        fos.write(code);  
        fos.close();  
		return (T) invokerClass.getConstructor(Invoker.class).newInstance(invoker);
	
	}

	private StringBuilder generateParmArray(Method method) {
    	int length = method.getParameterCount();
    	StringBuilder parmArrayBuilder = new StringBuilder();
    	parmArrayBuilder.append("Object[] parmArray")
                        .append(" = ");
    	if(length != 0) {
    		parmArrayBuilder.append("{");
    		for(int i = 0;i<length;i++) {
    			parmArrayBuilder.append("parm" + i);
        		if(i != length - 1) {
        			parmArrayBuilder.append(",");
        		} else {
        			parmArrayBuilder.append("}");
        		}
    		}
    	}
    	else {
    		parmArrayBuilder.append("null");
    	}
		return parmArrayBuilder;
	}
}
