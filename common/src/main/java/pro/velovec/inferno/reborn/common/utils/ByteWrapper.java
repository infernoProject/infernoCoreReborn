package pro.velovec.inferno.reborn.common.utils;

import pro.velovec.inferno.reborn.common.exceptions.BufferUnderflowException;
import pro.velovec.inferno.reborn.common.oid.OID;
import pro.velovec.libs.base.utils.HexBin;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ByteWrapper implements ByteConvertible {

    private final ByteBuffer buffer;
    private final int size;

    public ByteWrapper(byte[] bytes) {
        buffer = ByteBuffer.wrap(bytes);
        size = bytes.length;
    }

    public int getRemainingBytes() {
        return size - buffer.position();
    }

    private void checkEnoughBytes(int bytesRequired) {
        if (getRemainingBytes() < bytesRequired) {
            int currentPosition = buffer.position();

            buffer.rewind();
            byte[] data = buffer.array();
            buffer.position(currentPosition);

            throw new BufferUnderflowException(String.format(
                "Not enough bytes: %d expected, %d available\nHex: %s",
                bytesRequired, getRemainingBytes(), HexBin.encode(data)
            ));
        }
    }

    public byte getByte() {
        checkEnoughBytes(1);

        return buffer.get();
    }

    public boolean getBoolean() {
        checkEnoughBytes(1);

        return getByte() == 1;
    }

    public Integer getInt() {
        checkEnoughBytes(4);

        return buffer.getInt();
    }

    public Long getLong() {
        checkEnoughBytes(8);

        return buffer.getLong();
    }

    public Float getFloat() {
        checkEnoughBytes(4);

        return buffer.getFloat();
    }

    public Double getDouble() {
        checkEnoughBytes(8);

        return buffer.getDouble();
    }

    public byte[] getBytes() {
        byte[] bytes = new byte[getInt()];

        checkEnoughBytes(bytes.length);
        buffer.get(bytes);

        return bytes;
    }

    public String getString() {
        return new String(getBytes());
    }

    public BigInteger getBigInteger() {
        return new BigInteger(getBytes());
    }

    public String[] getStrings() {
        int arraySize = getInt();

        String[] array = new String[arraySize];

        for (int i = 0; i < arraySize; i++) {
            array[i] = getString();
        }

        return array;
    }

    public List<ByteWrapper> getList() {
        int listSize = getInt();
        List<ByteWrapper> list = new ArrayList<>();

        for (int i = 0; i < listSize; i++) {
            list.add(new ByteWrapper(getBytes()));
        }

        return list;
    }

    public LocalDateTime getLocalDateTime() {
        return LocalDateTime.parse(getString(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public ByteWrapper getWrapper() {
        return new ByteWrapper(getBytes());
    }

    public OID getOID() {
        return OID.fromLong(getWrapper().getLong());
    }

    public <T extends Enum<T>> T getEnum(Class<T> enumType) {
        String value = getString().toUpperCase();
        for (T enumValue: enumType.getEnumConstants()) {
            if (enumValue.toString().equals(value)) {
                return enumValue;
            }
        }

        throw new IllegalArgumentException(String.format(
            "Enum type '%s' has no value '%s'", enumType.getSimpleName(), value
        ));
    }

    public void skip(int bytes) {
        checkEnoughBytes(bytes);

        buffer.position(buffer.position() + bytes);
    }

    public void rewind() {
        buffer.rewind();
    }

    public static ByteWrapper readFile(File file) throws IOException {
        return new ByteWrapper(FileUtils.toByteArray(file));
    }

    public static ByteWrapper fromBytes(ByteConvertible data) {
        return new ByteWrapper(data.toByteArray());
    }

    @Override
    public String toString() {
        return HexBin.encode(buffer.array());
    }

    @Override
    public byte[] toByteArray() {
        buffer.rewind();

        return buffer.array();
    }
}
