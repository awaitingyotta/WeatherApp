package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
public class Model extends BaseXml {
    @Attribute
    private String name;

    @Attribute
    private String termin, runended, nextrun, from, to;
}
