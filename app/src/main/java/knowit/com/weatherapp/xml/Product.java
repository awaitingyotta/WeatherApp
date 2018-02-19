package knowit.com.weatherapp.xml;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root
public class Product extends BaseXml {

    @ElementList(inline=true)
    private List<Time> list;

    public List<Time> getList() {
        return list;
    }

}

