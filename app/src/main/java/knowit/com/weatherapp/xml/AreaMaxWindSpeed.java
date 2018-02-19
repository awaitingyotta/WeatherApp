package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
class AreaMaxWindSpeed extends BaseXml {
    @Attribute
    private double mps;
}