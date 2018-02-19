package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
class Precipitation {
    @Attribute
    private String unit;
    @Attribute
    private double value;
    @Attribute(required=false)
    private double minvalue;
    @Attribute(required=false)
    private double maxvalue;
}
