package org.red5.server.plugin.jni;



import java.io.File;

import java.lang.reflect.Method;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.red5.server.plugin.jni.objects.net.ChannelOut;


public class BootstrapJni {

	private static String VERSION = "BootstrapJni- v.0.1";
	
	private static BootstrapJni booter;

	private static Map<String,Class<?>> exports=new HashMap<String,Class<?>>();
	
	static {		
		BootstrapJni.exportClass(ChannelOut.class.getName(),ChannelOut.class);
	}
	
	
	private static void exportClass(String name, Class<?> class1) {
		exports.put(name,class1);
				
	}
	
	private static void forceClasspath(URL[] jarlist) throws Exception
	{

		 	
		 	URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		 	@SuppressWarnings("rawtypes")
			Class urlClass = URLClassLoader.class;
		 	
		 	@SuppressWarnings("unchecked")
			Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});
		 	
		 	method.setAccessible(true);
		 	
		 	
		 	
		 	for(URL url :jarlist ){
		 		method.invoke(urlClassLoader, new Object[]{url});
		 	}
	}
	
	/**
	 * @param args
	 * @throws Throwable 
	 */
	public static void main(String[] args) throws Throwable {
		
		File files[]=new File[]{new File("C:/workspaces/eclipse/ComServer/red5/lib") };
	
		List<URL> list = showFiles(files);
		
		File redJar=new File("C:/workspaces/eclipse/ComServer/red5/dist/red5.jar");
		
		System.out.println("Red5 jar is here ? "+redJar.exists());
		
		list.add(redJar.toURI().toURL());
		
		URL[] jarlist= list.toArray(new URL[list.size()]);		
				
		System.out.println(jarlist.length+" Jars loaded");
	
		forceClasspath(jarlist);
		
		URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
		
		
		Class<?> claTest = urlClassLoader.loadClass("org.springframework.core.task.TaskExecutor");

		if(claTest==null){
			//dead code.
		}
		
		
		
		booter =new BootstrapJni();	
		
		
		System.out.println(booter.getInfo());
	}
	
	public static void passOwner(Object owner){
		
		System.out.println("passOwner");
		System.out.println(owner!=null);
		System.out.println(owner.getClass().getName());
		
		Method[] methods = owner.getClass().getDeclaredMethods();
		for(Method m :methods)
		System.out.println(m.getName());
		
		
		
	}
	
	public String getInfo(){
		return VERSION;
	}
	
	public static List<URL> showFiles(File[] files) throws Exception {
		
		int depth=1;
		int currentDepth=0;
		
	   List<URL>resources =new ArrayList<URL>();
		for (File file : files) {
	        if (file.isDirectory()) {
	        	if(currentDepth<depth){
	        	  System.out.println("Directory: " + file.getName());
	        	  resources.addAll(showFiles(file.listFiles())); // Calls same method again.
	          }
	        } else {
	        	
	        	
	        	
	        	resources.add(file.toURI().toURL());
	        	
	        	System.out.println(file.toURI().toURL());
	        }
	    }
	
		return resources;
	}
	
	
	public static void initiate(int id){
		
			System.out.println("testing initiate");
			return;
	}
	
	public static Object getReference(String className)	{
		
		if(exports.containsKey(className)){			
			
			return exports.get(className);
		}
		
		return null;
	}
	public static Object createObject(String className)	{
		try{
			
		System.out.println("testing createObject : "+className);
		
		if(exports.containsKey(className)){			
			URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
			
			Class<?> claTest =urlClassLoader.loadClass(className);
			
			return claTest.newInstance();
		}
				
		}catch(Exception e){e.printStackTrace();}
		
		return null;
	}
	
	public static void config(int id, String prop, String val){
		
		System.out.println("config :"+ prop+" = "+val );
		
		
		System.out.println("testing " +( (prop!=null && val!=null)?"passed":"failed"));
		return;
		
	}
	
	public static void stop(int id){
		
		
			System.out.println("testing stop");
			return;
		

	}	

}
