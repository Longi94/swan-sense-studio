package interdroid.swan.jsonsensor.pojos;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * Created by steven on 20/05/15.
 */
public class JsonRequestInfo implements Parcelable {

    public int id;
    public String name;
    public String url;
    public String requestType;
    public ArrayList<Parameter> parameterList;
    public ArrayList<PathToValue> pathToValueList;
    public int maxPathToValueId;

    public JsonRequestInfo(int id, String name) {
        this.id = id;
        this.name = name;
        this.url = "";
        this.requestType = "GET";
        this.parameterList = new ArrayList<>();
        this.pathToValueList = new ArrayList<>();
        this.maxPathToValueId = -1;
    }

    public JsonRequestInfo(Parcel in) {
        readFromParcel(in);
    }

    public JsonRequestInfo(int id, String name, String url, String requestType, ArrayList<Parameter> parameterList,
                           ArrayList<PathToValue> pathToValueList, int maxPathToValueId) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.requestType = requestType;
        this.parameterList = parameterList;
        this.pathToValueList = pathToValueList;
        this.maxPathToValueId = maxPathToValueId;
    }

    public JsonRequestInfo(JsonRequestComplete jsonRequestComplete) {
        this.id = jsonRequestComplete.id;
        this.name = jsonRequestComplete.name;
        this.url = jsonRequestComplete.url;
        this.requestType = jsonRequestComplete.requestType;
        this.parameterList = jsonRequestComplete.parameterList;
        this.pathToValueList = null;
        this.maxPathToValueId = 0;
    }

    public void readFromParcel(Parcel in) {
        this.id = in.readInt();
        this.name = in.readString();
        this.url = in.readString();
        this.requestType = in.readString();
        this.parameterList = in.readArrayList(Parameter.class.getClassLoader());
        this.pathToValueList = in.readArrayList(PathToValue.class.getClassLoader());
        this.maxPathToValueId = in.readInt();
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(name);
        dest.writeString(url);
        dest.writeString(requestType);
        dest.writeList(parameterList);
        dest.writeList(pathToValueList);
        dest.writeInt(maxPathToValueId);
    }

    public static Creator<JsonRequestInfo> CREATOR = new Creator<JsonRequestInfo>() {

        @Override
        public JsonRequestInfo createFromParcel(Parcel source) {
            return new JsonRequestInfo(source);
        }

        @Override
        public JsonRequestInfo[] newArray(int size) {
            return new JsonRequestInfo[size];
        }

    };
}