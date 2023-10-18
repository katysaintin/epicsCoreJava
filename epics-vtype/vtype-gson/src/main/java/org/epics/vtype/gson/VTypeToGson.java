/**
 * Copyright information and license terms for this software can be
 * found in the file LICENSE.TXT included with the distribution.
 */
package org.epics.vtype.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.epics.util.array.*;
import org.epics.util.number.UByte;
import org.epics.util.number.UInteger;
import org.epics.util.number.ULong;
import org.epics.util.number.UShort;
import org.epics.vtype.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Gson adapted VTypeToJson
 *
 * @author <a href="mailto:changj@frib.msu.edu">Genie Jhang</a>
 *
 * Original author:
 * @author carcassi
 */
public class VTypeToGson {

    static VType toVType(JsonElement json) {
        switch(typeNameOf(json)) {
            case "VDouble":
            case "VFloat":
            case "VULong":
            case "VLong":
            case "VUInt":
            case "VInt":
            case "VUShort":
            case "VShort":
            case "VUByte":
            case "VByte":
                return toVNumber(json);
            case "VDoubleArray":
            case "VFloatArray":
            case "VULongArray":
            case "VLongArray":
            case "VUIntArray":
            case "VIntArray":
            case "VUShortArray":
            case "VShortArray":
            case "VUByteArray":
            case "VByteArray":
                return toVNumberArray(json);
            case "VString":
                return toVString(json);
            case "VStringArray":
                return toVStringArray(json);
            case "VEnum":
                return toVEnum(json);
            case "VTable":
                return toVTable(json);
            default:
                throw new UnsupportedOperationException("Not implemented yet");
        }
    }
    
    static String typeNameOf(JsonElement json) {
        JsonElement type = json.getAsJsonObject().get("type");
        if (type == null) {
            return null;
        }
        return type.getAsJsonObject().get("name").getAsString();
    }
    
    static JsonElement toJson(VType vType) {
        if (vType instanceof VNumber) {
            return toJson((VNumber) vType);
        } else if (vType instanceof VNumberArray) {
            return toJson((VNumberArray) vType);
        } else if (vType instanceof VString) {
            return toJson((VString) vType);
        } else if (vType instanceof VStringArray) {
            return toJson((VStringArray) vType);
        } else if (vType instanceof VEnum) {
            return toJson((VEnum) vType);
        } else if(vType instanceof VTable) {
            return toJson((VTable) vType);
        }
        throw new UnsupportedOperationException("Not implemented yet");
    }
    
    static VNumber toVNumber(JsonElement json) {
        VTypeGsonMapper mapper = new VTypeGsonMapper(json.getAsJsonObject());
        Number value;
        switch(mapper.getTypeName()) {
            case "VDouble":
                Double doubleValue = json.getAsJsonObject().get("value").getAsDouble();
                if(doubleValue != null){
                    return VDouble.of(doubleValue, mapper.getAlarm(), mapper.getTime(), mapper.getDisplay());
                }
                value = mapper.getJsonNumber("value").getAsDouble();
                break;
            case "VFloat":
                value = (float) mapper.getJsonNumber("value").getAsDouble();
                break;
            case "VULong":
                value = new ULong(mapper.getJsonNumber("value").getAsLong());
                break;
            case "VLong":
                value = (long) mapper.getJsonNumber("value").getAsLong();
                break;
            case "VUInt":
                value = new UInteger(mapper.getJsonNumber("value").getAsInt());
                break;
            case "VInt":
                value = (int) mapper.getJsonNumber("value").getAsInt();
                break;
            case "VUShort":
                value = new UShort((short) mapper.getJsonNumber("value").getAsInt());
                break;
            case "VShort":
                value = (short) mapper.getJsonNumber("value").getAsInt();
                break;
            case "VUByte":
                value = new UByte((byte) mapper.getJsonNumber("value").getAsInt());
                break;
            case "VByte":
                value = (byte) mapper.getJsonNumber("value").getAsInt();
                break;
            default:
                throw new UnsupportedOperationException("Not implemented yet");
        }
        return VNumber.of(value, mapper.getAlarm(), mapper.getTime(), mapper.getDisplay());
    }
    
    static VString toVString(JsonElement json) {
        VTypeGsonMapper mapper = new VTypeGsonMapper(json.getAsJsonObject());
        return VString.of(mapper.getString("value"), mapper.getAlarm(), mapper.getTime());
    }

    static VStringArray toVStringArray(JsonElement json) {
        VTypeGsonMapper mapper = new VTypeGsonMapper(json.getAsJsonObject());
        return VStringArray.of(mapper.getListString("value"), mapper.getAlarm(), mapper.getTime());
        
    }
    
    static VEnum toVEnum(JsonElement json) {
        VTypeGsonMapper mapper = new VTypeGsonMapper(json.getAsJsonObject());
        List<String> labels = mapper.getJsonObject("enum").getListString("labels");
        return VEnum.of(mapper.getInt("value"), EnumDisplay.of(labels), mapper.getAlarm(), mapper.getTime());
    }
    
    static VNumberArray toVNumberArray(JsonElement json) {
        VTypeGsonMapper mapper = new VTypeGsonMapper(json.getAsJsonObject());
        ListNumber value;
        switch(mapper.getTypeName()) {
            case "VDoubleArray":
                value = mapper.getListDouble("value");
                break;
            case "VFloatArray":
                value = mapper.getListFloat("value");
                break;
            case "VULongArray":
                value = mapper.getListULong("value");
                break;
            case "VLongArray":
                value = mapper.getListLong("value");
                break;
            case "VUIntArray":
                value = mapper.getListUInteger("value");
                break;
            case "VIntArray":
                value = mapper.getListInt("value");
                break;
            case "VUShortArray":
                value = mapper.getListUShort("value");
                break;
            case "VShortArray":
                value = mapper.getListShort("value");
                break;
            case "VUByteArray":
                value = mapper.getListUByte("value");
                break;
            case "VByteArray":
                value = mapper.getListByte("value");
                break;
            default:
                throw new UnsupportedOperationException("Not implemented yet");
        }
        return VNumberArray.of(value, mapper.getAlarm(), mapper.getTime(), mapper.getDisplay());
    }
    
    static JsonElement toJson(VNumber vNumber) {
        return new GsonVTypeBuilder()
                .addType(vNumber)
                .addObject("value", vNumber.getValue())
                .addAlarm(vNumber.getAlarm())
                .addTime(vNumber.getTime())
                .addDisplay(vNumber.getDisplay())
                .build();
    }
    
    static JsonElement toJson(VNumberArray vNumberArray) {
        return new GsonVTypeBuilder()
                .addType(vNumberArray)
                .addObject("value", vNumberArray.getData())
                .addAlarm(vNumberArray.getAlarm())
                .addTime(vNumberArray.getTime())
                .addDisplay(vNumberArray.getDisplay())
                .build();
    }
    
    static JsonElement toJson(VString vString) {
        return new GsonVTypeBuilder()
                .addType(vString)
                .add("value", vString.getValue())
                .addAlarm(vString.getAlarm())
                .addTime(vString.getTime())
                .build();
    }

    static JsonElement toJson(VStringArray vStringArray) {
        return new GsonVTypeBuilder()
                .addType(vStringArray)
                .addListString("value", vStringArray.getData())
                .addAlarm(vStringArray.getAlarm())
                .addTime(vStringArray.getTime())
                .build();
    }

    static JsonElement toJson(VEnum vEnum) {
        return new GsonVTypeBuilder()
                .addType(vEnum)
                .add("value", vEnum.getIndex())
                .addAlarm(vEnum.getAlarm())
                .addTime(vEnum.getTime())
                .addEnum(vEnum)
                .build();
    }

    /**
     * Converts an input (JSON) string to a {@link Double}, taking into account the special cases
     * {@link Double#NaN}, {@link Double#POSITIVE_INFINITY} and {@link Double#NEGATIVE_INFINITY}.
     * If the input string is not parseable as a double (including the special cases), a
     * {@link NumberFormatException} is thrown.
     * @param valueAsString
     * @return A valid double number, otherwise
     * {@link Double#NaN}, {@link Double#POSITIVE_INFINITY} and {@link Double#NEGATIVE_INFINITY}.
     */
    public static Double getDoubleFromJsonString(String valueAsString){
        if(VTypeGsonMapper.NAN_QUOTED.equals(valueAsString)){
            return Double.NaN;
        }
        else if(VTypeGsonMapper.POS_INF_QUOTED.equals(valueAsString)){
            return Double.POSITIVE_INFINITY;
        }
        else if(VTypeGsonMapper.NEG_INF_QUOTED.equals(valueAsString)){
            return Double.NEGATIVE_INFINITY;
        }
        else{
            return Double.parseDouble(valueAsString);
        }
    }

    /**
     * Deserializes the Gson representation of a {@link VTable}
     * @param jsonElement JSON representation
     * @return A {@link VTable} object.
     */
    static VTable toVTable(JsonElement jsonElement) {
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        int columnCount = jsonObject.get("columnCount").getAsInt();
        List<String> listString = new ArrayList<>();
        List<Class<?>> listClass = new ArrayList<>();
        JsonArray columnNames = jsonObject.get("columnNames").getAsJsonArray();
        JsonArray columnTypes = jsonObject.get("columnTypes").getAsJsonArray();
        List<Object> listObject = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            listString.add(i, columnNames.get(i).getAsString());
            Class<?> clazz = getClass(columnTypes.get(i).getAsString());
            listClass.add(i, clazz);
            JsonArray value = jsonObject.get("value").getAsJsonArray()
                    .get(i).getAsJsonArray();
            clazz = listClass.get(i);
            if (clazz == null) {
                listObject.add(i, Collections.emptyList());
            } else {
                String columnType = columnTypes.get(i).getAsString();
                switch (columnType){
                    case "bool":
                        listObject.add(i, GsonArrays.toListBoolean(value));
                        break;
                    case "byte":
                        listObject.add(i, GsonArrays.toListByte(value));
                        break;
                    case "ubyte":
                        listObject.add(i, GsonArrays.toListUByte(value));
                        break;
                    case "short":
                        listObject.add(i, GsonArrays.toListShort(value));
                        break;
                    case "ushort":
                        listObject.add(i, GsonArrays.toListUShort(value));
                        break;
                    case "int":
                        listObject.add(i, GsonArrays.toListInt(value));
                        break;
                    case "uint":
                        listObject.add(i, GsonArrays.toListUInteger(value));
                        break;
                    case "long":
                        listObject.add(i, GsonArrays.toListLong(value));
                        break;
                    case "ulong":
                        listObject.add(i, GsonArrays.toListULong(value));
                        break;
                    case "float":
                        listObject.add(i, GsonArrays.toListFloat(value));
                        break;
                    case "double":
                        listObject.add(i, GsonArrays.toListDouble(value));
                        break;
                    case "string":
                        listObject.add(i, GsonArrays.toListString(value));
                        break;
                    default:
                        listObject.add(i, Collections.emptyList());
                }
            }
        }
        return VTable.of(listClass, listString, listObject);
    }

    /**
     * @param name A type name determined at serialization time. The &quot;unsigned primitives&quot; (ushort, uint...)
     *             are allowed.
     * @return A {@link Class} derived from the name, where - for instance - int and uint both map to
     * {@link Integer#TYPE}. For unsupported type names <code>null</code> is returned.
     */
    static Class<?> getClass(String name) {
        switch (name) {
            case "bool":
                return Boolean.TYPE;
            case "int":
            case "uint":
                return Integer.TYPE;
            case "long":
            case "ulong":
                return Long.TYPE;
            case "short":
            case "ushort":
                return Short.TYPE;
            case "byte":
            case "ubyte":
                return Byte.TYPE;
            case "double":
                return Double.TYPE;
            case "float":
                return Float.TYPE;
            case "string":
                return String.class;
            default:
                return null;
        }
    }

    /**
     * Serializes a {@link VTable} object, though with the restriction that data is
     * structured as arrays, but not any other nested ot complex structure, e.g. arrays of arrays.
     * The array data may be of different types, but EPICS requires data columns to be of equal length.
     * @param vTable A {@link VTable} object
     * @return Gson representation.
     */
    static JsonObject toJson(VTable vTable) {
        GsonVTypeBuilder jsonVTypeBuilder = new GsonVTypeBuilder();
        int columnCount = vTable.getColumnCount();
        List<String> columnNames = new ArrayList<>();
        List<String> columnTypes = new ArrayList<>();
        for (int i = 0; i < columnCount; i++) {
            columnNames.add(i, vTable.getColumnName(i));
            columnTypes.add(i, getVTableDataType(vTable.getColumnData(i)));
        }
        jsonVTypeBuilder.addType(vTable)
                .add("columnCount", columnCount)
                .addListString("columnNames", columnNames);

        jsonVTypeBuilder.addListString("columnTypes", columnTypes);
        JsonArray valueBuilder = new JsonArray();
        for (int i = 0; i < columnCount; i++) {
            valueBuilder.add(GsonArrays.fromList(vTable.getColumnData(i)));
        }
        jsonVTypeBuilder.add("value", valueBuilder);
        return jsonVTypeBuilder.build();
    }

    /**
     * An NTTable's value columns are always of type struct_t[]. In order to be able to preserve
     * the type information when serializing, this method returns a string representation
     * of the array type.
     * @param data NTTable value field that is always of array type.
     * @return A string representation of the array type.
     */
    static String getVTableDataType(Object data){
        if(data instanceof ArrayBoolean){
            return "bool";
        }
        else if(data instanceof ArrayByte){
            return "byte";
        }
        else if(data instanceof ArrayUByte){
            return "ubyte";
        }
        else if(data instanceof ArrayShort){
            return "short";
        }
        else if(data instanceof ArrayUShort){
            return "ushort";
        }
        else if (data instanceof ArrayInteger){
            return "int";
        }
        else if(data instanceof ArrayUInteger){
            return "uint";
        }
        else if(data instanceof ArrayLong){
            return "long";
        }
        else if(data instanceof ArrayULong){
            return "ulong";
        }
        else if(data instanceof ArrayFloat){
            return "float";
        }
        else if(data instanceof ArrayDouble){
            return "double";
        }
        else if(data instanceof List){
            return "string";
        }
        else{
            return "invalid";
        }
    }
}
