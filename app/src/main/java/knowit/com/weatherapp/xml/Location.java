package knowit.com.weatherapp.xml;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
public class Location extends BaseXml {

    @Attribute
    private int altitude;
    @Attribute
    private double latitude;
    @Attribute
    private double longitude;

    @Element(required=false)
    private Precipitation precipitation; // unit="mm" value="0.7" minvalue="0.5" maxvalue="0.9"/>
    @Element(required=false)
    private Symbol symbol ; // id="Snow" number="13"/>
    @Element(required=false)
    private Temperature temperature; // id="TTT" unit="celsius" value="-4.3"/>
    @Element(required=false)
    private WindDirection windDirection; // <id="dd" deg="28.6" name="NE"/>
    @Element(required=false)
    private WindSpeed windSpeed; // < id="ff" mps="7.3" beaufort="4" name="Laber bris"/>
    @Element(required=false)
    private WindGust windGust; // < id="ff_gust" mps="10.9"/>
    @Element(required=false)
    private AreaMaxWindSpeed areaMaxWindSpeed ; // < mps="9.0"/>
    @Element(required=false)
    private Humidity humidity; // <value="94.0" unit="percent"/>
    @Element(required=false)
    private Pressure pressure; // <id="pr" unit="hPa" value="993.0"/>
    @Element(required=false)
    private Clouds cloudiness ; // < id="NN" percent="100.0"/>
    @Element(required=false)
    private Clouds fog ; // < id="FOG" percent="0.9"/>
    @Element(required=false)
    private Clouds lowClouds ; // < id="LOW" percent="100.0"/>
    @Element(required=false)
    private Clouds mediumClouds ; // < id="MEDIUM" percent="84.3"/>
    @Element(required=false)
    private Clouds highClouds ; // < id="HIGH" percent="91.5"/>
    @Element(required=false)
    private Temperature dewpointTemperature ; // < id="TD" unit="celsius" value="-5.4"/>

    public Symbol getSymbol() {
        return symbol;
    }

    public Temperature getTemperature() {
        return temperature;
    }
}
