package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
class Clouds extends BaseXml {
    @Attribute
    private String id;
    @Attribute
    private double percent;
}