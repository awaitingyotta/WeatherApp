package knowit.com.weatherapp;

import android.graphics.Bitmap;
import java.io.Serializable;

public interface AsyncTaskCompleteListener extends Serializable {

    static final long serialVersionUID = 1L;

    void onSuccess(Bitmap image);
    void onFailure();

}