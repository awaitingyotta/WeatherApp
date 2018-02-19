package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
class Humidity extends BaseXml {
    @Attribute
    private double value;
    @Attribute
    private String unit;
}