package pro.velovec.inferno.reborn.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pro.velovec.inferno.reborn.common.oid.OID;
import pro.velovec.libs.base.utils.HexBin;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.List;


public class ByteArray implements ByteConvertible {

    private final ByteArrayOutputStream byteStream;

    private static final Logger logger = LoggerFactory.getLogger(ByteArray.class);

    public ByteArray() {
        byteStream = new ByteArrayOutputStream();
    }

    public ByteArray(byte errCode) {
        byteStream = new ByteArrayOutputStream();

        put(errCode);
    }

    public ByteArray put(byte value) {
        try {
            byteStream.write(new byte[]{value});
        } catch (IOException e) {
            logger.error("Unable to put in buffer: {}", e.getMessage());
        }

        return this;
    }

    public ByteArray put(boolean value) {
        put(value ? (byte) 1 : (byte) 0);

        return this;
    }

    public ByteArray put(ByteBuffer value) {
        try {
            byteStream.write(value.array());
        } catch (IOException e) {
            logger.error("Unable to put in buffer: {}", e.getMessage());
        }

        return this;
    }

    public ByteArray put(byte[] value) {
        return put(
            ByteBuffer.allocate(4 + value.length)
                .putInt(value.length)
                .put(value)
        );
    }

    public ByteArray put(Integer value) {
        return put(ByteBuffer.allocate(4).putInt(value));
    }

    public ByteArray put(Long value) {
        return put(ByteBuffer.allocate(8).putLong(value));
    }

    public ByteArray put(Float value) {
        return put(ByteBuffer.allocate(4).putFloat(value));
    }

    public ByteArray put(Double value) {
        return put(ByteBuffer.allocate(8).putDouble(value));
    }

    public ByteArray put(BigInteger value) {
        return put(value != null ? value.toByteArray() : new byte[0]);
    }

    public ByteArray put(ByteConvertible value) {
        return put(value != null ? value.toByteArray() : new byte[0]);
    }

    public ByteArray put(List<? extends ByteConvertible> valueList) {
        put(valueList.size());

        for (ByteConvertible value: valueList) {
            put(value);
        }

        return this;
    }

    public ByteArray put(String value) {
        return put(value != null ? value.getBytes() : new byte[0]);
    }

    public ByteArray put(String[] values) {
        put(values.length);

        for (String value: values) {
            put(value);
        }

        return this;
    }

    public ByteArray put(Date value) {
        put(value != null ? value.getTime() : 0L);

        return this;
    }

    public ByteArray put(OID value) {
        put(new ByteArray().put(value.toLong()));

        return this;
    }

    public <T extends Enum<T>> ByteArray put(Enum<T> value) {
        put(value.toString().toLowerCase());

        return this;
    }

    @Override
    public byte[] toByteArray() {
        return byteStream.toByteArray();
    }

    @Override
    public String toString() {
        return HexBin.encode(toByteArray());
    }
}
