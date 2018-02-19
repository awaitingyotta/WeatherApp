package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.Root;

@Root
@Namespace(reference="http://www.w3.org/2001/XMLSchema-instance", prefix="xsi")
public class WeatherData extends BaseXml {

    @Attribute
    private String created;

    @Attribute
    private String noNamespaceSchemaLocation;

    @Element
    private Meta meta;

    @Element
    private Product product;

    public Product getProduct() {
        return product;
    }
}
