/**
 * Copyright (C) 2010-14 diirt developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.vtype.json;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import org.epics.util.array.ArrayDouble;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.AlarmStatus;
import org.epics.vtype.Display;
import org.epics.vtype.EnumDisplay;
import org.epics.vtype.Time;
import org.epics.vtype.VByte;
import org.epics.vtype.VDouble;
import org.epics.vtype.VDoubleArray;
import org.epics.vtype.VEnum;
import org.epics.vtype.VFloat;
import org.epics.vtype.VInt;
import org.epics.vtype.VLong;
import org.epics.vtype.VShort;
import org.epics.vtype.VString;
import org.epics.vtype.VType;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class VTypeToJsonTest {

    /**
     * Serializes the given value and compares the output with the given JSON file.
     * 
     * @param value the value to serialize
     * @param expectedJsonFileName the filename to compare
     */    
    public static void testSerialization(VType value, String expectedJsonFileName) {
        compareJson(VTypeToJson.toJson(value), expectedJsonFileName);
    }
    
    public static void compareJson(JsonObject json, String jsonFileName) {
        boolean success = false;
        try {
            JsonObject reference = loadJson(jsonFileName + ".json");
            assertThat(json, equalTo(reference));
            success = true;
        } finally {
            File failedJsonFile = new File("src/test/resources/org/epics/vtype/json/" + jsonFileName + ".failed.json");
            if (!success) {
                saveErrorJson(json, failedJsonFile);
            } else {
                if (failedJsonFile.exists()) {
                    failedJsonFile.delete();
                }
            }
        }
    }

    /**
     * Deserializes the JSON file and compares the value with the given VType object.
     * 
     * @param jsonFileName the JSON file to deserialize
     * @param expected the object to compare to
     */
    public static void testDeserialization(String jsonFileName, VType expected) {
        VType actual = VTypeToJson.toVType(loadJson(jsonFileName + ".json"));
        compareVType(actual, expected);
    }
    
    public static void compareVType(VType actual, VType expected) {
        assertThat("Type mismatch", VType.typeOf(actual), equalTo(VType.typeOf(expected)));
//        assertThat("Value mismatch", VTypeValueEquals.valueEquals(actual, expected), equalTo(true));
        assertThat("Alarm mismatch", Alarm.alarmOf(actual), equalTo(Alarm.alarmOf(expected)));
        assertThat("Time mismatch", Time.timeOf(actual), equalTo(Time.timeOf(expected)));
        assertThat("Display mismatch", Display.displayOf(actual), equalTo(Display.displayOf(expected)));
    }

    public static void compareVType(VType actual, String jsonFileName) {
        testDeserialization(jsonFileName, actual);
    }
    
    public static JsonObject loadJson(String jsonFile) {
        try (JsonReader reader = Json.createReader(VTypeToJsonTest.class.getResourceAsStream(jsonFile))) {
            return reader.readObject();
        }
    }
    
    public static void saveErrorJson(JsonObject json, File jsonFile) {
        StringWriter sw = new StringWriter();
        try (JsonWriter jsonWriter = Json.createWriterFactory(Collections.singletonMap(JsonGenerator.PRETTY_PRINTING, true)).createWriter(sw)) {
            jsonWriter.writeObject(json);
        }
        try (FileWriter fw = new FileWriter(jsonFile)) {
            fw.append(sw.toString().substring(1));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
//    public VFloatArray vFloatArray = VFloatArray.create(Arrayofof(new float[] {0, 1, 2}), Alarm.none(), Time.create(Instant.oofSecond(0, 0)), Display.none());
//    public String vFloatArrayJson = "{\"type\":{\"name\":\"VFloatArray\",\"version\":1},"
//                + "\"value\":[0.0,1.0,2.0],"
//                + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//                + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null},"
//                + "\"display\":{\"lowAlarm\":null,\"highAlarm\":null,\"lowDisplay\":null,\"highDisplay\":null,\"lowWarning\":null,\"highWarning\":null,\"units\":\"\"}}";
//    public VLongArray vLongArray = VLongArray.create(ArrayLong.of(ofng[] {0, 1, 2}), Alarm.none(), Time.create(Instant.ofEpochSeof, 0)), Display.none());
//    public String vLongArrayJson = "{\"type\":{\"name\":\"VLongArray\",\"version\":1},"
//                + "\"value\":[0,1,2],"
//                + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//                + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null},"
//                + "\"display\":{\"lowAlarm\":null,\"highAlarm\":null,\"lowDisplay\":null,\"highDisplay\":null,\"lowWarning\":null,\"highWarning\":null,\"units\":\"\"}}";
//    public VIntArray vIntArray = VIntArray.create(ArrayInteger.of(new int[]of, 2}), Alarm.none(), Time.create(Instant.ofEpochSecond(0, ofisplay.none());
//    public String vIntArrayJson = "{\"type\":{\"name\":\"VIntArray\",\"version\":1},"
//                + "\"value\":[0,1,2],"
//                + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//                + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null},"
//                + "\"display\":{\"lowAlarm\":null,\"highAlarm\":null,\"lowDisplay\":null,\"highDisplay\":null,\"lowWarning\":null,\"highWarning\":null,\"units\":\"\"}}";
//    public VShortArray vShortArray = newVShortArray(ArrayShort.of(new short[] {0, 1, 2}), Alarm.none(), Time.create(Instant.ofEpochSecond(0, 0)),ofay.none());
//    public String vShortArrayJson = "{\"type\":{\"name\":\"VShortArray\",\"version\":1},"
//                + "\"value\":[0,1,2],"
//                + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//                + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null},"
//                + "\"display\":{\"lowAlarm\":null,\"highAlarm\":null,\"lowDisplay\":null,\"highDisplay\":null,\"lowWarning\":null,\"highWarning\":null,\"units\":\"\"}}";
//    public VNumberArray vByteArray = newVNumberArray(ArrayByte.of(new byte[]{0, 1, 2}), Alarm.none(), Time.create(Instant.ofEpochSecond(0, 0)), Disofone());
//    public String vByteArrayJson = "{\"type\":{\"name\":\"VByteArray\",\"version\":1},"
//            + "\"value\":[0,1,2],"
//            + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//            + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null},"
//            + "\"display\":{\"lowAlarm\":null,\"highAlarm\":null,\"lowDisplay\":null,\"highDisplay\":null,\"lowWarning\":null,\"highWarning\":null,\"units\":\"\"}}";
//    public VBooleanArray vBooleanArray = newVBooleanArray(new ArrayBoolean(true, false, true), Alarm.none(), Time.create(Instant.ofEpochSecond(0, 0)));
//    of String vBooleanArrayJson = "{\"type\":{\"name\":\"VBooleanArray\",\"version\":1},"
//            + "\"value\":[true,false,true],"
//            + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//            + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null}}";
//    public VStringArray vStringArray = newVStringArray(Arrays.asList("A", "B", "C"), Alarm.none(), Time.create(Instant.ofEpochSecond(0, 0)));
//    publofing vStringArrayJson = "{\"type\":{\"name\":\"VStringArray\",\"version\":1},"
//            + "\"value\":[\"A\",\"B\",\"C\"],"
//            + "\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//            + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null}}";
//    public VEnumArray vEnumArray = newVEnumArray(new ArrayInteger(1,0,1), Arrays.asList("One", "Two", "Three"), Alarm.none(), Time.create(Instant.ofEpochSecond(0, 0)));
//    public SofvEnumArrayJson = "{\"type\":{\"name\":\"VEnumArray\",\"version\":1},"
//            + "\"value\":[1,0,1],\"alarm\":{\"severity\":\"NONE\",\"status\":\"None\"},"
//            + "\"time\":{\"unixSec\":0,\"nanoSec\":0,\"userTag\":null},"
//            + "\"enum\":{\"labels\":[\"One\",\"Two\",\"Three\"]}}";
//    public VTable vTable = newVTable(Arrays.<Class<?>>asList(String.class, int.class, double.class), Arrays.asList("Name", "Index", "Value"), Arrays.asList(Arrays.asList("A", "B", "C"), new ArrayInteger(1,2,3), new ArrayDouble(3.14, 1.25, -0.1)));
//    public String vTableJson = "{\"type\":{\"name\":\"VTable\",\"version\":1},"
//            + "\"columnNames\":[\"Name\",\"Index\",\"Value\"],"
//            + "\"columnTypes\":[\"String\",\"int\",\"double\"],"
//            + "\"columnValues\":[[\"A\",\"B\",\"C\"],[1,2,3],[3.14,1.25,-0.1]]}";
//    public VTable vTable2 = newVTable(Arrays.<Class<?>>asList(String.class, int.class, double.class, Timestamp.class), Arrays.asList("Name", "Index", "Value", "Timestamp"), Arrays.asList(Arrays.asList("A", "B", "C"), new ArrayInteger(1,2,3), new ArrayDouble(3.14, 1.25, -0.1), Arrays.asList(Instant.ofEpochSecond(1234, 0), Instant.ofEpochSecond(2345, 0), Instant.ofEpochSecond(3456, 0))));
//    public String vTable2Json = "{\"type\":{\"name\":\"VTable\",\"version\":1},"
//            + "\"columnNames\":[\"Name\",\"Index\",\"Value\",\"Timestamp\"],"
//            + "\"columnTypes\":[\"String\",\"int\",\"double\",\"Timestamp\"],"
//            + "\"columnValues\":[[\"A\",\"B\",\"C\"],[1,2,3],[3.14,1.25,-0.1],[1234000,2345000,3456000]]}";
//    public VTable vTable3 = newVTable(Arrays.<Class<?>>asList(String.class, int.class, double.class, Timestamp.class), Arrays.asList("Name", "Index", "Value", "Timestamp"), Arrays.asList(Arrays.asList(null, "B", "C"), new ArrayInteger(1,2,3), new ArrayDouble(Double.NaN, 1.25, -0.1), Arrays.asList(Instant.ofEpochSecond(1234, 0), null, Instant.ofEpochSecond(3456, 123000000))));
//    public String vTable3Json = "{\"type\":{\"name\":\"VTable\",\"version\":1},"
//            + "\"columnNames\":[\"Name\",\"Index\",\"Value\",\"Timestamp\"],"
//            + "\"columnTypes\":[\"String\",\"int\",\"double\",\"Timestamp\"],"
//            + "\"columnValues\":[[\"\",\"B\",\"C\"],[1,2,3],[null,1.25,-0.1],[1234000,null,3456123]]}";
    
    @Test
    public void vFloat1() {
        VFloat vFloat1 = VFloat.of((float) 3.125, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vFloat1, "vFloat1");
        testDeserialization("vFloat1", vFloat1);
    }
    
    @Test
    public void vDouble1() {
        VDouble vDouble1 = VDouble.of(3.14, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "LOW"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vDouble1, "VDouble1");
        testDeserialization("VDouble1", vDouble1);
    }
    
    @Test
    public void vByte1() {
        VByte vByte1 = VByte.of((byte) 31, Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vByte1, "vByte1");
        testDeserialization("vByte1", vByte1);
    }
    
    @Test
    public void vShort1() {
        VShort vShort1 = VShort.of((short) 314, Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vShort1, "vShort1");
        testDeserialization("vShort1", vShort1);
    }
    
    @Test
    public void vInt1() {
        VInt vInt1 = VInt.of(314, Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vInt1, "vInt1");
        testDeserialization("vInt1", vInt1);
    }
    
    @Test
    public void vLong1() {
        VLong vLong1 = VLong.of(313L, Alarm.of(AlarmSeverity.MINOR, AlarmStatus.DB, "HIGH"), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vLong1, "vLong1");
        testDeserialization("vLong1", vLong1);
    }
    
    @Test
    public void vString1() {
        VString vString1 = VString.of("Flower", Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)));
        testSerialization(vString1, "vString1");
        testDeserialization("vString1", vString1);
    }
    
    @Test
    public void vEnum1() {
        VEnum vEnum1 = VEnum.of(1, EnumDisplay.of(Arrays.asList("One", "Two", "Three")), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)));
        testSerialization(vEnum1, "vEnum1");
        testDeserialization("vEnum1", vEnum1);
    }
    
    @Test
    public void vDoubleArray1() {
        VDoubleArray vDoubleArray1 = VDoubleArray.of(ArrayDouble.of(0.0, 0.1, 0.2), Alarm.none(), Time.of(Instant.ofEpochSecond(0, 0)), Display.none());
        testSerialization(vDoubleArray1, "vDoubleArray1");
        testDeserialization("vDoubleArray1", vDoubleArray1);
    }

//    @Test
//    public void serializeVBoolean() {
//        compareJson(VTypeToJson.toJson(vBoolean), vBooleanJson);
//    }

//    @Test
//    public void serializeVFloatArray() {
//        compareJson(VTypeToJson.toJson(vFloatArray), vFloatArrayJson);
//    }
//
//    @Test
//    public void serializeVLongArray() {
//        compareJson(VTypeToJson.toJson(vLongArray), vLongArrayJson);
//    }
//
//    @Test
//    public void serializeVIntArray() {
//        compareJson(VTypeToJson.toJson(vIntArray), vIntArrayJson);
//    }
//
//    @Test
//    public void serializeVShortArray() {
//        compareJson(VTypeToJson.toJson(vShortArray), vShortArrayJson);
//    }
//
//    @Test
//    public void serializeVByteArray() {
//        compareJson(VTypeToJson.toJson(vByteArray), vByteArrayJson);
//    }
//
//    @Test
//    public void serializeVBooleanArray() {
//        compareJson(VTypeToJson.toJson(vBooleanArray), vBooleanArrayJson);
//    }
//
//    @Test
//    public void serializeVStringArray() {
//        compareJson(VTypeToJson.toJson(vStringArray), vStringArrayJson);
//    }
//
//    @Test
//    public void serializeVEnumArray() {
//        compareJson(VTypeToJson.toJson(vEnumArray), vEnumArrayJson);
//    }
//
//    @Test
//    public void serializeVTable1() {
//        compareJson(VTypeToJson.toJson(vTable), vTableJson);
//    }
//
//    @Test
//    public void serializeVTable2() {
//        compareJson(VTypeToJson.toJson(vTable2), vTable2Json);
//    }
//
//    @Test
//    public void serializeVTable3() {
//        compareJson(VTypeToJson.toJson(vTable3), vTable3Json);
//    }


//    @Test
//    public void parseVBoolean() {
//        compareVType(vBoolean, VTypeToJson.toVType(parseJson(vBooleanJson)));
//    }


//    @Test
//    public void parseVFloatArray() {
//        compareVType(vFloatArray, VTypeToJson.toVType(parseJson(vFloatArrayJson)));
//    }
//
//    @Test
//    public void parseVLongArray() {
//        compareVType(vLongArray, VTypeToJson.toVType(parseJson(vLongArrayJson)));
//    }
//
//    @Test
//    public void parseVIntArray() {
//        compareVType(vIntArray, VTypeToJson.toVType(parseJson(vIntArrayJson)));
//    }
//
//    @Test
//    public void parseVShortArray() {
//        compareVType(vShortArray, VTypeToJson.toVType(parseJson(vShortArrayJson)));
//    }
//
//    @Test
//    public void parseVByteArray() {
//        compareVType(vByteArray, VTypeToJson.toVType(parseJson(vByteArrayJson)));
//    }
//
//    @Test
//    public void parseVBooleanArray() {
//        compareVType(vBooleanArray, VTypeToJson.toVType(parseJson(vBooleanArrayJson)));
//    }
//
//    @Test
//    public void parseVStringArray() {
//        compareVType(vStringArray, VTypeToJson.toVType(parseJson(vStringArrayJson)));
//    }
//
//    @Test
//    public void parseVEnumArray() {
//        compareVType(vEnumArray, VTypeToJson.toVType(parseJson(vEnumArrayJson)));
//    }
//
//    @Test
//    public void parseVTable() {
//        compareVType(vTable, VTypeToJson.toVType(parseJson(vTableJson)));
//    }
//
//    @Test
//    public void parseVTable2() {
//        compareVType(vTable2, VTypeToJson.toVType(parseJson(vTable2Json)));
//    }
    
}
