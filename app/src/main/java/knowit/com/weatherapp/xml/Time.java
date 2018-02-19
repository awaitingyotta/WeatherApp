package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class Time extends BaseXml {

    @Attribute
    private String datatype;

    @Attribute
    private String from;

    @Attribute
    private String to;

    @Element
    private Location location;

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public Location getLocation() {
        return location;
    }
}
