package edu.unm.twin_cities.graphit.processor.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

/**
 * A model for UserPref table.
 */
@Data
@AllArgsConstructor(suppressConstructorProperties = true)
public class UserPref {
    /**
     * The field key in the table.
     */
    UserPrefType key;

    /**
     * The field value in the table.
     */
    String value;

    public UserPref(String key, String value) {
        setKey(UserPrefType.fromString(key));
        setValue(value);
    }

    public String getKeyStr() {
        return getKey().getStr();
    }

    /**
     * A enum for type of user preference we are storing.
     */
    public enum UserPrefType {
        /**
         * Record which sensor type plot the user was viewing, before closing the application.
         */
        SENSOR_TYPE_VIEWING("sensor_type_viewing");

        @Getter
        private String str;

        private UserPrefType(final String str) {
            this.str = str;
        }

        public static UserPrefType fromString(String enumStr){
            for(UserPrefType userPrefType : values()){
                if(userPrefType.getStr().equals(enumStr)){
                    return userPrefType;
                }
            }
            throw new UnsupportedOperationException("User prefernce not supported");
        }
    }
}
