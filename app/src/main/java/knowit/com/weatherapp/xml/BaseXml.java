package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Visitor;
import org.simpleframework.xml.strategy.VisitorStrategy;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.OutputNode;

import java.io.InputStream;

public class BaseXml {

    private static final String CLASS = "class";
    private static final String PRODUCT = "product";

    private static Serializer serializer = new Persister(new VisitorStrategy(new Visitor() {
        @Override
        public void read(Type type, NodeMap<InputNode> nodeMap) throws Exception {
            if (PRODUCT.equals(nodeMap.getName())){
                nodeMap.remove(CLASS);
            }
        }

        @Override
        public void write(Type type, NodeMap<OutputNode> nodeMap) throws Exception {

        }
    }));

    public BaseXml() {

    }

    public static <T extends BaseXml> BaseXml inputStreamToXml(InputStream inputStream, Class<? extends BaseXml> type)
            throws Exception {

        return serializer.read(type, inputStream,false);
    }


}
