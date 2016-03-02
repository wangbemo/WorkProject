import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.Test;

import com.teradata.dmet.DMET;


public class Tests {

	//@Test
	//public void test() {
	public static void main(String[] args){
		 Properties properties = new Properties();
		    try {
		      properties.load(new FileInputStream(new File(fmtPath("C:\\Personal\\soft\\myeclipse8.5\\workspace\\dmet\\tool", "", "dmet.properties"))));
		      Method mainMethod  =  Class.forName("com.teradata.dmet.DMET").getMethod("main", String[].class);
		      mainMethod.invoke(null,  (Object)new String[]{"C:\\Personal\\soft\\myeclipse8.5\\workspace\\dmet\\tool"});
		      //System.out.println(properties.getProperty(new StringBuilder("dmet.db.un.").append("1").toString()));
		    } 
		    catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
		    } catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	}
	
	 public static String fmtPath(String path, String dir, String file) {
		    String ret = new StringBuilder(path).append("/").append(dir).append("/").append(file).toString();
		    ret = ret.replaceAll("\\\\", "/");
		    ret = ret.replaceAll("/+", "/");
		    return ret;
		  }

}
