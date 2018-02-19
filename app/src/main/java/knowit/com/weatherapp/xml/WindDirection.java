package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
class WindDirection extends BaseXml {
    @Attribute
    private String id;
    @Attribute
    private double deg;
    @Attribute
    private String name;
}
