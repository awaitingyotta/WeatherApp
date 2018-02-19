package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
class Pressure extends BaseXml {
    @Attribute
    private String id;
    @Attribute
    private String unit;
    @Attribute
    private double value;
}