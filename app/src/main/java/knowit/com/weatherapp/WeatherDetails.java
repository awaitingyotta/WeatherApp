package knowit.com.weatherapp;

import android.os.Parcel;
import android.os.Parcelable;

public class WeatherDetails implements Parcelable{

    private int iconNumber;
    private double temperature;
    private String iconId;
    private String units;
    private String date;

    public WeatherDetails() {

    }

    protected WeatherDetails(Parcel in) {
        iconNumber = in.readInt();
        temperature = in.readDouble();
        iconId = in.readString();
        units = in.readString();
        date = in.readString();
    }

    public int getIconNumber() {
        return iconNumber;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getIconId() {
        return iconId;
    }

    public String getUnits() {
        return units;
    }

    public String getDate() {
        return date;
    }

    public void setIconNumber(int iconNumber) {
        this.iconNumber= iconNumber;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public void setIconId(String iconId) {
        this.iconId = iconId;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static final Creator<WeatherDetails> CREATOR = new Creator<WeatherDetails>() {
        @Override
        public WeatherDetails createFromParcel(Parcel in) {
            return new WeatherDetails(in);
        }

        @Override
        public WeatherDetails[] newArray(int size) {
            return new WeatherDetails[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(iconNumber);
        parcel.writeDouble(temperature);
        parcel.writeString(iconId);
        parcel.writeString(units);
        parcel.writeString(date);
    }
}
