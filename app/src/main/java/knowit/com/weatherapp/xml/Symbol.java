package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root
public class Symbol {

    @Attribute
    private String id;

    @Attribute
    private int number;

    public int getNumber() {
        return number;
    }

    public String getId() {
        return id;
    }
}
