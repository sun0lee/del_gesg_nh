package com.gof.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.hibernate.SessionFactory;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Environment;

import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ToString
public class HibernateUtil {
	
	private static StandardServiceRegistry registry;
	private static SessionFactory sessionFactory;    
    
	public static SessionFactory getSessionFactory() {
		
		if(sessionFactory == null) {
			
			Map<String, String> settings = new HashMap<String,String>();
//			settings.put(Environment.DRIVER,  "oracle.jdbc.driver.OracleDriver");
//			settings.put(Environment.DIALECT, "org.hibernate.dialect.OracleDialect");
//			settings.put(Environment.URL,     "jdbc:oracle:thin:@localhost:1521/ORCL");			
//			settings.put(Environment.USER,    "GESG");			
//			settings.put(Environment.PASS,    "gesg1234");		
			
//			settings.put(Environment.URL,     "jdbc:oracle:thin:@10.120.120.172:1521/NDKI");			
//			settings.put(Environment.USER,    "RS_APP");			
//			settings.put(Environment.PASS,    "app!2016");
			settings.put(Environment.DRIVER,  "org.h2.Driver");
			settings.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
			settings.put(Environment.URL,     "jdbc:h2:tcp://localhost/~/H2DB/db/ESG");			
			settings.put(Environment.USER,    "NESG");	
			settings.put(Environment.PASS,    "test");	
			log.info("getSesson Factory no Arg");			
			return genSessionFactory(settings);
			
		}
		return sessionFactory;
	  }

	
	public static SessionFactory getSessionFactory(Properties prop) {
		
		Map<String, String> settings = new HashMap<String,String>();
		
		settings.put(Environment.DRIVER,  prop.getProperty("driver"));
		settings.put(Environment.URL,     prop.getProperty("url"));
		settings.put(Environment.USER,    prop.getProperty("username"));
		settings.put(Environment.PASS,    prop.getProperty("password"));
		settings.put(Environment.DIALECT, prop.getProperty("dialect"));

		log.info("getSesson Factory with Arg");
		return genSessionFactory(settings);
		
	  }
	
	public static void shutdown() {
		
//		sessionFactory.close();
//		registry.close();		
		
	    if (registry != null) {
	      StandardServiceRegistryBuilder.destroy(registry);	      
	    }
	  }


	private static SessionFactory genSessionFactory(Map<String, String> settings) {
		
	    if (sessionFactory == null) {
	      try {
	        // Create registry
	        registry = new StandardServiceRegistryBuilder()
			        		.configure()
	        				.applySettings(settings)
	        				.build();

	        // Create MetadataSources
	        MetadataSources sources = new MetadataSources(registry);

	        // Create Metadata
	        Metadata metadata = sources.getMetadataBuilder().build();

	        // Create SessionFactory
	        sessionFactory = metadata.getSessionFactoryBuilder().build();
	        log.info("Generate Session : new Session is generated with [URL:{}, USER:{}]", settings.get(Environment.URL), settings.get(Environment.USER));
	        
	      } catch (Exception e) {
	        e.printStackTrace();
	        if (registry != null) {
	        	log.info("Generate Session : already Session is generated with [URL:{}, USER:{}]", settings.get(Environment.URL), settings.get(Environment.USER));
	          StandardServiceRegistryBuilder.destroy(registry);
	        }
	      }
	   }
	   return sessionFactory;
	}	 
	 
	 
//	private  static SessionFactory createSessionFactory(Map<String, String> settings) {
//	      try {
//	        // Create registry
//	        registry = new StandardServiceRegistryBuilder()
//			        		.configure()
//	        				.applySettings(settings)
//	        				.build();
//	
//	        // Create MetadataSources
//	        MetadataSources sources = new MetadataSources(registry);
//	
//	        // Create Metadata
//	        Metadata metadata = sources.getMetadataBuilder().build();
//	
//	        // Create SessionFactory
//	        sessionFactory = metadata.getSessionFactoryBuilder().build();
//	
//	      } catch (Exception e) {
//	        e.printStackTrace();
//	        if (registry != null) {
//	          StandardServiceRegistryBuilder.destroy(registry);
//	        }
//	      }
//	
//	    return sessionFactory;
//	 }

}
