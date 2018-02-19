package knowit.com.weatherapp.xml;

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

import java.util.List;

@Root
public class Meta extends BaseXml {

    @ElementList(inline=true)
    private List<Model> list;

}
