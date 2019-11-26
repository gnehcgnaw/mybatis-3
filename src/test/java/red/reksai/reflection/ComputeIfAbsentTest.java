package red.reksai.reflection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author : <a href="mailto:gnehcgnaw@gmail.com">gnehcgnaw</a>
 * @since : 2019/11/26 14:03
 */
public class ComputeIfAbsentTest {
  public static void main(String[] args) {
    ArrayList<String> arrayList =null ;
    Map<String, List<String >> map= new HashMap<>();
    if (map.get("map-1")==null){
      arrayList = new ArrayList<>();
      map.put("map-1" ,arrayList);
    }

    arrayList.add("list-1");
    System.out.println(map);

    List<String> list = map.computeIfAbsent("map-2",m->new ArrayList<String>());
    list.add("list-2");
    System.out.println(map);
  }


}
