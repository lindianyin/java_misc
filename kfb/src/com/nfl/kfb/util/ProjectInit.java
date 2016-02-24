package com.nfl.kfb.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class ProjectInit implements ServletContextListener {

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		System.out.println("contextDestroyed");
		
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		System.out.println("contextInitialized");
		System.out.println(DebugOption.REDIS_URL);
	}

}
