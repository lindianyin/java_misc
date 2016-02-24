package com.nfl.kfb.tcp;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.HashMap;

import org.apache.mina.core.session.IoSession;

public class MethodMap {
	private HashMap<Integer, Method> methodMap = new HashMap<>();
	private IoSession ioSession = null;
	
	public void registMethod(CMD cmd, Class<?> owner, String methodName,
			Object[] args) {
		Class ownerClass = owner.getClass();
		
		String name = owner.getName();
		System.out.println("name:"+name);
		Object attribute = ioSession.getAttribute(name);
		if(attribute == null){
			try {
				//attribute = owner.newInstance();
				Class[] pType = new Class[]{IoSession.class}; 
				Constructor ctor = owner.getConstructor(pType);
				Object[] param = new Object[]{ioSession};
				
				attribute = ctor.newInstance(param);
				ioSession.setAttribute(name, attribute);
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		Class[] argsClass = new Class[args.length];

		for (int i = 0, j = args.length; i < j; i++) {
			argsClass[i] = (Class)args[i];
		}
		
		Method method = null;
		try {
			method = owner.getMethod(methodName, argsClass);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		}
		
		methodMap.put(cmd.getValue(), method);
	}
	
	public  Method getInvokeMethod(int cmd){
		return methodMap.get(cmd);
	}
	public Object getInvokeObject(int cmd){
		
		String className = methodMap.get(cmd).getDeclaringClass().getName();
		System.out.println("className"+className);
		return ioSession.getAttribute(className);
	}
	
	
	public MethodMap(IoSession ioSession){
		this.ioSession = ioSession;
		registMethod(CMD.ADD,Arena.class,"add",new Object[]{String.class});
		registMethod(CMD.ENTER_ARENA,Arena.class,"enterArena",new Object[]{String.class});
		registMethod(CMD.LEAVE_ARENA,Arena.class,"leaveArena",new Object[]{});
		registMethod(CMD.ENTER_AREAAA,Arena.class,"enterAreaaa",new Object[]{int.class});
		registMethod(CMD.SUBMIT_POINT,Arena.class,"submitPoint",new Object[]{long.class,int.class,boolean.class,int.class});
		registMethod(CMD.GET_WORLD_FIGHT_LIST,Arena.class,"getWorldFightList",new Object[]{});
		
	}

	
	
	
	
}
