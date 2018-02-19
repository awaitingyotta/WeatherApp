package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
public class Temperature extends BaseXml {
    @Attribute
    private String id;
    @Attribute
    private String unit;
    @Attribute
    private double value;

    public String getUnit() {
        return unit;
    }

    public double getValue() {
        return value;
    }
}
