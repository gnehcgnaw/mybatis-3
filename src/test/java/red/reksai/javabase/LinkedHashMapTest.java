package red.reksai.javabase;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 高频词汇处理，热度排行
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/12/2 14:38
 */
public class LinkedHashMapTest {

  static Object eldKey ;
  public static void main(String[] args) {
    int size = 3;
    LinkedHashMap<String, String > linkedHashMap = new LinkedHashMap<String,String >(size, .75F, true){
      @Override
      protected boolean removeEldestEntry(Map.Entry<String,String> eldest) {
        if (size()>size){
          eldKey = eldest.getKey();
          //System.out.println(eldest);
         // System.out.println(eldKey);
        }
        return size()>size ;
      }
    };

    linkedHashMap.put("1","1");
    linkedHashMap.put("2","2");
    linkedHashMap.put("3","3");
    linkedHashMap.get("1");
    linkedHashMap.get("2");
    System.out.println(linkedHashMap);
    linkedHashMap.put("4","4");
    System.out.println(linkedHashMap);


  }


}
