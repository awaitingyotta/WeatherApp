package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
class WindSpeed extends BaseXml {
    @Attribute
    private String id;
    @Attribute
    private double mps;
    @Attribute
    private int beaufort;
    @Attribute
    private String name;
}